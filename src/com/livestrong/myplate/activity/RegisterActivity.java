package com.livestrong.myplate.activity;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplatelite.R;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.api.ApiHelper;
import com.livestrong.myplate.back.models.UserProfile;
import com.livestrong.myplate.back.models.UserProfile.Gender;

public class RegisterActivity extends LiveStrongFragmentActivity {
	
	LinearLayout view;
	EditText usernameEditText, passwordEditText, confirmPasswordEditText, nameEditText, birthdayEditText, emailEditText, postalCodeEditText;
	ProgressBar progressBar;
	Button createAccountButton;
	Date birthDate;
	Dialog dialog;
	Boolean agreesToTerms = false;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_register);
        
        this.usernameEditText 			= (EditText) findViewById(R.id.usernameEditText);		
		this.passwordEditText 			= (EditText) findViewById(R.id.passwordEditText);		
		this.confirmPasswordEditText 	= (EditText) findViewById(R.id.confirmPasswordEditText);
		this.nameEditText				= (EditText) findViewById(R.id.nameEditText);
		this.postalCodeEditText			= (EditText) findViewById(R.id.postalCodeEditText);
		this.emailEditText				= (EditText) findViewById(R.id.emailEditText);
		this.birthdayEditText 			= (EditText) findViewById(R.id.birthdayEditText);
		this.progressBar 				= (ProgressBar) findViewById(R.id.progressBar);
		
		this.createAccountButton = (Button) findViewById(R.id.createAccountButton);
		this.createAccountButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ApiHelper.isOnline() == false){ // Check if user has internet connection
					new AlertDialog.Builder(RegisterActivity.this)
				      .setMessage("Please ensure you are connected to the internet.")
				      .setTitle("No Internet Connection")
				      .setNeutralButton(android.R.string.ok,
				         new DialogInterface.OnClickListener() {
				         public void onClick(DialogInterface dialog, int whichButton){}
				         })
				      .show();
					return;
				}
				
				
				if (validateFields()){
					if (agreesToTerms == false){
						final Dialog dialog = new Dialog(RegisterActivity.this);
						dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
						dialog.setContentView(R.layout.dialog_register); 
						
						// Initialize Terms and conditions button
						Button termsButton = (Button) dialog.findViewById(R.id.termsAndConditionsButton);
						termsButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(RegisterActivity.this, WebViewActivity.class);
								intent.putExtra(WebViewActivity.INTENT_URL, "http://www.livestrong.com/terms/");
								intent.putExtra(WebViewActivity.INTENT_TITLE, "TERMS OF USE");
								startActivity(intent);
							}
						});
						
						// Initialize Privacy button
						Button privacyButton = (Button) dialog.findViewById(R.id.privacyButton);
						privacyButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(RegisterActivity.this, WebViewActivity.class);
								intent.putExtra(WebViewActivity.INTENT_URL, "http://www.livestrong.com/privacy-policy/");
								intent.putExtra(WebViewActivity.INTENT_TITLE, "PRIVACY POLICY");
								startActivity(intent);	
							}
						});
						
						Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
						cancelButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});	
						
						Button doneButton = (Button) dialog.findViewById(R.id.doneButton);
						doneButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								registerUser();
								dialog.cancel();
								agreesToTerms = true;
							}
						});
						
						dialog.show();
					} else { // User has already agreed to terms
						registerUser();
					}
				}
			}
		});
		
		Button cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		this.initializeBirthdayPicker();
	}
	
	private void registerUser(){
		this.progressBar.setVisibility(View.VISIBLE);
		this.createAccountButton.setVisibility(View.INVISIBLE);
		
		DataHelper.registerUser(
				usernameEditText.getText().toString(), 
				passwordEditText.getText().toString(),
				nameEditText.getText().toString(),
				emailEditText.getText().toString(),
				birthDate, 
				Gender.MALE, 
				postalCodeEditText.getText().toString(), 
				this);
	}
	
	private void initializeBirthdayPicker(){
		this.birthdayEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus){
					return;
				}
				
				if (dialog == null){
					dialog = new Dialog(RegisterActivity.this);
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog.setContentView(R.layout.dialog_birthday); 
					
					Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
					cancelButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
				            birthdayEditText.clearFocus();
							dialog.cancel();
						}
					});	
					
					Button doneButton = (Button) dialog.findViewById(R.id.doneButton);
					doneButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.birthdayDatePicker);
							birthDate = new Date(datePicker.getYear() - 1900, datePicker.getMonth(), datePicker.getDayOfMonth());
							
							refreshBirthdayEditText();
							birthdayEditText.clearFocus();
							dialog.dismiss();
						}
					});
				}
				
				if (!dialog.isShowing()){
					dialog.show();	
				}
			}
		});
	}
	
	private void refreshBirthdayEditText(){
		SimpleDateFormat formatter = new SimpleDateFormat("d MMMM, yyyy");
		this.birthdayEditText.setText(formatter.format(this.birthDate));
	}
	
	private boolean validateFields(){
		String errorMessage = "";
		if (this.usernameEditText.getText().toString().equals("")){
			errorMessage += "Your username must be entered.\n";
		}
		
		String password = this.passwordEditText.getText().toString(); 
		if (password.equals("")){
			errorMessage += "A password must be entered.\n";
		}
		String passwordConfirmation = this.confirmPasswordEditText.getText().toString(); 
		if (this.confirmPasswordEditText.getText().toString().equals("")){
			errorMessage += "You must retype your password.\n";
		}
		if (password.length() > 0 && passwordConfirmation.length() > 0 && !password.equals(passwordConfirmation)){
			errorMessage += "Your passwords do not match.\n";
		}
		if (this.nameEditText.getText().toString().equals("")){
			errorMessage += "Your name must be entered.\n";
		}
		if (this.birthdayEditText.getText().toString().equals("")){
			errorMessage += "You must enter your birthday.\n";
		}
		if (this.emailEditText.getText().toString().equals("")){
			errorMessage += "You must enter your email address.\n";
		}
		if (this.postalCodeEditText.getText().toString().equals("")){
			errorMessage += "You must enter your postal code.\n";
		}
		
		if (!errorMessage.equals("")){
			new AlertDialog.Builder(RegisterActivity.this)
		      .setMessage(errorMessage)
		      .setNeutralButton(android.R.string.ok,
		         new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int whichButton){}
		         })
		      .show();
			return false;
		}
		return true;
	}

	@Override
	public void dataReceived(Method methodCalled, Object data) {
		if (data instanceof UserProfile) {
			Intent intent = new Intent(this, ProfileActivity.class);
			startActivity(intent);
			
			setResult(Activity.RESULT_OK);
			finish();
		}
	}

	@Override
	public boolean errorOccurred(Method methodCalled, Exception error, String errorMessage) {
		this.progressBar.setVisibility(View.INVISIBLE);
		this.createAccountButton.setVisibility(View.VISIBLE);
		if (errorMessage == null) {
			errorMessage = "There was a problem creating your account.\nMake sure you are online, or try again later.";
		}
		new AlertDialog.Builder(MyPlateApplication.getFrontMostActivity())
	      .setMessage(errorMessage)
	      .setTitle(R.string.error)
	      .setNeutralButton(android.R.string.ok,
	         new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton){}
	         })
	      .show();
		return true;
	}
}
