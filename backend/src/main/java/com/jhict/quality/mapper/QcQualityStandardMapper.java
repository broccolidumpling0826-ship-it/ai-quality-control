package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcQualityStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface QcQualityStandardMapper extends BaseMapper<QcQualityStandard> {

    /**
     * 查询同体系时间窗口内是否存在已发布标准（用于重叠检测，MySQL 5.7兼容）
     */
    @Select("<script>" +
            "SELECT * FROM qc_quality_standard " +
            "WHERE standard_type = #{standardType} " +
            "AND variety = #{variety} " +
            "AND grade = #{grade} " +
            "AND status = 'PUBLISHED' " +
            "<if test='customerId != null'>AND customer_id = #{customerId} </if>" +
            "<if test='customerId == null'>AND customer_id IS NULL </if>" +
            "AND effective_date &lt; #{expiryDate} " +
            "AND expiry_date &gt; #{effectiveDate} " +
            "<if test='excludeId != null'>AND id != #{excludeId} </if>" +
            "</script>")
    List<QcQualityStandard> findOverlappingPublished(
            @Param("standardType") String standardType,
            @Param("variety") String variety,
            @Param("grade") String grade,
            @Param("customerId") String customerId,
            @Param("effectiveDate") LocalDate effectiveDate,
            @Param("expiryDate") LocalDate expiryDate,
            @Param("excludeId") String excludeId
    );

    /**
     * 三级优先级：客户协议标准查询（MySQL 5.7兼容）
     */
    @Select("SELECT * FROM qc_quality_standard " +
            "WHERE standard_type = 'CUSTOMER' " +
            "AND customer_id = #{customerId} " +
            "AND variety = #{variety} " +
            "AND grade = #{grade} " +
            "AND effective_date <= #{testDate} " +
            "AND expiry_date >= #{testDate} " +
            "AND status = 'PUBLISHED' " +
            "ORDER BY effective_date DESC " +
            "LIMIT 1")
    QcQualityStandard findCustomerStandard(
            @Param("customerId") String customerId,
            @Param("variety") String variety,
            @Param("grade") String grade,
            @Param("testDate") LocalDate testDate
    );

    /**
     * 三级优先级：企标查询（MySQL 5.7兼容）
     */
    @Select("SELECT * FROM qc_quality_standard " +
            "WHERE standard_type = 'ENTERPRISE' " +
            "AND variety = #{variety} " +
            "AND grade = #{grade} " +
            "AND effective_date <= #{testDate} " +
            "AND expiry_date >= #{testDate} " +
            "AND status = 'PUBLISHED' " +
            "ORDER BY effective_date DESC " +
            "LIMIT 1")
    QcQualityStandard findEnterpriseStandard(
            @Param("variety") String variety,
            @Param("grade") String grade,
            @Param("testDate") LocalDate testDate
    );

    /**
     * 三级优先级：国标查询（MySQL 5.7兼容）
     */
    @Select("SELECT * FROM qc_quality_standard " +
            "WHERE standard_type = 'NATIONAL' " +
            "AND variety = #{variety} " +
            "AND grade = #{grade} " +
            "AND effective_date <= #{testDate} " +
            "AND expiry_date >= #{testDate} " +
            "AND status = 'PUBLISHED' " +
            "ORDER BY effective_date DESC " +
            "LIMIT 1")
    QcQualityStandard findNationalStandard(
            @Param("variety") String variety,
            @Param("grade") String grade,
            @Param("testDate") LocalDate testDate
    );

    /**
     * 查询同体系其他已发布标准（发布时提示）
     */
    @Select("<script>" +
            "SELECT * FROM qc_quality_standard " +
            "WHERE standard_type = #{standardType} " +
            "AND variety = #{variety} " +
            "AND grade = #{grade} " +
            "AND status = 'PUBLISHED' " +
            "<if test='customerId != null'>AND customer_id = #{customerId} </if>" +
            "<if test='customerId == null'>AND customer_id IS NULL </if>" +
            "AND id != #{excludeId}" +
            "</script>")
    List<QcQualityStandard> findOtherPublishedInSameScope(
            @Param("standardType") String standardType,
            @Param("variety") String variety,
            @Param("grade") String grade,
            @Param("customerId") String customerId,
            @Param("excludeId") String excludeId
    );
}
