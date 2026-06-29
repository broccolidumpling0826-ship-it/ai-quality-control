package com.jhict.quality.common.util;

import com.jhict.quality.common.constant.JudgmentExplainConstants;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.vo.QcQualityCertDataVO;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.util.StringUtils;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 质保书 PDF 生成器（中文排版、表格化展示）。
 */
public final class QualityCertPdfBuilder {

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 42f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2;
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");

    private static final Color COLOR_BORDER = new Color(38, 89, 142);
    private static final Color COLOR_TITLE = new Color(26, 64, 115);
    private static final Color COLOR_HEADER_BG = new Color(230, 238, 248);
    private static final Color COLOR_ROW_ALT = new Color(248, 250, 252);
    private static final Color COLOR_TEXT = new Color(38, 38, 38);
    private static final Color COLOR_MUTED = new Color(102, 102, 102);
    private static final Color COLOR_PASS = new Color(34, 139, 58);
    private static final Color COLOR_FAIL = new Color(196, 52, 52);

    private QualityCertPdfBuilder() {
    }

    public static byte[] build(QcQualityCertDataVO vo) {
        try (PDDocument document = new PDDocument()) {
            PDFont font = loadChineseFont(document);
            List<QcQualityCertDataVO.IndicatorSnapshot> indicators = vo.getIndicators() == null
                    ? Collections.emptyList()
                    : vo.getIndicators();

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float y = PAGE_HEIGHT - MARGIN;
            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                y = drawOuterFrame(cs);
                y = drawTitle(cs, font, y);
                y -= 18f;
                y = drawBasicInfoTable(cs, font, y, vo);
                y -= 22f;
                y = drawSectionTitle(cs, font, y, "检测指标");
                y = drawIndicatorTable(cs, font, y, indicators);
                drawFooter(cs, font, vo);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new ServiceException("生成质保书PDF失败：" + e.getMessage());
        }
    }

    private static float drawOuterFrame(PDPageContentStream cs) throws IOException {
        setStroke(cs, COLOR_BORDER, 1.2f);
        cs.addRect(MARGIN, MARGIN, CONTENT_WIDTH, PAGE_HEIGHT - MARGIN * 2);
        cs.stroke();
        return PAGE_HEIGHT - MARGIN - 28f;
    }

    private static float drawTitle(PDPageContentStream cs, PDFont font, float y) throws IOException {
        drawCenteredText(cs, font, 22f, y, "质量证明书", COLOR_TITLE);
        y -= 20f;
        drawCenteredText(cs, font, 10f, y, "QUALITY CERTIFICATE", COLOR_MUTED);
        setStroke(cs, COLOR_BORDER, 0.8f);
        cs.moveTo(MARGIN + 16f, y - 10f);
        cs.lineTo(PAGE_WIDTH - MARGIN - 16f, y - 10f);
        cs.stroke();
        return y - 10f;
    }

