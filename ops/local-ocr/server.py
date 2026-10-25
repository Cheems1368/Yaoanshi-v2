"""Loopback-only, serial RapidOCR service. Models must be installed before startup."""
import argparse
import hashlib
import importlib.metadata
import io
import json
import os
import socket
import time
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path

MAX_BYTES = 10 * 1024 * 1024
FORMATS = {"image/png": "PNG", "image/jpeg": "JPEG", "image/webp": "WEBP"}


def block_outbound_network():
    original_connect = socket.socket.connect
    original_connect_ex = socket.socket.connect_ex

    def checked_connect(connection, address):
        if not isinstance(address, tuple) or address[0] not in ("127.0.0.1", "::1"):
            raise OSError("OCR_EXTERNAL_NETWORK_DISABLED")
        return original_connect(connection, address)

    def checked_connect_ex(connection, address):
        if not isinstance(address, tuple) or address[0] not in ("127.0.0.1", "::1"):
            raise OSError("OCR_EXTERNAL_NETWORK_DISABLED")
        return original_connect_ex(connection, address)

    socket.socket.connect = checked_connect
    socket.socket.connect_ex = checked_connect_ex


def load_engine(root):
    os.environ["OC_DISABLE_DOT_ACCESS_WARNING"] = "1"
    expected = {"rapidocr": "3.9.2", "onnxruntime": "1.30.0"}
    if any(importlib.metadata.version(name) != version for name, version in expected.items()):
        raise RuntimeError("OCR_VERSION_MISMATCH")
    manifest = json.loads((Path(__file__).with_name("models.lock.json")).read_text(encoding="utf-8-sig"))
    for entry in manifest:
        model = root / "models" / entry["name"]
        if not model.is_file():
            raise RuntimeError("OCR_MODEL_MISSING")
        if hashlib.sha256(model.read_bytes()).hexdigest() != entry["sha256"]:
            raise RuntimeError("OCR_MODEL_HASH_MISMATCH")
    block_outbound_network()
    import onnxruntime
    onnxruntime.disable_telemetry_events()
    from rapidocr import RapidOCR
    from rapidocr.utils.download_file import DownloadFile

    def disabled_download(*args, **kwargs):
        raise RuntimeError("OCR_RUNTIME_DOWNLOAD_DISABLED")

    DownloadFile.__call__ = disabled_download
    return RapidOCR(params={
        "Global.model_root_dir": str(root / "models"),
        "Global.log_level": "critical",
        "Det.model_path": str(root / "models" / "PP-OCRv6_det_small.onnx"),
        "Rec.model_path": str(root / "models" / "PP-OCRv6_rec_small.onnx"),
        "Cls.model_path": str(root / "models" / "ch_ppocr_mobile_v2.0_cls_mobile.onnx"),
        "EngineConfig.onnxruntime.intra_op_num_threads": 4,
        "EngineConfig.onnxruntime.inter_op_num_threads": 1,
        "EngineConfig.onnxruntime.use_cuda": False,
    })


def recognize(engine, data, content_type):
    from PIL import Image
    if content_type not in FORMATS or not data or len(data) > MAX_BYTES:
        raise ValueError("OCR_IMAGE_INVALID")
    try:
        with Image.open(io.BytesIO(data)) as image:
            if image.format != FORMATS[content_type]:
                raise ValueError("OCR_IMAGE_INVALID")
            image.verify()
    except Exception:
        raise ValueError("OCR_IMAGE_INVALID") from None
    started = time.perf_counter()
    result = engine(data)
    blocks = [] if result.boxes is None else [
        {"text": str(text), "score": float(score), "box": box.tolist()}
        for box, text, score in zip(result.boxes, result.txts, result.scores)
    ]
    return {"blocks": blocks, "elapsedMs": round((time.perf_counter() - started) * 1000, 2)}


class Handler(BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass

    def reply(self, status, data):
        body = json.dumps(data, ensure_ascii=False, allow_nan=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path != "/health":
            self.reply(404, {"error": "NOT_FOUND"})
            return
        self.reply(200, {"ready": True, "provider": "RAPIDOCR_LOCAL", "version": "3.9.2", "offline": True})

    def do_POST(self):
        if self.path != "/ocr":
            self.reply(404, {"error": "NOT_FOUND"})
            return
        try:
            length = int(self.headers.get("Content-Length", "0"))
            if length <= 0 or length > MAX_BYTES or self.headers.get("Transfer-Encoding"):
                self.reply(400, {"error": "OCR_IMAGE_INVALID"})
                return
            self.connection.settimeout(30)
            data = self.rfile.read(length)
            if len(data) != length:
                raise ValueError("OCR_IMAGE_INVALID")
            self.reply(200, recognize(self.server.engine, data, self.headers.get("Content-Type", "")))
        except ValueError:
            self.reply(400, {"error": "OCR_IMAGE_INVALID"})
        except (BrokenPipeError, ConnectionResetError, TimeoutError):
            return
        except Exception:
            self.reply(503, {"error": "OCR_INFERENCE_FAILED"})


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path("D:/RapidOCR"))
    args = parser.parse_args()
    try:
        engine = load_engine(args.root)
    except Exception as error:
        print(json.dumps({"ready": False, "errorType": type(error).__name__,
                          "reason": str(error) if str(error).startswith("OCR_") else "OCR_STARTUP_FAILED"}))
        raise SystemExit(1) from None
    with HTTPServer(("127.0.0.1", 19090), Handler) as service:
        service.engine = engine
        print('{"ready":true,"provider":"RAPIDOCR_LOCAL","port":19090}', flush=True)
        service.serve_forever()


if __name__ == "__main__":
    main()
