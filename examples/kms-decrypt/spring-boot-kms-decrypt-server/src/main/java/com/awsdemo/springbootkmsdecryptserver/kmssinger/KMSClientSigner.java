package com.awsdemo.springbootkmsdecryptserver.kmssinger;

import com.alibaba.fastjson2.JSONObject;
import com.amazonaws.auth.*;
import com.github.mrgatto.model.AWSCredential;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @Author seanguo@amazon.com
 */
@Slf4j
public class KMSClientSigner {

    /**
     * refer to https://docs.aws.amazon.com/zh_cn/kms/latest/APIReference/Welcome.html
     */
    private final static String API_GENERATE_RANDOM = "TrentService.GenerateRandom";
    private final static String API_GENERATE_DATAKEY = "TrentService.GenerateDataKey";
    private final static String API_ENCRYPT = "TrentService.Encrypt";
    private final static String API_DECRYPT = "TrentService.Decrypt";

    public static String generateRandom(AWSCredential credential, int numberOfBytes) {
        String payload = "{\"NumberOfBytes\":" + numberOfBytes + "}";
        return callKMS(credential, API_GENERATE_RANDOM, payload);
    }

    public static String encrypt(AWSCredential credential, String keyId, String plainText) {
        JSONObject payload = new JSONObject();
        payload.put("Plaintext", Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8)));
        payload.put("KeyId", keyId);
        return callKMS(credential, API_ENCRYPT, payload.toJSONString());
    }

    public static String decrypt(AWSCredential credential, String encryptedData) {
        JSONObject payload = new JSONObject();
        payload.put("CiphertextBlob", encryptedData);
        return callKMS(credential, API_DECRYPT, payload.toJSONString());
    }

    private static String callKMS(AWSCredential credential, String action, String payload) {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName("kms");
        signer.setRegionName("us-east-1");
        BasicSessionCredentials basicAWSCredentials = new BasicSessionCredentials(credential.getAccessKeyId(), credential.getSecretAccessKey(), credential.getSessionToken());
        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return basicAWSCredentials;
            }
            @Override
            public void refresh() {
            }
        };
        CloseableHttpResponse response = null;
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        AWSRequestSigningApacheInterceptor apacheInterceptor = new AWSRequestSigningApacheInterceptor("kms", signer, awsCredentialsProvider);
        httpClientBuilder.addInterceptorLast(apacheInterceptor);

        try (CloseableHttpClient httpClient = httpClientBuilder.build()) {
            HttpPost httpPost = new HttpPost("https://kms.us-east-1.amazonaws.com");
            httpPost.addHeader("X-Amz-Target", action);
            StringEntity stringEntity = new StringEntity(payload);
            httpPost.addHeader("Content-Type", "application/x-amz-json-1.1");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                log.info("call kms result: {}", result);
                return result;
            }
        } catch (IOException e) {
            log.error("call kms error", e);
        }
        return null;
    }

}
