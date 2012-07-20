package com.livestrong.myplate.fragment;

import java.lang.reflect.Method;

import android.app.Activity;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.livestrong.myplate.back.DataHelperDelegate;

public class FragmentDataHelperDelegate extends Fragment implements DataHelperDelegate {

    // pragma mark - DataHelperDelegate protocol

	private Message errorOccurredResponseMessage = null;

	@Override
	public void dataReceivedThreaded(final Method methodCalled, final Object data) {
		Activity activity = getActivity();
		if (activity == null){
			return;
		}
		
		activity.runOnUiThread(new Runnable() {
		    public void run() {
		    	dataReceived(methodCalled, data);
		    }
		});
	}

	@Override
	public boolean errorOccurredThreaded(final Method methodCalled, final Exception error, final String errorMessage) {
		Activity activity = getActivity();
		if (activity == null){
			return false;
		}
		
		activity.runOnUiThread(new Runnable() {
		    public void run() {
		    	Message msg = new Message();
		    	try {
			    	msg.obj = errorOccurred(methodCalled, error, errorMessage);
		    	} finally {
		    		FragmentDataHelperDelegate.this.errorOccurredResponseMessage = msg;
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
