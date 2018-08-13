mvn clean install
mv target/kafka-eagle-web-1.2.3-bin.tar.gz docker/
cd docker/
tar -xf kafka-eagle-web-1.2.3-bin.tar.gz && rm -rf kafka-eagle-web-1.2.3-bin.tar.gz
mkdir -p kafka-eagle-web-1.2.3/kms/logs && cp ../ke.db kafka-eagle-web-1.2.3/db/
docker build -t kafka-eagle:0812 .
docker-compose up -d