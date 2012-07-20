package com.livestrong.myplate.back.models;

import java.io.Serializable;
import java.util.Map;

import com.livestrong.myplate.back.api.models.AbstractLiveStrongApiObject;

public class CommunityUser extends AbstractLiveStrongApiObject implements Serializable {
	private static final long serialVersionUID = -2955074001927556054L;

	private int userId;
	private String username;
	private String profile;
	private Map<String, String> avatars;

	// For ORMlite
	public CommunityUser() {
	}

	public CommunityUser(String username) {
		this.username = username;
		this.userId = 0;
	}

	public int getUserId() {
		return this.userId;
	}

	public String getUsername() {
		return this.username;
	}

	public String getProfile() {
		return this.profile;
	}

	public String getAvatar() {
		if (this.avatars != null) {
			return this.avatars.get("medium");
		} else {
			return "";
		}
	}
}
