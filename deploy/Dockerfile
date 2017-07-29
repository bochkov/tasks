FROM openjdk:jre-alpine
ENV TZ="Asia/Yekaterinburg"
RUN apk update && apk upgrade && \
  apk add --update tzdata && \
  apk add ca-certificates wget curl && \
  update-ca-certificates && \
  wget -O /usr/local/bin/dumb-init https://github.com/Yelp/dumb-init/releases/download/v1.2.0/dumb-init_1.2.0_amd64 && \
  chmod +x /usr/local/bin/dumb-init
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
VOLUME /tmp
WORKDIR /opt
RUN mkdir media && \
    mkdir torrents && \
    mkdir logs
ADD tasks.jar /opt/tasks.jar
EXPOSE 8088
ENTRYPOINT ["/usr/local/bin/dumb-init", "--"]
CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "tasks.jar"]
