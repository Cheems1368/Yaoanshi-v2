package com.sqlapk.medicine.module.system.mq.producer.sensitiveword;

import com.sqlapk.medicine.framework.mq.core.RedisMQTemplate;
import com.sqlapk.medicine.module.system.mq.message.sensitiveword.SensitiveWordRefreshMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 敏感词相关的 Producer
 */
@Component
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
