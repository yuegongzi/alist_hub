FROM azul/zulu-openjdk-alpine:17-jre
FROM xiaoyaliu/alist:latest
EXPOSE 5244 5245
COPY --from=0 /usr/lib/jvm /usr/lib/jvm
ENV JAVA_HOME=/usr/lib/jvm/zulu17
ENV PATH=$PATH:$JAVA_HOME/bin
COPY target/*.jar /app.jar
COPY script/entrypoint.sh /start.sh
VOLUME ["/opt/alist/data"]
WORKDIR /opt/alist
ENTRYPOINT ["/start.sh"]
