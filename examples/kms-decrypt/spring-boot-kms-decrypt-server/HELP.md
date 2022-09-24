# Getting Started

### Guides

1. Maven
```aidl
mvn clean install
```
2. Docker
```shell
docker build ./ -t spring-boot-kms-decrypt-server
```
3. Enclave
```shell
nitro-cli build-enclave --docker-uri spring-boot-kms-decrypt-server:latest --output-file spring-boot-kms-decrypt-server.eif
```
4. Run Encalve
```shell
nitro-cli terminate-enclave --all
nitro-cli run-enclave --eif-path spring-boot-kms-decrypt-server.eif --memory 2048 --cpu-count 2 --enclave-cid 5 --debug-mode
```
5. check the Enclave status
```shell
nitro-cli console --enclave-id $(nitro-cli describe-enclaves | jq -r '.[0].EnclaveID')
```

