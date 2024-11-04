package com.awsdemo.springbootkmsdecryptserver.handler;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.github.mrgatto.model.AWSCredential;

public class KMSClientProducer {

    private volatile static AWSKMS kmsClient;

    private KMSClientProducer() {
    }

    public static AWSKMS getInstance(AWSCredential credentials) {
        if (kmsClient == null) {
            synchronized (KMSClientProducer.class) {
                if (kmsClient == null) {
                    kmsClient = AWSKMSClientBuilder.standard().withRegion(Regions.US_EAST_1)
                            .withCredentials(
                                    new AWSStaticCredentialsProvider(new BasicSessionCredentials(credentials.getAccessKeyId(),credentials.getSecretAccessKey(),credentials.getSessionToken()))).build();
                }
            }


        }
        return kmsClient;
    }
}
