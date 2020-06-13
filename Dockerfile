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
    && echo "java -server -Xms6G -Xmx6G -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:MaxGCPauseMillis=100 -XX:+DisableExplicitGC -XX:TargetSurvivorRatio=90 -XX:G1NewSizePercent=50 -XX:G1MaxNewSizePercent=80 -XX:G1MixedGCLiveThresholdPercent=35 -XX:+AlwaysPreTouch -XX:+ParallelRefProcEnabled -Dusing.aikars.flags=mcflags.emc.gs -jar server.jar" >> start.sh \
    && chmod +x start.sh

CMD [ "./start.sh" ]
