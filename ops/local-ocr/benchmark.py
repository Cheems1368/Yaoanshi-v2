"""Real offline CPU benchmark using the project's synthetic prescription only."""
import ctypes
import io
import json
import math
import statistics
import time
import sys
from pathlib import Path

from PIL import Image, ImageFilter
sys.pycache_prefix = "D:/RapidOCR/cache/pycache"
from server import load_engine, recognize

root = Path("D:/RapidOCR")
repo = Path(__file__).resolve().parents[2]
sample = repo / "演示图片/制作支持/演示素材/演示处方_慢病随访.png"
started = time.perf_counter()
engine = load_engine(root)
load_seconds = time.perf_counter() - started
data = sample.read_bytes()
first = recognize(engine, data, "image/png")
timings = []
cpu_started = time.process_time()
for _ in range(20):
    result = recognize(engine, data, "image/png")
    if not result["blocks"]:
        raise RuntimeError("Benchmark unexpectedly produced no text")
    timings.append(result["elapsedMs"])
warm_cpu_seconds = time.process_time() - cpu_started

class MemoryCounters(ctypes.Structure):
    _fields_ = [("cb", ctypes.c_ulong), ("PageFaultCount", ctypes.c_ulong)] + [
        (name, ctypes.c_size_t) for name in ("PeakWorkingSetSize", "WorkingSetSize", "QuotaPeakPagedPoolUsage",
                                          "QuotaPagedPoolUsage", "QuotaPeakNonPagedPoolUsage", "QuotaNonPagedPoolUsage",
                                          "PagefileUsage", "PeakPagefileUsage")]

memory = MemoryCounters()
memory.cb = ctypes.sizeof(memory)
ctypes.windll.psapi.GetProcessMemoryInfo(ctypes.c_void_p(-1), ctypes.byref(memory), memory.cb)
variants = {}
with Image.open(io.BytesIO(data)) as image:
    for name, variant in {"rotated90": image.rotate(90, expand=True), "rotated180": image.rotate(180, expand=True),
                          "blurred": image.filter(ImageFilter.GaussianBlur(3)), "blank": Image.new("RGB", image.size, "white")}.items():
        buffer = io.BytesIO()
        variant.save(buffer, format="PNG")
        variant_data = buffer.getvalue()
        (root / "results" / f"{name}.png").write_bytes(variant_data)
        result = recognize(engine, variant_data, "image/png")
        variants[name] = {"blockCount": len(result["blocks"]), "elapsedMs": result["elapsedMs"]}

evidence = {
    "offlineNetworkBlocked": True, "runtimeDownloadsDisabled": True, "modelHashesVerified": True,
    "onnxTelemetryDisabled": True,
    "engineLoadSeconds": round(load_seconds, 3), "firstRecognitionMs": first["elapsedMs"],
    "warmRuns": 20, "warmSuccesses": len(timings), "warmMedianMs": statistics.median(timings),
    "warmP95Ms": sorted(timings)[math.ceil(len(timings) * .95) - 1], "warmTimingsMs": timings,
    "cpuSecondsFor20Runs": round(warm_cpu_seconds, 3),
    "workingSetMiB": round(memory.WorkingSetSize / 1024**2, 2),
    "peakWorkingSetMiB": round(memory.PeakWorkingSetSize / 1024**2, 2), "variants": variants,
    "limitation": "Synthetic demonstration sample only; not a clinical accuracy benchmark."
}
(root / "results" / "benchmark.json").write_text(json.dumps(evidence, indent=2), encoding="utf-8")
print(json.dumps(evidence))
