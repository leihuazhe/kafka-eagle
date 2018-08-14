/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.quartz;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.maple.eagle.dingding.DispatchMail;
import com.maple.eagle.mail.MailUtils;
import com.maple.eagle.mail.entity.MailMsg;
import com.maple.eagle.mail.enums.MailMsgType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.smartloli.kafka.eagle.api.email.MailFactory;
import org.smartloli.kafka.eagle.api.email.MailProvider;
import org.smartloli.kafka.eagle.common.protocol.AlarmInfo;
import org.smartloli.kafka.eagle.common.protocol.OffsetZkInfo;
import org.smartloli.kafka.eagle.common.protocol.OffsetsLiteInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.NetUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.ZkFactory;
import org.smartloli.kafka.eagle.core.factory.ZkService;

/**
 * Per 5 mins to stats offsets to offsets table.
 *
 * @author smartloli.
 * <p>
 * Created by Aug 18, 2016
 */
public class OffsetsQuartz {

    private final Logger LOG = LoggerFactory.getLogger(OffsetsQuartz.class);

    /**
     * Kafka service interface.
     */
    private KafkaService kafkaService = new KafkaFactory().create();

    /**
     * Zookeeper service interface.
     */
    private ZkService zkService = new ZkFactory().create();

    /**
     * Get alarmer configure.
     */
    private List<AlarmInfo> alarmConfigure(String clusterAlias) {
        String alarmer = zkService.getAlarm(clusterAlias);
        List<AlarmInfo> targets = new ArrayList<>();
        JSONArray alarmers = JSON.parseArray(alarmer);
        for (Object object : alarmers) {
            AlarmInfo alarm = new AlarmInfo();
            JSONObject alarmSerialize = (JSONObject) object;
            alarm.setGroup(alarmSerialize.getString("group"));
            alarm.setTopics(alarmSerialize.getString("topic"));
            alarm.setLag(alarmSerialize.getLong("lag"));
            alarm.setOwners(alarmSerialize.getString("owner"));
            targets.add(alarm);
        }
        StringBuilder builder = new StringBuilder();
        targets.forEach(target -> builder.append("\nalarm info:[ " + target.toString() + " ]\n"));
        LOG.info("alarm info {}", builder.toString());
        return targets;
    }

