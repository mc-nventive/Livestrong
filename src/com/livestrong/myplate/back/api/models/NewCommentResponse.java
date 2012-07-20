package com.livestrong.myplate.back.api.models;

import java.io.Serializable;

public class NewCommentResponse implements LiveStrongApiObject, Serializable {
	private static final long serialVersionUID = 4064042841486602995L;

	public static final String SUCCESS = "success";

	private String status;

	private int commentId;

	public String getStatus() {
		return status;
	}

	public int getCommentId() {
		return commentId;
	}
	
}
