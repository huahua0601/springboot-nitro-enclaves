package com.github.mrgatto.enclave.kms;

import com.github.mrgatto.model.AWSCredential;

public interface KMSClient {

	String decrypt(AWSCredential credential, String content);

	String generateDataKey(AWSCredential credential, String keyId);

	String generateRandom(AWSCredential credential, int numberOfBytes);
}
