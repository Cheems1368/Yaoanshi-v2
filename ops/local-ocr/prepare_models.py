"""Installation-only model preparation; runtime downloads stay disabled."""
import argparse
import hashlib
import json
import os
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument("--root", type=Path, default=Path("D:/RapidOCR"))
args = parser.parse_args()
os.environ["OC_DISABLE_DOT_ACCESS_WARNING"] = "1"
from rapidocr import RapidOCR

RapidOCR(params={"Global.model_root_dir": str(args.root / "models"), "Global.log_level": "critical"})
manifest = json.loads(Path(__file__).with_name("models.lock.json").read_text(encoding="utf-8-sig"))
for entry in manifest:
    model = args.root / "models" / entry["name"]
    if not model.is_file() or hashlib.sha256(model.read_bytes()).hexdigest() != entry["sha256"]:
        raise SystemExit("OCR_MODEL_VERIFICATION_FAILED")
print(json.dumps({"modelsVerified": len(manifest)}))
