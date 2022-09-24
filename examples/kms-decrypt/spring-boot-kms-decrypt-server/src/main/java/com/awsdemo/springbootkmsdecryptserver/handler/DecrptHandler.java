package com.awsdemo.springbootkmsdecryptserver.handler;

import com.alibaba.fastjson2.JSONObject;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.util.Base64;
import com.awsdemo.kmsdecryptcommon.Actions;
import com.awsdemo.kmsdecryptcommon.model.MyPojoData;
import com.awsdemo.kmsdecryptcommon.model.MyPojoDataResult;
import com.awsdemo.springbootkmsdecryptserver.kmssinger.KMSClientSigner;
import com.github.mrgatto.enclave.handler.AbstractActionHandler;
import com.github.mrgatto.model.AWSCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class DecrptHandler extends AbstractActionHandler<MyPojoData, MyPojoDataResult> {

	@Override
	public boolean canHandle(String action) {
		return Actions.DECRYPT.name().equalsIgnoreCase(action);
	}

	@Override
	public MyPojoDataResult handle(MyPojoData data, AWSCredential credential) {
		log.info("receive decrpt data: {}", data);
		MyPojoDataResult result = new MyPojoDataResult();
		//AWSKMS kms = KMSClientProducer.getInstance(credential);
		//String encrptData = encrptData(data.getValue(), data.getKeyId(), kms);
		//String decrptData = decryptData(encrptData, data.getKeyId(), kms);

		String encryptData = KMSClientSigner.encrypt(credential, data.getKeyId(), data.getValue());
		JSONObject encryptJO =  JSONObject.parseObject(encryptData);
		String decrptData = KMSClientSigner.decrypt(credential, encryptJO.getString("CiphertextBlob"));
		JSONObject decryptJO = JSONObject.parseObject(decrptData);
		result.setValue("encrptData:" + data.getValue() + "||decrptData:||" + decryptJO.getString("Plaintext"));
		return result;
	}

	private String encrptData(String data, String keyId, AWSKMS kms) {
		ByteBuffer plaintext = ByteBuffer.wrap(data.getBytes());
		EncryptRequest req = new EncryptRequest().withKeyId(keyId).withPlaintext(plaintext);
		ByteBuffer ciphertext = kms.encrypt(req).getCiphertextBlob();
		byte[] base64EncodedValue = Base64.encode(ciphertext.array());
		return new String(base64EncodedValue, StandardCharsets.UTF_8);
	}

	private String decryptData(String data, String keyId, AWSKMS kms) {

		byte[] base64EncodedValue = Base64.decode(data);
		ByteBuffer plaintext = ByteBuffer.wrap(base64EncodedValue);
		DecryptRequest dereq = new DecryptRequest().withCiphertextBlob(plaintext);
		DecryptResult de = kms.decrypt(dereq);
		return StandardCharsets.UTF_8.decode(de.getPlaintext()).toString();
	}
}