    private static float drawBasicInfoTable(PDPageContentStream cs, PDFont font, float y,
                                            QcQualityCertDataVO vo) throws IOException {
        String[][] rows = {
                {"证书编号", nullToDash(vo.getId()), "卷号", nullToDash(vo.getCoilNo())},
                {"批次号", nullToDash(vo.getBatchNo()), "炉号", nullToDash(vo.getHeatNo())},
                {"品种", nullToDash(vo.getProductVariety()), "牌号", nullToDash(vo.getProductGrade())},
                {"最终结论", formatJudgmentType(vo.getFinalJudgmentType()), "客户编号", nullToDash(vo.getCustomerId())},
                {"生成时间", formatDateTime(vo.getGenerateTime()), "生成人", nullToDash(vo.getGeneratedBy())}
        };
        float rowHeight = 24f;
        float labelWidth = 78f;
        float valueWidth = (CONTENT_WIDTH - labelWidth * 2) / 2f;
        float tableTop = y;
        float tableHeight = rows.length * rowHeight;

        fillRect(cs, MARGIN + 1f, tableTop - tableHeight, CONTENT_WIDTH - 2f, tableHeight, COLOR_HEADER_BG);
        setStroke(cs, COLOR_BORDER, 0.6f);

        for (int i = 0; i <= rows.length; i++) {
            float lineY = tableTop - i * rowHeight;
            cs.moveTo(MARGIN + 1f, lineY);
            cs.lineTo(MARGIN + CONTENT_WIDTH - 1f, lineY);
            cs.stroke();
        }

        float[] colXs = {
                MARGIN + 1f,
                MARGIN + 1f + labelWidth,
                MARGIN + 1f + labelWidth + valueWidth,
                MARGIN + 1f + labelWidth * 2 + valueWidth,
                MARGIN + CONTENT_WIDTH - 1f
        };
        for (float colX : colXs) {
            cs.moveTo(colX, tableTop);
            cs.lineTo(colX, tableTop - tableHeight);
            cs.stroke();
        }

        float textY = tableTop - 16f;
        for (String[] row : rows) {
            drawText(cs, font, 10f, MARGIN + 8f, textY, row[0], COLOR_MUTED);
            drawText(cs, font, 10.5f, MARGIN + labelWidth + 8f, textY, row[1], COLOR_TEXT);
            drawText(cs, font, 10f, MARGIN + labelWidth + valueWidth + 8f, textY, row[2], COLOR_MUTED);
            drawText(cs, font, 10.5f, MARGIN + labelWidth * 2 + valueWidth + 8f, textY, row[3], COLOR_TEXT);
            textY -= rowHeight;
        }
        return tableTop - tableHeight;
    }

    private static float drawSectionTitle(PDPageContentStream cs, PDFont font, float y, String title)
            throws IOException {
        drawText(cs, font, 12f, MARGIN + 4f, y, title, COLOR_TITLE);
        setStroke(cs, COLOR_BORDER, 0.5f);
        cs.moveTo(MARGIN + 4f, y - 6f);
        cs.lineTo(MARGIN + 88f, y - 6f);
        cs.stroke();
        return y - 14f;
    }

    private static float drawIndicatorTable(PDPageContentStream cs, PDFont font, float y,
                                            List<QcQualityCertDataVO.IndicatorSnapshot> indicators)
            throws IOException {
        String[] headers = {"序号", "指标名称", "指标代码", "实测值", "单位", "标准下限", "标准上限", "结论"};
        float[] colWidths = {32f, 88f, 58f, 58f, 42f, 58f, 58f, 52f};
        float headerHeight = 24f;
        float rowHeight = 22f;

        float tableX = MARGIN + 1f;
        float tableWidth = CONTENT_WIDTH - 2f;
        float headerBottom = y - headerHeight;

        fillRect(cs, tableX, headerBottom, tableWidth, headerHeight, COLOR_HEADER_BG);
        drawTableGrid(cs, tableX, y, tableWidth, headerHeight, colWidths, true);

        float textX = tableX + 6f;
        float headerTextY = y - 16f;
        for (int i = 0; i < headers.length; i++) {
            drawText(cs, font, 9.5f, textX, headerTextY, headers[i], COLOR_TITLE);
            textX += colWidths[i];
        }

        float currentTop = headerBottom;
        if (indicators.isEmpty()) {
            float emptyHeight = rowHeight;
            fillRect(cs, tableX, currentTop - emptyHeight, tableWidth, emptyHeight, Color.WHITE);
            drawTableGrid(cs, tableX, currentTop, tableWidth, emptyHeight, colWidths, false);
            drawText(cs, font, 10f, tableX + 8f, currentTop - 15f, "暂无指标数据", COLOR_MUTED);
            return currentTop - emptyHeight;
        }

        int index = 1;
        for (QcQualityCertDataVO.IndicatorSnapshot item : indicators) {
            Color rowBg = index % 2 == 0 ? COLOR_ROW_ALT : Color.WHITE;
            fillRect(cs, tableX, currentTop - rowHeight, tableWidth, rowHeight, rowBg);
            drawTableGrid(cs, tableX, currentTop, tableWidth, rowHeight, colWidths, false);

            String[] cells = {
                    String.valueOf(index),
                    preferName(item),
                    nullToDash(item.getIndicatorCode()),
                    decimalToString(item.getTestValue()),
                    nullToDash(item.getUnit()),
                    decimalToString(item.getLowerLimit()),
                    decimalToString(item.getUpperLimit()),
                    formatIndicatorResult(item.getIndicatorResult())
            };

            float cellX = tableX + 6f;
            float cellY = currentTop - 15f;
            for (int i = 0; i < cells.length; i++) {
                Color textColor = i == cells.length - 1
                        ? resultColor(item.getIndicatorResult())
                        : COLOR_TEXT;
                drawText(cs, font, 9f, cellX, cellY, cells[i], textColor);
                cellX += colWidths[i];
            }

            currentTop -= rowHeight;
            index++;
        }
        return currentTop;
    }

