FROM hseeberger/scala-sbt
MAINTAINER vprusa@redhat.com

RUN git clone --depth=1 https://github.com/vprusa/echoIrc\
    && rm -rf /var/cache/yum

EXPOSE 8080

CMD cd echoIrc/ && sbt 'project backend;run'