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
package org.smartloli.kafka.eagle.web.pojo;

import com.google.gson.Gson;

/**
 * desc: ke_counter
 * id:
 * consumer_name:
 * topic:
 * pre_log_size:
 * now_log_size:
 * log_time:
 *
 * @author hz.lei
 * @since 2018年08月13日 下午3:15
 */

/**
 * CREATE TABLE `ke_counter`(`id` integer primary key autoincrement,`consumer_name` varchar(64),`topic` varchar(64),`pre_log_size` bigint(20), `now_log_size` bigint(20),`log_time` varchar(20))
 */
public class Counter {
    /**
     * consumerName
     */
    private String consumerName;
    /**
     * topic
     */
    private String topic;
    /**
     * preLogSize
     */
    private long logSize;
    /**
     * logTime
     */
    private String logTime;


    public Counter(String consumerName, String topic, long logSize, String logTime) {
        this.consumerName = consumerName;
        this.topic = topic;
        this.logSize = logSize;
        this.logTime = logTime;
    }

    public Counter() {
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getLogSize() {
        return logSize;
    }

    public void setLogSize(long logSize) {
        this.logSize = logSize;
    }

    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }
}
