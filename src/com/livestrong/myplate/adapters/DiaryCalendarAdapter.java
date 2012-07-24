package com.livestrong.myplate.adapters;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livestrong.myplatelite.R;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.utilities.SimpleDate;

public class DiaryCalendarAdapter extends AbstractBaseAdapterDataHelperDelegate {
	static final int FIRST_DAY_OF_WEEK = 0; // Sunday = 0, Monday = 1

	private Calendar month;
	private Map<SimpleDate, Integer> dailyCaloriesMap;
	private Map<SimpleDate, Integer> dailyCaloriesGoalMap;

	// references to our items
	public String[] days;

	public DiaryCalendarAdapter(Activity activity, Calendar monthCalendar) {
		this.activity = activity;
		this.month = monthCalendar;
		this.month.set(Calendar.DAY_OF_MONTH, 1);
		refreshDays();
	}

	public int getCount() {
		return this.days.length;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	// create a new view for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		TextView dayView;
		if (convertView == null) { // if it's not recycled, initialize some attributes
			LayoutInflater inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.calendar_item, null);
		}
		dayView = (TextView) view.findViewById(R.id.date);

		// disable empty days from the beginning
		if (this.days[position].equals("")) {
			dayView.setClickable(false);
			dayView.setFocusable(false);
			// Ensure cells have black background
			view.setClickable(false);
			view.setFocusable(false);
			view.setBackgroundResource(0);
		} else {

			// get date of cell
			Calendar date = Calendar.getInstance();
			date.setTime(this.month.getTime());
			date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(this.days[position]));

			Integer caloriesSum = this.dailyCaloriesMap.get(new SimpleDate(date.getTime()));
			Integer calorieGoal = this.dailyCaloriesGoalMap.get(new SimpleDate(date.getTime()));
			//UserProgress userProgress = DataHelper.getUserCaloriesProgress(this.month.getTime());

			dayView.setTextColor(R.color.white);
			
			// Check if cell is today, set background appropriately
			Calendar todayCalendar = Calendar.getInstance();
			todayCalendar.setTime(new Date());
			if (this.month.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) && // isToday
					this.month.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) && this.days[position].equals("" + todayCalendar.get(Calendar.DAY_OF_MONTH))) {
				view.setBackgroundResource(R.drawable.calendar_icon_orange);
			} else if (null != caloriesSum && 0 != caloriesSum && caloriesSum < calorieGoal) { // is within calorie goal
				view.setBackgroundResource(R.drawable.calendar_icon_green);
			} else if (caloriesSum > calorieGoal) { // surpassed calorie goal
				view.setBackgroundResource(R.drawable.calendar_icon_red);
			} else if (this.month.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) && // is in the future
					this.month.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) && date.get(Calendar.DAY_OF_MONTH) > todayCalendar.get(Calendar.DAY_OF_MONTH)) {
				view.setBackgroundResource(R.drawable.calendar_icon_light_grey);
				dayView.setTextColor(R.color.dark_grey);
			} else {
				view.setBackgroundResource(R.drawable.calendar_icon_grey);
				dayView.setTextColor(R.color.dark_grey);
			}
		}
		
		dayView.setText(this.days[position]);

		return view;
	}

	public void refreshDays() {
		int lastDay = this.month.getActualMaximum(Calendar.DAY_OF_MONTH);
		int firstDay = (int) this.month.get(Calendar.DAY_OF_WEEK);

		// figure size of the array
		if (firstDay == 1) {
			this.days = new String[lastDay + (FIRST_DAY_OF_WEEK * 6)];
		} else {
			this.days = new String[lastDay + firstDay - (FIRST_DAY_OF_WEEK + 1)];
		}

		int j = FIRST_DAY_OF_WEEK;

		// populate empty days before first real day
		if (firstDay > 1) {
			for (j = 0; j < firstDay - FIRST_DAY_OF_WEEK; j++) {
				this.days[j] = "";
			}
		} else {
			for (j = 0; j < FIRST_DAY_OF_WEEK * 6; j++) {
				this.days[j] = "";
			}
			j = FIRST_DAY_OF_WEEK * 6 + 1; // sunday => 1, monday => 7
		}

		// populate days
		int dayNumber = 1;
		for (int i = j - 1; i < this.days.length; i++) {
			this.days[i] = "" + dayNumber;
			dayNumber++;
		}
	}

	public void loadDailyCaloriesMap() {
		Date date = this.month.getTime();
		Date firstDayOfMonth = new Date(date.getYear(), date.getMonth(), 1);

		Calendar nextMonth = Calendar.getInstance();
		nextMonth.setTime(firstDayOfMonth);
		nextMonth.add(Calendar.MONTH, 1);
		
		this.dailyCaloriesMap = DataHelper.getDailyCaloriesSum(firstDayOfMonth, nextMonth.getTime());
		this.dailyCaloriesGoalMap = DataHelper.getDailyCaloriesGoals(firstDayOfMonth, nextMonth.getTime());
	}
}
