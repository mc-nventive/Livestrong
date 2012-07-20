package com.livestrong.myplate.back.api.models;


public class AbstractSearchResponse implements LiveStrongApiObject {
	private String query;
	private int start;
	private int limit;
	private int found;
	
	public AbstractSearchResponse() {
	}

	public AbstractSearchResponse(String query, int start, int limit, int found) {
		this.query = query;
		this.start = start;
		this.limit = limit;
		this.found = found;
	}

	public String getQuery() {
		return query;
	}
	public int getStart() {
		return start;
	}
	public int getLimit() {
		return limit;
	}
	public int getFound() {
		return found;
	}
}
