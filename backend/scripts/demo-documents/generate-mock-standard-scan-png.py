#!/usr/bin/env python3
"""Generate mock scanned standard image for Vision OCR upload testing."""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

OUTPUT = Path(__file__).resolve().parent / "mock-standard-scan-q235b.png"

FONT_CANDIDATES = [
    "/System/Library/Fonts/Supplemental/Songti.ttc",
    "/System/Library/Fonts/STHeiti Light.ttc",
    "/System/Library/Fonts/Hiragino Sans GB.ttc",
    "/Library/Fonts/Arial Unicode.ttf",
]

LINES = [
    "西南建材集团 Q235B 冷轧板供货协议（扫描件模拟）",
    "协议编号：协议D2026-001-v1",
    "",
    "3.1 力学性能",
    "Q235B 冷轧板抗拉强度 Rm 应为 375 MPa 至 505 MPa。",
    "断后伸长率 A 应不小于 27%。",
    "屈服强度 ReL 应为 235 MPa 至 360 MPa。",
    "",
    "3.2 尺寸偏差",
    "厚度偏差 Δt 允许范围为 -0.10 mm 至 0.10 mm。",
    "",
    "注：本页为联调模拟扫描件，仅用于 Vision OCR 测试。",
]


def load_font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    for path in FONT_CANDIDATES:
        if Path(path).exists():
            return ImageFont.truetype(path, size=size)
    return ImageFont.load_default()


def main() -> None:
    width, height = 1200, 1600
    image = Image.new("RGB", (width, height), color=(248, 246, 240))
    draw = ImageDraw.Draw(image)

    title_font = load_font(34)
    body_font = load_font(28)

    y = 80
    for idx, line in enumerate(LINES):
        font = title_font if idx == 0 else body_font
        if not line:
            y += 18
            continue
        draw.text((80, y), line, fill=(20, 20, 20), font=font)
        y += 46 if idx == 0 else 40

    # light scan noise border
    draw.rectangle([(20, 20), (width - 20, height - 20)], outline=(180, 175, 165), width=3)
    image.save(OUTPUT, format="PNG")
    print(f"Wrote {OUTPUT}")


if __name__ == "__main__":
    main()
