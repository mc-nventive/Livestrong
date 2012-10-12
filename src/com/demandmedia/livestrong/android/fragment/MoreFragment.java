package com.demandmedia.livestrong.android.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.livestrong.myplate.R;
import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.models.UserProfile;

public class MoreFragment extends FragmentDataHelperDelegate {

	LinearLayout view;
	Button accountButton, profileButton, supportButton, aboutButton;
	MoreAccountFragment accountFragment;
	MoreProfileFragment profileFragment;
	MoreSupportFragment supportFragment;
	MoreAboutFragment aboutFragment;
	int lastTabIndex;
	
	private class UserProfileTask extends AsyncTask<Void, Void, UserProfile>
	{
		
		@Override
		protected UserProfile doInBackground(Void... params) 
		{
			return DataHelper.getUserProfile(null);
		}
		
		protected void onPostExecute(UserProfile profile) 
		{
			if (profile != null) 
			{
				profileFragment.saveProfile();
				accountFragment.saveProfile();
			}
		};

	};
	
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
		//Log.d("MoreFragment", "MoreFragment - onCreate");
		// Hook up outlets
		this.view 			= (LinearLayout) inflater.inflate(R.layout.fragment_more, container, false);
		this.accountButton 	= (Button) this.view.findViewById(R.id.accountButton);
		this.profileButton 	= (Button) this.view.findViewById(R.id.profileButton);
		this.supportButton 	= (Button) this.view.findViewById(R.id.supportButton);
		this.aboutButton 	= (Button) this.view.findViewById(R.id.aboutButton);
		
		this.initializeButtons();
		
		// Initialize Fragments
		this.accountFragment 	= new MoreAccountFragment();
		this.profileFragment 	= new MoreProfileFragment();
		this.supportFragment 	= new MoreSupportFragment();
		this.aboutFragment 		= new MoreAboutFragment();
		
		Integer selectedTab = (Integer) DataHelper.getPref(DataHelper.PREFS_MORE_SELECTED_TAB, 0);
		switch (selectedTab) {
			case 0:
				this.displayAccountFragment(false);	
				break;
			case 1:
				this.displayProfileFragment(false);	
				break;
			case 2:
				this.displaySupportFragment(false);	
				break;
			case 3:
				this.displayAboutFragment(false);	
				break;			
		}
		
		return this.view;
	}
	
	@Override
	public void onPause() {
		new UserProfileTask().execute(new Void[]{});
		
		super.onPause();
	}
	
	@Override
	public void onResume() {
		//Log.d("MoreFragment", "TabBarActivity - MoreFragment - onResume");
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		//Log.d("MoreFragment", "TabBarActivity - MoreFragment - onDestroy");
		super.onDestroy();
	}
	
	@Override
	public void onDetach() {
		//Log.d("MoreFragment", "TabBarActivity - MoreFragment - onDetach");
		super.onDetach();
	}
	
	private void initializeButtons(){
		View.OnClickListener onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View buttonView) {
				if (buttonView.isSelected()){
					return;
				}
				
				MoreFragment.this.unSelectTabButtons();

				profileFragment.saveProfile();
				
				int buttonID = buttonView.getId();
					
				switch (buttonID) {
					case R.id.accountButton:
						displayAccountFragment(true);
						break;
					case R.id.profileButton:
						displayProfileFragment(true);				
						break;
					case R.id.supportButton:
						displaySupportFragment(true);
						break;
					case R.id.aboutButton:
						displayAboutFragment(true);
						break;
				default:
					break;
				}
			}
		};
		
		this.accountButton.setOnClickListener(onClickListener);
		this.profileButton.setOnClickListener(onClickListener);
		this.supportButton.setOnClickListener(onClickListener);
		this.aboutButton.setOnClickListener(onClickListener);
	}
	
	private void unSelectTabButtons(){
		this.accountButton.setSelected(false);
		this.profileButton.setSelected(false);
		this.supportButton.setSelected(false);
		this.aboutButton.setSelected(false);
	}
	
	protected void displayAccountFragment(Boolean animated){
		int index = 0;
		Boolean toTheLeft = true; 
		if (lastTabIndex > index){
			toTheLeft = false;
		}
		this.changeTabContent(animated, toTheLeft, accountFragment);
		
		unSelectTabButtons();
		this.accountButton.setSelected(true);
		DataHelper.setPref(DataHelper.PREFS_MORE_SELECTED_TAB, 0);
		this.lastTabIndex = index;
	}
	
	protected void displayProfileFragment(Boolean animated){	
		int index = 1;
		Boolean toTheLeft = true; 
		if (lastTabIndex > index){
			toTheLeft = false;
		}
		this.changeTabContent(animated, toTheLeft, this.profileFragment);
				
		unSelectTabButtons();
		this.profileButton.setSelected(true);
		DataHelper.setPref(DataHelper.PREFS_MORE_SELECTED_TAB, 1);
		this.lastTabIndex = index;
	}
	
	protected void displaySupportFragment(Boolean animated){
		int index = 2;
		Boolean toTheLeft = true; 
		if (lastTabIndex > index){
			toTheLeft = false;
		}
		this.changeTabContent(animated, toTheLeft, this.supportFragment);
		
		unSelectTabButtons();
		this.supportButton.setSelected(true);
		DataHelper.setPref(DataHelper.PREFS_MORE_SELECTED_TAB, 2);
		this.lastTabIndex = index;
	}
	
	protected void displayAboutFragment(Boolean animated){
		int index = 3;
		Boolean toTheLeft = true; 
		if (lastTabIndex > index){
			toTheLeft = false;
		}
		this.changeTabContent(animated, toTheLeft, this.aboutFragment);
		
		unSelectTabButtons();
		this.aboutButton.setSelected(true);
		DataHelper.setPref(DataHelper.PREFS_MORE_SELECTED_TAB, 3);
		this.lastTabIndex = index;
	}
	
	protected void changeTabContent(Boolean animated, Boolean toTheLeft, Fragment fragment){
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();		
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if (animated){
			if (toTheLeft){
				fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);	
			} else {
				fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);				
			}
		}	
		
		fragmentTransaction.replace(R.id.frameLayout, fragment);
		fragmentTransaction.commit();
	}
}