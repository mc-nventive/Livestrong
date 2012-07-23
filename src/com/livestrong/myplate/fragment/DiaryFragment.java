package com.livestrong.myplate.fragment;

import java.util.Date;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplatelite.R;
import com.livestrong.myplate.back.DataHelper;

public class DiaryFragment extends FragmentDataHelperDelegate {
	
	private View view;
	Button monthlyButton, dailyButton;
	DiaryCalendarFragment calendarFragment;
	DiaryListFragment listFragment;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d("D", "onCreateView");
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
		
		this.view = (LinearLayout)inflater.inflate(R.layout.fragment_diary, container, false);  
		this.calendarFragment = new DiaryCalendarFragment(this);
		this.listFragment = new DiaryListFragment();		
		
		this.initializeButtons();
		
		this.displayDiaryListFragment(false, MyPlateApplication.getWorkingDateStamp());
//		Integer selectedTab = (Integer) DataHelper.getPref(DataHelper.PREFS_DIARY_SELECTED_TAB, 1);
//		switch (selectedTab) {
//			case 0:
//				this.displayDiaryCalendarFragment(false, MyPlateApplication.getWorkingDateStamp());
//				break;
//			case 1:
//				this.displayDiaryListFragment(false, MyPlateApplication.getWorkingDateStamp());			
//				break;
//		}
//		
		return this.view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	private void initializeButtons(){
		View.OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View button) {
				if (button.isSelected()){
					return;
				} 
				unSelectTabButtons();
				
				int buttonId = button.getId();
				switch (buttonId) {
				case R.id.monthlyButton:
					displayDiaryCalendarFragment(true, listFragment.getSelectedDate());
					break;
				case R.id.dailyButton:
					displayDiaryListFragment(true, null);
					break;
				}
			}
		};
		
		this.monthlyButton = (Button) this.view.findViewById(R.id.monthlyButton);
		this.dailyButton = (Button) this.view.findViewById(R.id.dailyButton);
		
		this.monthlyButton.setOnClickListener(onClickListener);
		this.dailyButton.setOnClickListener(onClickListener);
	}
	
	public void unSelectTabButtons(){
		this.monthlyButton.setSelected(false);
		this.dailyButton.setSelected(false);		
	}
	
	protected void displayDiaryListFragment(Boolean animated, Date date){
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();		
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if (animated){
			fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);	
		}			
		
		if (date != null){
			this.listFragment.selectedDate = date;
			MyPlateApplication.setWorkingDateStamp(date);
		}
		
		fragmentTransaction.replace(R.id.frameLayout, this.listFragment);
		fragmentTransaction.commit();		
		
		unSelectTabButtons();
		this.dailyButton.setSelected(true);
		DataHelper.setPref(DataHelper.PREFS_DIARY_SELECTED_TAB, 1);
	}
	
	protected void displayDiaryCalendarFragment(Boolean animated, Date date){
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();		
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if (animated){
			fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
		}
		if (date != null){
			this.calendarFragment.setDate(date);
		}
				
		fragmentTransaction.replace(R.id.frameLayout, this.calendarFragment);
		fragmentTransaction.commit();
		
		unSelectTabButtons();
		this.monthlyButton.setSelected(true);
		DataHelper.setPref(DataHelper.PREFS_DIARY_SELECTED_TAB, 0);
	}
}
