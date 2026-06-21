#!/usr/bin/env python3
"""Generate mock Excel source file for multi-format standard upload testing."""

from pathlib import Path

from openpyxl import Workbook

OUTPUT = Path(__file__).resolve().parent / "mock-standard-table-q345b.xlsx"

ROWS = [
    ["指标", "下限", "上限", "单位", "备注"],
    ["Rm", "470", "630", "MPa", "抗拉强度"],
    ["ReL", "345", "460", "MPa", "屈服强度"],
    ["A", "20", "", "%", "断后伸长率下限"],
    ["Δt", "-0.20", "0.20", "mm", "厚度偏差"],
]


def main() -> None:
    wb = Workbook()
    ws = wb.active
    ws.title = "指标"
    for row in ROWS:
        ws.append(row)
    wb.save(OUTPUT)
    print(f"Wrote {OUTPUT}")


if __name__ == "__main__":
    main()
