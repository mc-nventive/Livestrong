package com.livestrong.myplate.back.api;

import org.springframework.http.HttpAuthentication;

public class OAuth2Authentication extends HttpAuthentication {

	private String accessToken;
	
	public OAuth2Authentication(String accessToken) {
		this.accessToken = accessToken;
	}
	
	@Override
	public String getHeaderValue() {
		return "Bearer " + accessToken;
	}

}
