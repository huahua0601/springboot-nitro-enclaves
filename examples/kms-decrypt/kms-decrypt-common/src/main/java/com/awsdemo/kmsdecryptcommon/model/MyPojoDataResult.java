package com.awsdemo.kmsdecryptcommon.model;

import lombok.ToString;

import java.io.Serializable;

@ToString
public class MyPojoDataResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
