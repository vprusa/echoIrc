FROM hseeberger/scala-sbt
MAINTAINER vprusa@redhat.com

RUN rm -rf echoIrc

RUN git clone --depth=1 https://github.com/vprusa/echoIrc.git\
    && rm -rf /var/cache/yum
EXPOSE 9000
CMD cd echoIrc/ && sbt run