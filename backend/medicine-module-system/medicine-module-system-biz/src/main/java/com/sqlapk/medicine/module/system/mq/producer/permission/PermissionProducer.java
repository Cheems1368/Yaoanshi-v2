package com.sqlapk.medicine.module.system.mq.producer.permission;

import com.sqlapk.medicine.framework.mq.core.RedisMQTemplate;
import com.sqlapk.medicine.module.system.mq.message.permission.RoleMenuRefreshMessage;
import com.sqlapk.medicine.module.system.mq.message.permission.UserRoleRefreshMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Permission 权限相关消息的 Producer
 * 仅在 RedisMQTemplate Bean 存在时创建（即 MQ 功能启用时）
 */
@Component
@ConditionalOnBean(RedisMQTemplate.class)
public class PermissionProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link RoleMenuRefreshMessage} 消息
     */
    public void sendRoleMenuRefreshMessage() {
        RoleMenuRefreshMessage message = new RoleMenuRefreshMessage();
        redisMQTemplate.send(message);
    }

    /**
     * 发送 {@link UserRoleRefreshMessage} 消息
     */
    public void sendUserRoleRefreshMessage() {
        UserRoleRefreshMessage message = new UserRoleRefreshMessage();
        redisMQTemplate.send(message);
    }

}
