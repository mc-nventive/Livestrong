package com.demandmedia.livestrong.android.activity;

import java.io.File;

import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.db.DatabaseHelper;
import com.demandmedia.livestrong.android.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

public class EntryPageActivity extends Activity 
{
	public static final String SHOULD_PROMPT_FOR_MIGRATION = "WAS_NOT_PROMPTED_FOR_LITE_MIGRATION";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_entrypage);
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		File liteDatabasePath = null;
		try 
		{
			liteDatabasePath = createPackageContext("com.livestrong.myplatelite", CONTEXT_IGNORE_SECURITY)
					.getDatabasePath(DatabaseHelper.DATABASE_NAME);
		} catch (NameNotFoundException e) 
		{
			//If no prexisting Lite version was found at first prompt, never ask the user to migrate again.
			DataHelper.setPref(SHOULD_PROMPT_FOR_MIGRATION, false);
		}
		
		if(DataHelper.getPref(SHOULD_PROMPT_FOR_MIGRATION, true) && null != liteDatabasePath)
		{
			startActivity(new Intent(this, LiteMigrationActivity.class));
		}
		else
		{
			startActivity(new Intent(this, WelcomeActivity.class));
		}
	}
}
