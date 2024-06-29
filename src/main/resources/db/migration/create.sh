#!/usr/bin/env sh

# 函数：检查并创建目录
create_dir_with_check() {
   local dir=$1
       if [ ! -d "$dir" ]; then
           echo "目录 $dir 不存在，正在创建..."
           mkdir -p "$dir"
       fi
}

# 移动并覆盖文件前检查是否存在
move_with_check() {
    src=$1
    dest=$2
    if [ -e "$dest" ]; then
        echo "目标 $dest 已存在，正在删除..."
        rm -rf "$dest"
    fi
    mv "$src" "$dest"
}

# 创建目录
create_dir_with_check "/data"
create_dir_with_check "/index"
create_dir_with_check "/www/cgi-bin"
create_dir_with_check "/etc/nginx/http.d"

# 删除并解压文件
rm -rf /var/lib/data
unzip /var/lib/data.zip -d /var/lib/data



# 移动并覆盖文件前检查是否存在
move_with_check "/var/lib/data/header.html" "/www/cgi-bin/header.html"
move_with_check "/var/lib/data/search" "/www/cgi-bin/search"
move_with_check "/var/lib/data/sou" "/www/cgi-bin/sou"
move_with_check "/var/lib/data/whatsnew" "/www/cgi-bin/whatsnew"

# 解压 tar.gz 文件
cd /var/lib/data
tar -zx -f mobi.tgz

# 删除并移动目录
move_with_check "/var/lib/data/foliate-js" "/www/foliate-js"

rm -rf /etc/nginx/http.d/*

mv /var/lib/data/*.js /etc/nginx/http.d
mv /var/lib/data/emby* /etc/nginx/http.d

# 启动 HTTP 服务器
/bin/busybox-extras httpd -p 81 -h /www

echo "脚本文件执行完成"