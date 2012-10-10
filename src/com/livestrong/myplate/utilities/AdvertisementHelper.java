package com.livestrong.myplate.utilities;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.admarvel.android.ads.AdMarvelInitializeListener;
import com.admarvel.android.ads.AdMarvelUtils.ErrorReason;
import com.admarvel.android.ads.AdMarvelView;
import com.admarvel.android.ads.AdMarvelUtils.SDKAdNetwork;
import com.admarvel.android.ads.AdMarvelView.AdMarvelViewListener;
import com.livestrong.myplate.Constants;
import com.livestrong.myplate.activity.TabBarActivity;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.models.UserProfile;
import com.livestrong.myplatelite.R;

public class AdvertisementHelper {

	protected AdMarvelView adMarvelView;
	protected Activity parentActivity;
	protected Map<String, Object>targetParams;
	
	private Handler refreshHandler;
	
	public AdvertisementHelper() {
		super();
		// TODO init handler here?
	}
	
	public void initWithAdMarvelViewAndActivity(AdMarvelView view, Activity activity) {
		this.adMarvelView = view;
		this.parentActivity = activity;
		
		//in case we need to do something here later on!
//		this.adMarvelView.setListener(new AdMarvelEventHandler());
		
		Map<String, Object> targetParams = new HashMap<String, Object>();

		// Let's provide some basic targeting parameters.
		// See admarvel_android_targeting_params.pdf for more.
		UserProfile profile = DataHelper.getUserProfile(null);
		if (profile != null) {
			if (profile.getGender().equals(UserProfile.Gender.FEMALE))
				targetParams.put("GENDER", "female");
			else if (profile.getGender().equals(UserProfile.Gender.MALE))
				targetParams.put("GENDER", "male");
			int age = profile.getAge();
			if (age > 0)
				targetParams.put("AGE", Integer.toString(age));
		}

		targetParams.put("APP_VERSION", Double.toString(Constants.APPLICATION_VERSION)); // version of your app
		this.targetParams = targetParams;
		this.refreshHandler = new Handler();
	}
	
	public void unloadAdMarvelView() {
		stopAdvertising();
		
		if (this.adMarvelView != null) {
			adMarvelView.destroy();
		}
		
		//is this allowed in java?
		this.parentActivity = null;
	}
	
	public void startAdvertising() {
		if (this.targetParams != null && this.adMarvelView != null && this.parentActivity != null) {
			// The AdMarvel SDK throw a NPE in a different thread, if we try to run
			// it while not having a network connection. Workaround here is to only
			// request ads if there is a connection. However, this will unfortunately
			// also crash, if the connection is not stable.
			if (!isNetworkAvailable(this.parentActivity)) {
				Log.w(AdvertisementHelper.class.getName(), "Network unavailable. Not requesting any ad.");
				return;
			}
			this.adMarvelView.requestNewAd(this.targetParams, Constants.ADMARVEL_PARTNER_ID,Constants.ADMARVEL_SITE_ID, this.parentActivity);
			
			//this is NOT a good idea - just enable auto refresh on client side!
			this.refreshHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					startAdvertising();
				}
				
			}, 30000);
		}
	}
	
	public void stopAdvertising() {
		this.refreshHandler.removeCallbacksAndMessages(null); //null removes all
	}
	
	private static boolean isNetworkAvailable(Activity activity) {
	    ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    if (activeNetworkInfo == null)
	    	return false;
	    return activeNetworkInfo.isConnected();
	}

//	private class AdMarvelEventHandler implements AdMarvelViewListener {
//
//		@Override
//		public void onClickAd(AdMarvelView arg0, String arg1) {
//			// TODO Auto-generated method stub
//			Log.i(AdvertisementHelper.class.getName(), "onClickAd");
//		}
//
//		@Override
//		public void onClose() {
//			// TODO Auto-generated method stub
//			Log.i(AdvertisementHelper.class.getName(), "onClose");
//		}
//
//		@Override
//		public void onExpand() {
//			// TODO Auto-generated method stub
//			Log.i(AdvertisementHelper.class.getName(), "onExpand");
//		}
//
//		@Override
//		public void onFailedToReceiveAd(AdMarvelView arg0, int arg1,
//				ErrorReason arg2) {
//			// TODO Auto-generated method stub
//			Log.i(AdvertisementHelper.class.getName(), "FAILED to receive Ad");
//		}
//
//		@Override
//		public void onReceiveAd(AdMarvelView arg0) {
//			// TODO Auto-generated method stub
//			Log.i(AdvertisementHelper.class.getName(), "onReceiveAd");
//			
//		}
//
//		@Override
//		public void onRequestAd(AdMarvelView arg0) {
//			// TODO Auto-generated method stub
//			Log.i(AdvertisementHelper.class.getName(), "onRequestAd");
//		}
//		
//	}
}
