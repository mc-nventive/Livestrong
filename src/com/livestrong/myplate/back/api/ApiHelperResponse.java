package com.livestrong.myplate.back.api;

public class ApiHelperResponse<T> {
	public T response;
	public boolean error;
	public Exception exception;
	public String errorMessage;
	
	public ApiHelperResponse(T response) {
		this.error = false;
		this.response = response;
	}

	public ApiHelperResponse(Exception exception, String errorMessage) {
		this.error = true;
		this.exception = exception;
		if (errorMessage != null) {
			this.errorMessage = errorMessage;
		} else if (exception != null) {
			this.errorMessage = exception.getLocalizedMessage();
		}
	}
}
