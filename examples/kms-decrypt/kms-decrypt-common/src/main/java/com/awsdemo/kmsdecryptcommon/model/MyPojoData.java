package com.awsdemo.kmsdecryptcommon.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Getter
@Setter
public class MyPojoData implements Serializable {

	private static final long serialVersionUID = 1L;

	private String keyId;

	private String value;
}
