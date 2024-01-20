#!/bin/sh

if ! test -e /opt/alist/data/data.db; then
      unzip -d data /var/lib/data.zip  > /dev/null 2>&1
      chmod 777 data/data.db
fi
java -Duser.timezone='GMT+08' -jar /app.jar --spring.profiles.active=test