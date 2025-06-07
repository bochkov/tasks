FROM eclipse-temurin:21-jre
ENV TZ=Asia/Yekaterinburg
ENV LANG=ru_RU.UTF-8
ENV LANGUAGE=ru_RU:en
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt-get update && apt-get install -y curl unzip
RUN wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get install -y ./google-chrome-stable_current_amd64.deb && \
    rm ./google-chrome-stable_current_amd64.deb
RUN wget -q https://storage.googleapis.com/chrome-for-testing-public/137.0.7151.68/linux64/chromedriver-linux64.zip && \
    unzip chromedriver-linux64.zip && \
    mv ./chromedriver-linux64/chromedriver /usr/bin && \
    rm ./chromedriver-linux64.zip
VOLUME /tmp
WORKDIR /opt
RUN mkdir media && \
    mkdir torrents && \
    mkdir logs
ADD build/libs/tasks.jar /opt/tasks.jar
EXPOSE 8088
ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_OPTS -Dfile.encoding=UTF-8 -jar tasks.jar"]
