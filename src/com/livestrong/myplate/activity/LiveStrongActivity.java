package com.livestrong.myplate.activity;

import java.lang.reflect.Method;
import java.util.TreeMap;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.livestrong.myplate.Constants;
import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.DataHelperDelegate;
import com.livestrong.myplate.back.db.DatabaseHelper;

public class LiveStrongActivity extends OrmLiteBaseActivity<DatabaseHelper> implements DataHelperDelegate {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DataHelper.setDatabaseHelper(getHelper());
		Log.d(this.getClass().getName(), "Setting database helper");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Make sure nobody tries to use the DatabaseHelper without extending one of the AbstractLiveStrong*Activity classes.
		Log.d(this.getClass().getName(), "Releasing database helper");
		DataHelper.setDatabaseHelper(null);
	}

	@Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        
        // We track running activities like this, to be able to detect when the app gets backgrounded.
        MyPlateApplication app = (MyPlateApplication) getApplication();
        app.plusActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused"); commit unsaved changes to persistent data, etc.
        // -> onStop()

        // We track running activities like this, to be able to detect when the app gets backgrounded.
        MyPlateApplication app = (MyPlateApplication) getApplication();
        app.minusActivity();
    }
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	
    	FlurryAgent.onStartSession(this, Constants.Flurry.LITE_VERSION_API_KEY);
    }
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	
    	FlurryAgent.onEndSession(this);
    }
    
    // pragma mark - DataHelperDelegate protocol

	private Message errorOccurredResponseMessage = null;

	@Override
	public void dataReceivedThreaded(final Method methodCalled, final Object data) {
		Runnable r = new Runnable() {
		    public void run() {
		    	dataReceived(methodCalled, data);
		    }
		};
		runOnUiThread(r);
	}

	@Override
	public boolean errorOccurredThreaded(final Method methodCalled, final Exception error, final String errorMessage) {
		runOnUiThread(new Runnable() {
		    public void run() {
		    	Message msg = new Message();
		    	try {
			    	msg.obj = errorOccurred(methodCalled, error, errorMessage);
		    	} finally {
		    		LiveStrongActivity.this.errorOccurredResponseMessage = msg;
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

	protected boolean errorOccurred(Method methodCalled, Exception error, final String errorMessage) 
	{
		FlurryAgent.logEvent(Constants.Flurry.SERVER_ERROR_ON_SYNC_EVENT, new TreeMap<String, String>()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = -612146922218353300L;

			{
				put("message", errorMessage);
			}
		});
				
		return false;
	}
}
