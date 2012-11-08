package com.demandmedia.livestrong.android.back;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.AsyncTask;

@SuppressWarnings("rawtypes")
public class AsyncDataHelper extends AsyncTask {
	
	private DataHelperDelegate delegate;
	private Method methodCalled;
	
	public AsyncDataHelper(DataHelperDelegate delegate, Method methodCalled) {
		this.delegate = delegate;
		this.methodCalled = methodCalled;
	}

	@Override
	protected Object doInBackground(Object... params) {
		Object response = null;
		try {
			response = this.methodCalled.invoke(null, params);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} finally {
			this.delegate.dataReceivedThreaded(methodCalled, response);
		}
		return response;
	}
}
