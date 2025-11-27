FROM eclipse-temurin:25-jre-alpine
ENV TZ=Asia/Yekaterinburg
ENV LANG=ru_RU.UTF-8
ENV LANGUAGE=ru_RU:en
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apk add --no-cache curl firefox geckodriver
VOLUME /tmp
WORKDIR /opt
RUN mkdir media && \
    mkdir torrents && \
    mkdir logs
COPY build/libs/tasks.jar /opt/tasks.jar
EXPOSE 8088
ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_OPTS -Dfile.encoding=UTF-8 -jar tasks.jar"]
