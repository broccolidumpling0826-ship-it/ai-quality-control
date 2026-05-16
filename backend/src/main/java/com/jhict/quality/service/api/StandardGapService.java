package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.StandardGapPageQuery;
import com.jhict.quality.vo.StandardGapVO;

public interface StandardGapService {

    /**
     * 分页查询标准覆盖缺口
     */
    IPage<StandardGapVO> pageStandardGaps(StandardGapPageQuery query);

    /**
     * 标记缺口已解决
     */
    void resolveStandardGap(String id);
}
