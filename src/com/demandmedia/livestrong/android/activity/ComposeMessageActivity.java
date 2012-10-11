package com.demandmedia.livestrong.android.activity;

import java.io.Serializable;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
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
import com.demandmedia.livestrong.android.back.models.CommunityMessage;

public class ComposeMessageActivity extends LiveStrongActivity {

	private Button cancelButton, postButton;
	private ProgressBar progressBar;
	private EditText messageEditText;
	private CommunityMessage message;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_compose_message);
        
        this.messageEditText = (EditText) findViewById(R.id.messageEditText);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.progressBar.setVisibility(View.INVISIBLE);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null){
        	this.message = (CommunityMessage) extras.get(CommunityMessage.class.getName());
        	TextView headerTextView = (TextView) findViewById(R.id.headerTextView);
        	headerTextView.setText("REPLY...");
        }
        
        this.initializeButtons();
        
        
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Show keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(this.messageEditText, InputMethodManager.SHOW_IMPLICIT);
        
        if (inputMethodManager != null){
        	inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private void initializeButtons(){
		this.cancelButton = (Button) findViewById(R.id.cancelButton);
		this.postButton = (Button) findViewById(R.id.postButton);
		
		View.OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View button) {
				int btnId = button.getId();
				
				hideKeyboard();
				if (btnId == R.id.cancelButton){
					finish();
				} else if (btnId == R.id.postButton){
					if (ComposeMessageActivity.this.messageEditText.getText().toString().equals("")){
						return;
					}
					
					progressBar.setVisibility(View.VISIBLE);
					postButton.setVisibility(View.INVISIBLE);
					
					if (ComposeMessageActivity.this.message == null) {
						// New post
						DataHelper.postNewMessage(ComposeMessageActivity.this.messageEditText.getText().toString(), ComposeMessageActivity.this);
					} else {
						// New comment
						DataHelper.postNewComment(ComposeMessageActivity.this.message.getPostId(), ComposeMessageActivity.this.messageEditText.getText().toString(), ComposeMessageActivity.this);
					}
				}				
			}
		};
		
		this.cancelButton.setOnClickListener(onClickListener);
		this.postButton.setOnClickListener(onClickListener);
	}
	
	private void hideKeyboard(){
		// Hide Keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.messageEditText.getWindowToken(), 0);
	}

	@Override
	public void dataReceived(Method methodCalled, Object data) {
		progressBar.setVisibility(View.INVISIBLE);
		postButton.setVisibility(View.VISIBLE);
		
		Intent response = new Intent();
		response.putExtra(data.getClass().getName(), (Serializable) data);
		if (getParent() == null) {
		    setResult(Activity.RESULT_OK, response);
		} else {
		    getParent().setResult(Activity.RESULT_OK, response);
		}
		finish();
	}
}
