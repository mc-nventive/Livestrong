package com.livestrong.myplate.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.livestrong.myplate.R;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.models.UserProfile;

public class WelcomeActivity extends LiveStrongFragmentActivity {
	
	private AsyncTask<Void, Void, UserProfile> _userProfileTask = new AsyncTask<Void, Void, UserProfile>()
	{
		protected void onPreExecute() 
		{
			WelcomeActivity.this.toggleUserLoginContent(true);
		};
		
		@Override
		protected UserProfile doInBackground(Void... params) 
		{
			return DataHelper.getUserProfile(null);
		}
		
		protected void onPostExecute(UserProfile profile) 
		{
			if (profile != null) {
	        	Intent intent = new Intent(WelcomeActivity.this, TabBarActivity.class);
				startActivity(intent);
				
				finish();
				return;
	        }
			else
			{
				WelcomeActivity.this.toggleUserLoginContent(false);
			}
		};

	};
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_welcome);
        
        // If user is signed in, or has a profile, show TabBarActivity
        _userProfileTask.execute(new Void[]{});
        
        // Download activity levels 
        DataHelper.getActivityLevels(this);
        
        Button yesButton = (Button) findViewById(R.id.yesButton);
        yesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
				startActivityForResult(intent, 0);
			}
		});
        
        Button notNowButton = (Button) findViewById(R.id.notNowButton);
        notNowButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, ProfileActivity.class);
				startActivityForResult(intent, 0);
			}
		});
	}

	protected void toggleUserLoginContent(boolean showProgress) 
	{
		WelcomeActivity.this.findViewById(R.id.headerTextView).setVisibility(showProgress ? View.GONE : View.VISIBLE);
		WelcomeActivity.this.findViewById(R.id.yesNoLayout).setVisibility(showProgress ? View.GONE : View.VISIBLE);
		
		WelcomeActivity.this.findViewById(R.id.progressBar1).setVisibility(showProgress ? View.VISIBLE : View.GONE);
		WelcomeActivity.this.findViewById(R.id.progressText).setVisibility(showProgress ? View.VISIBLE : View.GONE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Activity.RESULT_OK){
			finish();
		}
	}
}
