package com.maple.eagle.mail.common;


import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;


/**
 * <p>Title: 邮件配置信息</p>
 * <p>Description: 描述（简要描述类的职责、实现方式、使用注意事项等）</p>
 */
public class MailCfg {
    /**
     * 邮件服务器
     * SystemConfigUtils.getProperty("kafka.eagle.mail.server.host")
     */
    public final static String HOST = SystemConfigUtils.getProperty("mail.smtp.host");

    /**
     * 邮件编码
     */
    public final static String CHARSET = SystemConfigUtils.getProperty("mail.charset");

    /**
     * 发送者邮箱
     */
    public final static String DEFAULT_FROM_EMAIL = SystemConfigUtils.getProperty("mail.sender.username");

    /**
     * 发送者密码
     */
    public final static String DEFAULT_FROM_PASSWD = SystemConfigUtils.getProperty("mail.sender.password");

    /**
     * 发送者名称
     */
    public final static String DEFAULT_FROM_NAME = SystemConfigUtils.getProperty("mail.from.name");

    public final static String DEFAULT_TO_NAME = SystemConfigUtils.getProperty("mail.to.email");
}
