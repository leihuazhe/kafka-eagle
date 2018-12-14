[![Build Status](https://travis-ci.org/smartloli/kafka-eagle.svg?branch=master)](https://travis-ci.org/smartloli/kafka-eagle)
![](https://img.shields.io/badge/language-java-orange.svg)
[![codebeat badge](https://codebeat.co/badges/bf22a7b2-76ac-4aba-b840-00328841d9e3)](https://codebeat.co/projects/github-com-smartloli-kafka-eagle-master)
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/smartloli/kafka-eagle/blob/master/LICENSE)

# docker 镜像打法
- 1.第一步，拉取代码: `https://github.com/leihuazhe/kafka-eagle`
- 2.`ke(root)` 目录 `mvn clean package` 
- 3.解压 `web/target` 下面的 `kafka-eagle-web-1.2.3-bin.tar.gz`，解压得到 `kafka-eagle-web-1.2.3`
- 4.将 `system-config.properties` 加入到 解压出来的 `kafka-eagle-web-1.2.3/bin` 下面
- 5.在 `kafka-eagle-web-1.2.3/kms` 文件夹下面创建 `logs` 文件夹
- 6.将 `web` 工程目录下的 `ke.db` 加入到 `kafka-eagle-web-1.2.3/db` 下面
- 7.Copy `kafka-eagle-web-1.2.3` 到 `kafka-eagle-web/docker` 下
- 8.在此路径下执行打镜像 `docker build -t docker.today36524.com.cn:5000/basic/kafka-eagle:1.2.3 .`

# 使用 docker-compose 形式启动 kafka-eagle
> 基本 docker-compose 配置文件如下:

```yml
version: '2'
services:
  kafkaEagle:
   container_name: kafkaEagle
   image: docker.today36524.com.cn:5000/basic/kafka-eagle:1.2.3
   environment:
     - TZ=CST-8
     - LANG=zh_CN.UTF-8
   volumes:
     - "./config/eagle.properties:/kafka-eagle/bin/system-config.properties"
     - "./config/eagle.properties:/kafka-eagle/conf/system-config.properties"
     - "./logs:/kafka-eagle/logs"
   ports:
     - "8048:8048"
```
我们需要配置的 `volumes`

- 需要在宿主机配置好 `eagle.properties`,并映射到容器内
- 需要定义 `logs` 存放地址

## 常见 eagle.properties 配置

```properties
######################################
# kafka zk 集群，默认即可
######################################
kafka.eagle.zk.cluster.alias=cluster

# zk 集群地址 
cluster.zk.list=127.0.0.1:2181

#cluster2.zk.list=xdn10:2181,xdn11:2181,xdn12:2181

######################################
# zk client thread limit
######################################
kafka.zk.limit.size=25

######################################
# kafka eagle 端口号
######################################
kafka.eagle.webui.port=8048

######################################
# kafka offset storage
######################################
kafka.eagle.offset.storage=kafka

######################################
# alarm email configure
######################################
kafka.eagle.mail.enable=true
kafka.eagle.mail.sa=alert_sa
kafka.eagle.mail.username=
kafka.eagle.mail.password=
kafka.eagle.mail.server.host=
kafka.eagle.mail.server.port=

######################################
# delete kafka topic token
######################################
kafka.eagle.topic.token=keadmin

######################################
# kafka sasl authenticate
######################################
kafka.eagle.sasl.enable=false
kafka.eagle.sasl.protocol=SASL_PLAINTEXT
kafka.eagle.sasl.mechanism=PLAIN

######################################
# kafka jdbc driver address
######################################
kafka.eagle.driver=org.sqlite.JDBC
kafka.eagle.url=jdbc:sqlite:/kafka-eagle/db/ke.db
kafka.eagle.username=root
kafka.eagle.password=smartloli


#----------------邮箱发送配置----------
#smtp服务器
mail.smtp.host=
#身份验证
mail.smtp.auth=true
#发送者的邮箱用户名
mail.sender.username=
mail.from.name=
#发送者的邮箱密码
mail.sender.password=

mail.charset=utf-8

mail.to.email=""


# 钉钉告警的配置信息
dd.token=
mail.to.dd=
```



# Kafka Eagle

This is an monitor system and monitor your kafka clusters, and visual consumer thread,offsets,owners etc.

When you install [Kafka Eagle](http://download.smartloli.org/), you can see the current consumer group,for each group the topics that they are consuming and the offsets, lag, logsize position of the group in each topic. This is useful to understand how fast you are consuming from a message queue and how quick the message queue is increase. This will help you debuging kafka producers and consumers or just to have an idea of what is going on in your system.

The system shows the trend of consumer and producer trends on the same day, so you can see what happened that day.

Supported on kafka version: ``` 0.8.2.x ```,``` 0.9.x ```,``` 0.10.x ```,``` 1.0.x ``` .

Supported platform: ```Mac OS X```,```Linux```,```Windows```.

Here are a few Kafka Eagle system screenshots:

# List of Consumer Groups & Active Group Graph
![Consumer & Active Graph](https://ke.smartloli.org/res/consumer@2x.png)

# List of Topics Detail
![Topics](https://ke.smartloli.org/res/list@2x.png)

# Consumer & Producer Rate Chart
![Rate Chart](https://ke.smartloli.org/res/consumer_producer_rate@2x.png)

# Start Kafka Eagle
![KE Script](https://ke.smartloli.org/res/ke_script@2x.png)

# Kafka Offset Types

Kafka is designed to be flexible on how the offsets are managed. Consumer can choose arbitrary storage and format to persist kafka offsets. Kafka Eagle currently support following popular storage format:
  * Zookeeper. Old version of Kafka (0.8.2 before) default storage in Zookeeper.
  * Kafka. New version of Kafka (0.10.0 in the future) default recommend storage in Kafka Topic(__consumer_offsets).
  
Each runtime instance of Kafka Eagle can only support a single type of storage format.

# Kafka SQL

Use the SQL statement to query the topic message log, and visualize the results, you can read [Kafka SQL](https://ke.smartloli.org/3.Manuals/9.KafkaSQL.html) to view the syntax.

# Quickstart

Please read [Kafka Eagle Install](https://ke.smartloli.org/2.Install/2.Installing.html) for setting up and running Kafka Eagle. It is worth noting that, please use ```chrome``` to access Kafka Eagle.

# Deploy

The project is a maven project that uses the Maven command to pack the deployment as follows:
```bash
./build.sh
```
# More Information

Please see the [Kafka Eagle Manual](https://ke.smartloli.org) for for more information including:
  * System environment settings and installation instructions.
  * Information about how to use script command.
  * Visual group,topic,offset metadata information etc.
  * Metadata collection and log change information.
 
# Contributing

The Kafka Eagle is released under the Apache License and we welcome any contributions within this license. Any pull request is welcome and will be reviewed and merged as quickly as possible.

Since this is an open source tool, please comply with the relevant laws and regulations, the use of civilization.

# Committers

Thanks to the following members for maintaining the project.

|Alias |Github |Email |
|:-- |:-- |:-- |
|smartloli|[smartloli](https://github.com/smartloli)|smartloli.org@gmail.com|
|hexiang|[hexian55](https://github.com/hexian55)|hexiang55@gmail.com|
