package com.livestrong.myplate.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.livestrong.myplate.R;
import com.livestrong.myplate.back.DataHelper;

public class WelcomeActivity extends LiveStrongFragmentActivity {
	
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // If user is signed in, or has a profile, show TabBarActivity
        if (DataHelper.getUserProfile(null) != null) {
        	Intent intent = new Intent(this, TabBarActivity.class);
			startActivity(intent);
			
			finish();
			return;
        }

        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_welcome);
        
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Activity.RESULT_OK){
			finish();
		}
	}
}
