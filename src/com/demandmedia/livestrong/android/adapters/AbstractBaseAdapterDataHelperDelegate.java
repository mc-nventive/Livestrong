package com.demandmedia.livestrong.android.adapters;

import java.lang.reflect.Method;

import android.app.Activity;
import android.os.Message;
import android.widget.BaseAdapter;

import com.demandmedia.livestrong.android.back.DataHelperDelegate;

public abstract class AbstractBaseAdapterDataHelperDelegate extends BaseAdapter implements DataHelperDelegate {

    // pragma mark - DataHelperDelegate protocol

	private Message errorOccurredResponseMessage = null;
	protected Activity activity;

	@Override
	public void dataReceivedThreaded(final Method methodCalled, final Object data) {
		if (this.activity == null){
			return;
		}
		
		this.activity.runOnUiThread(new Runnable() {
		    public void run() {
		    	dataReceived(methodCalled, data);
		    }
		});
	}

	@Override
	public boolean errorOccurredThreaded(final Method methodCalled, final Exception error, final String errorMessage) {
		if (this.activity == null){
			return false;
		}
		
		this.activity.runOnUiThread(new Runnable() {
		    public void run() {
		    	Message msg = new Message();
		    	try {
			    	msg.obj = errorOccurred(methodCalled, error, errorMessage);
		    	} finally {
		    		AbstractBaseAdapterDataHelperDelegate.this.errorOccurredResponseMessage = msg;
		    	}
		    }
		});
		
		while (errorOccurredResponseMessage == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (this.errorOccurredResponseMessage.obj instanceof Boolean) {
			return (Boolean) this.errorOccurredResponseMessage.obj;
		}
		return false;
	}

	protected void dataReceived(Method methodCalled, Object data) {
	}

	protected boolean errorOccurred(Method methodCalled, Exception error, String message) {
		return false;
	}
}
