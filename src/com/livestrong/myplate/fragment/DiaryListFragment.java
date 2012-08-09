package com.livestrong.myplate.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplate.activity.AddExerciseActivity;
import com.livestrong.myplate.activity.AddFoodActivity;
import com.livestrong.myplate.activity.AddWaterActivity;
import com.livestrong.myplate.activity.AddWeightActivity;
import com.livestrong.myplate.activity.TabBarActivity;
import com.livestrong.myplate.activity.TrackActivity;
import com.livestrong.myplate.adapters.DiaryAdapter;
import com.livestrong.myplate.back.models.DiaryEntry;
import com.livestrong.myplate.back.models.ExerciseDiaryEntry;
import com.livestrong.myplate.back.models.FoodDiaryEntry;
import com.livestrong.myplate.back.models.FoodDiaryEntry.TimeOfDay;
import com.livestrong.myplate.back.models.WaterDiaryEntry;
import com.livestrong.myplate.back.models.WeightDiaryEntry;
import com.livestrong.myplate.utilities.PinnedHeaderListView;
import com.livestrong.myplate.utilities.SessionMHelper;
import com.livestrong.myplatelite.R;
import com.sessionm.api.SessionM;

public class DiaryListFragment extends FragmentDataHelperDelegate implements OnItemClickListener {
	
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("d MMMM, yyyy");
	
	private PinnedHeaderListView listView;
	private DiaryAdapter adapter;
	private ImageButton backBtn, forwardBtn;
	private Button trackNowButton;
	protected Date selectedDate;
	private TextView dateTextView;
	private LinearLayout messageContainer;

	private String _sessionM;
	
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
		
		View view = (LinearLayout)inflater.inflate(R.layout.fragment_diary_list, container, false);
    
		// Hooking up outlets
		this.backBtn 		= (ImageButton) view.findViewById(R.id.backButton);
		this.forwardBtn 	= (ImageButton) view.findViewById(R.id.forwardButton);
		this.trackNowButton = (Button) view.findViewById(R.id.trackNowButton);
		this.dateTextView 	= (TextView) view.findViewById(R.id.dateTextView);
		this.messageContainer = (LinearLayout) view.findViewById(R.id.messageContainer);
		
		// Initialize ListView
		this.listView = (PinnedHeaderListView)view.findViewById(android.R.id.list);
	    this.listView.setPinnedHeaderView(LayoutInflater.from(getActivity()).inflate(R.layout.list_item_diary_header, listView, false));
	    this.listView.setDividerHeight(0);
	  
		this.adapter = new DiaryAdapter(getActivity());
	    this.listView.setAdapter(adapter);
	    this.listView.setOnScrollListener(adapter);
	    this.listView.setOnItemClickListener(this);

		// Initialize date & datelabel
		if (this.selectedDate == null){
			this.setSelectedDate(new Date());
		}
		this.refreshDateLabel();
	    
	    this.initializeButtons();
	    
	    setHasOptionsMenu(true);
	    
