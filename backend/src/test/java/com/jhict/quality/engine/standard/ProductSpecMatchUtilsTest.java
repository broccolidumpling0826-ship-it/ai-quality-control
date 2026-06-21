package com.jhict.quality.engine.standard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductSpecMatchUtilsTest {

    @Test
    void matchesP0DemoProductSpecAgainstAlternativeStockRange() {
        assertTrue(ProductSpecMatchUtils.isCompatible(
                "厚度1.5mm×宽度1000mm",
                "厚度1.0-2.0mm，宽度600-1500mm"));
    }

    @Test
    void rejectsOutOfRangeThickness() {
        assertFalse(ProductSpecMatchUtils.isCompatible(
                "厚度2.5mm×宽度1000mm",
                "厚度1.0-2.0mm，宽度600-1500mm"));
    }

    @Test
    void rejectsOutOfRangeWidth() {
        assertFalse(ProductSpecMatchUtils.isCompatible(
                "厚度1.5mm×宽度1600mm",
                "厚度1.0-2.0mm，宽度600-1500mm"));
    }

    @Test
    void acceptsUniversalStockRange() {
        assertTrue(ProductSpecMatchUtils.isCompatible(
                "厚度1.5mm×宽度1000mm",
                "全规格"));
    }
}
