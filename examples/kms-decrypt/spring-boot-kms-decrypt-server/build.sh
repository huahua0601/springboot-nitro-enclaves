#!/bin/bash
FILE=spring-boot-kms-decrypt-server.eif
if [ -f "$FILE" ]; then
    rm $FILE
fi

RunningEnclave=$(nitro-cli describe-enclaves | jq -r ".[0].EnclaveID")
if [ -n "$RunningEnclave" ]; then
	nitro-cli terminate-enclave --enclave-id $(nitro-cli describe-enclaves | jq -r ".[0].EnclaveID");
fi

#docker rmi -f $(docker images -a -q)
docker rmi spring-boot-kms-decrypt-server:latest
pkill vsock-proxy


docker build -t spring-boot-kms-decrypt-server:latest .
nitro-cli build-enclave --docker-uri spring-boot-kms-decrypt-server:latest  --output-file spring-boot-kms-decrypt-server.eif > EnclaveImage.log

vsock-proxy 8000 kms.us-east-1.amazonaws.com 443 &

nitro-cli run-enclave --cpu-count 4 --memory 4096 --enclave-cid 5 --eif-path spring-boot-kms-decrypt-server.eif --debug-mode
# nitro-cli console --enclave-id $(nitro-cli describe-enclaves | jq -r ".[0].EnclaveID")