    private static void drawFooter(PDPageContentStream cs, PDFont font, QcQualityCertDataVO vo)
            throws IOException {
        String footer = "本证明书由质量管理系统自动生成，仅供内部质量追溯与出证使用。";
        drawCenteredText(cs, font, 8.5f, MARGIN + 18f, footer, COLOR_MUTED);

        String stamp = "结论：" + formatJudgmentType(vo.getFinalJudgmentType());
        drawText(cs, font, 9f, PAGE_WIDTH - MARGIN - 140f, MARGIN + 28f, stamp, COLOR_TITLE);
    }

    private static void drawTableGrid(PDPageContentStream cs, float tableX, float tableTop,
                                      float tableWidth, float rowHeight, float[] colWidths,
                                      boolean headerRow) throws IOException {
        setStroke(cs, COLOR_BORDER, headerRow ? 0.7f : 0.45f);
        float bottom = tableTop - rowHeight;
        cs.moveTo(tableX, tableTop);
        cs.lineTo(tableX + tableWidth, tableTop);
        cs.stroke();
        cs.moveTo(tableX, bottom);
        cs.lineTo(tableX + tableWidth, bottom);
        cs.stroke();

        float colX = tableX;
        cs.moveTo(colX, tableTop);
        cs.lineTo(colX, bottom);
        cs.stroke();
        for (float colWidth : colWidths) {
            colX += colWidth;
            cs.moveTo(colX, tableTop);
            cs.lineTo(colX, bottom);
            cs.stroke();
        }
    }

