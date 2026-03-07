package com.sqlapk.medicine.module.system.mq.producer.mail;

import com.sqlapk.medicine.framework.mq.core.RedisMQTemplate;
import com.sqlapk.medicine.module.system.mq.message.mail.MailAccountRefreshMessage;
import com.sqlapk.medicine.module.system.mq.message.mail.MailSendMessage;
import com.sqlapk.medicine.module.system.mq.message.mail.MailTemplateRefreshMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Mail 邮件相关消息的 Producer
 * 仅在 RedisMQTemplate Bean 存在时创建（即 MQ 功能启用时）
 *
 * @author wangjingyi
 * @since 2021/4/19 13:33
 */
@Slf4j
@Component
@ConditionalOnBean(RedisMQTemplate.class)
public class MailProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link MailTemplateRefreshMessage} 消息
     */
    public void sendMailTemplateRefreshMessage() {
        MailTemplateRefreshMessage message = new MailTemplateRefreshMessage();
        redisMQTemplate.send(message);
    }

    /**
     * 发送 {@link MailAccountRefreshMessage} 消息
     */
    public void sendMailAccountRefreshMessage() {
        MailAccountRefreshMessage message = new MailAccountRefreshMessage();
        redisMQTemplate.send(message);
    }

    /**
     * 发送 {@link MailSendMessage} 消息
     *
     * @param sendLogId 发送日志编码
     * @param mail 接收邮件地址
     * @param accountId 邮件账号编号
     * @param nickname 邮件发件人
     * @param title 邮件标题
     * @param content 邮件内容
     */
    public void sendMailSendMessage(Long sendLogId, String mail, Long accountId,
                                    String nickname, String title, String content) {
        MailSendMessage message = new MailSendMessage()
                .setLogId(sendLogId).setMail(mail).setAccountId(accountId)
                .setNickname(nickname).setTitle(title).setContent(content);
        redisMQTemplate.send(message);
    }

}
