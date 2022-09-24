package com.awsdemo.springbootkmsdecryptserver.handler;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.awsdemo.kmsdecryptcommon.Actions;
import com.awsdemo.kmsdecryptcommon.model.SignVerifyInput;
import com.awsdemo.kmsdecryptcommon.model.SignVerifyResult;
import com.awsdemo.springbootkmsdecryptserver.utils.AES;
import com.github.mrgatto.enclave.handler.AbstractActionHandler;
import com.github.mrgatto.enclave.kms.KMSClient;
import com.github.mrgatto.model.AWSCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@Slf4j
public class SignVerifyHandler extends AbstractActionHandler<SignVerifyInput, SignVerifyResult> {

    @Autowired
    private KMSClient kmsClient;

    @Override
    public boolean canHandle(String action) {
        return Actions.SIGN_VERIFY.name().equalsIgnoreCase(action);
    }

    @Override
    public SignVerifyResult handle(SignVerifyInput data, AWSCredential credential) {
        AWSKMS kms = KMSClientProducer.getInstance(credential);
        return signDataWithCLI(data, credential);
    }

    private SignVerifyResult signDataWithCLI(SignVerifyInput input, AWSCredential credential) {
        try {
            String propOut = kmsClient.decrypt(credential, input.getEncrptedDataKey());
            String plainDataKey = getPlainDataKey(propOut);
            log.info("plain data key:{},privatekey:{}", plainDataKey.length(), input.getEncrptedPrivateKey());
            String privateKey = AES.decrypt(input.getEncrptedPrivateKey(), plainDataKey.trim());
            log.info("sign data private key: {}", privateKey);
            PKCS8EncodedKeySpec formatted_private = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
            KeyFactory kf = KeyFactory.getInstance("EC");
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initSign(kf.generatePrivate(formatted_private));
            ecdsaSign.update(input.getRawMessage().getBytes(StandardCharsets.UTF_8));
            String signedData = Base64.getEncoder().encodeToString(ecdsaSign.sign());
            log.info("Signed Data: {}", signedData);
            boolean verified = verify(signedData, input);

            return SignVerifyResult.builder().signedMessage(signedData).verify(verified).build();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        return null;
    }

    private SignVerifyResult signData(SignVerifyInput input, AWSKMS kms) {
        try {
            DecryptRequest dereq = new DecryptRequest().withCiphertextBlob(
                    ByteBuffer.wrap(Base64.getDecoder().decode(input.getEncrptedDataKey())));
            DecryptResult de = kms.decrypt(dereq);
            String privateKey = AES.decrypt(input.getEncrptedPrivateKey(), Base64.getEncoder().encodeToString(de.getPlaintext().array()));
            log.info("sign data private key: {}", privateKey);
            PKCS8EncodedKeySpec formatted_private = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
            KeyFactory kf = KeyFactory.getInstance("EC");
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initSign(kf.generatePrivate(formatted_private));
            ecdsaSign.update(input.getRawMessage().getBytes(StandardCharsets.UTF_8));
            String signedData = Base64.getEncoder().encodeToString(ecdsaSign.sign());
            log.info("Signed Data: {}", signedData);

            boolean verified = verify(signedData, input);

            return SignVerifyResult.builder().signedMessage(signedData).verify(verified).build();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean verify(String signedMessage, SignVerifyInput input) {
        Signature ecdsaVerify = null;
        try {
            ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            KeyFactory kf = KeyFactory.getInstance("EC");

            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(input.getPublicKey()));

            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(input.getRawMessage().getBytes(StandardCharsets.UTF_8));
            return ecdsaVerify.verify(Base64.getDecoder().decode(signedMessage));

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getPlainDataKey(String propOut) {
        return propOut.replace("PLAINTEXT:", "").trim();
    }

}
