FROM harbor.ymt.io/inf/java:openjdk-8-jre-alpine

ENV service_name_arg=davinci

ARG BUILD_ENV
ENV env_arg=${BUILD_ENV}

WORKDIR /data/$service_name_arg

COPY ./target/$service_name_arg-0.0.1-SNAPSHOT.jar app.jar

CMD java -jar -Xms256m -Xmx8192m -DSpring.profiles.active=$env_arg -Djava.security.egd=file:/dev/./urandom -Duser.timezone=GMT+08 -XX:ParallelGCThreads=4 -XX:+PrintGCDetails -Xloggc:/data/logs/$service_name_arg/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/logs/$service_name_arg/dump app.jar