    private static void drawCenteredText(PDPageContentStream cs, PDFont font, float fontSize,
                                         float y, String text, Color color) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        float x = (PAGE_WIDTH - textWidth) / 2f;
        drawText(cs, font, fontSize, x, y, text, color);
    }

    private static void drawText(PDPageContentStream cs, PDFont font, float fontSize,
                                 float x, float y, String text, Color color) throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        cs.showText(safeText(text));
        cs.endText();
    }

    private static void fillRect(PDPageContentStream cs, float x, float y, float width, float height,
                                 Color color) throws IOException {
        cs.setNonStrokingColor(color);
        cs.addRect(x, y, width, height);
        cs.fill();
    }

    private static void setStroke(PDPageContentStream cs, Color color, float width) throws IOException {
        cs.setStrokingColor(color);
        cs.setLineWidth(width);
    }

    private static PDFont loadChineseFont(PDDocument document) throws IOException {
        List<String> classpathFonts = new ArrayList<>();
        classpathFonts.add("/fonts/msyh.ttc");
        classpathFonts.add("/fonts/NotoSansSC-Regular.otf");
        classpathFonts.add("/fonts/NotoSansSC-Regular.ttf");

        for (String classpathFont : classpathFonts) {
            InputStream stream = QualityCertPdfBuilder.class.getResourceAsStream(classpathFont);
            if (stream == null) {
                continue;
            }
            try (InputStream fontStream = stream) {
                if (classpathFont.endsWith(".ttc")) {
                    PDType0Font font = loadFromTrueTypeCollection(document, fontStream,
                            "MicrosoftYaHei", "Microsoft YaHei", "SimSun");
                    if (font != null) {
                        return font;
                    }
                    continue;
                }
                return PDType0Font.load(document, fontStream, true);
            }
        }

        List<Path> systemFonts = new ArrayList<>();
        systemFonts.add(Paths.get("C:/Windows/Fonts/msyh.ttc"));
        systemFonts.add(Paths.get("C:/Windows/Fonts/simsun.ttc"));
        systemFonts.add(Paths.get("/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc"));
        systemFonts.add(Paths.get("/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"));

        for (Path path : systemFonts) {
            if (!Files.isRegularFile(path)) {
                continue;
            }
            if (path.toString().endsWith(".ttc")) {
                try (InputStream fontStream = Files.newInputStream(path)) {
                    PDType0Font font = loadFromTrueTypeCollection(document, fontStream,
                            "MicrosoftYaHei", "Microsoft YaHei", "SimSun", "Noto Sans CJK SC");
                    if (font != null) {
                        return font;
                    }
                }
                continue;
            }
            return PDType0Font.load(document, path.toFile());
        }

        throw new ServiceException("未找到可用的中文字体，请将字体文件放到 backend/src/main/resources/fonts/ 目录");
    }

    private static PDType0Font loadFromTrueTypeCollection(PDDocument document, InputStream fontStream,
                                                          String... preferredNames) throws IOException {
        try (TrueTypeCollection collection = new TrueTypeCollection(fontStream)) {
            for (String preferredName : preferredNames) {
                TrueTypeFont ttf = collection.getFontByName(preferredName);
                if (ttf != null) {
                    return PDType0Font.load(document, ttf, true);
                }
            }
            final TrueTypeFont[] firstFont = new TrueTypeFont[1];
            collection.processAllFonts(font -> {
                if (firstFont[0] == null) {
                    firstFont[0] = font;
                }
            });
            if (firstFont[0] != null) {
                return PDType0Font.load(document, firstFont[0], true);
            }
        }
        return null;
    }

    private static String preferName(QcQualityCertDataVO.IndicatorSnapshot item) {
        if (StringUtils.hasText(item.getIndicatorName())) {
            return item.getIndicatorName();
        }
        return nullToDash(item.getIndicatorCode());
    }

    private static String formatJudgmentType(String code) {
        if (!StringUtils.hasText(code)) {
            return "-";
        }
        switch (code) {
            case "QUALIFIED":
                return "合格";
            case "UNQUALIFIED":
                return "不合格";
            case "NEED_REINSPECTION":
                return "需复检";
            case "CAN_CONCESSION":
                return "可让步";
            case "STANDARD_CONFLICT":
                return "标准冲突";
            default:
                return code;
        }
    }

    private static String formatIndicatorResult(String code) {
        if (!StringUtils.hasText(code)) {
            return "-";
        }
        if (JudgmentExplainConstants.INDICATOR_RESULT_PASS.equals(code)) {
            return "合格";
        }
        if (JudgmentExplainConstants.INDICATOR_RESULT_FAIL.equals(code)) {
            return "不合格";
        }
        if (JudgmentExplainConstants.INDICATOR_RESULT_CONCESSION.equals(code)) {
            return "让步";
        }
        if (JudgmentExplainConstants.INDICATOR_RESULT_WARNING.equals(code)) {
            return "缺失";
        }
        return code;
    }

    private static Color resultColor(String code) {
        if (JudgmentExplainConstants.INDICATOR_RESULT_PASS.equals(code)) {
            return COLOR_PASS;
        }
        if (JudgmentExplainConstants.INDICATOR_RESULT_FAIL.equals(code)
                || JudgmentExplainConstants.INDICATOR_RESULT_WARNING.equals(code)) {
            return COLOR_FAIL;
        }
        return COLOR_TEXT;
    }

    private static String formatDateTime(LocalDateTime time) {
        return time == null ? "-" : time.format(DATE_TIME_FMT);
    }

    private static String decimalToString(BigDecimal value) {
        return value == null ? "-" : value.stripTrailingZeros().toPlainString();
    }

    private static String nullToDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private static String safeText(String value) {
        if (value == null || value.isEmpty()) {
            return "-";
        }
        return value.replace('\r', ' ').replace('\n', ' ');
    }
}
