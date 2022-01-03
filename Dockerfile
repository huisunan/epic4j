FROM openjdk:8-jdk-alpine
MAINTAINER huisunan

RUN echo "http://mirrors.aliyun.com/alpine/edge/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/edge/community" >> /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/edge/testing" >> /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.12/main" >> /etc/apk/repositories \
    && apk upgrade -U -a \
    && apk add \
    libstdc++ \
    chromium \
    harfbuzz \
    nss \
    freetype \
    ttf-freefont \
    font-noto-emoji \
    wqy-zenhei \
    && rm -rf /var/cache/* \
    && mkdir /var/cache/apk
COPY local.conf /etc/fonts/local.conf


RUN mkdir -p /usr/src/app \
    && adduser -D chrome \
    && chown -R chrome:chrome /usr/src/app

ENV DEBIAN_FRONTEND=noninteractive TZ=Asia/Shanghai
ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium-browser
# Run Chrome as non-privileged
USER chrome

COPY target/epic4j.jar /usr/src/app/epic4j.jar
RUN touch /usr/src/app/application.yml
RUN mkdir /usr/src/app/error
WORKDIR /usr/src/app


CMD java -jar epic4j.jar