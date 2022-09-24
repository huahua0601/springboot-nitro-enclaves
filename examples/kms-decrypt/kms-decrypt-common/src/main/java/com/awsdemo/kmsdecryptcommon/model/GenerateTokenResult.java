package com.awsdemo.kmsdecryptcommon.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenerateTokenResult {

    private String encrptedDataKey;

    private String encrptedPrivateKey;

    private String publicKey;

}
