from __future__ import print_function

import os
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    KeepTogether,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)


ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
OUT_DIR = os.path.join(ROOT, "tmp", "pdfs", "std-doc")
FONT_NAME = "ArialUnicode"
FONT_PATHS = [
    "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
    "/System/Library/Fonts/STHeiti Medium.ttc",
    "/System/Library/Fonts/Supplemental/Songti.ttc",
]


def register_font():
    for path in FONT_PATHS:
        if os.path.exists(path):
            try:
                pdfmetrics.registerFont(TTFont(FONT_NAME, path))
                return
            except Exception:
                pass
    raise RuntimeError("No usable Chinese font found")


def build_styles():
    sample = getSampleStyleSheet()
    base = ParagraphStyle(
        "BaseCJK",
        parent=sample["Normal"],
        fontName=FONT_NAME,
        fontSize=9.6,
        leading=15,
        wordWrap="CJK",
        alignment=TA_JUSTIFY,
        spaceAfter=5,
    )
    return {
        "title": ParagraphStyle(
            "TitleCJK",
            parent=base,
            fontSize=20,
            leading=28,
            alignment=TA_CENTER,
            spaceAfter=18,
        ),
        "subtitle": ParagraphStyle(
            "SubtitleCJK",
            parent=base,
            fontSize=12,
            leading=18,
            alignment=TA_CENTER,
            textColor=colors.HexColor("#333333"),
            spaceAfter=10,
        ),
        "notice": ParagraphStyle(
            "NoticeCJK",
            parent=base,
            fontSize=9,
            leading=14,
            alignment=TA_CENTER,
            textColor=colors.HexColor("#7A1F1F"),
            borderColor=colors.HexColor("#B65B5B"),
            borderWidth=0.5,
            borderPadding=6,
            backColor=colors.HexColor("#FFF5F3"),
            spaceBefore=10,
            spaceAfter=12,
        ),
        "h1": ParagraphStyle(
            "H1CJK",
            parent=base,
            fontSize=14,
            leading=20,
            spaceBefore=12,
            spaceAfter=8,
            textColor=colors.HexColor("#1F3A5F"),
        ),
        "h2": ParagraphStyle(
            "H2CJK",
            parent=base,
            fontSize=11.5,
            leading=17,
            spaceBefore=8,
            spaceAfter=5,
            textColor=colors.HexColor("#1F3A5F"),
        ),
        "body": base,
        "small": ParagraphStyle(
            "SmallCJK",
            parent=base,
            fontSize=8,
            leading=12,
            alignment=TA_LEFT,
            textColor=colors.HexColor("#555555"),
        ),
        "cell": ParagraphStyle(
            "CellCJK",
            parent=base,
            fontSize=8,
            leading=11,
            alignment=TA_LEFT,
            spaceAfter=0,
        ),
        "cell_center": ParagraphStyle(
            "CellCenterCJK",
            parent=base,
            fontSize=8,
            leading=11,
            alignment=TA_CENTER,
            spaceAfter=0,
        ),
    }


def p(text, style):
    return Paragraph(escape(text).replace("\n", "<br/>"), style)


def table(data, styles, widths=None, repeat_rows=1):
    wrapped = []
    for row_index, row in enumerate(data):
        wrapped_row = []
        for cell in row:
            style = styles["cell_center"] if row_index == 0 else styles["cell"]
            wrapped_row.append(p(str(cell), style))
        wrapped.append(wrapped_row)
    t = Table(wrapped, colWidths=widths, repeatRows=repeat_rows, hAlign="LEFT")
    t.setStyle(
        TableStyle(
            [
                ("FONTNAME", (0, 0), (-1, -1), FONT_NAME),
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E8EEF7")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.HexColor("#1F3A5F")),
                ("GRID", (0, 0), (-1, -1), 0.35, colors.HexColor("#9AA8B8")),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("LEFTPADDING", (0, 0), (-1, -1), 4),
                ("RIGHTPADDING", (0, 0), (-1, -1), 4),
                ("TOPPADDING", (0, 0), (-1, -1), 4),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F9FBFD")]),
            ]
        )
    )
    return t


def section(story, styles, heading, paragraphs=None, rows=None, widths=None):
    story.append(p(heading, styles["h1"]))
    if paragraphs:
        for text in paragraphs:
            story.append(p(text, styles["body"]))
    if rows:
        story.append(Spacer(1, 3))
        story.append(table(rows, styles, widths=widths))
        story.append(Spacer(1, 6))


def cover(story, styles, doc):
    story.append(Spacer(1, 26 * mm))
    story.append(p(doc["title"], styles["title"]))
    story.append(p(doc["code"], styles["subtitle"]))
    story.append(p(doc["doc_type"], styles["subtitle"]))
    story.append(Spacer(1, 10 * mm))
    meta = [
        ["项目", "内容"],
        ["版本", doc["version"]],
        ["发布/签署日期", doc["issued"]],
        ["实施日期", doc["effective"]],
        ["适用品种", doc["variety"]],
        ["适用牌号", doc["grade"]],
        ["适用规格", doc["spec"]],
        ["适用客户", doc.get("customer", "通用")],
        ["拟关联系统类型", doc["system_type"]],
    ]
    story.append(table(meta, styles, widths=[42 * mm, 102 * mm]))
    story.append(
        p(
            "模拟测试文件：仅用于钢铁质量控制系统标准维护、PDF抽取、RAG检索、判定解释和让步风险评测；"
            "不代表任何真实国家标准、企业正式标准或有效合同文本。",
            styles["notice"],
        )
    )
    story.append(Spacer(1, 20 * mm))
    story.append(p("编制单位：江淮智能质控系统测试组", styles["subtitle"]))
    story.append(PageBreak())


def common_intro(story, styles, doc):
    section(
        story,
        styles,
        "前言",
        [
            "本文件按钢铁产品质量控制系统的测试需要编写，条款结构参考常见钢材标准和供货协议的表达方式。"
            "文件中的数值、客户、批次和案例均为模拟数据，主要用于验证文档上传、确定性切块、来源引用、"
            "标准优先级、判定解释、让步评审和质量证明书问答等功能。",
            "本文件中出现的化学成分、力学性能、尺寸偏差和让步范围可作为RAG引用依据；"
            "系统中的结构化标准指标仍是自动判定唯一依据，PDF抽取文本不得自动覆盖结构化限值。",
        ],
    )
    toc_rows = [["条款", "标题", "评测用途"]]
    for item in doc["toc"]:
        toc_rows.append(item)
    section(story, styles, "目录及评测关注点", rows=toc_rows, widths=[22 * mm, 52 * mm, 78 * mm])


def append_sections(story, styles, doc):
    for block in doc["sections"]:
        section(
            story,
            styles,
            block["heading"],
            block.get("paragraphs"),
            block.get("rows"),
            block.get("widths"),
        )


def on_page(doc_meta):
    def _draw(canvas, doc):
        canvas.saveState()
        width, height = A4
        canvas.setFillColor(colors.white)
        canvas.rect(0, 0, width, height, stroke=0, fill=1)
        canvas.setFont(FONT_NAME, 8)
        canvas.setFillColor(colors.HexColor("#555555"))
        canvas.drawString(18 * mm, height - 13 * mm, doc_meta["code"])
        canvas.drawRightString(width - 18 * mm, height - 13 * mm, doc_meta["short_title"])
        canvas.setStrokeColor(colors.HexColor("#C6CED8"))
        canvas.setLineWidth(0.3)
        canvas.line(18 * mm, height - 16 * mm, width - 18 * mm, height - 16 * mm)
        canvas.line(18 * mm, 16 * mm, width - 18 * mm, 16 * mm)
        canvas.setFillColor(colors.HexColor("#777777"))
        canvas.drawCentredString(width / 2, 9.8 * mm, "第 %d 页" % doc.page)
        canvas.drawRightString(width - 18 * mm, 9.8 * mm, "模拟测试文件")
        canvas.restoreState()
    return _draw


def build_pdf(doc, styles):
    output_path = os.path.join(OUT_DIR, doc["filename"])
    frame = Frame(18 * mm, 18 * mm, 174 * mm, 258 * mm, id="normal")
    template = PageTemplate(id="all", frames=[frame], onPage=on_page(doc))
    pdf = BaseDocTemplate(
        output_path,
        pagesize=A4,
        rightMargin=18 * mm,
        leftMargin=18 * mm,
        topMargin=18 * mm,
        bottomMargin=18 * mm,
        title=doc["title"],
        author="quality-control test data generator",
    )
    pdf.addPageTemplates([template])
    story = []
    cover(story, styles, doc)
    common_intro(story, styles, doc)
    append_sections(story, styles, doc)
    pdf.build(story)
    return output_path


