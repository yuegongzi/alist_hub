#!/bin/sh

if ! test -e /opt/alist/data/data.db; then
      unzip -d /var/lib/data /var/lib/data.zip  > /dev/null 2>&1
      mv /var/lib/data/data.db data
fi
mkdir -p /web
unzip -o /dist.zip -d /web
mv /web/dist /web/@hub
java -Duser.timezone='GMT+08' -jar /app.jar --spring.profiles.active=prod