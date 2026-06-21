package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.entity.QcCustomerUsageProfile;
import com.jhict.quality.mapper.QcCustomerUsageProfileMapper;
import com.jhict.quality.service.api.CustomerUsageProfileService;
import com.jhict.quality.vo.CustomerUsageProfileVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

@Service
public class CustomerUsageProfileServiceImpl implements CustomerUsageProfileService {

    @Resource
    private QcCustomerUsageProfileMapper usageProfileMapper;

    @Override
    public CustomerUsageProfileVO findBestMatch(String customerId, String variety, String grade) {
        if (!StringUtils.hasText(customerId)) {
            return null;
        }
        List<QcCustomerUsageProfile> profiles = usageProfileMapper.selectList(
                new LambdaQueryWrapper<QcCustomerUsageProfile>()
                        .eq(QcCustomerUsageProfile::getCustomerId, customerId)
                        .eq(QcCustomerUsageProfile::getStatus, "ACTIVE"));
        return profiles.stream()
                .filter(p -> !StringUtils.hasText(p.getVariety()) || p.getVariety().equals(variety))
                .filter(p -> !StringUtils.hasText(p.getGrade()) || p.getGrade().equals(grade))
                .max(Comparator.comparingInt(p -> matchScore(p, variety, grade)))
                .map(this::toVO)
                .orElse(null);
    }

    private int matchScore(QcCustomerUsageProfile profile, String variety, String grade) {
        int score = 0;
        if (StringUtils.hasText(profile.getVariety()) && profile.getVariety().equals(variety)) {
            score += 2;
        }
        if (StringUtils.hasText(profile.getGrade()) && profile.getGrade().equals(grade)) {
            score += 2;
        }
        return score;
    }

    private CustomerUsageProfileVO toVO(QcCustomerUsageProfile profile) {
        CustomerUsageProfileVO vo = new CustomerUsageProfileVO();
        vo.setId(profile.getId());
        vo.setCustomerId(profile.getCustomerId());
        vo.setCustomerName(profile.getCustomerName());
        vo.setDefaultUsage(profile.getDefaultUsage());
        vo.setRiskCategory(profile.getRiskCategory());
        vo.setVariety(profile.getVariety());
        vo.setGrade(profile.getGrade());
        vo.setRemark(profile.getRemark());
        return vo;
    }
}
