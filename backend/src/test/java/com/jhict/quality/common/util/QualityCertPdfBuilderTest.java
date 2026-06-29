package com.jhict.quality.common.util;

import com.jhict.quality.common.constant.JudgmentExplainConstants;
import com.jhict.quality.vo.QcQualityCertDataVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityCertPdfBuilderTest {

    @Test
    void build_shouldProducePdfWithChineseContent() {
        QcQualityCertDataVO vo = new QcQualityCertDataVO();
        vo.setId("2071478660849704962");
        vo.setCoilNo("FT-QUAL-001");
        vo.setBatchNo("HT-MAN-QUAL-001");
        vo.setHeatNo("HT-MAN-QUAL-001");
        vo.setProductVariety("热轧板");
        vo.setProductGrade("Q235B");
        vo.setFinalJudgmentType("QUALIFIED");
        vo.setGenerateTime(LocalDateTime.of(2026, 6, 29, 14, 19, 34));
        vo.setGeneratedBy("admin");

        QcQualityCertDataVO.IndicatorSnapshot indicator = new QcQualityCertDataVO.IndicatorSnapshot();
        indicator.setIndicatorName("抗拉强度");
        indicator.setIndicatorCode("Rm");
        indicator.setTestValue(new BigDecimal("430"));
        indicator.setUnit("MPa");
        indicator.setLowerLimit(new BigDecimal("375"));
        indicator.setUpperLimit(new BigDecimal("505"));
        indicator.setIndicatorResult(JudgmentExplainConstants.INDICATOR_RESULT_PASS);
        vo.setIndicators(Arrays.asList(indicator));

        byte[] pdf = QualityCertPdfBuilder.build(vo);

        assertTrue(pdf.length > 1000);
        assertTrue(new String(pdf, 0, Math.min(pdf.length, 8)).startsWith("%PDF-"));
    }
}
