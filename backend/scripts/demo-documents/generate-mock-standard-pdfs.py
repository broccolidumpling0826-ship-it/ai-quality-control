#!/usr/bin/env python3
"""Generate mock standard PDF files for standard maintenance upload testing."""

from __future__ import annotations

import textwrap
from pathlib import Path

from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas

OUTPUT_DIR = Path(__file__).resolve().parent
FONT_CANDIDATES = [
    "/System/Library/Fonts/Supplemental/Songti.ttc",
    "/System/Library/Fonts/STHeiti Light.ttc",
    "/System/Library/Fonts/Hiragino Sans GB.ttc",
    "/Library/Fonts/Arial Unicode.ttf",
]


def register_chinese_font() -> str:
    for path in FONT_CANDIDATES:
        if Path(path).exists():
            font_name = "MockStandardCN"
            pdfmetrics.registerFont(TTFont(font_name, path))
            return font_name
    raise RuntimeError("No Chinese font found on this machine")


def draw_wrapped_text(c: canvas.Canvas, x: float, y: float, text: str, font_name: str, font_size: int, max_width: float, line_height: float) -> float:
    c.setFont(font_name, font_size)
    for paragraph in text.split("\n"):
        if not paragraph.strip():
            y -= line_height
            continue
        for line in textwrap.wrap(paragraph, width=42):
            if y < 20 * mm:
                c.showPage()
                c.setFont(font_name, font_size)
                y = A4[1] - 20 * mm
            c.drawString(x, y, line)
            y -= line_height
    return y


def build_pdf(filename: str, title: str, meta_lines: list[str], sections: list[tuple[str, str]]) -> Path:
    font_name = register_chinese_font()
    output = OUTPUT_DIR / filename
    c = canvas.Canvas(str(output), pagesize=A4)
    width, height = A4
    left = 20 * mm
    y = height - 22 * mm

    c.setFont(font_name, 16)
    c.drawString(left, y, title)
    y -= 10 * mm

    c.setFont(font_name, 10)
    for line in meta_lines:
        c.drawString(left, y, line)
        y -= 5 * mm
    y -= 4 * mm

    for heading, body in sections:
        c.setFont(font_name, 12)
        if y < 35 * mm:
            c.showPage()
            y = height - 20 * mm
        c.drawString(left, y, heading)
        y -= 7 * mm
        y = draw_wrapped_text(c, left, y, body, font_name, 10, width - 40 * mm, 5.2 * mm)
        y -= 4 * mm

    c.setFont(font_name, 9)
    c.drawString(left, 12 * mm, "【模拟测试文档】仅供标准维护 PDF 上传/RAG 入库联调，不作为生产判定依据。")
    c.save()
    return output


STANDARD_A = {
    "filename": "mock-standard-A-q345b-enterprise.pdf",
    "title": "Q345B低合金高强度结构钢企业标准（模拟）",
    "meta": [
        "标准编号：Q/ZX-STEEL-2026-Q345B",
        "标准名称：Q345B低合金高强度结构钢企业标准",
        "标准类型：企业标准 ENTERPRISE",
        "适用品种/牌号：热轧板 / Q345B",
        "规格范围：厚度2.0-12.0mm，宽度900-1800mm",
        "版本号：V2026.1    生效日期：2026-01-01",
    ],
    "sections": [
        ("1 适用范围", "本企业标准适用于热轧 Q345B 低合金高强度结构钢板的出厂检验与质量判定。适用规格为厚度 2.0 mm 至 12.0 mm、宽度 900 mm 至 1800 mm 的热轧板。本文件为系统联调模拟文档。"),
        ("5.1 抗拉强度 Rm", "Q345B 热轧板抗拉强度 Rm 的合格范围为 470 MPa 至 630 MPa。实测值低于 470 MPa 或高于 630 MPa 时判定为不合格。若 Rm 不低于 460 MPa 且低于 470 MPa，可提交让步评审，但不得直接判定合格。"),
        ("5.2 屈服强度 ReL", "Q345B 热轧板下屈服强度 ReL 的合格下限为 345 MPa，合格上限为 460 MPa。实测 ReL 低于 345 MPa 时不满足合格要求。"),
        ("5.3 断后延伸率 A", "Q345B 热轧板断后延伸率 A 的合格下限为 20%。当 A 低于 20% 时不满足合格范围。若 A 不低于 18% 且低于 20%，可进入让步评审。"),
        ("6.1 厚度公差", "Q345B 热轧板厚度公差应控制在 -0.200 mm 至 0.200 mm。超出该范围但仍处于 -0.250 mm 至 0.250 mm 的，可结合用途进入让步评审。"),
        ("7 引用与说明", "结构化指标限值以质量系统中 qc_standard_indicator 配置为准。本 PDF 文本仅用于 RAG 检索、引用与解释，不得覆盖系统结构化判定规则。"),
    ],
}

STANDARD_B = {
    "filename": "mock-standard-B-q235b-customer.pdf",
    "title": "西南建材集团Q235B冷轧板供货协议（模拟）",
    "meta": [
        "协议编号：协议D2026-001-v1",
        "协议名称：西南建材集团Q235B冷轧板供货协议",
        "标准类型：客户协议 CUSTOMER",
        "关联客户：CUST-002 西南建材集团",
        "适用品种/牌号：冷轧板 / Q235B",
        "规格范围：厚度0.8-2.5mm，宽度800-1250mm",
        "版本号：协议D2026-001-v1    有效期：2026-01-01 至 2026-12-31",
    ],
    "sections": [
        ("1 协议范围", "本协议适用于西南建材集团采购的 Q235B 冷轧板。适用规格为厚度 0.8 mm 至 2.5 mm、宽度 800 mm 至 1250 mm。本文件为系统联调模拟文档。"),
        ("4.1 抗拉强度 Rm", "Q235B 冷轧板抗拉强度 Rm 的协议合格范围为 375 MPa 至 505 MPa。实测值低于 375 MPa 或高于 505 MPa 时，按协议判定为不合格。若 Rm 为 370 MPa 至 375 MPa，可申请让步评审。"),
        ("4.2 断后延伸率 A", "Q235B 冷轧板断后延伸率 A 的协议合格下限为 27%。当 A 低于 27% 时不满足协议要求。若 A 为 25% 至 27%，需客户书面确认后方可让步。"),
        ("4.3 屈服强度 ReL", "Q235B 冷轧板下屈服强度 ReL 的协议范围为 235 MPa 至 360 MPa。低于 235 MPa 时不得出厂。"),
        ("5.1 厚度公差", "Q235B 冷轧板厚度公差应控制在 -0.100 mm 至 0.100 mm。超出至 -0.130 mm 至 0.130 mm 时，须附用途说明并进入让步流程。"),
        ("6 质保书与交付", "按本协议判定合格且完成出厂检验的批次，可生成正式质保书。未完成客户确认的让步批次，仅可出具内部预览说明。"),
    ],
}


def main() -> None:
    created = []
    for spec in (STANDARD_A, STANDARD_B):
        path = build_pdf(spec["filename"], spec["title"], spec["meta"], spec["sections"])
        created.append(path)
        print(f"created: {path}")
    print(f"done: {len(created)} pdf(s)")


if __name__ == "__main__":
    main()
