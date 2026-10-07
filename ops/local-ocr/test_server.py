import io
import json
import socket
import tempfile
import threading
import unittest
import urllib.error
import urllib.request
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import patch

from PIL import Image
import server


class ServerTest(unittest.TestCase):
    def png(self):
        data = io.BytesIO()
        Image.new("RGB", (32, 32), "white").save(data, format="PNG")
        return data.getvalue()

    def test_missing_models_fail_before_network_or_engine_initialization(self):
        with tempfile.TemporaryDirectory(dir="D:/RapidOCR/tmp") as directory:
            with patch("socket.socket.connect") as connect:
                with self.assertRaisesRegex(RuntimeError, "OCR_MODEL_MISSING"):
                    server.load_engine(Path(directory))
                connect.assert_not_called()

    def test_invalid_images_and_mime_never_reach_engine(self):
        engine = unittest.mock.Mock()
        for data, mime in [(b"", "image/png"), (b"corrupt", "image/png"),
                           (self.png(), "image/jpeg"), (self.png(), "text/plain"),
                           (b"x" * (server.MAX_BYTES + 1), "image/png")]:
            with self.assertRaises(ValueError):
                server.recognize(engine, data, mime)
        engine.assert_not_called()

    def test_http_health_valid_image_and_invalid_request(self):
        service = server.HTTPServer(("127.0.0.1", 0), server.Handler)
        service.engine = lambda _: SimpleNamespace(boxes=None)
        worker = threading.Thread(target=service.serve_forever, daemon=True)
        worker.start()
        base = f"http://127.0.0.1:{service.server_port}"
        try:
            with urllib.request.urlopen(base + "/health") as response:
                self.assertTrue(json.load(response)["offline"])
            request = urllib.request.Request(base + "/ocr", data=self.png(), headers={"Content-Type": "image/png"})
            with urllib.request.urlopen(request) as response:
                self.assertEqual(json.load(response)["blocks"], [])
            request = urllib.request.Request(base + "/ocr", data=b"bad", headers={"Content-Type": "image/png"})
            with self.assertRaises(urllib.error.HTTPError) as error:
                urllib.request.urlopen(request)
            self.assertEqual(error.exception.code, 400)
            self.assertEqual(json.load(error.exception)["error"], "OCR_IMAGE_INVALID")
        finally:
            service.shutdown()
            service.server_close()
            worker.join()

    def test_outbound_connections_are_blocked(self):
        with patch.object(socket.socket, "connect"), patch.object(socket.socket, "connect_ex"):
            server.block_outbound_network()
            with socket.socket() as connection:
                with self.assertRaisesRegex(OSError, "OCR_EXTERNAL_NETWORK_DISABLED"):
                    connection.connect(("203.0.113.1", 443))
                with self.assertRaises(OSError):
                    connection.connect_ex(("example.com", 443))


if __name__ == "__main__":
    unittest.main()