    private void alert(String clusterAlias, List<OffsetsLiteInfo> offsetLites) {
        boolean enableAlarm = SystemConfigUtils.getBooleanProperty("kafka.eagle.mail.enable");
        if (enableAlarm) {
            List<AlarmInfo> alarmers = alarmConfigure(clusterAlias);
            for (AlarmInfo alarm : alarmers) {
                for (OffsetsLiteInfo offset : offsetLites) {
                    //判断
                    LOG.info("=======> 判断是否发送告警 to alert offset info ... \n lag:{}", offset.getLag() - alarm.getLag());
                    if (offset.getGroup().equals(alarm.getGroup()) && offset.getTopic().equals(alarm.getTopics()) && offset.getLag() > alarm.getLag()) {
                        try {
                            LOG.info("=======> begin to send email ");
                            MailProvider provider = new MailFactory();
                            String subject = "Kafka Eagle Consumer Alert";
                            String address = alarm.getOwners();

                            StringBuilder content = new StringBuilder();

                            content.append("Dear All:<br/> <strong>中台kafka事件消费组消费事件出现堆积:</strong><br/>");
                            content.append("<strong>消费组: </strong>").append(alarm.getGroup())
                                    .append("<br/><strong>消息topic: </strong>").append(alarm.getTopics()).append("<br/>出现堆积，堆积消息量已经达到: ").append(offset.getLag())
                                    .append("<br/>");
                            content.append("<br/><br/><br/><span style='color:red;'><strong>请及时检查事件是否积压!!!</strong></span><br/>");

                            MailMsg msg = new MailMsg("Kafka事件监控Offset告警!!!", content.toString(), MailMsgType.multi);


                            String dMsg = "\n事件堆积监控报警," +
                                    "\n消费组: " + alarm.getGroup() +
                                    "\n订阅Topic: " + alarm.getTopics() +
                                    "\n消息出现堆积,消息量已经达到" + offset.getLag() +
                                    "\n\n请安排相关人员查看错误原因并及时处理。确保系统正常运行！\n";

                            DispatchMail.sendDingDing(dMsg);
                        } catch (Exception ex) {
                            LOG.error("Topic[" + alarm.getTopics() + "] Send alarm mail has error,msg is " + ex.getMessage(), ex);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get kafka brokers.
     */
    private List<String> getBrokers(String clusterAlias) {
        String brokers = kafkaService.getAllBrokersInfo(clusterAlias);
        JSONArray kafkaBrokers = JSON.parseArray(brokers);
        List<String> targets = new ArrayList<String>();
        for (Object object : kafkaBrokers) {
            JSONObject kafkaBroker = (JSONObject) object;
            String host = kafkaBroker.getString("host");
            int port = kafkaBroker.getInteger("port");
            targets.add(host + ":" + port);
        }
        return targets;
    }

    private OffsetZkInfo getKafkaOffset(String clusterAlias, String bootstrapServers, String topic, String group, int partition) {
        JSONArray kafkaOffsets = JSON.parseArray(kafkaService.getKafkaOffset(clusterAlias));
        OffsetZkInfo targets = new OffsetZkInfo();
        if (kafkaOffsets != null) {
            for (Object object : kafkaOffsets) {
                JSONObject kafkaOffset = (JSONObject) object;
                String _topic = kafkaOffset.getString("topic");
                String _group = kafkaOffset.getString("group");
                int _partition = kafkaOffset.getInteger("partition");
                long timestamp = kafkaOffset.getLong("timestamp");
                long offset = kafkaOffset.getLong("offset");
                if (topic.equals(_topic) && group.equals(_group) && partition == _partition) {
                    targets.setOffset(offset);
                    targets.setCreate(CalendarUtils.convertUnixTime2Date(timestamp));
                    targets.setModify(CalendarUtils.convertUnixTime2Date(timestamp));
                }
            }
        }

        return targets;
    }

    /**
     * Get the corresponding string per minute.
     */
    private String getStatsPerDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return df.format(new Date());
    }

    public void jobQuartz() {
        String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
        for (String clusterAlias : clusterAliass) {
            execute(clusterAlias);
        }
    }

    /**
     * Perform offset statistical tasks on time.
     */
    private void execute(String clusterAlias) {
        LOG.info("======> start to execute offset quartz");
        try {
            List<String> hosts = getBrokers(clusterAlias);
            List<OffsetsLiteInfo> offsetLites = new ArrayList<>();
            String formatter = SystemConfigUtils.getProperty("kafka.eagle.offset.storage");
            Map<String, List<String>> consumers = null;
            if ("kafka".equals(formatter)) {
                Map<String, List<String>> consumerGroupMap = new HashMap<String, List<String>>();
                try {
                    JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
                    for (Object object : consumerGroups) {
                        JSONObject consumerGroup = (JSONObject) object;
                        String group = consumerGroup.getString("group");
                        List<String> topics = new ArrayList<>();
                        for (String topic : kafkaService.getKafkaConsumerTopic(clusterAlias, group)) {
                            topics.add(topic);
                        }
                        consumerGroupMap.put(group, topics);
                    }
                    consumers = consumerGroupMap;
                } catch (Exception e) {
                    LOG.error("Get consumer info from [kafkaService.getKafkaConsumer] has error,msg is " + e.getMessage(), e);
                }
            } else {
                consumers = kafkaService.getConsumers(clusterAlias);
            }
            String statsPerDate = getStatsPerDate();
            for (Entry<String, List<String>> entry : consumers.entrySet()) {
                String group = entry.getKey();
                for (String topic : entry.getValue()) {
                    OffsetsLiteInfo offsetSQLite = new OffsetsLiteInfo();
                    for (String partitionStr : kafkaService.findTopicPartition(clusterAlias, topic)) {
                        int partition = Integer.parseInt(partitionStr);
                        long logSize = 0L;
                        if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
                            logSize = kafkaService.getKafkaLogSize(clusterAlias, topic, partition);
                        } else {
                            logSize = kafkaService.getLogSize(hosts, topic, partition);
                        }
                        OffsetZkInfo offsetZk;
                        if ("kafka".equals(formatter)) {
                            StringBuilder bootstrapServers = new StringBuilder();
                            for (String host : hosts) {
                                bootstrapServers.append(host).append(",");
                            }
                            bootstrapServers = new StringBuilder(bootstrapServers.substring(0, bootstrapServers.length() - 1));
                            offsetZk = getKafkaOffset(clusterAlias, bootstrapServers.toString(), topic, group, partition);
                        } else {
                            offsetZk = kafkaService.getOffset(clusterAlias, topic, group, partition);
                        }
                        offsetSQLite.setGroup(group);
                        offsetSQLite.setCreated(statsPerDate);
                        offsetSQLite.setTopic(topic);
                        if (logSize == 0) {
                            offsetSQLite.setLag(0L + offsetSQLite.getLag());
                        } else {
                            long lag = offsetSQLite.getLag() + (offsetZk.getOffset() == -1 ? 0 : logSize - offsetZk.getOffset());
                            offsetSQLite.setLag(lag);
                        }
                        offsetSQLite.setLogSize(logSize + offsetSQLite.getLogSize());
                        offsetSQLite.setOffsets(offsetZk.getOffset() + offsetSQLite.getOffsets());
                    }
                    offsetLites.add(offsetSQLite);
                }
            }
            // Plan ASSS: Storage into zookeeper.
//            zkService.insert(clusterAlias, offsetLites);

            // Plan B: Storage single file.
            // keService.write(clusterAlias, offsetLites.toString());
            StringBuilder logBuilder = new StringBuilder();
            offsetLites.forEach(lite -> logBuilder.append("\nINFO:[" + lite.toString() + "],\n"));
            LOG.info("offsetLites info:{} ", logBuilder.toString());
            alert(clusterAlias, offsetLites);
//            alertBroker(clusterAlias);


        } catch (Exception ex) {
            LOG.error("Quartz statistics offset has error,msg is " + ex.getMessage(), ex);
        }
        LOG.info("======> end to execute offset quartz");
    }

    private void alertBroker(String clusterAlias) {
        String brokers = kafkaService.getAllBrokersInfo(clusterAlias);
        JSONArray kafkaBrokers = JSON.parseArray(brokers);
        for (Object object : kafkaBrokers) {
            JSONObject kafkaBroker = (JSONObject) object;
            String host = kafkaBroker.getString("host");
            int port = kafkaBroker.getInteger("port");
            boolean status = NetUtils.telnet(host, port);
            if (!status) {
                try {
                    MailProvider provider = new MailFactory();
                    String subject = "Kafka Eagle On-Site Inspection Alert";
//                    String content = "Thread Service [" + host + ":" + port + "] has crashed,please check it.";
                    StringBuilder content = new StringBuilder();

                    content.append("Dear All:<br/> <strong>kafka broker 告警:</strong><br/>");
                    content.append("<strong>kafka broker host: " + host + ",port: " + port + "  crashed了，请及时检查</strong>");

                    MailMsg msg = new MailMsg("kafka broker 告警!!!", content.toString(), MailMsgType.multi);

//                            provider.create().send(subject, address, content, "");

                    LOG.info("send to address:{},info:{}", "", msg.toString());
                    MailUtils.sendEmail("", msg);

//                    provider.create().send(subject, address, content, "");
                } catch (Exception ex) {
                    LOG.error("Alertor[" + "" + "] Send alarm mail has error,msg is " + ex.getMessage(), ex);
                }
            }
        }
    }
}
