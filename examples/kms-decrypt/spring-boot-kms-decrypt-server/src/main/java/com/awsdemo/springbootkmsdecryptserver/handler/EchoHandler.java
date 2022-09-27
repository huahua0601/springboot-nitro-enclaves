package com.awsdemo.springbootkmsdecryptserver.handler;

import com.awsdemo.kmsdecryptcommon.Actions;
import com.awsdemo.kmsdecryptcommon.model.MyPojoData;
import com.awsdemo.kmsdecryptcommon.model.MyPojoDataResult;
import com.github.mrgatto.enclave.handler.AbstractActionHandler;
import com.github.mrgatto.enclave.nsm.NsmClient;
import com.github.mrgatto.model.AWSCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EchoHandler extends AbstractActionHandler<MyPojoData, MyPojoDataResult> {

	@Autowired
	private NsmClient nsmClient;

	@Override
	public boolean canHandle(String action) {
		return Actions.ECHO.name().equalsIgnoreCase(action);
	}

	@Override
	public MyPojoDataResult handle(MyPojoData data, AWSCredential credential) {
		String nsmModuleId = this.nsmClient.describeNsm().getModuleId();

		MyPojoDataResult result = new MyPojoDataResult();
		result.setValue("Echo from Enclave " + nsmModuleId + ": " + data.getValue());

		return result;
	}

}
