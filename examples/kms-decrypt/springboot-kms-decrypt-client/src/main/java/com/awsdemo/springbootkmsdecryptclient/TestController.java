package com.awsdemo.springbootkmsdecryptclient;

import com.alibaba.fastjson2.JSONObject;
import com.awsdemo.kmsdecryptcommon.Actions;
import com.awsdemo.kmsdecryptcommon.model.*;
import com.awsdemo.springbootkmsdecryptclient.entity.UserToken;
import com.awsdemo.springbootkmsdecryptclient.repository.UserTokenRepository;
import com.github.mrgatto.host.NitroEnclaveClient;
import com.github.mrgatto.model.AWSCredential;
import com.github.mrgatto.model.EnclaveRequest;
import com.github.mrgatto.model.EnclaveResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.regions.Region;

@RestController
@Slf4j
public class TestController {

    private NitroEnclaveClient nitroEnclaveClient;

    private RestTemplate restTemplate;

    private UserTokenRepository userTokenRepository;

    public TestController(RestTemplateBuilder restTemplateBuilder,
                          NitroEnclaveClient nitroEnclaveClient,
                          UserTokenRepository userTokenRepository) {
        this.restTemplate = restTemplateBuilder.build();
        this.nitroEnclaveClient = nitroEnclaveClient;
        this.userTokenRepository = userTokenRepository;
    }

    @GetMapping("test")
    public String test(@RequestParam String keyId) {
        MyPojoData pojo = new MyPojoData();
        pojo.setValue(String.valueOf(System.currentTimeMillis()));
        pojo.setKeyId(keyId);

        EnclaveRequest<MyPojoData> request = new EnclaveRequest<>();
        request.setAction(Actions.DECRYPT.name());
        request.setData(pojo);
        request.setAwsCredentials(getAwsCredential());
        EnclaveResponse<MyPojoDataResult> response = nitroEnclaveClient.send(request);
        return response.getData().getValue();
    }

    @GetMapping("echo")
    public String echo() {
        MyPojoData pojo = new MyPojoData();
        pojo.setValue(String.valueOf(System.currentTimeMillis()));
        EnclaveRequest<MyPojoData> request = new EnclaveRequest<>();
        request.setAction(Actions.ECHO.name());
        request.setData(pojo);
        EnclaveResponse<MyPojoDataResult> response = nitroEnclaveClient.send(request);
        return response.getData().getValue();
    }


    @GetMapping("testsign")
    public SignVerifyResult testsign(@RequestParam String keyId, @RequestParam String userId, @RequestParam String rawMessage) {
        UserToken ut = userTokenRepository.getUserToken(userId);
        SignVerifyInput signVerifyInput = SignVerifyInput.builder().rawMessage(rawMessage).encrptedDataKey(ut.getEncrptedDataKey())
                .publicKey(ut.getPublicKey()).encrptedPrivateKey(ut.getEncrptedPrivateKey()).build();
        EnclaveRequest<SignVerifyInput> request = new EnclaveRequest<>();
        request.setAction(Actions.SIGN_VERIFY.name());
        request.setData(signVerifyInput);
        request.setAwsCredentials(getAwsCredential());
        EnclaveResponse<SignVerifyResult> response = nitroEnclaveClient.send(request);
        return response.getData();
    }

    @GetMapping("testgen")
    public UserToken generateToken(@RequestParam String keyId, @RequestParam String userId) {
        MyPojoData pojo = new MyPojoData();
        pojo.setValue(String.valueOf(System.currentTimeMillis()));
        pojo.setKeyId(keyId);
        EnclaveRequest<MyPojoData> request = new EnclaveRequest<>();
        request.setAction(Actions.GENERATE_TOKEN.name());
        request.setData(pojo);
        request.setAwsCredentials(getAwsCredential());
        EnclaveResponse<GenerateTokenResult> response = nitroEnclaveClient.send(request);
        GenerateTokenResult data = response.getData();
        UserToken ut =
                UserToken.builder().userId(userId).encrptedDataKey(data.getEncrptedDataKey())
                        .encrptedPrivateKey(data.getEncrptedPrivateKey()).publicKey(data.getPublicKey()).build();
        log.info("UserToken saveing : {}", ut);
        userTokenRepository.save(ut);

        return userTokenRepository.getUserToken(userId);
    }

    private AWSCredential getAwsCredential() {
        String instanceProfileName = restTemplate.getForObject("http://169.254.169.254/latest/meta-data/iam/security-credentials/", String.class);
        String credentials = restTemplate.getForObject("http://169.254.169.254/latest/meta-data/iam/security-credentials/" + instanceProfileName, String.class);
        JSONObject credentialMap = JSONObject.parse(credentials);
        System.out.println(credentialMap);
        AWSCredential credential = new AWSCredential();
        credential.setAccessKeyId(credentialMap.getString("AccessKeyId"));
        credential.setSecretAccessKey(credentialMap.getString("SecretAccessKey"));
        credential.setSessionToken(credentialMap.getString("Token"));
        credential.setRegion(Region.US_EAST_1.toString());
        return credential;
    }

    @GetMapping("/testddb")
    public UserToken testDDB() {
        UserToken ut =
                UserToken.builder().userId("u1").encrptedDataKey("dk1").encrptedPrivateKey("pk1").publicKey("pubk").build();
        userTokenRepository.save(ut);
        return userTokenRepository.getUserToken("u1");
    }

}
