package com.maple.eagle.mail;


import com.maple.eagle.mail.entity.MailMsg;
import com.maple.eagle.mail.service.MailService;
import com.maple.eagle.mail.service.impl.ApacheMailServiceImpl;

/**
 * @author huyj
 * @Created 2018/5/4 16:49
 */
public class MailUtils {


    public static void sendEmail(String toUser, MailMsg msg) {
        MailService service = new ApacheMailServiceImpl();
        service.sendMail(toUser, msg);
    }
}
