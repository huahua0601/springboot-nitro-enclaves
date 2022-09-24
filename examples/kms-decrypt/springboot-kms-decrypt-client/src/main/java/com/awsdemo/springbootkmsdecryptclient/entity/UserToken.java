package com.awsdemo.springbootkmsdecryptclient.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserToken {

    private String userId;

    private String encrptedDataKey;

    private String encrptedPrivateKey;

    private String publicKey;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("userid")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @DynamoDbAttribute("encrypted_datakey")
    public String getEncrptedDataKey() {
        return encrptedDataKey;
    }

    public void setEncrptedDataKey(String encrptedDataKey) {
        this.encrptedDataKey = encrptedDataKey;
    }

    @DynamoDbAttribute("encrypted_privatekey")
    public String getEncrptedPrivateKey() {
        return encrptedPrivateKey;
    }

    public void setEncrptedPrivateKey(String encrptedPrivateKey) {
        this.encrptedPrivateKey = encrptedPrivateKey;
    }

    @DynamoDbAttribute("publickey")
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
