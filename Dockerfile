FROM mongo:bionic

ENV APPLICATION_USER ktor

RUN useradd $APPLICATION_USER \
    && mkdir /app \
    && chown -R $APPLICATION_USER /app \
    && echo "deb https://mirrors.tuna.tsinghua.edu.cn/ubuntu/ bionic main restricted universe multiverse" >> /etc/apt/sources.list \
    && echo "deb https://mirrors.tuna.tsinghua.edu.cn/ubuntu/ bionic-updates main restricted universe multiverse" >> /etc/apt/sources.list \
    && echo "deb https://mirrors.tuna.tsinghua.edu.cn/ubuntu/ bionic-backports main restricted universe multiverse" >> /etc/apt/sources.list \
    && echo "deb https://mirrors.tuna.tsinghua.edu.cn/ubuntu/ bionic-security main restricted universe multiverse" >> /etc/apt/sources.list \
    && apt-get update \
    && apt-get install -y openjdk-11-jre-headless

USER $APPLICATION_USER

VOLUME [ "/data/db" ]

COPY ./build/libs/server.jar /app/server.jar
WORKDIR /app

RUN echo "#!/bin/sh" > start.sh \
    && echo "mongod --port 27000 &" >> start.sh \
    && echo "java -server -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:InitialRAMFraction=2 -XX:MinRAMFraction=2 -XX:MaxRAMFraction=2 -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication -jar server.jar" >> start.sh \
    && chmod +x start.sh

CMD [ "./start.sh" ]
