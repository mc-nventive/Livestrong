package com.livestrong.myplate.activity;

import android.os.Bundle;
import android.util.Log;

import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.db.DatabaseHelper;
import com.livestrong.myplate.back.db.OrmLiteFragmentActivity;

public class AbstractLiveStrongFragmentActivity extends OrmLiteFragmentActivity<DatabaseHelper> {

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
}
