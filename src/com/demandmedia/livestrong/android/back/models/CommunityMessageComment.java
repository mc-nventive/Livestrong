package com.demandmedia.livestrong.android.back.models;

import java.text.ParseException;
import java.util.Date;

import com.demandmedia.livestrong.android.back.api.ISO8601DateParser;
import com.demandmedia.livestrong.android.back.api.models.AbstractLiveStrongApiObject;

public class CommunityMessageComment extends AbstractLiveStrongApiObject {
	private int dareCommentId;
	
	private String post;

	private Date dateCreated;
	
	private CommunityUser user;
	
	private int anonymous;

	public CommunityMessageComment(){
		
	}
	
	public void setDateCreated(String string) {
	    try {
			this.dateCreated = ISO8601DateParser.parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return "[" + getDateCreated() + "] " + getUser().getUsername() + ": " + getPost();
	}

	public int getCommentId() {
		return dareCommentId;
	}
	
	public String getPost() {
		return post;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}

	public CommunityUser getUser() {
		if (this.anonymous == 1) {
			return new CommunityUser("Anonymous");
		}
		return user;
	}
	
	public String getPrettyDate() {
		if (this.dateCreated == null){
			return "";
		}
		return CommunityMessage.getPrettyDate(this.dateCreated);
	}
}
