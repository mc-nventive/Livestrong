package com.livestrong.myplate.back.models;

import java.text.NumberFormat;
import java.util.Date;

import android.os.AsyncTask;

import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.utilities.SimpleDate;

public class UserProgress {

	private final int _dailyCaloriesGoal;
	private int caloriesSum;
	
	public UserProgress(Date date, int dailyCaloriesGoal) {
		SimpleDate day = new SimpleDate(date);

		_dailyCaloriesGoal = dailyCaloriesGoal;
		
		DiaryEntries dailyDiary = new DiaryEntries();
		dailyDiary.loadEntriesForDay(day);
		this.caloriesSum = dailyDiary.getNetCalories(day);
	}
	
	public String getProgress() {
		NumberFormat format = NumberFormat.getInstance();
		if (isOverGoal()) {
			return format.format(this.caloriesSum - _dailyCaloriesGoal) + " Over Goal";
		}
		return format.format(_dailyCaloriesGoal - this.caloriesSum) + " Remaining Today";
	}
	
	public String getDailyCaloriesGoal(){
		NumberFormat format = NumberFormat.getInstance();
		
		return format.format(_dailyCaloriesGoal);
	}
	
	public boolean isOverGoal() {
		return this.caloriesSum > _dailyCaloriesGoal;
	}
	
	public double getProgressBarPercentage() {
		return Math.min((double) caloriesSum / _dailyCaloriesGoal, 1.0);
	}
}
