package com.sqlapk.medicine.module.system.mq.producer.auth;

import com.sqlapk.medicine.framework.mq.core.RedisMQTemplate;
import com.sqlapk.medicine.module.system.mq.message.auth.OAuth2ClientRefreshMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * OAuth 2.0 客户端相关消息的 Producer
 * 仅在 RedisMQTemplate Bean 存在时创建（即 MQ 功能启用时）
 */
@Component
@ConditionalOnBean(RedisMQTemplate.class)
public class OAuth2ClientProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link OAuth2ClientRefreshMessage} 消息
     */
    public void sendOAuth2ClientRefreshMessage() {
        OAuth2ClientRefreshMessage message = new OAuth2ClientRefreshMessage();
        redisMQTemplate.send(message);
    }

}
