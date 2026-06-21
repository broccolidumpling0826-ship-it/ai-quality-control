package com.jhict.quality.engine.standard;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 产品规格与库存规格范围的确定性匹配工具。
 * 支持「厚度1.5mm×宽度1000mm」与「厚度1.0-2.0mm，宽度600-1500mm」这类常见格式。
 */
public final class ProductSpecMatchUtils {

    private static final Pattern THICKNESS_POINT = Pattern.compile("厚度\\s*([\\d.]+)");
    private static final Pattern WIDTH_POINT = Pattern.compile("宽度\\s*([\\d.]+)");
    private static final Pattern THICKNESS_RANGE = Pattern.compile("厚度\\s*([\\d.]+)\\s*[-~～至]\\s*([\\d.]+)");
    private static final Pattern WIDTH_RANGE = Pattern.compile("宽度\\s*([\\d.]+)\\s*[-~～至]\\s*([\\d.]+)");

    private ProductSpecMatchUtils() {
    }

    public static boolean isCompatible(String productSpec, String stockSpecRange) {
        if (!StringUtils.hasText(productSpec) || !StringUtils.hasText(stockSpecRange)) {
            return true;
        }
        String normalizedProduct = normalize(productSpec);
        String normalizedStock = normalize(stockSpecRange);
        if (normalizedStock.contains("全规格") || normalizedStock.contains("通用")) {
            return true;
        }
        if (normalizedProduct.contains(normalizedStock) || normalizedStock.contains(normalizedProduct)) {
            return true;
        }

        DimensionPoint productPoint = parseProductPoint(productSpec);
        DimensionRange stockRange = parseStockRange(stockSpecRange);
        if (productPoint.isEmpty() || stockRange.isEmpty()) {
            return fallbackContains(productSpec, stockSpecRange);
        }
        if (productPoint.getThickness() != null && stockRange.getThicknessLower() != null) {
            if (!withinRange(productPoint.getThickness(), stockRange.getThicknessLower(), stockRange.getThicknessUpper())) {
                return false;
            }
        }
        if (productPoint.getWidth() != null && stockRange.getWidthLower() != null) {
            if (!withinRange(productPoint.getWidth(), stockRange.getWidthLower(), stockRange.getWidthUpper())) {
                return false;
            }
        }
        return true;
    }

    static DimensionPoint parseProductPoint(String productSpec) {
        DimensionPoint point = new DimensionPoint();
        if (!StringUtils.hasText(productSpec)) {
            return point;
        }
        Matcher thicknessMatcher = THICKNESS_POINT.matcher(normalize(productSpec));
        if (thicknessMatcher.find()) {
            point.setThickness(new BigDecimal(thicknessMatcher.group(1)));
        }
        Matcher widthMatcher = WIDTH_POINT.matcher(normalize(productSpec));
        if (widthMatcher.find()) {
            point.setWidth(new BigDecimal(widthMatcher.group(1)));
        }
        return point;
    }

    static DimensionRange parseStockRange(String stockSpecRange) {
        DimensionRange range = new DimensionRange();
        if (!StringUtils.hasText(stockSpecRange)) {
            return range;
        }
        String normalized = normalize(stockSpecRange);
        Matcher thicknessMatcher = THICKNESS_RANGE.matcher(normalized);
        if (thicknessMatcher.find()) {
            BigDecimal first = new BigDecimal(thicknessMatcher.group(1));
            BigDecimal second = new BigDecimal(thicknessMatcher.group(2));
            range.setThicknessLower(first.min(second));
            range.setThicknessUpper(first.max(second));
        }
        Matcher widthMatcher = WIDTH_RANGE.matcher(normalized);
        if (widthMatcher.find()) {
            BigDecimal first = new BigDecimal(widthMatcher.group(1));
            BigDecimal second = new BigDecimal(widthMatcher.group(2));
            range.setWidthLower(first.min(second));
            range.setWidthUpper(first.max(second));
        }
        return range;
    }

    private static boolean withinRange(BigDecimal value, BigDecimal lower, BigDecimal upper) {
        if (value == null || lower == null) {
            return true;
        }
        if (value.compareTo(lower) < 0) {
            return false;
        }
        if (upper != null && value.compareTo(upper) > 0) {
            return false;
        }
        return true;
    }

    private static boolean fallbackContains(String productSpec, String stockSpecRange) {
        return stockSpecRange.contains(productSpec) || productSpec.contains(stockSpecRange);
    }

    private static String normalize(String raw) {
        return raw.trim()
                .replace("（", "(")
                .replace("）", ")")
                .replace("，", ",")
                .replace("×", "x")
                .replace("*", "x")
                .replace("～", "-")
                .replace("—", "-")
                .replace("－", "-")
                .replace(" ", "")
                .replace("　", "")
                .toLowerCase(Locale.ROOT);
    }

    static final class DimensionPoint {
        private BigDecimal thickness;
        private BigDecimal width;

        boolean isEmpty() {
            return thickness == null && width == null;
        }

        BigDecimal getThickness() {
            return thickness;
        }

        void setThickness(BigDecimal thickness) {
            this.thickness = thickness;
        }

        BigDecimal getWidth() {
            return width;
        }

        void setWidth(BigDecimal width) {
            this.width = width;
        }
    }

    static final class DimensionRange {
        private BigDecimal thicknessLower;
        private BigDecimal thicknessUpper;
        private BigDecimal widthLower;
        private BigDecimal widthUpper;

        boolean isEmpty() {
            return thicknessLower == null && widthLower == null;
        }

        BigDecimal getThicknessLower() {
            return thicknessLower;
        }

        void setThicknessLower(BigDecimal thicknessLower) {
            this.thicknessLower = thicknessLower;
        }

        BigDecimal getThicknessUpper() {
            return thicknessUpper;
        }

        void setThicknessUpper(BigDecimal thicknessUpper) {
            this.thicknessUpper = thicknessUpper;
        }

        BigDecimal getWidthLower() {
            return widthLower;
        }

        void setWidthLower(BigDecimal widthLower) {
            this.widthLower = widthLower;
        }

        BigDecimal getWidthUpper() {
            return widthUpper;
        }

        void setWidthUpper(BigDecimal widthUpper) {
            this.widthUpper = widthUpper;
        }
    }
}