def base_docs():
    indicator_widths = [20 * mm, 30 * mm, 30 * mm, 30 * mm, 42 * mm]
    chem_widths = [24 * mm, 30 * mm, 30 * mm, 30 * mm, 38 * mm]
    return [
        {
            "filename": "GB-T-SIM-912-2026_Q235B_cold_rolled_sheet_national.pdf",
            "code": "GB/T SIM 912-2026",
            "short_title": "Q235B冷轧板模拟国标",
            "title": "碳素结构钢冷轧薄板及钢带质量要求（模拟国标）",
            "doc_type": "标准类型：NATIONAL / 国家标准类测试样本",
            "system_type": "NATIONAL",
            "version": "2026版",
            "issued": "2026-01-15",
            "effective": "2026-03-01",
            "variety": "冷轧板、冷轧钢带",
            "grade": "Q235B",
            "spec": "厚度0.50mm-3.00mm，宽度600mm-1500mm",
            "toc": [
                ["1", "范围", "品种、牌号、规格过滤"],
                ["5.1", "化学成分", "C/Mn/Si/P/S限值引用"],
                ["6.1", "力学性能", "Rm、ReL、A判定解释"],
                ["7.2", "厚度允许偏差", "厚度偏差与让步边界"],
                ["8.3", "复验规则", "复验/重判建议"],
                ["9.2", "让步接收边界", "CAN_CONCESSION场景"],
            ],
            "sections": [
                {
                    "heading": "1 范围",
                    "paragraphs": [
                        "本文件规定了Q235B冷轧薄板及钢带的订货内容、化学成分、力学性能、尺寸允许偏差、表面质量、检验规则、复验规则、让步接收边界和质量证明书内容。",
                        "本文件适用于厚度0.50mm至3.00mm、宽度600mm至1500mm的Q235B冷轧板卷，适用用途为一般冲压件、建筑围护件和普通结构件。对汽车安全结构件、压力容器和承重焊接关键件，应采用客户协议或专用标准。",
                    ],
                },
                {
                    "heading": "2 规范性引用文件",
                    "paragraphs": [
                        "下列文件中的内容通过文中的规范性引用而构成本文件必不可少的条款。凡是注日期的引用文件，仅该日期对应的版本适用于本文件；凡是不注日期的引用文件，其最新适用版本用于试验方法说明。",
                        "GB/T SIM 228.1 金属材料拉伸试验模拟方法；GB/T SIM 247 钢板和钢带包装、标志及质量证明书模拟规则；GB/T SIM 8170 数值修约模拟规则。",
                    ],
                },
                {
                    "heading": "3 术语和定义",
                    "paragraphs": [
                        "3.1 抗拉强度Rm：试样在拉伸过程中承受最大力对应的应力，单位为MPa。",
                        "3.2 下屈服强度ReL：有明显屈服现象时试样屈服阶段的最低应力，单位为MPa。",
                        "3.3 断后伸长率A：拉伸试样断后标距伸长与原始标距之比，单位为%。",
                    ],
                },
                {
                    "heading": "4 订货内容",
                    "paragraphs": [
                        "订货文件至少应包含牌号、品种、规格、交货重量、表面级别、边部状态、用途说明、检验批量和质量证明书要求。若订货方提出高于本文件的指标，应形成客户协议并按客户协议优先执行。",
                    ],
                },
                {
                    "heading": "5.1 化学成分",
                    "paragraphs": [
                        "Q235B冷轧板熔炼分析的化学成分应符合表1。表中元素含量按质量分数计算，单位为%。当客户协议未规定更严要求时，本条作为国家标准类结构化录入的默认依据。",
                    ],
                    "rows": [
                        ["元素", "合格下限", "合格上限", "单位", "说明"],
                        ["C", "无", "0.200", "%", "碳含量超过上限时不得判为合格"],
                        ["Mn", "0.300", "1.400", "%", "锰含量用于强度稳定性评价"],
                        ["Si", "无", "0.350", "%", "硅含量按熔炼分析记录"],
                        ["P", "无", "0.045", "%", "磷为有害元素，应控制上限"],
                        ["S", "无", "0.045", "%", "硫为有害元素，应控制上限"],
                    ],
                    "widths": chem_widths,
                },
                {
                    "heading": "6.1 力学性能",
                    "paragraphs": [
                        "厚度0.50mm至3.00mm的Q235B冷轧板力学性能应符合表2。抗拉强度Rm低于370MPa或高于510MPa时，不满足本文件合格范围；下屈服强度ReL低于235MPa时，不满足本文件合格范围；断后伸长率A低于26%时，不满足本文件合格范围。",
                    ],
                    "rows": [
                        ["指标", "合格下限", "合格上限", "单位", "RAG引用说明"],
                        ["Rm 抗拉强度", "370", "510", "MPa", "用于QUALIFIED/UNQUALIFIED解释"],
                        ["ReL 下屈服强度", "235", "无", "MPa", "必检项目，低于下限不得放行"],
                        ["A 断后伸长率", "26", "无", "%", "低于26%进入不合格或让步评审"],
                    ],
                    "widths": indicator_widths,
                },
                {
                    "heading": "7.1 表面质量",
                    "paragraphs": [
                        "钢板表面不应有裂纹、结疤、折叠、夹杂压入、明显划伤和影响使用的氧化色差。允许存在不影响成形和涂装的轻微压痕、辊印和局部色差，但应在检验记录中标注。",
                        "当客户用途为外观件或涂装件时，表面质量应按客户协议要求执行；客户协议没有规定时，本文件仅给出一般用途要求。",
                    ],
                },
                {
                    "heading": "7.2 厚度允许偏差",
                    "paragraphs": [
                        "厚度0.50mm至1.50mm的冷轧板，厚度偏差应控制在-0.100mm至+0.100mm；厚度大于1.50mm且不大于3.00mm的冷轧板，厚度偏差应控制在-0.120mm至+0.120mm。",
                        "厚度偏差超出合格范围但未超过-0.150mm至+0.150mm时，可进入让步评审；超过该让步边界时，不得作为本文件允许的让步接收。",
                    ],
                },
                {
                    "heading": "8.1 组批与取样",
                    "paragraphs": [
                        "同一牌号、同一炉号、同一轧制批次、同一热处理状态和相近规格的产品可组成一个检验批。力学性能每批至少取1组拉伸试样，化学成分以炉号分析为基础，尺寸偏差按卷号或板包抽检。",
                    ],
                },
                {
                    "heading": "8.3 复验规则",
                    "paragraphs": [
                        "若力学性能单项结果不合格，允许从同一检验批另取双倍数量试样进行复验。复验结果全部符合要求时，该项可判为合格；复验仍有任一试样不符合要求时，该批应判为不合格或转入人工评审。",
                        "因试样加工缺陷、夹持异常或明显设备异常造成的无效结果，应记录无效原因并重新取样，不得将无效数据用于自动判定。",
                    ],
                },
                {
                    "heading": "9.2 让步接收边界",
                    "paragraphs": [
                        "当抗拉强度Rm不低于360MPa且低于370MPa，或断后伸长率A不低于24%且低于26%，或厚度偏差超出合格范围但未超过-0.150mm至+0.150mm时，可提交让步评审。",
                        "让步评审应至少记录客户用途、偏差程度、历史投诉、替代库存、增加抽检方案和客户书面确认。让步评审通过前，不得生成正式质量证明书；仅可生成明确标注为内部预览的证明书草稿。",
                    ],
                },
                {
                    "heading": "10 质量证明书",
                    "paragraphs": [
                        "质量证明书应包括标准编号、牌号、规格、炉号、卷号或批号、化学成分、力学性能、尺寸偏差、判定结果、检验日期和签发人员。存在未解决标准冲突或未完成让步审批时，不得签发正式质量证明书。",
                    ],
                },
                {
                    "heading": "附录A 条款检索关键词",
                    "paragraphs": [
                        "资料性附录：Q235B、冷轧板、Rm 370至510MPa、ReL不低于235MPa、A不低于26%、厚度偏差-0.120mm至+0.120mm、让步下限Rm 360MPa、让步下限A 24%。",
                    ],
                },
            ],
        },
        {
            "filename": "Q-JH-STEEL-SIM-2026-Q345B_hot_rolled_plate_enterprise.pdf",
            "code": "Q/JH STEEL SIM 010-2026",
            "short_title": "Q345B热轧板模拟企标",
            "title": "Q345B低合金高强度结构钢热轧板企业技术条件（模拟企标）",
            "doc_type": "标准类型：ENTERPRISE / 企业标准类测试样本",
            "system_type": "ENTERPRISE",
            "version": "V2026.1",
            "issued": "2026-02-10",
            "effective": "2026-03-01",
            "variety": "热轧板、热轧钢带",
            "grade": "Q345B",
            "spec": "厚度2.00mm-12.00mm，宽度900mm-1800mm",
            "toc": [
                ["1", "范围", "热轧板Q345B过滤"],
                ["5.1", "冶炼与化学成分", "企业内控化学限值"],
                ["6.2", "力学性能", "ReL/Rm/A企业内控"],
                ["7.3", "板形与厚度偏差", "尺寸质量引用"],
                ["8.2", "让步评审", "企业让步边界与人工审批"],
                ["9.1", "质量证明", "证书引用和冲突限制"],
            ],
            "sections": [
                {
                    "heading": "1 范围",
                    "paragraphs": [
                        "本文件规定了Q345B低合金高强度结构钢热轧板的企业内控要求，适用于厚度2.00mm至12.00mm、宽度900mm至1800mm的热轧板和热轧钢带。",
                        "本文件用于桥梁附属构件、工程机械非关键受力件、一般焊接结构件的出厂质量控制。客户协议对同一指标提出更严要求时，应按客户协议执行；无客户协议时，本文件优先于通用国家标准作为企业内控依据。",
                    ],
                },
                {
                    "heading": "2 订货与状态",
                    "paragraphs": [
                        "订货信息应包含牌号、厚度、宽度、长度或卷重、交货状态、用途、冲击要求、表面质量级别和质量证明书格式。交货状态为热轧或控轧状态，必要时可约定正火或控冷状态。",
                    ],
                },
                {
                    "heading": "3 术语和批次",
                    "paragraphs": [
                        "同一炉号、同一轧制批次、同一交货状态且厚度差不大于2.0mm的产品可作为一个检验批。若客户对批次边界有更细要求，应按客户协议拆分。",
                    ],
                },
                {
                    "heading": "4 外观与尺寸",
                    "paragraphs": [
                        "钢板不得有裂纹、折叠、分层、气泡、夹杂压入和影响使用的边裂。允许局部修磨，修磨后厚度不得低于订货厚度允许负偏差下限。",
                    ],
                },
                {
                    "heading": "5.1 冶炼与化学成分",
                    "paragraphs": [
                        "Q345B热轧板熔炼分析应符合表1。企业内控要求适当严于通用要求，以保证焊接结构件强度稳定和批间一致性。",
                    ],
                    "rows": [
                        ["元素", "企业下限", "企业上限", "单位", "说明"],
                        ["C", "无", "0.200", "%", "高碳会增加焊接冷裂风险"],
                        ["Mn", "1.000", "1.600", "%", "锰含量用于保证强度"],
                        ["Si", "0.120", "0.350", "%", "硅含量过低时需关注脱氧稳定性"],
                        ["P", "无", "0.035", "%", "企业内控上限"],
                        ["S", "无", "0.035", "%", "企业内控上限"],
                    ],
                    "widths": chem_widths,
                },
                {
                    "heading": "5.2 碳当量提示",
                    "paragraphs": [
                        "当订货用途涉及焊接结构件时，宜计算碳当量并记录在检验备注中。碳当量提示用于工艺和让步风险评估，不作为本文件自动判定的单独结构化指标，除非订货协议另有规定。",
                    ],
                },
                {
                    "heading": "6.1 取样方向",
                    "paragraphs": [
                        "拉伸试样宜沿轧制方向取样。厚度不大于6.0mm时可采用全厚度试样；厚度大于6.0mm时按试验方法加工比例试样。试样位置应避开头尾不稳定区和可见缺陷区。",
                    ],
                },
                {
                    "heading": "6.2 力学性能",
                    "paragraphs": [
                        "厚度2.00mm至12.00mm的Q345B热轧板应符合表2。抗拉强度Rm合格范围为470MPa至630MPa；下屈服强度ReL不低于345MPa；断后伸长率A不低于20%。",
                    ],
                    "rows": [
                        ["指标", "合格下限", "合格上限", "单位", "说明"],
                        ["Rm 抗拉强度", "470", "630", "MPa", "企业内控强度范围"],
                        ["ReL 下屈服强度", "345", "无", "MPa", "低于下限时不得直接放行"],
                        ["A 断后伸长率", "20", "无", "%", "成形和焊接结构件风险指标"],
                    ],
                    "widths": indicator_widths,
                },
                {
                    "heading": "6.3 冲击和弯曲",
                    "paragraphs": [
                        "除订货协议明确要求外，本企业标准不将冲击功作为默认必检项目。客户要求零下冲击、冷弯或Z向性能时，应在结构化标准中另建客户协议或专用企业标准条目。",
                    ],
                },
                {
                    "heading": "7.2 厚度允许偏差",
                    "paragraphs": [
                        "厚度2.00mm至6.00mm时，厚度偏差应控制在-0.180mm至+0.180mm；厚度大于6.00mm且不大于12.00mm时，厚度偏差应控制在-0.250mm至+0.250mm。",
                    ],
                },
                {
                    "heading": "7.3 板形与表面修磨",
                    "paragraphs": [
                        "不平度、镰刀弯和边浪应满足普通结构件装配要求。表面修磨应平滑过渡，不得形成新的应力集中。修磨处厚度接近负偏差下限时，应增加复测点并记录位置。",
                    ],
                },
                {
                    "heading": "8.1 检验与复验",
                    "paragraphs": [
                        "力学性能不合格时可进行一次复验。复验应从同一批次重新取双倍试样，复验全部符合要求时允许判定该项合格；复验任一试样不合格时，应判为不合格或提交技术质量评审。",
                    ],
                },
                {
                    "heading": "8.2 让步评审",
                    "paragraphs": [
                        "当Rm不低于460MPa且低于470MPa，或A不低于18%且低于20%，或厚度偏差超出合格范围但不超过对应合格边界外0.050mm时，可提交企业内部让步评审。",
                        "让步评审必须包含用途确认、偏差程度、客户是否接受、同炉批稳定性、历史投诉、替代库存和追加检验方案。若用途为主承载焊接件、压力件或客户明确禁止让步的订单，应直接判为高风险，不得建议常规让步。",
                    ],
                },
                {
                    "heading": "9.1 质量证明",
                    "paragraphs": [
                        "质量证明书应列出本企业标准编号、炉号、批号、规格、化学成分、力学性能和检验结论。存在同优先级企业标准冲突、客户协议冲突或未完成让步审批时，只允许生成内部预览，不得签发正式质量证明书。",
                    ],
                },
                {
                    "heading": "附录A 条款检索关键词",
                    "paragraphs": [
                        "资料性附录：Q345B、热轧板、Rm 470至630MPa、ReL不低于345MPa、A不低于20%、企业让步Rm 460MPa、断后伸长率让步下限18%、厚度2.00mm至12.00mm。",
                    ],
                },
            ],
        },
        {
            "filename": "CUST-XNJC-SIM-2026-Q235B_customer_agreement.pdf",
            "code": "AGREEMENT XNJC-SIM-2026-001",
            "short_title": "西南建材Q235B模拟客户协议",
            "title": "西南建材集团Q235B冷轧板供货质量协议（模拟客户协议）",
            "doc_type": "标准类型：CUSTOMER / 客户协议类测试样本",
            "system_type": "CUSTOMER",
            "version": "协议V1.0",
            "issued": "2026-01-20",
            "effective": "2026-02-01 至 2026-12-31",
            "variety": "冷轧板、定尺开平板",
            "grade": "Q235B",
            "spec": "厚度0.80mm-2.50mm，宽度800mm-1250mm",
            "customer": "CUST-002 西南建材集团",
            "toc": [
                ["1", "协议范围", "客户/用途过滤"],
                ["3.1", "客户机械性能", "客户优先限值"],
                ["3.3", "化学成分", "客户补充限值"],
                ["4.2", "厚度与表面", "客户尺寸/外观要求"],
                ["6.1", "让步接收", "客户确认和风险维度"],
                ["7.1", "质量证明书", "证书放行限制"],
            ],
            "sections": [
                {
                    "heading": "1 协议范围",
                    "paragraphs": [
                        "本协议适用于供西南建材集团的Q235B冷轧板和定尺开平板，主要用途为建筑围护件、普通支撑件、轻型龙骨配套件和非安全关键结构件。",
                        "适用规格为厚度0.80mm至2.50mm、宽度800mm至1250mm。超出本规格范围、用于高强承重构件或由客户指定为安全关键用途的订单，不得套用本协议的让步接收条款。",
                    ],
                },
                {
                    "heading": "2 优先级和资料",
                    "paragraphs": [
                        "在本协议有效期内，本协议对相同客户、相同品种、相同牌号和相同规格范围内的订单优先于企业标准和国家标准执行。国家标准和企业标准未被本协议覆盖的项目，按供方已发布结构化标准执行。",
                        "供方应在发货前提供质量证明书、炉批号追溯、尺寸抽检记录和必要的让步评审记录。客户有权对有争议批次提出复验要求。",
                    ],
                },
                {
                    "heading": "3.1 客户机械性能",
                    "paragraphs": [
                        "供西南建材集团的Q235B冷轧板，抗拉强度Rm应为375MPa至505MPa；下屈服强度ReL不应低于235MPa且不宜高于360MPa；断后伸长率A不应低于27%。",
                        "本条款用于客户协议优先判定。若国家标准允许较低断后伸长率，本协议仍以A不低于27%作为合格要求。Rm低于375MPa或A低于27%时，不得直接按国家标准判为合格。",
                    ],
                    "rows": [
                        ["指标", "客户合格下限", "客户合格上限", "单位", "客户关注点"],
                        ["Rm 抗拉强度", "375", "505", "MPa", "过低影响支撑稳定，过高影响现场折弯"],
                        ["ReL 下屈服强度", "235", "360", "MPa", "避免屈服过高导致折弯回弹大"],
                        ["A 断后伸长率", "27", "无", "%", "低于客户下限需让步评审"],
                    ],
                    "widths": indicator_widths,
                },
                {
                    "heading": "3.2 成形性能提示",
                    "paragraphs": [
                        "客户加工工艺包含辊压成形和现场轻度折弯。若A低于27%但不低于25%，应结合用途、折弯半径、历史投诉和替代库存评估；若A低于25%，客户原则上不接受让步。",
                    ],
                },
                {
                    "heading": "3.3 化学成分",
                    "paragraphs": [
                        "化学成分按炉号提供。客户对C、P、S提出补充控制要求：C不大于0.190%，P不大于0.040%，S不大于0.040%。Mn和Si按供方已发布标准执行，但异常波动应在质量证明书备注中说明。",
                    ],
                    "rows": [
                        ["元素", "客户下限", "客户上限", "单位", "说明"],
                        ["C", "无", "0.190", "%", "客户补充上限"],
                        ["P", "无", "0.040", "%", "客户补充上限"],
                        ["S", "无", "0.040", "%", "客户补充上限"],
                        ["Mn", "按供方标准", "按供方标准", "%", "证书列实测值"],
                        ["Si", "按供方标准", "按供方标准", "%", "证书列实测值"],
                    ],
                    "widths": chem_widths,
                },
                {
                    "heading": "4.1 表面质量",
                    "paragraphs": [
                        "表面不得有影响涂装和装配的折叠、结疤、边裂、连续划伤和明显辊印。单处轻微压痕面积不大于20mm乘20mm且不连续出现时，可按普通围护件用途接收，但应在出货记录中标注。",
                    ],
                },
                {
                    "heading": "4.2 厚度与尺寸偏差",
                    "paragraphs": [
                        "厚度0.80mm至1.50mm时，厚度偏差应控制在-0.080mm至+0.080mm；厚度大于1.50mm且不大于2.50mm时，厚度偏差应控制在-0.100mm至+0.100mm。",
                        "宽度偏差应控制在0mm至+4mm。定尺开平板长度偏差应控制在0mm至+6mm。尺寸超差不得通过文字说明直接放行，必须进入让步或返修评审。",
                    ],
                },
                {
                    "heading": "5.1 检验批和抽检",
                    "paragraphs": [
                        "同一客户订单、同一牌号、同一炉号和相同厚度规格的产品可作为一个客户检验批。每批至少抽检一组力学性能和三个厚度位置；当批量超过80吨时，应增加一组力学性能复核。",
                    ],
                },
                {
                    "heading": "5.2 复验与异议",
                    "paragraphs": [
                        "客户对性能数据有异议时，双方可约定第三方实验室复验。复验样品应来自原批次留样或未加工余料。复验结果低于本协议限值时，该批次不得按合格批次出具正式质量证明书。",
                    ],
                },
                {
                    "heading": "6.1 让步接收",
                    "paragraphs": [
                        "当Rm不低于370MPa且低于375MPa，或A不低于25%且低于27%，或厚度偏差超出合格范围但未超过对应边界外0.020mm时，可提交客户让步评审。",
                        "让步评审必须取得客户书面确认，并明确用途限制为建筑围护件或普通支撑件；不得用于高强承重件、安全关键连接件或客户指定的外观一级件。存在同类历史投诉时，风险等级至少提高一级。",
                    ],
                },
                {
                    "heading": "6.2 替代发货优先",
                    "paragraphs": [
                        "若供方在约定交期内存在同规格或兼容规格的合格替代库存，应优先评估替代发货。只有在替代库存不可用、客户用途允许且偏差处于6.1规定范围内时，方可继续客户让步流程。",
                    ],
                },
                {
                    "heading": "6.3 让步附加条件",
                    "paragraphs": [
                        "客户让步通过时，供方应在发货记录中增加批次追溯、用途限制、增加抽检、质量证明书备注和售后跟踪。低置信度评估、缺少客户用途、缺少历史投诉核查或存在未解决标准冲突时，必须转人工质量负责人复核。",
                    ],
                },
                {
                    "heading": "7.1 质量证明书",
                    "paragraphs": [
                        "正式质量证明书应列明本协议编号、客户订单号、炉号、卷号、规格、化学成分、Rm、ReL、A、尺寸偏差和最终放行结论。让步批次的证明书备注必须包含客户确认编号和用途限制。",
                        "存在未解决的同优先级客户协议冲突、客户未确认让步、复验未完成或替代发货仍在评估时，不得签发正式质量证明书，可生成内部预览版本供审批流查看。",
                    ],
                },
                {
                    "heading": "附录A 条款检索关键词",
                    "paragraphs": [
                        "资料性附录：CUST-002、西南建材集团、Q235B冷轧板、Rm 375至505MPa、ReL 235至360MPa、A不低于27%、客户让步Rm 370MPa、客户让步A 25%、建筑围护件、替代库存优先。",
                    ],
                },
            ],
        },
    ]


