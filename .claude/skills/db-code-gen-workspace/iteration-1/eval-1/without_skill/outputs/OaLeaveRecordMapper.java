package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.OaLeaveRecord;
import com.example.query.OaLeaveRecordQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 请假记录 Mapper 接口
 */
@Mapper
public interface OaLeaveRecordMapper extends BaseMapper<OaLeaveRecord> {

    /**
     * 分页查询请假记录
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<OaLeaveRecord> selectPageByQuery(Page<OaLeaveRecord> page, @Param("query") OaLeaveRecordQuery query);
}
