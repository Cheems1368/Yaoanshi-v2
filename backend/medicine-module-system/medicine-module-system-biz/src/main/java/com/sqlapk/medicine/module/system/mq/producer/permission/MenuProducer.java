package com.sqlapk.medicine.module.system.mq.producer.permission;

import com.sqlapk.medicine.framework.mq.core.RedisMQTemplate;
import com.sqlapk.medicine.module.system.mq.message.permission.MenuRefreshMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Menu 菜单相关消息的 Producer
 * 仅在 RedisMQTemplate Bean 存在时创建（即 MQ 功能启用时）
 */
@Component
@ConditionalOnBean(RedisMQTemplate.class)
public class MenuProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link MenuRefreshMessage} 消息
     */
    public void sendMenuRefreshMessage() {
        MenuRefreshMessage message = new MenuRefreshMessage();
        redisMQTemplate.send(message);
    }

}
