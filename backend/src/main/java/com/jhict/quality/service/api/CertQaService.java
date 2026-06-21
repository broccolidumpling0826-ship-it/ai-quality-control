package com.jhict.quality.service.api;

import com.jhict.quality.dto.CertQaQueryCmd;
import com.jhict.quality.vo.CertQaAnswerVO;

public interface CertQaService {

    CertQaAnswerVO answer(CertQaQueryCmd cmd);
}
