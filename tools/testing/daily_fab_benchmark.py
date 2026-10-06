#!/usr/bin/env python3
"""Reproducible, non-saving Daily FAB benchmark (ARTEMIS verified on Waydroid).

First open Daily with no dialog visible, then:
  python3 tools/testing/daily_fab_benchmark.py --serial SERIAL --out DIR --calibrate
  python3 tools/testing/daily_fab_benchmark.py --serial SERIAL --out DIR --verify
  python3 tools/testing/daily_fab_benchmark.py --serial SERIAL --out DIR --label after

Calibration uses text/description XML locators; verified 480x1000 coordinates are
fallbacks scaled to screen size. XML waits happen BEFORE measurement, never inside
the animation window. Each draft remains $0 and is cancelled with Back.
Optional Perfetto recording should already be running before the second command.
"""
import argparse
import json
import re
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path

PACKAGE = "com.example.vibefinance"
LABELS = {"fab": ["新增交易", "Add transaction", "Add Transaction"],
          "expense": ["支出", "Expense"], "income": ["收入", "Income"],
          "transfer": ["轉帳", "Transfer"]}
FALLBACK = {"fab": (412, 790), "expense": (390, 715),
            "income": (390, 650), "transfer": (390, 580)}
TITLES = {"expense": ["新增支出", "Add Expense"],
          "income": ["新增收入", "Add Income"], "transfer": ["帳戶轉帳", "Transfer"]}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True)
    parser.add_argument("--out", type=Path, required=True)
    parser.add_argument("--label", default="after")
    parser.add_argument("--calibrate", action="store_true")
    parser.add_argument("--verify", action="store_true", help="Verify popup modes outside timed recording")
    args = parser.parse_args()
    args.out.mkdir(parents=True, exist_ok=True)

    def adb(*parts):
        return subprocess.check_output(["adb", "-s", args.serial, *parts], text=True, timeout=30)

    def shell(*parts):
        return adb("shell", *map(str, parts))

    def tap(point):
        shell("input", "tap", *point)

    def hierarchy():
        shell("uiautomator", "dump", "/data/local/tmp/vibe-fab-benchmark.xml")
        return ET.fromstring(shell("cat", "/data/local/tmp/vibe-fab-benchmark.xml"))

    def locate(tree, name, fallback):
        matches = [n for n in tree.iter("node") if any(
            n.get(attr, "") in LABELS[name] for attr in ("text", "content-desc"))]
        for node in matches:
            bounds = list(map(int, re.findall(r"\d+", node.get("bounds", ""))))
            if len(bounds) == 4 and bounds[2] > bounds[0] and bounds[3] > bounds[1]:
                return [(bounds[0] + bounds[2]) // 2, (bounds[1] + bounds[3]) // 2]
        print(f"XML locator missing: {name}; using ARTEMIS-verified fallback {fallback}")
        return fallback

    size = list(map(int, re.findall(r"(\d+)x(\d+)", shell("wm", "size"))[-1]))
    points = {key: [round(x * size[0] / 480), round(y * size[1] / 1000)]
              for key, (x, y) in FALLBACK.items()}
    calibration = args.out / "locations.json"
    if args.calibrate:
        points["fab"] = locate(hierarchy(), "fab", points["fab"])
        tap(points["fab"])
        try:
            time.sleep(0.6)
            tree = hierarchy()
            for key in ("expense", "income", "transfer"):
                points[key] = locate(tree, key, points[key])
        finally:
            shell("input", "keyevent", "KEYCODE_BACK")
            time.sleep(0.8)
        calibration.write_text(json.dumps({"serial": args.serial, "size": size, "points": points}, indent=2))
        print(calibration)
        return
    if calibration.exists():
        data = json.loads(calibration.read_text())
        if data["serial"] == args.serial and data["size"] == size:
            points = data["points"]
        else:
            raise RuntimeError("Resolution/device changed; recalibrate before benchmarking")
    if args.verify:
        for mode in ("expense", "income", "transfer"):
            tap(points["fab"])
            time.sleep(0.6)
            target = locate(hierarchy(), mode, points[mode])
            tap(target)
            try:
                deadline = time.monotonic() + 15
                while True:
                    texts = [n.get("text", "") for n in hierarchy().iter("node")]
                    if any(title in texts for title in TITLES[mode]) and any("HKD" in t for t in texts):
                        print(f"Verified {mode} popup title")
                        with (args.out / f"{args.label}-{mode}-verified.png").open("wb") as image:
                            subprocess.run(["adb", "-s", args.serial, "exec-out", "screencap", "-p"],
                                           stdout=image, check=True, timeout=30)
                        break
                    if time.monotonic() >= deadline:
                        raise AssertionError(f"{mode} popup did not display its expected title: {texts}")
                    time.sleep(0.2)
            finally:
                shell("input", "keyevent", "KEYCODE_BACK")
                time.sleep(0.8)
        return
    events = []
    started = time.monotonic()
    time.sleep(1)
    for mode in ("expense", "income", "transfer"):
        tap(points["fab"])
        time.sleep(0.45)
        shell("dumpsys", "gfxinfo", PACKAGE, "reset")
        events.append({"mode": mode, "action": "open", "hostElapsed": time.monotonic() - started,
                       "deviceUptime": shell("cat", "/proc/uptime").split()[0]})
        tap(points[mode])
        try:
            time.sleep(1.2)
            (args.out / f"{args.label}-{mode}-open-gfx.txt").write_text(
                shell("dumpsys", "gfxinfo", PACKAGE, "framestats"))
            shell("dumpsys", "gfxinfo", PACKAGE, "reset")
            time.sleep(1)
            (args.out / f"{args.label}-{mode}-held-gfx.txt").write_text(
                shell("dumpsys", "gfxinfo", PACKAGE, "framestats"))
        finally:
            shell("input", "keyevent", "KEYCODE_BACK")
            time.sleep(0.8)
    (args.out / f"{args.label}-events.json").write_text(json.dumps(events, indent=2))
    print(f"Finished {args.label}; all three zero-amount drafts cancelled.")


if __name__ == "__main__":
    main()
