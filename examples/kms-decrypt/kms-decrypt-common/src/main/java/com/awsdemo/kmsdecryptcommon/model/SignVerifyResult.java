package com.awsdemo.kmsdecryptcommon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignVerifyResult {

    private String signedMessage;

    private boolean verify;
}
