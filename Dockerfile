FROM openjdk:8
MAINTAINER huisunan
ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/google-chrome-stable
WORKDIR /opt/epic4j
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys  78BD65473CB3BD13 \
    && sh -c 'echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list' \
    && apt-get update \
    && apt-get install google-chrome-stable -y
COPY target/epic4j.jar ./
COPY start.sh ./
CMD /opt/epic4j/start.sh
