package com.maple.eagle.dingding;

import java.util.Set;

/**
 * @author huyj
 * @Created 2018-07-23 17:31
 */
public class MailUser {
    public String userName;
    public String mailsTo;
    public String logTag;
    public Set<String> phones;

    public MailUser(String userName, String mailsTo, String logTag, Set<String> phones) {
        this.userName = userName;
        this.mailsTo = mailsTo;
        this.logTag = logTag;
        this.phones = phones;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMailsTo() {
        return mailsTo;
    }

    public void setMailsTo(String mailsTo) {
        this.mailsTo = mailsTo;
    }

    public String getLogTag() {
        return logTag;
    }

    public void setLogTag(String logTag) {
        this.logTag = logTag;
    }
}
