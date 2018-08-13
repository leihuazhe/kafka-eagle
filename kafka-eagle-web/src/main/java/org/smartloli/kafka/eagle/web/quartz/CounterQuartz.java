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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.maple.eagle.dingding.DispatchMail;
import com.maple.eagle.mail.MailUtils;
import com.maple.eagle.mail.entity.MailMsg;
import com.maple.eagle.mail.enums.MailMsgType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.smartloli.kafka.eagle.web.dao.CounterDao;
import org.smartloli.kafka.eagle.web.pojo.Counter;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;

/**
 * Per 5 mins to stats offsets to offsets table.
 *
 * @author smartloli.
 * <p>
 * Created by Aug 18, 2016
 */
public class CounterQuartz {
    private final Logger logger = LoggerFactory.getLogger(CounterQuartz.class);
    /**
     * Kafka service interface.
     */
    private KafkaService kafkaService = new KafkaFactory().create();

    private final String STORAGE_WAY = "kafka";


    @Autowired
    private CounterDao counterDao;

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
        logger.info("======> start to execute counter quartz");
        try {
            List<String> hosts = getBrokers(clusterAlias);
            List<OffsetsLiteInfo> offsetLites = new ArrayList<>();
            String formatter = SystemConfigUtils.getProperty("kafka.eagle.offset.storage");
            Map<String, List<String>> consumers = null;

            if (STORAGE_WAY.equals(formatter)) {
                Map<String, List<String>> consumerGroupMap = new HashMap<>();
                try {
                    JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
                    for (Object object : consumerGroups) {
                        JSONObject consumerGroup = (JSONObject) object;
                        String group = consumerGroup.getString("group");
                        List<String> topics = new ArrayList<>(kafkaService.getKafkaConsumerTopic(clusterAlias, group));
                        consumerGroupMap.put(group, topics);
                    }
                    consumers = consumerGroupMap;
                } catch (Exception e) {
                    logger.error("Get consumer info from [kafkaService.getKafkaConsumer] has error,msg is " + e.getMessage(), e);
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
                        long logSize;

                        logSize = kafkaService.getLogSize(hosts, topic, partition);

                        OffsetZkInfo offsetZk;
                        if (STORAGE_WAY.equals(formatter)) {
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
            counter(offsetLites);


        } catch (Exception ex) {
            logger.error("Quartz statistics offset has error,msg is " + ex.getMessage(), ex);
        }
        logger.info("======> end to execute offset quartz");
    }


    private void counter(List<OffsetsLiteInfo> offsetLites) {
        StringBuilder msgBuilder = new StringBuilder();
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        String nowDate = now.toString();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
        String formatDate = df.format(dateTime);

        offsetLites.forEach(offset -> {
            HashMap<String, String> params = new HashMap<>();
            params.put("consumerName", offset.getGroup());
            params.put("logTime", nowDate);

            List<Counter> groupCounters = counterDao.findByGroupAndTime(params);

            long preLogSize;
            if (groupCounters.size() > 0) {
                counterDao.insertCounter(new Counter(offset.getGroup(), offset.getTopic(), groupCounters.get(0).getNowLogSize(), offset.getLogSize(), nowDate));
                preLogSize = groupCounters.get(0).getNowLogSize();
            } else {
                counterDao.insertCounter(new Counter(offset.getGroup(), offset.getTopic(), offset.getLogSize(), offset.getLogSize(), nowDate));
                preLogSize = offset.getLogSize();
            }

            msgBuilder.append("\n时间: ").append(formatDate)
                    .append(", 消费组: ").append(offset.getGroup())
                    .append(", Topic: ").append(offset.getTopic())
                    .append(", 之前Size: ").append(preLogSize)
                    .append(", 现在Size: ").append(offset.getLogSize())
                    .append(", 消息量:").append(offset.getLogSize() - preLogSize)
                    .append("\n");
        });
//        List<Counter> counters = counterDao.findByLogTime(nowDate);
        DispatchMail.sendDingDing(msgBuilder.toString());
    }
}