def additional_doc(spec):
    kind_name = {
        "NATIONAL": "NATIONAL / 国家标准类测试样本",
        "ENTERPRISE": "ENTERPRISE / 企业标准类测试样本",
        "CUSTOMER": "CUSTOMER / 客户协议类测试样本",
    }[spec["system_type"]]
    standard_scope = spec.get("standard_scope", "通用")
    customer = spec.get("customer", "通用")
    chem_rows = [["元素/项目", "合格下限", "合格上限", "单位", "说明"]] + spec["chem_rows"]
    perf_rows = [["指标", "合格下限", "合格上限", "单位", "RAG引用说明"]] + spec["perf_rows"]
    if spec["system_type"] == "CUSTOMER":
        priority_text = "本协议对相同客户、品种、牌号、规格和有效期内的订单优先于企业标准和国家标准执行；未覆盖项目按供方已发布结构化标准执行。"
        concession_heading = "6.1 客户让步接收"
        cert_heading = "7.1 质量证明书"
        performance_heading = "3.1 客户关键性能"
        chemistry_heading = "3.3 化学成分与补充要求"
    elif spec["system_type"] == "ENTERPRISE":
        priority_text = "本企业标准用于企业内控和出厂检验；客户协议提出更严或更专用要求时，按客户协议优先执行。"
        concession_heading = "8.2 让步评审"
        cert_heading = "9.1 质量证明"
        performance_heading = "6.2 关键性能"
        chemistry_heading = "5.1 冶炼与化学成分"
    else:
        priority_text = "本文件作为通用国家标准类测试依据。客户协议或企业标准有更严要求时，系统应按标准优先级选择结构化规则。"
        concession_heading = "9.2 让步接收边界"
        cert_heading = "10 质量证明书"
        performance_heading = "6.1 关键性能"
        chemistry_heading = "5.1 化学成分"

    return {
        "filename": spec["filename"],
        "code": spec["code"],
        "short_title": spec["short_title"],
        "title": spec["title"],
        "doc_type": "标准类型：" + kind_name,
        "system_type": spec["system_type"],
        "version": spec["version"],
        "issued": spec["issued"],
        "effective": spec["effective"],
        "variety": spec["variety"],
        "grade": spec["grade"],
        "spec": spec["spec"],
        "customer": customer,
        "toc": [
            ["1", "范围", "品种、牌号、规格和用途过滤"],
            ["2", "优先级和订货资料", "标准优先级与结构化规则"],
            [chemistry_heading.split()[0], chemistry_heading[4:], "化学成分或关键制造指标引用"],
            [performance_heading.split()[0], performance_heading[4:], "判定解释和RAG问答"],
            ["7.2", "尺寸与外观", "厚度、宽度、表面质量引用"],
            [concession_heading.split()[0], concession_heading[4:], "让步边界和风险评估"],
            [cert_heading.split()[0], cert_heading[4:], "质量证明书限制"],
        ],
        "sections": [
            {
                "heading": "1 范围",
                "paragraphs": [
                    "本文件规定了%s的订货内容、化学成分或关键制造指标、力学或专用性能、尺寸允许偏差、检验规则、让步边界和质量证明书要求。" % spec["title"].replace("（模拟国标）", "").replace("（模拟企标）", "").replace("（模拟客户协议）", ""),
                    "适用范围为%s，牌号或钢种为%s，规格范围为%s。典型用途为%s。%s" % (spec["variety"], spec["grade"], spec["spec"], spec["usage"], spec.get("scope_note", "")),
                ],
            },
            {
                "heading": "2 优先级和订货资料",
                "paragraphs": [
                    priority_text,
                    "订货或维护资料至少应包含标准编号、版本、有效期、品种、牌号、规格范围、客户范围、用途说明、检验批规则和质量证明书要求。PDF抽取文本仅用于检索和引用，不得自动覆盖结构化指标。",
                ],
            },
            {
                "heading": "3 术语和检验口径",
                "paragraphs": [
                    "抗拉强度Rm、屈服强度ReL或Rp0.2、断后伸长率A、硬度、镀层重量、磁性能或冲击功等指标，应按本文件或订货协议规定的单位记录。不同单位或不同试样方向不能直接比较时，应转人工确认。",
                    "同一炉号、同一轧制批次、同一交货状态和相近规格的产品可作为一个检验批。%s" % spec.get("batch_note", "客户或专用标准有更细批次要求时，应按更严要求拆分。"),
                ],
            },
            {
                "heading": chemistry_heading,
                "paragraphs": [
                    "%s的化学成分或关键制造控制项目应符合表1。表中数据为模拟测试限值，用于验证RAG条款引用、结构化规则比对和一致性检查。" % spec["grade"],
                ],
                "rows": chem_rows,
                "widths": [25 * mm, 27 * mm, 27 * mm, 24 * mm, 49 * mm],
            },
            {
                "heading": performance_heading,
                "paragraphs": [
                    "%s在%s范围内的关键性能应符合表2。系统自动判定仍以结构化标准指标为准，本PDF条款用于解释、问答和人工复核。" % (spec["grade"], spec["spec"]),
                    spec.get("performance_note", "当实测值超出合格范围时，应结合偏差程度、用途、历史投诉和替代资源决定是否进入让步或复验流程。"),
                ],
                "rows": perf_rows,
                "widths": [28 * mm, 28 * mm, 28 * mm, 23 * mm, 45 * mm],
            },
            {
                "heading": "7.1 表面质量",
                "paragraphs": [
                    spec.get("surface", "产品表面不得有裂纹、折叠、分层、结疤、夹杂压入、连续划伤和影响最终用途的缺陷。允许存在不影响使用的轻微色差、局部压痕或边部毛刺，但应在检验记录中说明。"),
                ],
            },
            {
                "heading": "7.2 尺寸与外观允许偏差",
                "paragraphs": [
                    spec["dimension"],
                    "尺寸或外观超出合格范围时，不得通过自然语言说明直接改判；应依据结构化标准、复验记录、让步审批和客户确认形成最终结论。",
                ],
            },
            {
                "heading": "8.1 检验与复验",
                "paragraphs": [
                    spec.get("retest", "关键性能单项不合格时，可从同一检验批另取双倍数量试样进行一次复验。复验全部符合要求时，该项可判为合格；复验仍有任一试样不符合要求时，该批应判为不合格或提交人工评审。"),
                    "因取样、制样、夹持或设备异常造成的无效结果，应记录无效原因并重新取样，不得将无效数据用于自动判定。",
                ],
            },
            {
                "heading": concession_heading,
                "paragraphs": [
                    spec["concession"],
                    spec.get("concession_review", "让步评审应至少记录用途、偏差程度、客户确认、历史投诉、替代库存、追加检验和质量证明书备注。低置信度、缺少用途、缺少证据或存在未解决标准冲突时，必须转人工质量负责人复核。"),
                ],
            },
            {
                "heading": cert_heading,
                "paragraphs": [
                    spec["certificate"],
                    "存在未解决标准冲突、未完成复验、未完成让步审批或客户确认缺失时，不得签发正式质量证明书；系统仅可生成明确标注为内部预览的证明书草稿。",
                ],
            },
            {
                "heading": "附录A 条款检索关键词",
                "paragraphs": [
                    "资料性附录：%s、%s、%s、%s、让步接收、复验规则、质量证明书、标准优先级、%s。" % (
                        spec["grade"],
                        spec["variety"],
                        spec["spec"],
                        spec["keywords"],
                        standard_scope,
                    ),
                ],
            },
        ],
    }


