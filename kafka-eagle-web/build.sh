mvn clean install
mv target/kafka-eagle-web-1.2.3-bin.tar.gz docker/
cd docker/
tar -xf kafka-eagle-web-1.2.3-bin.tar.gz && rm -rf kafka-eagle-web-1.2.3-bin.tar.gz
mkdir -p kafka-eagle-web-1.2.3/kms/logs && cp ../ke.db kafka-eagle-web-1.2.3/db/
docker build -t docker.today36524.com.cn:5000/basic/kafka-eagle:2.0.4 .
docker push docker.today36524.com.cn:5000/basic/kafka-eagle:2.0.4
docker-compose up -d