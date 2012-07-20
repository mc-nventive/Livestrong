package com.livestrong.myplate.back.api.models;

import java.io.Serializable;

public class NewMessageResponse implements LiveStrongApiObject, Serializable {
	private static final long serialVersionUID = 6239479120623325831L;

	public static final String SUCCESS = "success";

	private String status;

	private int postId;

	public String getStatus() {
		return status;
	}

	public int getMessageId() {
		return postId;
	}
	
}
