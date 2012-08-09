package com.livestrong.myplate.utilities;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.admarvel.android.ads.AdMarvelView;
import com.livestrong.myplate.Constants;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.models.UserProfile;

public class AdvertisementHelper {

	public static void requestAd(AdMarvelView view, Activity activity) {
		// The AdMarvel SDK throw a NPE in a different thread, if we try to run
		// it while not having a network connection. Workaround here is to only
		// request ads if there is a connection. However, this will unfortunately
		// also crash, if the connection is not stable.
		if (!isNetworkAvailable(activity)) {
			Log.w(AdvertisementHelper.class.getName(), "Network unavailable. Not requesting any ad.");
			return;
		}
		
		
		HashMap<String, Object> targetParams = new HashMap<String, Object>();

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
		view.requestNewAd(targetParams, Constants.ADMARVEL_PARTNER_ID, Constants.ADMARVEL_SITE_ID, activity);
	}
	
	private static boolean isNetworkAvailable(Activity activity) {
	    ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    if (activeNetworkInfo == null)
	    	return false;
	    return activeNetworkInfo.isConnected();
	}


}
