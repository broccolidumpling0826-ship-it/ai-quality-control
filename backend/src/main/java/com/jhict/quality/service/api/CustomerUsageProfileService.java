package com.jhict.quality.service.api;

import com.jhict.quality.vo.CustomerUsageProfileVO;

public interface CustomerUsageProfileService {

    CustomerUsageProfileVO findBestMatch(String customerId, String variety, String grade);
}
