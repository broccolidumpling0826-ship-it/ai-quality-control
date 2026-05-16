package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.entity.SysNotification;

public interface NotificationService {

    /**
     * 发送站内通知
     *
     * @param receiverNo  接收人工号
     * @param title       标题
     * @param content     内容
     * @param relatedType 关联业务类型
     * @param relatedId   关联业务ID
     */
    void send(String receiverNo, String title, String content, String relatedType, String relatedId);

    /**
     * 获取未读消息数
     *
     * @param userNo 用户工号
     * @return 未读数量
     */
    long getUnreadCount(String userNo);

    /**
     * 分页查询通知
     *
     * @param userNo   用户工号
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    IPage<SysNotification> page(String userNo, int pageNum, int pageSize);

    /**
     * 标记单条消息为已读
     *
     * @param id 通知ID
     */
    void markRead(String id);

    /**
     * 标记该用户所有消息为已读
     *
     * @param userNo 用户工号
     */
    void markAllRead(String userNo);
}
