package com.livestrong.myplate.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.livestrong.myplate.R;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.models.UserProfile;
import com.livestrong.myplate.fragment.MoreProfileFragment;

public class ProfileActivity extends LiveStrongFragmentActivity {
		
	private class UserProfileTask  extends AsyncTask<Void, Void, UserProfile>
	{		
		@Override
		protected UserProfile doInBackground(Void... params) 
		{
			return DataHelper.getUserProfile(null);
		}
		
		protected void onPostExecute(UserProfile profile) 
		{
			// load profile fragment
	        FragmentManager fragmentManager = getSupportFragmentManager();		
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.replace(R.id.frameLayout, _profileFragment);
			fragmentTransaction.commit();   
			
			Button doneButton = (Button) findViewById(R.id.doneButton);
			doneButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					_profileFragment.saveProfile();
					Intent intent = new Intent(ProfileActivity.this, TabBarActivity.class);
					startActivity(intent);
					setResult(Activity.RESULT_OK);
					finish();
				}
			});
			
			 if (profile == null) 
			 {
		            // Use chose to not log in
		            _profileFragment.createNewUserProfile();
	         } 
			 else 
			 {
	        	profile.setProfileDefaults();
	         }
		};

	};
	
	private MoreProfileFragment _profileFragment;
			
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_profile);
        
        _profileFragment = new MoreProfileFragment();
        
        new UserProfileTask().execute(new Void[]{});
	}
}
