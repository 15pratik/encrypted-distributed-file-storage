## Setup
Minio Docker Setup
```
docker run -d --name minio -p 9000:9000 -e "MINIO_ACCESS_KEY=accessKey" -e "MINIO_SECRET_KEY=secretKey" minio/minio server /home/pratik/personal/dev/minio
```

Create buckets:
my-bucket-0, my-bucket-1, my-bucket-2


Build fat jar
```
cd encrypted-distributed-file-storage
mvn clean install -Pfat-jars
```

Upload Encrypted File
```
java -Dfile=src/main/resources/text.txt -Dout=ENC_FILE -Dkey=src/main/resources/key.pem -cp target/encrypted-distributed-file-storage-1.0-SNAPSHOT-jar-with-dependencies.jar filesystem.FileUploader
```

Download Decrypted File
```
java -Dfile=ENC_FILE -Dout=/tmp/Downloads -Dkey=src/main/resources/key.pem -cp target/encrypted-distributed-file-storage-1.0-SNAPSHOT-jar-with-dependencies.jar filesystem.FileDownloader
```