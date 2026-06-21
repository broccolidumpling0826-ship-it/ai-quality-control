package com.jhict.quality.service.api;

import com.jhict.quality.dto.PromptActivateCmd;
import com.jhict.quality.vo.AiAuditDashboardVO;
import com.jhict.quality.vo.AiAuditLogVO;

public interface AiAuditDashboardService {

    AiAuditDashboardVO getDashboard();

    AiAuditLogVO replay(String auditLogId);

    void activatePrompt(String promptKey, PromptActivateCmd cmd, String userNo);
}
