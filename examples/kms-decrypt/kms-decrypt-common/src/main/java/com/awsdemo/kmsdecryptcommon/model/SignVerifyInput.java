package com.awsdemo.kmsdecryptcommon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignVerifyInput {

    private String encrptedDataKey;

    private String encrptedPrivateKey;

    private String publicKey;

    private String rawMessage;
}
