# 基础镜像
FROM 192.168.2.29:8443/proxy_cache/library/mysql:5.7

# 执行sql脚本
ADD ./db/*.sql /docker-entrypoint-initdb.d/
