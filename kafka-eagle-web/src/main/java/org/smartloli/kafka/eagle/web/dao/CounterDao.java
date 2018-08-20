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
package org.smartloli.kafka.eagle.web.dao;

import org.apache.ibatis.annotations.Param;
import org.smartloli.kafka.eagle.web.pojo.Counter;
import org.smartloli.kafka.eagle.web.pojo.Signiner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User interface definition
 *
 * @author smartloli.
 * <p>
 * Created by May 16, 2017
 */
public interface CounterDao {

    /**
     * 根据 consumer_group 查找
     *
     * @param consumerName 消费组名
     * @param topic        订阅主题名
     * @return
     */
    Counter findByConsumerGroup(@Param("consumerName") String consumerName, @Param("topic") String topic);


    int insertCounter(Counter counter);

    int updateLogSize(Counter counter);

}
