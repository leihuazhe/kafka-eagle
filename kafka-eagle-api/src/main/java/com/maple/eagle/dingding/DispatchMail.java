package com.maple.eagle.dingding;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

import java.io.UnsupportedEncodingException;
import java.util.*;


/**
 * @author lhz
 * @Created 2018-08-12 22:05
 */
public class DispatchMail {

    private static final Logger logger = LoggerFactory.getLogger(DispatchMail.class);

    private static final String DD_TOKEN = SystemConfigUtils.getProperty("dd.token");


    /**
     * send message to DD
     *
     * @param context
     * @return
     */
    public static boolean sendDingDing(String context) {
        String sendDD = null;
        try {
            sendDD = new String(SystemConfigUtils.getProperty("mail.to.dd").getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        Set<String> sendPeoples = new HashSet<>(Arrays.asList(sendDD.split(",")));

        StringBuilder builder = new StringBuilder();
        builder.append("Dear ");
        sendPeoples.forEach(p -> builder.append(p + " "));
        builder.append(":\n").append(context);

        String _response = HttpUtils.doPostJson(DD_TOKEN, buildSendDDMap(sendPeoples, builder.toString()), "UTF-8");
        logger.info("to DD response: ", _response);
        return true;
    }


    public static Map buildSendDDMap(Set<String> atPeoples, String context) {
        Map root = putMap(null, "msgtype", "text");
        putMap(root, "text", putMap(null, "content", context));
        putMap(root, "at", putMap(null, "atMobiles", atPeoples));
        putMap(root, "isAtAll", false);
        return root;
    }

    private String getDDMessage(String logTag, MailUser mailUser) {
        String messageInfo = "Dear " + mailUser.getUserName() + ":\n" +
                "  您负责的项目" + logTag + "出现异常日志，请安排相关人员查看错误原因并及时处理。确保系统正常运行！\n" +
                "  相关信息已经发送至您的邮箱，请查看邮件内容并及时处理，特殊情况请说明原因，谢谢合作！\n";
        return messageInfo;
    }


    private static Map putMap(Map map, String key, Object value) {
        if (map == null) {
            map = new HashMap();
        }
        map.put(key, value);
        return map;
    }
}



