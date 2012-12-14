package com.livestrong.myplate.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.utilities.Utils;
import com.livestrong.myplatelite.R;

public class UpgradeActivity extends LiveStrongFragmentActivity {
	
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Download activity levels
        DataHelper.getActivityLevels(this);
        
        onCreateLight(savedInstanceState);

	}
	
	/**
	 * Create the Welcome activity for the light app.
	 */
	public void onCreateLight(Bundle savedInstanceState) {
        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_upgrade);
        
        Button toMarketButton = (Button)findViewById(R.id.toMarketButton);
        toMarketButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.openPlayStore(UpgradeActivity.this);
			}
		});
        
        Button notNowButton = (Button) findViewById(R.id.notNowButton);
        notNowButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					finish();
					return;
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
