package com.demandmedia.livestrong.android.fragment;

import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.demandmedia.livestrong.android.R;
import com.demandmedia.livestrong.android.adapters.DiaryCalendarAdapter;

public class DiaryCalendarFragment extends FragmentDataHelperDelegate {
	
	private DiaryFragment diaryFragment;
	private Calendar calendar;
	private DiaryCalendarAdapter adapter;
	private View view;

	public DiaryCalendarFragment(DiaryFragment diaryFragment){
		this.diaryFragment = diaryFragment;		
		this.calendar = Calendar.getInstance();
	}
	
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

		this.adapter = new DiaryCalendarAdapter(getActivity(), calendar);
		
		this.view = (LinearLayout)inflater.inflate(R.layout.fragment_diary_calendar, container, false);  
	    	      
	    this.initializeCalendar();
	      
	    this.initializeButtons();
		
	    this.refreshCalendar();	  
		
		return view;
	}
	
	private void initializeCalendar(){
	    GridView gridview = (GridView) this.view.findViewById(R.id.gridview);
	    gridview.setAdapter(adapter);	    
	    // Prevent Grid View from scrolling
	    gridview.setOnTouchListener(new OnTouchListener(){
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            if(event.getAction() == MotionEvent.ACTION_MOVE){
	                return true;
	            }
	            return false;
	        }
	    });
		
		gridview.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		    	TextView date = (TextView)v.findViewById(R.id.date);
		        if (date instanceof TextView && !date.getText().equals("")) {
		        	String day = date.getText().toString();
		        			        	
		        	Date selectedDate = new Date(calendar.getTime().getYear(), calendar.getTime().getMonth(), Integer.parseInt(day));
		        	
		        	if (selectedDate.after(new Date())){
		        		return;
		        	}
		        	
		        	diaryFragment.displayDiaryListFragment(true, selectedDate);
		        }
		    }
		});
	}
	
	private void initializeButtons(){
		// Go to previous month button
		ImageButton previous  = (ImageButton) this.view.findViewById(R.id.backButton);
	    previous.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(calendar.get(Calendar.MONTH) == calendar.getActualMinimum(Calendar.MONTH)) {				
					calendar.set((calendar.get(Calendar.YEAR) - 1), calendar.getActualMaximum(Calendar.MONTH), 1);
				} else {
					calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
				}
				refreshCalendar();
			}
		});
	    
	    // Go to next month button
	    ImageButton next  = (ImageButton) this.view.findViewById(R.id.forwardButton);
	    next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar todayCalendar = Calendar.getInstance();
				todayCalendar.setTime(new Date());				
				
				// Prevent user from picking months in the future
				if (todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)){
					return;
				}
				
				if (calendar.get(Calendar.MONTH) == calendar.getActualMaximum(Calendar.MONTH)) {				 
					calendar.set((calendar.get(Calendar.YEAR) + 1), calendar.getActualMinimum(Calendar.MONTH), 1);
				} else {
					calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
				}
				refreshCalendar();	
			}
		});
	}
	
	public void refreshCalendar(){
		TextView monthTextView  = (TextView) this.view.findViewById(R.id.monthTextView);
		monthTextView.setText(android.text.format.DateFormat.format("MMMM yyyy", calendar));
				
		this.adapter.refreshDays();
		this.adapter.loadDailyCaloriesMap();
		this.adapter.notifyDataSetChanged();
	}
	
	public void setDate(Date date){
		calendar.setTime(date);
	}
}
