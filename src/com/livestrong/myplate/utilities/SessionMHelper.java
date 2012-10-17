package com.livestrong.myplate.utilities;

import org.json.JSONObject;

import android.util.Log;

import com.sessionm.api.ActivityListener;
import com.sessionm.api.SessionListener;
import com.sessionm.api.SessionM;
import com.sessionm.api.SessionM.State;
import com.sessionm.api.User;

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
	public void onDismissed(SessionM sessionM) {
		Log.i(getClass().getName(), "SessionM dismissed");
	}


	@Override
	public void onPresented(SessionM sessionM) {
		Log.i(getClass().getName(), "SessionM presented");
	}

	@Override
	public void onUserUpdated(SessionM arg0, User arg1) {
	}

	@Override
	public void onUnavailable(SessionM arg0) {
		// TODO Auto-generated method stub
		Log.i(getClass().getName(), String.format("Activity unavailable: %1$s", arg0.toString()));
	}

	@Override
	public boolean shouldAutopresentActivity(SessionM arg0) {
		// TODO Auto-generated method stub
		Log.i(getClass().getName(), String.format("Activity shouldAutoPresentActivity: %1$s", arg0.toString()));
		return true;
	}

	@Override
	public void onSessionStateChanged(SessionM arg0, State arg1) {
		// TODO Auto-generated method stub
		Log.i(getClass().getName(), String.format("Session State Changed To: %1$s", arg1.toString()));
	}

}
