package com.livestrong.myplate.activity;

import java.lang.reflect.Method;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplate.adapters.CommunityCommentsAdapter;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.api.models.NewCommentResponse;
import com.livestrong.myplate.back.models.CommunityMessage;
import com.livestrong.myplate.constants.BuildValues;
import com.livestrong.myplate.utilities.Utils;
import com.livestrong.myplatelite.R;

public class CommunityCommentsActivity extends LiveStrongActivity {

	private ListView list;
	private CommunityMessage message;
	private CommunityCommentsAdapter adapter;
	private Button sendButton;
	private ProgressBar progressBar;
	private EditText messageEditText;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
			// UI elements states are restored automatically by super.onCreate()
		}

		// The activity is being created; create views, bind data to lists, etc.
		setContentView(R.layout.activity_community_comments);

		this.list = (ListView) findViewById(R.id.communityListView);
		this.sendButton = (Button) findViewById(R.id.sendButton);
		this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
		this.messageEditText = (EditText) findViewById(R.id.messageEditText);
		
		this.progressBar.setVisibility(View.INVISIBLE);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			this.message = (CommunityMessage) extras.get(CommunityMessage.class.getName());
			setNumComments(this.message.getComments());

			this.adapter = new CommunityCommentsAdapter(this, this.list, this.message);
			this.list.setAdapter(this.adapter);

			this.adapter.loadComments(1);
		}

		this.list.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyboard();
				return false;
			}
		});
		
		
		// This is used to removed a banding effect caused when drawing gradients in list view items
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

		this.initializeButtons();
	}

	private void setNumComments(int numComments) {
		TextView headerTextView = (TextView) findViewById(R.id.headerTextView);
		headerTextView.setText(numComments + " COMMENT" + (numComments != 1 ? "S" : ""));
	}

	private void initializeButtons() {
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View button) {
				sendMessage();
			}
		});
	}

	@SuppressWarnings("unused")
	private void sendMessage() {
		if (DataHelper.isLoggedIn() == false || BuildValues.IS_LIGHT){
			new AlertDialog.Builder(MyPlateApplication.getFrontMostActivity())
		      .setMessage("This function is not available in the lite version. Would you like to visit the Play store now?")
		      .setTitle("")
		      .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Utils.openPlayStore(CommunityCommentsActivity.this);
				}
			  })
			  .setNegativeButton(android.R.string.no, null)
		      .show();
			
			return;
		}
		else
		{
			this.sendButton.setVisibility(View.INVISIBLE);
			this.progressBar.setVisibility(View.VISIBLE);
			this.messageEditText.setEnabled(false);
			
			DataHelper.postNewComment(
				this.message.getPostId(),
				this.messageEditText.getText().toString(),
				this);
			this.hideKeyboard();
		}
	}
	
	@Override
	public void dataReceived(Method methodCalled, Object data) {
		if (data instanceof NewCommentResponse){ // Just created a new post
			int numComments = this.message.getComments();
			this.message.setComments(++numComments);
			this.setNumComments(numComments);
			this.adapter.reloadComments();
			this.progressBar.setVisibility(View.INVISIBLE);
			this.sendButton.setVisibility(View.VISIBLE);
			this.messageEditText.setEnabled(true);
			this.messageEditText.setText("");
		}
	}
	
	private void hideKeyboard(){
		if (this.messageEditText.hasFocus()){
			// Hide Keyboard
			InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(this.messageEditText.getWindowToken(), 0);
			// Clear focus
			this.messageEditText.clearFocus();
		}
	}
}
