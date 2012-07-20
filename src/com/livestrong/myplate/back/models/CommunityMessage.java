package com.livestrong.myplate.back.models;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import org.ocpsoft.pretty.time.PrettyTime;

import com.livestrong.myplate.back.api.ISO8601DateParser;
import com.livestrong.myplate.back.api.models.AbstractLiveStrongApiObject;

public class CommunityMessage extends AbstractLiveStrongApiObject implements Serializable {
	private static final long serialVersionUID = -565112084313093815L;

	private int darePostId;
	private String post;
	private Date dateCreated;
	private Date dateUpdated;
	private int comments;
	private String image;
	private CommunityUser user;

	public void setDateCreated(String string) {
	    try {
			this.dateCreated = ISO8601DateParser.parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void setDateUpdated(String string) {
	    try {
			this.dateUpdated = ISO8601DateParser.parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void setImage(String string) {
		if (string == null || string.trim().length() == 0) {
			return;
		}
		this.image = string;
	}
	
	public int getPostId() {
		return darePostId;
	}
	public String getPost() {
		return post;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public Date getDateUpdated() {
		return dateUpdated;
	}
	public int getComments() {
		return comments;
	}
	public String getImage() {
		return image;
	}
	public CommunityUser getUser() {
		return user;
	}
	
	public String getPrettyDate(){
		return CommunityMessage.getPrettyDate(this.dateUpdated);
	}
	
	public static String getPrettyDate(Date date){
		PrettyTime prettyTime = new PrettyTime();
		return prettyTime.format(date);
	}
	
	public void setComments(int commentsCount){
		this.comments = commentsCount;
	}
}
