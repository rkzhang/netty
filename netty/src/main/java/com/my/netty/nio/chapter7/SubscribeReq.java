package com.my.netty.nio.chapter7;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class SubscribeReq implements Serializable {
	
	private static final long serialVersionUID = 5921435776374874157L;

	@Getter
	@Setter
	private int subReqID;
	
	@Getter
	@Setter
	private String userName;
	
	@Getter
	@Setter
	private String productName;
	
	@Getter
	@Setter
	private String phoneNumber;
	
	@Getter
	@Setter
	private String address;
	
}
