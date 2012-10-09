package com.demandmedia.livestrong.android.back.api.models;

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
