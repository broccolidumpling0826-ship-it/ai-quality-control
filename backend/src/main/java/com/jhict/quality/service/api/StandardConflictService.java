package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.StandardConflictResolveCmd;
import com.jhict.quality.vo.StandardConflictVO;

public interface StandardConflictService {

    IPage<StandardConflictVO> page(int pageNum, int pageSize, String status);

    StandardConflictVO getById(String id);

    void resolve(String id, StandardConflictResolveCmd cmd, String userNo);
}
