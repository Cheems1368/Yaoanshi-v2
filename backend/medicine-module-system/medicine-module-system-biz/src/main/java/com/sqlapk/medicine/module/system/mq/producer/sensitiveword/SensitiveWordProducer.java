package com.sqlapk.medicine.module.system.mq.producer.sensitiveword;

import com.sqlapk.medicine.framework.mq.core.RedisMQTemplate;
import com.sqlapk.medicine.module.system.mq.message.sensitiveword.SensitiveWordRefreshMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 敏感词相关的 Producer
 * 仅在 RedisMQTemplate Bean 存在时创建（即 MQ 功能启用时）
 */
@Component
@ConditionalOnBean(RedisMQTemplate.class)
public class SensitiveWordProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link SensitiveWordRefreshMessage} 消息
     */
    public void sendSensitiveWordRefreshMessage() {
        SensitiveWordRefreshMessage message = new SensitiveWordRefreshMessage();
        redisMQTemplate.send(message);
    }

}
