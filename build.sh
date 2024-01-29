#!/usr/bin/env sh
mvn clean package -Dmaven.test.skip=true
docker build -f ./Dockerfile -t aetherlib/alist-hub:$1 .
docker push aetherlib/alist-hub:$1