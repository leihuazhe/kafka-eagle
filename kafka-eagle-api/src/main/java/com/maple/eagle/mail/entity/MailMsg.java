package com.maple.eagle.mail.entity;


import com.maple.eagle.mail.enums.MailMsgType;

import java.util.ArrayList;
import java.util.List;


public class MailMsg {
    /**
     * 标题
     */
    private String subject;

    /**
     * 内容
     */
    private String content;

    /**
     * 邮件类型
     */
    private MailMsgType type;

    /**
     * 附件列表
     */
    List<MailAttach> attachList = new ArrayList<MailAttach>();

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MailMsgType getType() {
        return type;
    }

    public void setType(MailMsgType type) {
        this.type = type;
    }

    public List<MailAttach> getAttachList() {
        return attachList;
    }

    public void setAttachList(List<MailAttach> attachList) {
        this.attachList = attachList;
    }

    public MailMsg(String subject, String content, MailMsgType type) {
        this.subject = subject;
        this.content = content;
        this.type = type;
    }

    public MailMsg() {
    }

    public MailMsg(String subject, String content, MailMsgType type, List<MailAttach> attachList) {
        this.subject = subject;
        this.content = content;
        this.type = type;
        this.attachList = attachList;
    }


    @Override
    public String toString() {
        return "MailMsg{" +
                "subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", attachList=" + attachList +
                '}';
    }
}
