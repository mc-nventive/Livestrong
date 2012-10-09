package com.demandmedia.livestrong.android.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.demandmedia.livestrong.android.R;
import com.demandmedia.livestrong.android.activity.TipsActivity;
import com.demandmedia.livestrong.android.activity.WebViewActivity;
import com.demandmedia.livestrong.android.activity.WelcomeActivity;
import com.demandmedia.livestrong.android.back.DataHelper;

public class MoreSupportFragment extends FragmentDataHelperDelegate {
	
	private LinearLayout view;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
	        // We have different layouts, and in one of them this
	        // fragment's containing frame doesn't exist.  The fragment
	        // may still be created from its saved state, but there is
	        // no reason to try to create its view hierarchy because it
	        // won't be displayed.  Note this is not needed -- we could
	        // just run the code below, where we would create and return
	        // the view hierarchy; it would just never be used.
	        return null;
	    }
		
		// Hook up outlets
		this.view = (LinearLayout) inflater.inflate(R.layout.fragment_more_support, container, false);	
			
			
		Button termsButton = (Button) this.view.findViewById(R.id.termsAndConditionsButton);
		termsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), WebViewActivity.class);
				intent.putExtra(WebViewActivity.INTENT_URL, "http://www.livestrong.com/terms/");
				intent.putExtra(WebViewActivity.INTENT_TITLE, "TERMS OF USE");
				startActivity(intent);
			}
		});
		
		Button privacyButton = (Button) view.findViewById(R.id.privacyButton);
		privacyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), WebViewActivity.class);
				intent.putExtra(WebViewActivity.INTENT_URL, "http://www.livestrong.com/privacy-policy/");
				intent.putExtra(WebViewActivity.INTENT_TITLE, "PRIVACY POLICY");
				startActivity(intent);
			}
		});
		
		Button customerServiceButton = (Button) view.findViewById(R.id.customerServiceButton);
		customerServiceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
            	final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            	emailIntent.setType("plain/text");
            	emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ "support@livestrong.com"});
            	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Support Request (Android app)");
            	startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			}
		});			
		
		Button tellAFriendButton = (Button) view.findViewById(R.id.tellAFriendButton);
		tellAFriendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            	emailIntent.setType("plain/text");
            	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "LIVESTRONG.COM Calorie Tracker");
            	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Check out the LIVESTRONG.COM Calorie Tracker that helps me track my progress daily! \n\n");

            	startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			}
		});
		
		Button tipsButton = (Button) view.findViewById(R.id.tipsButton);
		tipsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), TipsActivity.class);
				startActivity(intent);
			}
		});
		
		Button eraseButton = (Button) view.findViewById(R.id.eraseButton);
		eraseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(getActivity());
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.dialog_delete_data); 
				final LinearLayout progressContainer = (LinearLayout) dialog.findViewById(R.id.loaderContainer);
				progressContainer.setVisibility(View.INVISIBLE);
				final LinearLayout buttonsContainer = (LinearLayout) dialog.findViewById(R.id.buttonsContainer);
				
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
						progressContainer.setVisibility(View.VISIBLE);
						buttonsContainer.setVisibility(View.INVISIBLE);
						
						final Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
						  @Override
						  public void run() {
							  DataHelper.resetDatabase();
								Intent intent = new Intent(getActivity(), WelcomeActivity.class);
								startActivity(intent);
								
								dialog.cancel();
								
								getActivity().finish();	  
						  }
						}, 500);					
					}
				});
				
				dialog.show();
			}
		});
		
		return this.view;
	}
}
