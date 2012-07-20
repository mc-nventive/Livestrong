package com.livestrong.myplate.back.api.models;

public class OAuth2TokenResponse implements LiveStrongApiObject {

	private String accessToken;

	private String refreshToken;

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

}