ADDITIONAL_SPECS = [
    {
        "filename": "GB-T-SIM-1499-2026_HRB400E_rebar_national.pdf",
        "code": "GB/T SIM 1499-2026",
        "short_title": "HRB400E钢筋模拟国标",
        "title": "热轧带肋钢筋质量要求（模拟国标）",
        "system_type": "NATIONAL",
        "version": "2026版",
        "issued": "2026-01-18",
        "effective": "2026-04-01",
        "variety": "热轧带肋钢筋",
        "grade": "HRB400E",
        "spec": "公称直径12mm-32mm",
        "usage": "抗震设防区域普通钢筋混凝土结构",
        "chem_rows": [["C", "无", "0.250", "%", "熔炼分析上限"], ["Mn", "1.000", "1.600", "%", "保证强度稳定"], ["P", "无", "0.045", "%", "有害元素上限"], ["S", "无", "0.045", "%", "有害元素上限"], ["Ceq", "无", "0.540", "%", "焊接性提示"]],
        "perf_rows": [["ReL 屈服强度", "400", "无", "MPa", "抗震钢筋必检"], ["Rm 抗拉强度", "540", "无", "MPa", "用于强屈比计算"], ["A 断后伸长率", "16", "无", "%", "塑性指标"], ["强屈比", "1.25", "无", "-", "抗震性能指标"]],
        "dimension": "公称直径12mm至20mm时，重量偏差应控制在-6.0%至+6.0%；公称直径22mm至32mm时，重量偏差应控制在-5.0%至+5.0%。横肋间距、肋高和弯曲度应满足普通轧制钢筋要求。",
        "concession": "抗震钢筋的屈服强度、强屈比和最大力总延伸率不允许常规让步。仅重量偏差超出合格范围但不超过0.5个百分点且用途不涉及抗震关键部位时，可提交人工评审。",
        "certificate": "质量证明书应列明炉号、批号、公称直径、ReL、Rm、A、强屈比、重量偏差和抗震标识。抗震性能未检或冲突未解决时不得正式签发。",
        "keywords": "ReL不低于400MPa、Rm不低于540MPa、强屈比不低于1.25、重量偏差",
    },
    {
        "filename": "GB-T-SIM-3274-2026_Q355B_hot_rolled_plate_national.pdf",
        "code": "GB/T SIM 3274-2026",
        "short_title": "Q355B热轧中厚板模拟国标",
        "title": "碳素和低合金结构钢热轧钢板质量要求（模拟国标）",
        "system_type": "NATIONAL",
        "version": "2026版",
        "issued": "2026-01-25",
        "effective": "2026-04-01",
        "variety": "热轧中厚板",
        "grade": "Q355B",
        "spec": "厚度6mm-40mm，宽度1500mm-3200mm",
        "usage": "普通焊接结构件、平台梁和设备底座",
        "chem_rows": [["C", "无", "0.220", "%", "碳含量上限"], ["Mn", "1.000", "1.700", "%", "强度稳定元素"], ["Si", "0.100", "0.500", "%", "脱氧元素"], ["P", "无", "0.035", "%", "有害元素上限"], ["S", "无", "0.035", "%", "有害元素上限"]],
        "perf_rows": [["ReL 屈服强度", "355", "无", "MPa", "厚度6mm-16mm"], ["Rm 抗拉强度", "470", "630", "MPa", "结构强度范围"], ["A 断后伸长率", "21", "无", "%", "塑性指标"], ["冷弯", "合格", "合格", "-", "弯曲后不得有裂纹"]],
        "dimension": "厚度6mm至16mm时厚度偏差应控制在-0.40mm至+0.60mm；厚度大于16mm且不大于40mm时厚度偏差应控制在-0.50mm至+0.80mm。",
        "concession": "Rm低于下限不超过10MPa或A低于下限不超过1个百分点时，可在非主承载用途下提交人工让步；ReL低于355MPa时不得常规让步。",
        "certificate": "质量证明书应列明标准编号、炉批号、厚度、ReL、Rm、A、冷弯、尺寸偏差和表面质量等级。",
        "keywords": "Q355B热轧板、ReL 355MPa、Rm 470至630MPa、A不低于21%",
    },
    {
        "filename": "GB-T-SIM-2518-2026_DX51D-Z_galvanized_sheet_national.pdf",
        "code": "GB/T SIM 2518-2026",
        "short_title": "DX51D+Z镀锌板模拟国标",
        "title": "连续热镀锌钢板及钢带质量要求（模拟国标）",
        "system_type": "NATIONAL",
        "version": "2026版",
        "issued": "2026-02-01",
        "effective": "2026-05-01",
        "variety": "连续热镀锌板",
        "grade": "DX51D+Z",
        "spec": "厚度0.40mm-3.00mm，镀层Z80-Z275",
        "usage": "一般成形件、通风管、家电背板和建筑配件",
        "chem_rows": [["C", "无", "0.120", "%", "低碳成形用途"], ["Mn", "无", "0.600", "%", "熔炼分析上限"], ["P", "无", "0.100", "%", "成形级上限"], ["S", "无", "0.045", "%", "有害元素上限"], ["Al", "0.020", "0.080", "%", "脱氧稳定性提示"]],
        "perf_rows": [["Rm 抗拉强度", "270", "500", "MPa", "一般成形级范围"], ["A80 断后伸长率", "22", "无", "%", "成形性能"], ["镀层重量", "80", "275", "g/m2", "双面三点平均"], ["弯曲试验", "合格", "合格", "-", "弯曲处镀层不得剥落"]],
        "dimension": "厚度0.40mm至1.20mm时厚度偏差应控制在-0.060mm至+0.060mm；厚度大于1.20mm且不大于3.00mm时厚度偏差应控制在-0.090mm至+0.090mm。镀层重量应按订货牌号记录。",
        "concession": "镀层重量低于订货下限时不得按本文件常规让步。Rm或A80轻微偏离且用途为非外观、非深冲件时，可提交人工评审并增加弯曲试验。",
        "certificate": "质量证明书应列明基板牌号、镀层代号、镀层重量、Rm、A80、弯曲试验、表面结构和钝化/涂油状态。",
        "keywords": "DX51D+Z、热镀锌板、镀层重量Z80至Z275、A80不低于22%",
    },
    {
        "filename": "GB-T-SIM-4237-2026_06Cr19Ni10_stainless_sheet_national.pdf",
        "code": "GB/T SIM 4237-2026",
        "short_title": "06Cr19Ni10不锈钢模拟国标",
        "title": "不锈钢冷轧钢板和钢带质量要求（模拟国标）",
        "system_type": "NATIONAL",
        "version": "2026版",
        "issued": "2026-02-05",
        "effective": "2026-05-01",
        "variety": "冷轧不锈钢板",
        "grade": "06Cr19Ni10",
        "spec": "厚度0.40mm-3.00mm，宽度600mm-1500mm",
        "usage": "厨房设备、装饰件、一般耐蚀结构件",
        "chem_rows": [["C", "无", "0.080", "%", "奥氏体不锈钢上限"], ["Cr", "18.000", "20.000", "%", "耐蚀性关键元素"], ["Ni", "8.000", "11.000", "%", "奥氏体稳定元素"], ["P", "无", "0.045", "%", "有害元素上限"], ["S", "无", "0.030", "%", "有害元素上限"]],
        "perf_rows": [["Rp0.2 屈服强度", "205", "无", "MPa", "规定塑性延伸强度"], ["Rm 抗拉强度", "520", "无", "MPa", "强度下限"], ["A 断后伸长率", "40", "无", "%", "冷轧退火状态"], ["硬度", "无", "201", "HBW", "硬度上限"]],
        "dimension": "厚度0.40mm至1.00mm时偏差应控制在-0.050mm至+0.050mm；厚度大于1.00mm且不大于3.00mm时偏差应控制在-0.080mm至+0.080mm。表面状态可为2B、BA或NO.4。",
        "concession": "Cr或Ni超出化学成分范围不得常规让步。硬度略高但用途为非深冲装饰件时，可提交人工评审并追加弯曲或成形验证。",
        "certificate": "质量证明书应列明牌号、炉号、Cr、Ni、C、Rp0.2、Rm、A、硬度、表面状态和耐蚀用途提示。",
        "keywords": "06Cr19Ni10、冷轧不锈钢、Cr 18至20%、Ni 8至11%、A不低于40%",
    },
    {
        "filename": "GB-T-SIM-713-2026_Q345R_pressure_vessel_plate_national.pdf",
        "code": "GB/T SIM 713-2026",
        "short_title": "Q345R容器板模拟国标",
        "title": "锅炉和压力容器用钢板质量要求（模拟国标）",
        "system_type": "NATIONAL",
        "version": "2026版",
        "issued": "2026-02-12",
        "effective": "2026-05-15",
        "variety": "压力容器钢板",
        "grade": "Q345R",
        "spec": "厚度6mm-30mm，正火或控轧交货",
        "usage": "非低温压力容器筒体、封头和承压附件",
        "scope_note": "承压用途为高风险场景，系统让步评估应默认进入人工复核。",
        "chem_rows": [["C", "无", "0.200", "%", "焊接性控制"], ["Mn", "1.200", "1.700", "%", "强度元素"], ["Si", "0.150", "0.500", "%", "脱氧元素"], ["P", "无", "0.025", "%", "承压件严控"], ["S", "无", "0.015", "%", "承压件严控"]],
        "perf_rows": [["ReL 屈服强度", "345", "无", "MPa", "厚度6mm-16mm"], ["Rm 抗拉强度", "510", "640", "MPa", "承压强度范围"], ["A 断后伸长率", "21", "无", "%", "塑性指标"], ["KV2 冲击功", "34", "无", "J", "0摄氏度横向冲击"]],
        "dimension": "厚度6mm至30mm钢板厚度偏差应控制在-0.30mm至+0.80mm；不平度应满足压力容器制造装配要求，边部分层和可见裂纹不允许存在。",
        "concession": "承压用途下ReL、Rm、A、冲击功和P/S化学成分不允许常规让步。仅外观轻微缺陷修磨后仍满足厚度要求时，可提交技术负责人审批。",
        "certificate": "质量证明书应列明热处理状态、无损检测要求、ReL、Rm、A、冲击功、P、S和炉批追溯。承压关键指标缺失时不得签发。",
        "keywords": "Q345R、压力容器板、Rm 510至640MPa、KV2不低于34J、P不大于0.025%",
    },
    {
        "filename": "GB-T-SIM-9711-2026_L245M_pipeline_steel_national.pdf",
        "code": "GB/T SIM 9711-2026",
        "short_title": "L245M管线钢模拟国标",
        "title": "石油天然气输送管用管线钢板卷质量要求（模拟国标）",
        "system_type": "NATIONAL",
        "version": "2026版",
        "issued": "2026-02-16",
        "effective": "2026-05-20",
        "variety": "管线钢板卷",
        "grade": "L245M",
        "spec": "厚度4mm-20mm，宽度900mm-1800mm",
        "usage": "低中压输送管和一般能源管线",
        "chem_rows": [["C", "无", "0.120", "%", "低碳焊接控制"], ["Mn", "0.800", "1.600", "%", "强韧性平衡"], ["P", "无", "0.020", "%", "管线钢严控"], ["S", "无", "0.010", "%", "管线钢严控"], ["Ceq", "无", "0.420", "%", "焊接性提示"]],
        "perf_rows": [["ReL 屈服强度", "245", "450", "MPa", "管线钢等级范围"], ["Rm 抗拉强度", "415", "565", "MPa", "强度范围"], ["A 断后伸长率", "24", "无", "%", "塑性指标"], ["冲击功", "40", "无", "J", "0摄氏度夏比冲击"]],
        "dimension": "厚度偏差应控制在-0.20mm至+0.20mm，镰刀弯和塔形应满足制管生产线稳定开卷要求。边部缺口、分层和明显浪形应记录并隔离。",
        "concession": "P、S、Ceq、ReL上限和冲击功不允许常规让步。Rm低于下限不超过10MPa且用途为低压非酸性环境时，可提交人工风险评审。",
        "certificate": "质量证明书应列明钢级、交货状态、Ceq、ReL、Rm、A、冲击功、厚度偏差和制管用途。酸性服役用途需另行协议。",
        "keywords": "L245M、管线钢、ReL 245至450MPa、Rm 415至565MPa、Ceq不大于0.420%",
    },
    {
        "filename": "GB-T-SIM-5213-2026_DC04_deep_drawing_sheet_national.pdf",
        "code": "GB/T SIM 5213-2026",
        "short_title": "DC04深冲板模拟国标",
        "title": "深冲用冷轧低碳钢板及钢带质量要求（模拟国标）",
        "system_type": "NATIONAL",
        "version": "2026版",
        "issued": "2026-02-20",
        "effective": "2026-06-01",
        "variety": "深冲冷轧板",
        "grade": "DC04",
        "spec": "厚度0.50mm-2.00mm，宽度700mm-1600mm",
        "usage": "家电面板、浅拉伸件和一般深冲件",
        "chem_rows": [["C", "无", "0.080", "%", "深冲低碳控制"], ["Mn", "无", "0.400", "%", "成形稳定性"], ["P", "无", "0.030", "%", "有害元素上限"], ["S", "无", "0.030", "%", "有害元素上限"], ["Alt", "0.020", "0.070", "%", "铝镇静控制"]],
        "perf_rows": [["ReL 屈服强度", "140", "210", "MPa", "成形回弹控制"], ["Rm 抗拉强度", "270", "350", "MPa", "深冲强度范围"], ["A80 断后伸长率", "38", "无", "%", "深冲塑性"], ["r90 塑性应变比", "1.60", "无", "-", "深冲性能提示"]],
        "dimension": "厚度0.50mm至1.20mm时厚度偏差应控制在-0.050mm至+0.050mm；厚度大于1.20mm至2.00mm时控制在-0.070mm至+0.070mm。表面级别为FB及以上时不得有影响涂装的压痕。",
        "concession": "A80低于38%但不低于36%、r90轻微低于要求且用途为浅拉伸件时，可提交让步评审；用于深拉伸外观件时不得建议常规让步。",
        "certificate": "质量证明书应列明ReL、Rm、A80、r90、表面级别、涂油状态和尺寸偏差。深冲关键指标缺失时不得正式签发。",
        "keywords": "DC04、深冲冷轧板、ReL 140至210MPa、A80不低于38%、r90不低于1.60",
    },
    {
        "filename": "GB-T-SIM-2521-2026_50W800_electrical_steel_national.pdf",
        "code": "GB/T SIM 2521-2026",
        "short_title": "50W800电工钢模拟国标",
        "title": "冷轧无取向电工钢带质量要求（模拟国标）",
        "system_type": "NATIONAL",
        "version": "2026版",
        "issued": "2026-03-01",
        "effective": "2026-06-10",
        "variety": "冷轧无取向电工钢",
        "grade": "50W800",
        "spec": "公称厚度0.50mm，宽度900mm-1250mm",
        "usage": "中小电机铁芯、压缩机电机和普通磁性部件",
        "chem_rows": [["C", "无", "0.005", "%", "磁时效控制"], ["Si", "1.000", "2.000", "%", "磁性能调节"], ["Al", "0.200", "0.800", "%", "电阻率控制"], ["P", "无", "0.030", "%", "有害元素上限"], ["S", "无", "0.010", "%", "夹杂控制"]],
        "perf_rows": [["P15/50 铁损", "无", "8.00", "W/kg", "磁性能上限"], ["B50 磁感", "1.65", "无", "T", "磁感应强度下限"], ["叠装系数", "0.95", "无", "-", "铁芯有效面积"], ["硬度", "无", "190", "HV5", "冲片加工性"]],
        "dimension": "公称厚度0.50mm时厚度偏差应控制在-0.020mm至+0.020mm，同板差不大于0.015mm。毛刺高度应不大于0.030mm，浪形应满足高速冲片要求。",
        "concession": "铁损P15/50超过上限或B50低于下限时不得常规让步。仅毛刺高度轻微超差且客户冲片验证合格时，可提交人工让步。",
        "certificate": "质量证明书应列明铁损、磁感、叠装系数、硬度、厚度偏差、绝缘涂层类型和涂层电阻。",
        "keywords": "50W800、电工钢、P15/50不大于8.00W/kg、B50不低于1.65T",
    },
    {
        "filename": "GB-T-SIM-1222-2026_60Si2Mn_spring_flat_steel_national.pdf",
        "code": "GB/T SIM 1222-2026",
        "short_title": "60Si2Mn弹簧钢模拟国标",
        "title": "弹簧钢热轧扁钢质量要求（模拟国标）",
        "system_type": "NATIONAL",
        "version": "2026版",
        "issued": "2026-03-05",
        "effective": "2026-06-15",
        "variety": "热轧弹簧扁钢",
        "grade": "60Si2Mn",
        "spec": "厚度3mm-20mm，宽度20mm-120mm",
        "usage": "汽车板簧、机械弹簧和弹性元件",
        "chem_rows": [["C", "0.560", "0.640", "%", "弹簧强度关键"], ["Si", "1.500", "2.000", "%", "弹性极限关键"], ["Mn", "0.600", "0.900", "%", "淬透性元素"], ["P", "无", "0.030", "%", "有害元素上限"], ["S", "无", "0.030", "%", "有害元素上限"]],
        "perf_rows": [["硬度", "269", "321", "HBW", "热轧退火状态"], ["Rm 热处理后", "1274", "1568", "MPa", "调质后参考"], ["A 断后伸长率", "5", "无", "%", "热处理后参考"], ["脱碳层", "无", "0.25", "mm", "表面质量关键"]],
        "dimension": "厚度偏差应控制在-0.20mm至+0.20mm，宽度偏差应控制在-0.50mm至+0.80mm。边部裂纹、折叠、严重脱碳和明显氧化皮压入不允许存在。",
        "concession": "C、Si、Mn超出范围或脱碳层超过上限时不得常规让步。硬度轻微偏离且可重新热处理时，可提交返工评审而非直接让步。",
        "certificate": "质量证明书应列明化学成分、硬度、脱碳层、尺寸偏差、热处理状态和表面质量。弹簧关键用途缺少脱碳检测时不得签发。",
        "keywords": "60Si2Mn、弹簧扁钢、硬度269至321HBW、脱碳层不大于0.25mm",
    },
    {
        "filename": "Q-JH-STEEL-SIM-020-2026_SPFH590_pickled_strip_enterprise.pdf",
        "code": "Q/JH STEEL SIM 020-2026",
        "short_title": "SPFH590酸洗钢带模拟企标",
        "title": "SPFH590汽车结构用酸洗高强钢带企业技术条件（模拟企标）",
        "system_type": "ENTERPRISE",
        "version": "V2026.1",
        "issued": "2026-03-08",
        "effective": "2026-04-01",
        "variety": "酸洗热轧钢带",
        "grade": "SPFH590",
        "spec": "厚度1.60mm-6.00mm，宽度800mm-1600mm",
        "usage": "汽车底盘支架、加强件和辊压结构件",
        "chem_rows": [["C", "无", "0.120", "%", "焊接性控制"], ["Mn", "1.200", "1.900", "%", "强化元素"], ["Si", "无", "0.500", "%", "成形稳定"], ["P", "无", "0.025", "%", "企业内控"], ["S", "无", "0.015", "%", "企业内控"]],
        "perf_rows": [["ReL 屈服强度", "420", "560", "MPa", "成形回弹控制"], ["Rm 抗拉强度", "590", "700", "MPa", "高强钢范围"], ["A 断后伸长率", "18", "无", "%", "成形风险指标"], ["扩孔率", "45", "无", "%", "边部成形提示"]],
        "dimension": "厚度偏差应控制在-0.120mm至+0.120mm，酸洗表面不得有欠酸洗、过酸洗、连续划伤和影响焊接的氧化皮残留。",
        "concession": "A低于18%但不低于16%、扩孔率低于45%但不低于40%时，仅非安全加强件可提交让步；汽车安全件必须转客户确认。",
        "certificate": "质量证明书应列明酸洗状态、ReL、Rm、A、扩孔率、厚度偏差和表面质量。客户安全件未确认不得放行。",
        "keywords": "SPFH590、酸洗钢带、Rm 590至700MPa、扩孔率不低于45%",
        "standard_scope": "企业汽车结构钢",
    },
    {
        "filename": "Q-JH-STEEL-SIM-030-2026_HC340LA_cold_rolled_enterprise.pdf",
        "code": "Q/JH STEEL SIM 030-2026",
        "short_title": "HC340LA冷轧结构板模拟企标",
        "title": "HC340LA低合金高强度冷轧钢板企业技术条件（模拟企标）",
        "system_type": "ENTERPRISE",
        "version": "V2026.1",
        "issued": "2026-03-12",
        "effective": "2026-04-10",
        "variety": "冷轧低合金高强钢板",
        "grade": "HC340LA",
        "spec": "厚度0.80mm-2.50mm，宽度800mm-1500mm",
        "usage": "汽车座椅骨架、支架和一般结构件",
        "chem_rows": [["C", "无", "0.100", "%", "焊接性控制"], ["Mn", "0.600", "1.500", "%", "强化元素"], ["Nb+Ti", "0.020", "0.120", "%", "微合金化"], ["P", "无", "0.030", "%", "企业内控"], ["S", "无", "0.020", "%", "企业内控"]],
        "perf_rows": [["ReL 屈服强度", "340", "430", "MPa", "低合金高强等级"], ["Rm 抗拉强度", "410", "510", "MPa", "强度范围"], ["A80 断后伸长率", "21", "无", "%", "冷成形指标"], ["n值", "0.120", "无", "-", "成形硬化提示"]],
        "dimension": "厚度偏差应控制在-0.070mm至+0.070mm，板形浪高不应影响自动送料。表面级别为FB时，不得有影响电泳涂装的连续缺陷。",
        "concession": "ReL高于430MPa且客户工艺存在回弹风险时，不建议让步。A80低于21%但不低于19%时，可在非安全件场景提交人工评审。",
        "certificate": "质量证明书应列明ReL、Rm、A80、n值、微合金元素、表面级别和尺寸偏差。",
        "keywords": "HC340LA、冷轧低合金高强钢、ReL 340至430MPa、A80不低于21%",
        "standard_scope": "企业冷轧汽车钢",
    },
    {
        "filename": "Q-JH-STEEL-SIM-040-2026_NM400_wear_plate_enterprise.pdf",
        "code": "Q/JH STEEL SIM 040-2026",
        "short_title": "NM400耐磨板模拟企标",
        "title": "NM400工程机械用耐磨钢板企业技术条件（模拟企标）",
        "system_type": "ENTERPRISE",
        "version": "V2026.1",
        "issued": "2026-03-15",
        "effective": "2026-04-15",
        "variety": "调质耐磨钢板",
        "grade": "NM400",
        "spec": "厚度6mm-40mm，宽度1500mm-3200mm",
        "usage": "矿山机械衬板、料斗、溜槽和工程机械耐磨件",
        "chem_rows": [["C", "0.180", "0.260", "%", "硬度基础"], ["Mn", "0.800", "1.600", "%", "淬透性"], ["Cr", "0.300", "1.200", "%", "耐磨和淬透性"], ["P", "无", "0.020", "%", "企业内控"], ["S", "无", "0.010", "%", "企业内控"]],
        "perf_rows": [["表面硬度", "360", "440", "HBW", "核心判定指标"], ["Rm 参考", "1100", "无", "MPa", "仅作参考记录"], ["A 断后伸长率", "8", "无", "%", "韧性提示"], ["-20C冲击功", "24", "无", "J", "低温韧性提示"]],
        "dimension": "厚度偏差应控制在-0.30mm至+0.80mm，不平度应满足切割和焊接装配要求。表面允许轻微氧化色，但不得有淬火裂纹、分层和严重压坑。",
        "concession": "表面硬度低于360HBW或高于440HBW时不得常规让步；冲击功略低且用途非低温冲击磨损件时，可提交技术评审。",
        "certificate": "质量证明书应列明硬度测点、热处理状态、C、Cr、冲击功、尺寸偏差和表面质量。",
        "keywords": "NM400、耐磨钢板、硬度360至440HBW、调质状态",
        "standard_scope": "企业耐磨板",
    },
    {
        "filename": "Q-JH-STEEL-SIM-050-2026_S355J2_wind_tower_plate_enterprise.pdf",
        "code": "Q/JH STEEL SIM 050-2026",
        "short_title": "S355J2风电塔筒板模拟企标",
        "title": "S355J2风电塔筒用中厚板企业技术条件（模拟企标）",
        "system_type": "ENTERPRISE",
        "version": "V2026.1",
        "issued": "2026-03-20",
        "effective": "2026-05-01",
        "variety": "风电塔筒用中厚板",
        "grade": "S355J2",
        "spec": "厚度8mm-50mm，宽度1800mm-3500mm",
        "usage": "陆上风电塔筒筒节、门框加强板和法兰连接附件",
        "chem_rows": [["C", "无", "0.200", "%", "焊接性控制"], ["Mn", "1.000", "1.700", "%", "强度元素"], ["Si", "无", "0.550", "%", "脱氧元素"], ["P", "无", "0.025", "%", "低温韧性控制"], ["S", "无", "0.015", "%", "低温韧性控制"]],
        "perf_rows": [["ReL 屈服强度", "355", "无", "MPa", "厚度8mm-16mm"], ["Rm 抗拉强度", "470", "630", "MPa", "强度范围"], ["A 断后伸长率", "22", "无", "%", "焊接结构塑性"], ["-20C冲击功", "27", "无", "J", "J2等级要求"]],
        "dimension": "厚度偏差按-0.30mm至+0.80mm控制，不平度应满足卷制塔筒要求。切边裂纹、分层、严重浪形和影响焊缝质量的表面缺陷不允许存在。",
        "concession": "-20C冲击功低于27J、P/S超出内控或存在分层时不得让步。A低于下限不超过1个百分点且非主筒节用途时，可提交人工评审。",
        "certificate": "质量证明书应列明J2冲击温度、冲击功、ReL、Rm、A、化学成分、厚度偏差和无损检测要求。",
        "keywords": "S355J2、风电塔筒板、-20C冲击功不低于27J、A不低于22%",
        "standard_scope": "企业风电钢",
    },
    {
        "filename": "CUST-HDQC-SIM-2026-HC340LA_customer_agreement.pdf",
        "code": "AGREEMENT HDQC-SIM-2026-002",
        "short_title": "华东汽配HC340LA模拟客户协议",
        "title": "华东汽车配件有限公司HC340LA冷轧结构板供货质量协议（模拟客户协议）",
        "system_type": "CUSTOMER",
        "version": "协议V1.0",
        "issued": "2026-02-10",
        "effective": "2026-03-01 至 2026-12-31",
        "variety": "冷轧低合金高强钢板",
        "grade": "HC340LA",
        "spec": "厚度0.90mm-2.00mm，宽度900mm-1450mm",
        "customer": "CUST-101 华东汽车配件有限公司",
        "usage": "汽车座椅骨架和安全带支架",
        "scope_note": "该用途涉及乘员安全，低置信度让步评估必须人工复核。",
        "chem_rows": [["C", "无", "0.090", "%", "客户补充上限"], ["Mn", "0.700", "1.400", "%", "强度稳定"], ["Nb+Ti", "0.030", "0.100", "%", "微合金控制"], ["P", "无", "0.025", "%", "客户严控"], ["S", "无", "0.015", "%", "客户严控"]],
        "perf_rows": [["ReL 屈服强度", "340", "410", "MPa", "回弹控制"], ["Rm 抗拉强度", "420", "500", "MPa", "客户窗口"], ["A80 断后伸长率", "23", "无", "%", "安全件成形要求"], ["焊接试验", "合格", "合格", "-", "点焊剥离不得异常"]],
        "dimension": "厚度偏差应控制在-0.050mm至+0.050mm，板形应满足自动冲压送料；表面不得有影响电泳和焊接的连续缺陷。",
        "concession": "安全带支架和座椅骨架订单原则上不接受ReL、Rm、A80让步。仅尺寸轻微超差且客户工程书面确认不影响装配时，可提交客户让步评审。",
        "certificate": "质量证明书应列明客户订单号、ReL、Rm、A80、焊接试验、厚度偏差和客户确认编号。安全件让步未确认时不得签发。",
        "keywords": "华东汽车配件、HC340LA、ReL 340至410MPa、A80不低于23%、安全件",
        "standard_scope": "客户汽车安全件",
    },
    {
        "filename": "CUST-BYEV-SIM-2026-DX56D-Z_customer_agreement.pdf",
        "code": "AGREEMENT BYEV-SIM-2026-003",
        "short_title": "北源电池DX56D+Z模拟客户协议",
        "title": "北源新能源电池壳用DX56D+Z镀锌深冲板质量协议（模拟客户协议）",
        "system_type": "CUSTOMER",
        "version": "协议V1.0",
        "issued": "2026-02-18",
        "effective": "2026-03-15 至 2026-12-31",
        "variety": "热镀锌深冲板",
        "grade": "DX56D+Z",
        "spec": "厚度0.60mm-1.20mm，镀层Z120",
        "customer": "CUST-205 北源新能源有限公司",
        "usage": "动力电池壳体浅拉伸件和盖板支撑件",
        "chem_rows": [["C", "无", "0.040", "%", "深冲客户上限"], ["Mn", "无", "0.300", "%", "成形稳定"], ["P", "无", "0.020", "%", "客户严控"], ["S", "无", "0.015", "%", "客户严控"], ["Alt", "0.020", "0.070", "%", "铝镇静控制"]],
        "perf_rows": [["ReL 屈服强度", "120", "180", "MPa", "回弹控制"], ["Rm 抗拉强度", "270", "370", "MPa", "深冲窗口"], ["A80 断后伸长率", "38", "无", "%", "深冲性能"], ["镀层重量", "110", "140", "g/m2", "Z120客户窗口"]],
        "dimension": "厚度偏差应控制在-0.040mm至+0.040mm，镀层表面不得有露铁、锌粒压入和影响电池壳密封的划伤。宽度偏差应控制在0mm至+3mm。",
        "concession": "镀层重量低于110g/m2、A80低于38%或表面存在露铁时不接受让步。ReL轻微偏高但客户试冲合格时，可提交客户书面确认。",
        "certificate": "质量证明书应列明镀层重量、ReL、Rm、A80、表面质量、钝化状态、客户订单号和试冲确认。",
        "keywords": "北源新能源、DX56D+Z、镀层Z120、A80不低于38%、电池壳",
        "standard_scope": "客户新能源电池壳",
    },
    {
        "filename": "CUST-ZYSHIP-SIM-2026-AH36_customer_agreement.pdf",
        "code": "AGREEMENT ZYSHIP-SIM-2026-004",
        "short_title": "中远船配AH36模拟客户协议",
        "title": "中远船舶配套AH36船体结构钢板质量协议（模拟客户协议）",
        "system_type": "CUSTOMER",
        "version": "协议V1.0",
        "issued": "2026-02-25",
        "effective": "2026-04-01 至 2026-12-31",
        "variety": "船体结构钢板",
        "grade": "AH36",
        "spec": "厚度8mm-32mm，宽度1800mm-3200mm",
        "customer": "CUST-306 中远船舶配套有限公司",
        "usage": "船体甲板、舷侧结构和加强构件",
        "scope_note": "船体结构用途为高风险场景，不适用普通让步放行。",
        "chem_rows": [["C", "无", "0.180", "%", "焊接性控制"], ["Mn", "0.900", "1.600", "%", "强度元素"], ["P", "无", "0.025", "%", "船板客户严控"], ["S", "无", "0.015", "%", "船板客户严控"], ["Ceq", "无", "0.400", "%", "焊接性要求"]],
        "perf_rows": [["ReH 屈服强度", "355", "无", "MPa", "船级强度"], ["Rm 抗拉强度", "490", "620", "MPa", "强度范围"], ["A 断后伸长率", "21", "无", "%", "塑性指标"], ["0C冲击功", "34", "无", "J", "横向冲击"]],
        "dimension": "厚度偏差应控制在-0.30mm至+0.70mm，钢板不得有分层、裂纹、影响焊接的压入氧化皮和边部裂口。必要时应提供超声检测记录。",
        "concession": "船体结构用途下ReH、Rm、A、冲击功、Ceq和超声检测不允许常规让步。仅非结构边部缺陷修磨后仍满足厚度要求时，可由客户和船级代表共同确认。",
        "certificate": "质量证明书应列明船级用途、炉批号、ReH、Rm、A、冲击功、Ceq、超声检测和客户确认。未完成客户确认不得签发。",
        "keywords": "中远船舶、AH36、ReH不低于355MPa、0C冲击功不低于34J、Ceq不大于0.400%",
        "standard_scope": "客户船体结构钢",
    },
    {
        "filename": "CUST-HXPIPE-SIM-2026-L360M_customer_agreement.pdf",
        "code": "AGREEMENT HXPIPE-SIM-2026-005",
        "short_title": "华新管道L360M模拟客户协议",
        "title": "华新管道工程L360M管线钢卷供货质量协议（模拟客户协议）",
        "system_type": "CUSTOMER",
        "version": "协议V1.0",
        "issued": "2026-03-01",
        "effective": "2026-04-15 至 2026-12-31",
        "variety": "管线钢热轧卷",
        "grade": "L360M",
        "spec": "厚度5mm-18mm，宽度1000mm-1800mm",
        "customer": "CUST-408 华新管道工程有限公司",
        "usage": "天然气集输管线和城市管网配套制管",
        "chem_rows": [["C", "无", "0.100", "%", "客户焊接性上限"], ["Mn", "1.000", "1.700", "%", "强韧性"], ["P", "无", "0.018", "%", "客户严控"], ["S", "无", "0.008", "%", "客户严控"], ["Pcm", "无", "0.220", "%", "焊接裂纹敏感性"]],
        "perf_rows": [["ReL 屈服强度", "360", "530", "MPa", "客户窗口"], ["Rm 抗拉强度", "460", "620", "MPa", "强度范围"], ["A 断后伸长率", "22", "无", "%", "塑性指标"], ["DWTT剪切面积", "85", "无", "%", "落锤撕裂提示"]],
        "dimension": "厚度偏差应控制在-0.15mm至+0.15mm，卷形、镰刀弯和边部质量应满足螺旋焊管或直缝焊管连续生产。边部分层和夹杂暴露不允许存在。",
        "concession": "Pcm、P、S、DWTT和ReL上限不允许让步。Rm低于下限不超过10MPa且非高压管线用途时，可提交客户技术评审和追加制管验证。",
        "certificate": "质量证明书应列明客户项目号、Pcm、ReL、Rm、A、DWTT、冲击功、厚度偏差和制管验证结论。",
        "keywords": "华新管道、L360M、ReL 360至530MPa、DWTT剪切面积不低于85%、Pcm不大于0.220%",
        "standard_scope": "客户管线工程",
    },
]


def docs():
    result = base_docs()
    result.extend(additional_doc(spec) for spec in ADDITIONAL_SPECS)
    return result


def main():
    register_font()
    styles = build_styles()
    if not os.path.exists(OUT_DIR):
        os.makedirs(OUT_DIR)
    paths = []
    for doc in docs():
        paths.append(build_pdf(doc, styles))
    for path in paths:
        print(path)


if __name__ == "__main__":
    main()
