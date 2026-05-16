package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.entity.SysNotification;
import com.jhict.quality.service.api.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/notifications")
@Api(tags = "站内消息通知")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @GetMapping("/unread-count")
    @ApiOperation(value = "获取当前用户未读消息数")
    public ApiResult<Long> unreadCount() {
        String userNo = StpUtil.getLoginIdAsString();
        return ApiResult.success(notificationService.getUnreadCount(userNo));
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页查询当前用户通知列表")
    public ApiResult<IPage<SysNotification>> page(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") int pageSize) {
        String userNo = StpUtil.getLoginIdAsString();
        return ApiResult.success(notificationService.page(userNo, pageNum, pageSize));
    }

    @PutMapping("/{id}/read")
    @ApiOperation(value = "标记单条消息为已读")
    public ApiResult<Void> markRead(
            @ApiParam(value = "通知ID", required = true) @PathVariable String id) {
        notificationService.markRead(id);
        return ApiResult.success();
    }

    @PutMapping("/read-all")
    @ApiOperation(value = "标记当前用户所有消息为已读")
    public ApiResult<Void> markAllRead() {
        String userNo = StpUtil.getLoginIdAsString();
        notificationService.markAllRead(userNo);
        return ApiResult.success();
    }
}
