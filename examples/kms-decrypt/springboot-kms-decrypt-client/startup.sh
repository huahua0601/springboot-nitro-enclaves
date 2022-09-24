#!/bin/sh

client_id=`ps -ef | grep springboot-kms-decrypt-client | grep -v "grep" | awk '{print $2}'`
echo $client_id
for id in $client_id
do
    kill -9 $id
    echo "killed $id"
done

java -jar ./target/springboot-kms-decrypt-client-1.0.0-SNAPSHOT.jar > client.log &