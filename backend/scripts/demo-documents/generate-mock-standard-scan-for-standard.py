#!/usr/bin/env python3
"""Generate mock scanned PNG for a specific standard ID (Vision OCR testing)."""

from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

OUTPUT_DIR = Path(__file__).resolve().parent

FONT_CANDIDATES = [
    "/System/Library/Fonts/Supplemental/Songti.ttc",
    "/System/Library/Fonts/STHeiti Light.ttc",
    "/System/Library/Fonts/Hiragino Sans GB.ttc",
    "/Library/Fonts/Arial Unicode.ttf",
]

STANDARD_PRESETS = {
    "2068759377758277633": {
        "filename": "mock-standard-scan-2068759377758277633.png",
        "lines": [
            "Q345B低合金高强度结构钢企业标准（扫描件模拟）",
            "标准编号：Q/ZX-MULTI-1782065642",
            "标准名称：Q345B多文件E2E-1782065642",
            "标准ID：2068759377758277633",
            "",
            "5.1 抗拉强度 Rm",
            "Q345B 热轧板抗拉强度 Rm 的合格范围为 470 MPa 至 630 MPa。",
            "实测值低于 470 MPa 或高于 630 MPa 时判定为不合格。",
            "",
            "5.2 屈服强度 ReL",
            "Q345B 热轧板下屈服强度 ReL 的合格下限为 345 MPa，合格上限为 460 MPa。",
            "",
            "5.3 断后延伸率 A",
            "Q345B 热轧板断后延伸率 A 的合格下限为 20%。",
            "",
            "6.1 厚度公差",
            "Q345B 热轧板厚度公差应控制在 -0.200 mm 至 0.200 mm。",
            "",
            "注：本页为联调模拟扫描件，仅用于 Vision OCR 测试，不得用于生产判定。",
        ],
    },
}


def load_font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    for path in FONT_CANDIDATES:
        if Path(path).exists():
            return ImageFont.truetype(path, size=size)
    return ImageFont.load_default()


def render_scan_png(output: Path, lines: list[str]) -> None:
    width, height = 1200, 1800
    image = Image.new("RGB", (width, height), color=(245, 242, 235))
    draw = ImageDraw.Draw(image)

    title_font = load_font(32)
    body_font = load_font(26)

    y = 70
    for idx, line in enumerate(lines):
        if not line:
            y += 16
            continue
        font = title_font if idx == 0 else body_font
        draw.text((72, y), line, fill=(25, 25, 25), font=font)
        y += 44 if idx == 0 else 38

    draw.rectangle([(18, 18), (width - 18, height - 18)], outline=(170, 165, 155), width=3)
    output.parent.mkdir(parents=True, exist_ok=True)
    image.save(output, format="PNG")


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate mock OCR scan PNG for a standard")
    parser.add_argument(
        "--standard-id",
        default="2068759377758277633",
        help="Structured standard ID preset",
    )
    args = parser.parse_args()

    preset = STANDARD_PRESETS.get(args.standard_id)
    if preset is None:
        raise SystemExit(f"No preset for standard id {args.standard_id}")

    output = OUTPUT_DIR / preset["filename"]
    render_scan_png(output, preset["lines"])
    print(f"Wrote {output}")


if __name__ == "__main__":
    main()