		return view;
	}
	
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		super.onCreateOptionsMenu(menu, inflater);
//		menu.add(0, 1, 0, "Track").setIcon(android.R.drawable.ic_menu_add);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		super.onOptionsItemSelected(item);
//
//		MyPlateApplication.setWorkingDateStamp(this.selectedDate);
//		Intent intent = new Intent(getActivity(), TrackActivity.class);
//		startActivity(intent);
//
//		return true;
//	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (this.adapter != null && this.selectedDate != null){
			this.adapter.setDate(this.selectedDate);
		}
		
		// Show or hide Track Now button
		if (this.messageContainer != null){
			if (this.adapter != null && this.adapter.getCount() == 0){
				this.messageContainer.setVisibility(View.VISIBLE);
			} else {
				this.messageContainer.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// Diary entry item selected
		DiaryEntry clickedEntry = (DiaryEntry) this.adapter.getItem(position);
		
		MyPlateApplication.setWorkingDateStamp(this.selectedDate);
		int section = this.adapter.getSectionForPosition(position);
		switch (section){
			case 0:
				MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.BREAKFAST);
				break;
			case 1:
				MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.LUNCH);
				break;
			case 2:
				MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.DINNER);
				break;
			case 3:
				MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.SNACKS);
				break;
		}
		
		
		if (clickedEntry instanceof FoodDiaryEntry) {
			FoodDiaryEntry diaryEntry = (FoodDiaryEntry) clickedEntry;
			Intent intent = new Intent(MyPlateApplication.getContext(), AddFoodActivity.class);
			intent.putExtra(FoodDiaryEntry.class.getName(), diaryEntry);
			startActivity(intent);
		}
		else if (clickedEntry instanceof ExerciseDiaryEntry) {
			ExerciseDiaryEntry diaryEntry = (ExerciseDiaryEntry) clickedEntry;
			Intent intent = new Intent(MyPlateApplication.getContext(), AddExerciseActivity.class);
			intent.putExtra(ExerciseDiaryEntry.class.getName(), diaryEntry);
			startActivity(intent);
		}
		else if (clickedEntry instanceof WaterDiaryEntry) {
			WaterDiaryEntry diaryEntry = (WaterDiaryEntry) clickedEntry;
			Intent intent = new Intent(MyPlateApplication.getContext(), AddWaterActivity.class);
			intent.putExtra(WaterDiaryEntry.class.getName(), diaryEntry);
			startActivity(intent);
		}
		else if (clickedEntry instanceof WeightDiaryEntry) {
			WeightDiaryEntry diaryEntry = (WeightDiaryEntry) clickedEntry;
			Intent intent = new Intent(MyPlateApplication.getContext(), AddWeightActivity.class);
			intent.putExtra(WeightDiaryEntry.class.getName(), diaryEntry);
			startActivity(intent);
		}

	}
	
	private void initializeButtons(){
		View.OnClickListener onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View button) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(selectedDate);
				
				int btnId = button.getId();
				switch (btnId) {
					case R.id.backButton:
						calendar.add(Calendar.DATE, -1);  // subtract one day
						break;
					case R.id.forwardButton:
						if (!MyPlateApplication.isToday(selectedDate)){
							calendar.add(Calendar.DATE, 1);  // add one day
						}
						break;
				}
				
				setSelectedDate(calendar.getTime());
				MyPlateApplication.setWorkingDateStamp(calendar.getTime());
			}
		};
		
		this.backBtn.setOnClickListener(onClickListener);
		this.forwardBtn.setOnClickListener(onClickListener);
		
		this.trackNowButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				MyPlateApplication.setWorkingDateStamp(selectedDate);
				Intent intent = new Intent(getActivity(), TrackActivity.class);
				startActivityForResult(intent, 1);
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// An activity might post a SessionM event
		if (data != null && data.getExtras() != null) {
			_sessionM = data.getExtras().getString(SessionMHelper.INTENT_SESSIONM);
			if (_sessionM != null)
			{
				((TabBarActivity)this.getActivity()).SessionMAchievement = _sessionM;
			}
		}
	}
	
	protected void setSelectedDate(Date date){
		this.selectedDate = date;
		this.refreshDateLabel();
		this.adapter.setDate(this.selectedDate);
		this.listView.setSelectionAfterHeaderView();
		if (this.adapter.getCount() == 0){
			this.messageContainer.setVisibility(View.VISIBLE);
		} else {
			this.messageContainer.setVisibility(View.INVISIBLE);
		}
	}
	
	protected Date getSelectedDate(){
		return this.selectedDate;
	}
	
	private void refreshDateLabel(){
		if (dateTextView == null){
			return;
		}
		if (MyPlateApplication.isToday(this.selectedDate)) {
			this.dateTextView.setText(R.string.today);
		} else if (MyPlateApplication.isYesterday(this.selectedDate)) {
			this.dateTextView.setText(R.string.yesterday);
		} else {
			this.dateTextView.setText(dateFormatter.format(this.selectedDate));
		}
	}
}
