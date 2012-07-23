package com.livestrong.myplate.utilities;

import org.json.JSONObject;

import android.util.Log;

import com.sessionm.api.ActivityListener;
import com.sessionm.api.SessionListener;
import com.sessionm.api.SessionM;
import com.sessionm.api.SessionM$USER_SUBSCRIPTION_STATUS;

public class SessionMHelper implements SessionListener, ActivityListener {

	public static final String INTENT_SESSIONM = "INTENT_SESSIONM";

	private static final SessionMHelper instance = new SessionMHelper();
	
	private SessionMHelper() {
	}
	
	public static SessionMHelper getInstance() {
		return instance;
	}
	
	@Override
	public void onSessionFailed(SessionM sessionM, int errorCode) {
		Log.e(getClass().getName(), "SessionM failed. Error code: " + errorCode);
	}

	@Override
	public void onSessionStarted(SessionM sessionM) {
		Log.i(getClass().getName(), "SessionM started");
	}

	@Override
	public void onUserChangedSubscriptionStatus(SessionM sessionM, SessionM$USER_SUBSCRIPTION_STATUS subscrStatus) {
		Log.i(getClass().getName(), "SessionM subscription changed: " + subscrStatus);
	}

	@Override
	public void onCancelled(SessionM sessionM) {
		Log.i(getClass().getName(), "SessionM cancelled");
	}

	@Override
	public void onDismissed(SessionM sessionM) {
		Log.i(getClass().getName(), "SessionM dismissed");
	}

	@Override
	public void onFinishLoading(SessionM sessionM) {
		Log.i(getClass().getName(), "SessionM loaded");
		
	}

	@Override
	public void onPresented(SessionM sessionM) {
		Log.i(getClass().getName(), "SessionM presented");
	}

	@Override
	public void onStartLoading(SessionM sessionM) {
	}

	@Override
	public void onUserInfoChanged(SessionM sessionM, JSONObject json) {
	}

}
