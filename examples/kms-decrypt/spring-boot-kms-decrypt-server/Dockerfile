FROM amazoncorretto:11

WORKDIR /app

COPY target/spring-boot-kms-decrypt-server-1.0.0-SNAPSHOT.jar ./app.jar

COPY libs/libvsockj-native-1.0-SNAPSHOT.so /usr/lib/
COPY libs/nsm-cli ./nsm-cli
COPY libs/kmstool_enclave_cli ./kmstool_enclave_cli
COPY libs/libnsm.so /usr/lib/

COPY scripts/traffic-forwarder.py ./
COPY scripts/run.sh ./
RUN yum -y install net-tools && yum -y install rng-tools && yum -y install python3 &&yum -y clean all  && rm -rf /var/cache
RUN chmod +x run.sh
CMD ["/app/run.sh"]