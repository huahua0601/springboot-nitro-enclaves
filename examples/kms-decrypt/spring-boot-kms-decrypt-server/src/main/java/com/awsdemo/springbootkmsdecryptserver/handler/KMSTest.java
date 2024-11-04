package com.awsdemo.springbootkmsdecryptserver.handler;


import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CommitmentPolicy;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kmssdkv2.AwsKmsMrkAwareMasterKey;
import com.amazonaws.encryptionsdk.kmssdkv2.AwsKmsMrkAwareMasterKeyProvider;
import software.amazon.awssdk.services.kms.KmsClientBuilder;

import java.util.Collections;
import java.util.Map;

public class KMSTest {



    public void test() {

        final AwsCrypto crypto = AwsCrypto.builder()
                .withCommitmentPolicy(CommitmentPolicy.RequireEncryptRequireDecrypt)
                .build();

// Multi-Region keys have a distinctive key ID that begins with 'mrk'
// Specify a multi-Region key in us-east-1
        final String mrkUSEast1 = "arn:aws:kms:us-east-1:111122223333:key/mrk-1234abcd12ab34cd56ef1234567890ab";

// Instantiate an AWS KMS master key provider in strict mode for multi-Region keys
// Configure it to encrypt with the multi-Region key in us-east-1
        final AwsKmsMrkAwareMasterKeyProvider kmsMrkProvider = AwsKmsMrkAwareMasterKeyProvider
                .builder()
                .buildStrict(mrkUSEast1);

// Create an encryption context
        final Map<String, String> encryptionContext = Collections.singletonMap("Purpose", "Test");

// Encrypt your plaintext data
        final CryptoResult<byte[], AwsKmsMrkAwareMasterKey> encryptResult = crypto.encryptData(
                kmsMrkProvider,
                encryptionContext,
                sourcePlaintext);
        byte[] ciphertext = encryptResult.getResult();

    }
}
