package com.livestrong.myplate.back.models;

import java.text.NumberFormat;
import java.util.Date;

import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.utilities.SimpleDate;

public class UserProgress {

	private int dailyCaloriesGoal;
	private int caloriesSum;
	
	public UserProgress(Date date) {
		SimpleDate day = new SimpleDate(date);

		this.dailyCaloriesGoal = DataHelper.getUserDailyCaloriesGoal();

		DiaryEntries dailyDiary = new DiaryEntries();
		dailyDiary.loadEntriesForDay(day);
		this.caloriesSum = dailyDiary.getNetCalories(day);
	}
	
	public String getProgress() {
		NumberFormat format = NumberFormat.getInstance();
		if (isOverGoal()) {
			return format.format(this.caloriesSum - this.dailyCaloriesGoal) + " Over Goal";
		}
		return format.format(this.dailyCaloriesGoal - this.caloriesSum) + " Remaining Today";
	}
	
	public String getDailyCaloriesGoal(){
		NumberFormat format = NumberFormat.getInstance();
		
		return format.format(this.dailyCaloriesGoal);
	}
	
	public boolean isOverGoal() {
		return this.caloriesSum > this.dailyCaloriesGoal;
	}
	
	public double getProgressBarPercentage() {
		return Math.min((double) caloriesSum / dailyCaloriesGoal, 1.0);
	}
}
