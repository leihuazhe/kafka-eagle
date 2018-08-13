FROM docker.today36524.com.cn:5000/base/openjdk:server-jre8
MAINTAINER hz.lei

ENV KE_HOME /kafka-eagle
ENV JAVA_HOME /opt/oracle-server-jre/
ENV PATH $KE_HOME:$JAVA_HOME:$PATH

RUN mkdir -p "$KE_HOME"
COPY kafka-eagle-web-1.2.3 /kafka-eagle
RUN chmod +x /kafka-eagle/bin/*.sh
CMD sh /kafka-eagle/bin/ke.sh start  && tail -F /kafka-eagle/bin/ke.sh
