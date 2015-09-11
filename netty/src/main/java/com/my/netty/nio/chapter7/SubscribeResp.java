package com.my.netty.nio.chapter7;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class SubscribeResp implements Serializable {

	private static final long serialVersionUID = 4528876253575429760L;

	@Getter
	@Setter
	private int subReqID;
	
	@Getter
	@Setter
	private int respCode;
	
	@Getter
	@Setter
	private String desc;
	
}
