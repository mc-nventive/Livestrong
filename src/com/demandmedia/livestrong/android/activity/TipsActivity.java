package com.demandmedia.livestrong.android.activity;

import android.os.Bundle;

import com.livestrong.myplate.R;

public class TipsActivity extends LiveStrongFragmentActivity {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_tips);	
	}
}
