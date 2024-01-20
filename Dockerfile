FROM xiaoyaliu/alist:latest
FROM azul/zulu-openjdk-alpine:17-jre
EXPOSE 5244 5245
COPY target/*.jar app.jar
COPY --from=0 /opt/alist /opt/alist
COPY --from=0 /var/lib/data.zip /var/lib/data.zip
VOLUME ["/opt/alist/data"]
WORKDIR /opt/alist
COPY script/entrypoint.sh /
ENTRYPOINT ["/entrypoint.sh"]
