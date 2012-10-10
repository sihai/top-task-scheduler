#!/bin/bash

export JAVA_HOME=/opt/taobao/java1
if [ ! -z "`$JAVA_HOME/bin/java -version 2>&1 | grep JRockit`" ]; then
    JAVA_OPTS="-jrockit -Xms512m -Xmx512m -Xgc:parallel -XX:PermSize=64M -XX:MaxPermSize=256M -Xjvmpi:allocs=off,monitors=off,entr
yexit=off"
else
    JAVA_OPTS="-server -Xms1024m -Xmx1024m -XX:NewSize=320m -XX:MaxNewSize=320m -XX:PermSize=64M -XX:MaxPermSize=256M"
fi;
JAVA_OPTS="${JAVA_OPTS} -Djava.awt.headless=true"
JAVA_OPTS="${JAVA_OPTS} -Dsun.net.client.defaultConnectTimeout=10000"
JAVA_OPTS="${JAVA_OPTS} -Dsun.net.client.defaultReadTimeout=30000"
#JAVA_OPTS="${JAVA_OPTS} -Djava.awt.headless=true -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8787"
JAVA_OPTS="${JAVA_OPTS} -Djava.net.preferIPv4Stack=true"
export JAVA_OPTS
echo ${JAVA_OPTS}

java -jar top-task-schedule-0.0.6-SNAPSHOT.jar -mode ${1}