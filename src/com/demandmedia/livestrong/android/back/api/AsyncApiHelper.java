package com.demandmedia.livestrong.android.back.api;

import java.lang.reflect.Method;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import android.os.AsyncTask;

import com.demandmedia.livestrong.android.back.DataHelperDelegate;
import com.demandmedia.livestrong.android.back.api.ApiHelper.AuthUsing;

@SuppressWarnings("rawtypes")
public class AsyncApiHelper<T> extends AsyncTask {

	public boolean done = false;
	public ApiHelperResponse response = null;
	
	public AsyncApiHelper() {
	}

	@Override
	protected Object doInBackground(Object... params) {
		HttpMethod httpMethod = (HttpMethod) params[0];
		String url = (String) params[1];
		Class<?> responseObjectClass = (Class<?>) params[2];
		Object body = params[3];
		MediaType contentType = (MediaType) params[4];
		boolean retry = (Boolean) params[5];
		Method methodCalled = (Method) params[6];
		DataHelperDelegate delegate = (DataHelperDelegate) params[7];
		AuthUsing authUsing = (AuthUsing) params[8];
		
		this.response = ApiHelper.requestRestSynchronous(httpMethod, url, responseObjectClass, body, contentType, retry, methodCalled, delegate, authUsing);
		this.done = true;

		return this.response;
	}
}
