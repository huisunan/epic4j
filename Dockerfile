FROM openjdk:8-jdk-alpine3.8
MAINTAINER huisunan
ENV DEBIAN_FRONTEND=noninteractive TZ=Asia/Shanghai
RUN echo "http://mirrors.aliyun.com/alpine/v3.12/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.12/community" >> /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/edge/testing" >> /etc/apk/repositories \
    && apk upgrade -U -a \
    && apk add \
    libstdc++ \
    chromium \
    harfbuzz \
    nss \
    freetype \
    ttf-freefont \
    bash \
    && rm -rf /var/cache/* \
    && mkdir /var/cache/apk
COPY local.conf /etc/fonts/local.conf


RUN mkdir -p /opt/epic4j

COPY target/epic4j.jar /opt/epic4j/epic4j.jar
COPY start.sh /opt/epic4j/start.sh

RUN adduser -D chrome \
  && chown -R chrome:chrome /opt/epic4j \
  && chmod +x /opt/epic4j/start.sh

ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium-browser
# Run Chrome as non-privileged
USER chrome
WORKDIR /opt/epic4j
CMD /opt/epic4j/start.sh