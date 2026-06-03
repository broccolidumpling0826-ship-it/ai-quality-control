package com.jhict.quality.service.api;

import java.util.List;

public interface RbacQueryService {

    List<String> getUserRoleCodes(String userNo);

    List<String> getUserRoleIds(String userNo);

    List<String> getUserPermissionCodes(String userNo);

    void syncUserRoles(String userId, List<String> roleCodes, String primaryRoleCode);
}
