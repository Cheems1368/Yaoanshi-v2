package com.sqlapk.medicine.module.system.mq.producer.permission;

import com.sqlapk.medicine.framework.mq.core.RedisMQTemplate;
import com.sqlapk.medicine.module.system.mq.message.permission.RoleRefreshMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Role 角色相关消息的 Producer
 * 仅在 RedisMQTemplate Bean 存在时创建（即 MQ 功能启用时）
 *
 * @author 芋道源码
 */
@Component
@ConditionalOnBean(RedisMQTemplate.class)
public class RoleProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link RoleRefreshMessage} 消息
     */
    public void sendRoleRefreshMessage() {
        RoleRefreshMessage message = new RoleRefreshMessage();
        redisMQTemplate.send(message);
    }

}
