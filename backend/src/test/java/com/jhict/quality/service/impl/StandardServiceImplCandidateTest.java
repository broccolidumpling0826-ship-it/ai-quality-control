package com.jhict.quality.service.impl;

import com.jhict.quality.dto.StandardCandidateQuery;
import com.jhict.quality.entity.QcIndicatorItem;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.entity.QcStandardIndicator;
import com.jhict.quality.mapper.QcQualityStandardMapper;
import com.jhict.quality.mapper.QcStandardIndicatorMapper;
import com.jhict.quality.service.api.IndicatorService;
import com.jhict.quality.vo.StandardCandidateSetVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandardServiceImplCandidateTest {

    @Mock
    private QcQualityStandardMapper qualityStandardMapper;

    @Mock
    private QcStandardIndicatorMapper standardIndicatorMapper;

    @Mock
    private IndicatorService indicatorService;

    private StandardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StandardServiceImpl();
        ReflectionTestUtils.setField(service, "qualityStandardMapper", qualityStandardMapper);
        ReflectionTestUtils.setField(service, "standardIndicatorMapper", standardIndicatorMapper);
        ReflectionTestUtils.setField(service, "indicatorService", indicatorService);
    }

    @Test
    void findCandidateStandards_shouldRecordPriorityResolvableLimitDifference() {
        QcQualityStandard customer = standard("std-c", "CUSTOMER", "CUST-001", "2025-01-01", "2025-12-31");
        QcQualityStandard enterprise = standard("std-e", "ENTERPRISE", null, "2025-01-01", "2025-12-31");
        when(qualityStandardMapper.findApplicableCandidateStandards(eq("CUST-001"), eq("冷轧板"), eq("Q235B"), any()))
                .thenReturn(Arrays.asList(customer, enterprise));
        when(standardIndicatorMapper.selectList(any()))
                .thenReturn(Arrays.asList(rule("std-c", "ind001", "380", "500"),
                        rule("std-e", "ind001", "370", "510")));
        when(indicatorService.listEntitiesByIds(any())).thenReturn(Collections.singletonList(indicator()));

        StandardCandidateSetVO result = service.findCandidateStandards(query());

        assertNotNull(result.getSelectedStandard());
        assertEquals("std-c", result.getSelectedStandard().getId());
        assertEquals(1, result.getSuppressedStandards().size());
        assertEquals(1, result.getConflicts().size());
        assertEquals("PRIORITY_RESOLVABLE", result.getConflicts().get(0).getConflictLevel());
        assertEquals("RESOLVED", result.getConflicts().get(0).getStatus());
    }

    @Test
    void findCandidateStandards_shouldBlockSamePriorityLimitDifference() {
        QcQualityStandard customerV1 = standard("std-c1", "CUSTOMER", "CUST-001", "2025-01-01", "2025-12-31");
        QcQualityStandard customerV2 = standard("std-c2", "CUSTOMER", "CUST-001", "2025-04-01", "2025-12-31");
        when(qualityStandardMapper.findApplicableCandidateStandards(eq("CUST-001"), eq("冷轧板"), eq("Q235B"), any()))
                .thenReturn(Arrays.asList(customerV2, customerV1));
        when(standardIndicatorMapper.selectList(any()))
                .thenReturn(Arrays.asList(rule("std-c1", "ind001", "380", "500"),
                        rule("std-c2", "ind001", "395", "500")));
        when(indicatorService.listEntitiesByIds(any())).thenReturn(Collections.singletonList(indicator()));

        StandardCandidateSetVO result = service.findCandidateStandards(query());

        assertNull(result.getSelectedStandard());
        assertEquals(1, result.getConflicts().size());
        assertEquals("BLOCKING", result.getConflicts().get(0).getConflictLevel());
        assertEquals("PENDING", result.getConflicts().get(0).getStatus());
        assertEquals(2, result.getConflictStandards().size());
    }

    private StandardCandidateQuery query() {
        StandardCandidateQuery query = new StandardCandidateQuery();
        query.setCustomerId("CUST-001");
        query.setVariety("冷轧板");
        query.setGrade("Q235B");
        query.setProductSpec("厚度1.5mm");
        query.setTestDate(LocalDate.of(2025, 5, 16));
        query.setIndicatorIds(Collections.singletonList("ind001"));
        return query;
    }

    private QcQualityStandard standard(String id, String type, String customerId, String effective, String expiry) {
        QcQualityStandard standard = new QcQualityStandard();
        standard.setId(id);
        standard.setStandardType(type);
        standard.setStandardCode(type + "-" + id);
        standard.setStandardName(type + "标准");
        standard.setCustomerId(customerId);
        standard.setVariety("冷轧板");
        standard.setGrade("Q235B");
        standard.setSpecRange("1.0-2.0mm");
        standard.setVersionNo("V1");
        standard.setEffectiveDate(LocalDate.parse(effective));
        standard.setExpiryDate(LocalDate.parse(expiry));
        return standard;
    }

    private QcStandardIndicator rule(String standardId, String indicatorId, String lower, String upper) {
        QcStandardIndicator rule = new QcStandardIndicator();
        rule.setStandardId(standardId);
        rule.setIndicatorId(indicatorId);
        rule.setLowerLimit(new BigDecimal(lower));
        rule.setUpperLimit(new BigDecimal(upper));
        return rule;
    }

    private QcIndicatorItem indicator() {
        QcIndicatorItem item = new QcIndicatorItem();
        item.setId("ind001");
        item.setIndicatorCode("RM");
        item.setIndicatorName("抗拉强度");
        item.setUnit("MPa");
        return item;
    }
}
