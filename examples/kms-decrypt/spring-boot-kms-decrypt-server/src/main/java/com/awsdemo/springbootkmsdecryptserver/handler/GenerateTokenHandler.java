package com.awsdemo.springbootkmsdecryptserver.handler;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.GenerateDataKeyRequest;
import com.amazonaws.services.kms.model.GenerateDataKeyResult;
import com.awsdemo.kmsdecryptcommon.Actions;
import com.awsdemo.kmsdecryptcommon.model.GenerateTokenResult;
import com.awsdemo.kmsdecryptcommon.model.MyPojoData;
import com.awsdemo.springbootkmsdecryptserver.utils.AES;
import com.github.mrgatto.enclave.handler.AbstractActionHandler;
import com.github.mrgatto.enclave.kms.KMSClient;
import com.github.mrgatto.model.AWSCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

@Component
@Slf4j
public class GenerateTokenHandler extends AbstractActionHandler<MyPojoData, GenerateTokenResult> {

    @Autowired
    private KMSClient kmsClient;

    @Override
    public boolean canHandle(String action) {
        return Actions.GENERATE_TOKEN.name().equalsIgnoreCase(action);
    }

    @Override
    public GenerateTokenResult handle(MyPojoData data, AWSCredential credential) {
        log.info("receive generate request:{}", data);
        AWSKMS kms = KMSClientProducer.getInstance(credential);
        GenerateTokenResult generateTokenResult = generateTokenWithCLI(data.getKeyId(), kms, credential);
        log.info("generateTokenResult: {}", generateTokenResult);
        return generateTokenResult;
    }

    private GenerateTokenResult generateToken(String keyId, AWSKMS kms, AWSCredential credential) {
        try {
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
            g.initialize(ecSpec, new SecureRandom());
            KeyPair keypair = g.generateKeyPair();
            PublicKey publicKey = keypair.getPublic();
            PrivateKey privateKey = keypair.getPrivate();
            String pub = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            GenerateDataKeyRequest generateDataKeyRequest = new GenerateDataKeyRequest();
            generateDataKeyRequest.withKeyId(keyId);
            generateDataKeyRequest.setNumberOfBytes(32);
            GenerateDataKeyResult generateDataKeyResult = kms.generateDataKey(generateDataKeyRequest);
            String plain_datakey = Base64.getEncoder().encodeToString(generateDataKeyResult.getPlaintext().array());
            String encrptedDataKey = Base64.getEncoder().encodeToString(generateDataKeyResult.getCiphertextBlob().array());
            log.info("plain_datakey : {} , decrpt data key: {}", plain_datakey, kmsClient.decrypt(credential, encrptedDataKey));
            log.info("generate data key:{}", kmsClient.generateDataKey(credential, keyId));
            String encodedPrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            log.info("generate data private key: {}", encodedPrivateKey);
            String encrptedPrivateKey = AES.encrypt(encodedPrivateKey, plain_datakey);
            return GenerateTokenResult.builder()
                    .encrptedDataKey(encrptedDataKey)
                    .encrptedPrivateKey(encrptedPrivateKey)
                    .publicKey(pub)
                    .build();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            log.error("generate token failure", e);
        }
        return null;
    }

    private GenerateTokenResult generateTokenWithCLI(String keyId, AWSKMS kms, AWSCredential credential) {
        try {

            String randPropOut = kmsClient.generateRandom(credential, 24);
            String randomString = getRandomString(randPropOut);
            log.info("RANDOM String: {}", randomString);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
            g.initialize(ecSpec, new SecureRandom(randomString.getBytes(StandardCharsets.UTF_8)));
            KeyPair keypair = g.generateKeyPair();
            PublicKey publicKey = keypair.getPublic();
            PrivateKey privateKey = keypair.getPrivate();
            String pub = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String propOut = kmsClient.generateDataKey(credential, keyId);
            String plain_datakey = getPlainDataKey(propOut);
            String encrptedDataKey = getEncryptedDataKey(propOut);
            String encodedPrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            log.info("plain_datakey: {} || generate data private key: {}", plain_datakey, encodedPrivateKey);
            String encrptedPrivateKey = AES.encrypt(encodedPrivateKey, plain_datakey);
            return GenerateTokenResult.builder()
                    .encrptedDataKey(encrptedDataKey)
                    .encrptedPrivateKey(encrptedPrivateKey)
                    .publicKey(pub)
                    .build();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            log.error("generate token failure", e);
        }
        return null;
    }

    private String getPlainDataKey(String propOut) {
        String[] propLines = propOut.split("[\\r\\n]+");
        return propLines[1].replace("PLAINTEXT: ", "").trim();
    }

    private String getEncryptedDataKey(String propOut) {
        String[] propLines = propOut.split("[\\r\\n]+");
        return propLines[0].replace("CIPHERTEXT: ", "").trim();
    }

    private String getRandomString(String propOut) {
        String[] propLines = propOut.split("[\\r\\n]+");
        return propLines[0].replace("RANDOM:", "").trim();
    }
}
