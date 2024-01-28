#!/usr/bin/env sh
mkdir /data
mkdir /index
mkdir -p /www/cgi-bin
rm -rf /var/lib/data
unzip /var/lib/data.zip -d /var/lib/data
cd /var/lib/data
mv header.html   /www/cgi-bin/
mv search   /www/cgi-bin/
mv  sou   /www/cgi-bin/
mv  whatsnew   /www/cgi-bin/
tar -zx -f  mobi.tgz
mv foliate-js /www/
mv *.js /etc/nginx/http.d
mv emby*  /etc/nginx/http.d
/bin/busybox-extras httpd -p 81 -h /www