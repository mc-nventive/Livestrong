package com.demandmedia.livestrong.android.activity;

import java.lang.reflect.Method;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.demandmedia.livestrong.android.R;
import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.api.ApiHelper;
import com.demandmedia.livestrong.android.back.models.UserProfile;

public class LoginActivity extends LiveStrongActivity {
	
	public static String INTENT_APP_OFFLINE_MODE = "appOfflineMode";
	
	protected String username;
	protected String password;
	private ProgressBar progressBar;
	private TextView loadingTextView;
	private Button loginButton;
	private Boolean appOfflineMode; // app is being used in offline mode (user is using app but has never signed in)
	private EditText usernameEditText, passwordEditText;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
               
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        this.appOfflineMode = false;
        Bundle extras = getIntent().getExtras();
        if (extras != null){
        	this.appOfflineMode = extras.getBoolean(INTENT_APP_OFFLINE_MODE);
        }
        
        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_login);

		this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
		this.loadingTextView = (TextView) findViewById(R.id.loadingTextView);
		this.progressBar.setVisibility(View.INVISIBLE);
		this.loadingTextView.setVisibility(View.INVISIBLE);
		
		this.usernameEditText = (EditText) findViewById(R.id.usernameEditText);
		this.passwordEditText = (EditText) findViewById(R.id.passwordEditText);
		this.passwordEditText.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {				
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						initiateLogin();					
						return true;
					} 
				}
				return false;
			}
        });
		

        this.loginButton = (Button) findViewById(R.id.loginButton);
	    
        this.loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				initiateLogin();
			}
		});
        
        Button registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				startActivityForResult(intent, 0);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK){
			setResult(Activity.RESULT_OK);
			finish();
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void initiateLogin(){
		if (ApiHelper.isOnline() == false){
			new AlertDialog.Builder(LoginActivity.this)
		      .setMessage("Please ensure you are connected to the internet.")
		      .setTitle("No Internet Connection")
		      .setNeutralButton(android.R.string.ok,
		         new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int whichButton){}
		         })
		      .show();
			return;
		}
		
		
		this.progressBar.setVisibility(View.VISIBLE);
		this.loginButton.setVisibility(View.GONE);
		this.loadingTextView.setVisibility(View.VISIBLE);
		this.loadingTextView.setText("Authenticating...");
		
		this.username = this.usernameEditText.getText().toString();
		this.password = this.passwordEditText.getText().toString();

		hideKeyboard();
		
		ApiHelper.authenticate(this.username, this.password, this);
	}

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        // -> onResume()
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    	// Called before making the activity vulnerable to destruction; save your activity state in outState.
        // UI elements states are saved automatically by super.onSaveInstanceState()
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused"); commit unsaved changes to persistent data, etc.
        // -> onStop()
    }

    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // The activity was stopped, and is about to be started again. It was not destroyed, so all members are intact.
        // -> onStart()
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        if (isFinishing()) {
        	// Someone called finish()
        } else {
        	// System is temporarily destroying this instance of the activity to save space
        }
    }
	
	private void hideKeyboard(){
		// Hide Keyboard
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(usernameEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
		
		this.usernameEditText.clearFocus();
		this.passwordEditText.clearFocus();
	}

	@Override
	public void dataReceived(Method methodCalled, Object data) {
		if (methodCalled.equals(DataHelper.METHOD_SYNC_DIARY)) {
			DataHelper.setPref(DataHelper.PREFS_LAST_ONLOAD_SYNC, new Date().getTime());

			// Done with initial data load.
			this.progressBar.setVisibility(View.GONE);

			// Pop the stack :)
			if (appOfflineMode == false){
				Intent intent = new Intent(this, TabBarActivity.class);
				startActivity(intent);	
			} 
			setResult(Activity.RESULT_OK);
			
			finish();
		}
		else if (methodCalled.equals(DataHelper.METHOD_GET_USER_PROFILE)) {
			if (data instanceof UserProfile) {
				LoginActivity.this.loadingTextView.setText("Syncing...");
				// Launch the 1st data sync
				DataHelper.refreshData(this);
			}
		}
	}

	@Override
	public boolean errorOccurred(Method methodCalled, Exception error, String message) {
		if (methodCalled.equals(ApiHelper.METHOD_GET_ACCESS_TOKEN)) {
			// Auth failed
			this.progressBar.setVisibility(View.GONE);
			this.loadingTextView.setVisibility(View.INVISIBLE);
			this.loginButton.setVisibility(View.VISIBLE);
			
			new AlertDialog.Builder(LoginActivity.this)
		      .setMessage(R.string.auth_failed)
		      .setTitle(R.string.auth_failed_title)
		      .setNeutralButton(android.R.string.ok,
		         new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int whichButton){}
		         })
		      .show();

			return true;
		}
		return false;
	}
}
