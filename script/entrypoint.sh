#!/bin/sh

if test -e /opt/alist/data/data.db; then
    echo "Already initialized"
else
  echo "Initializing alist ... ..."
  /opt/alist/alist server  --no-prefix > /opt/alist/data/init.log 2>&1 &
  sleep 10s
  pkill -f /opt/alist/alist
  echo "Initializing Done"
fi
java -Duser.timezone='GMT+08' -jar /app.jar --spring.profiles.active=test