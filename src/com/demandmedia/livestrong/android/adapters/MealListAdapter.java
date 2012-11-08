package com.demandmedia.livestrong.android.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.demandmedia.livestrong.android.MyPlateApplication;
import com.livestrong.myplate.R;
import com.demandmedia.livestrong.android.back.models.Meal;
import com.demandmedia.livestrong.android.back.models.MealItem;
import com.demandmedia.livestrong.android.back.models.FoodDiaryEntry.TimeOfDay;
import com.demandmedia.livestrong.android.utilities.ImageLoader;

public class MealListAdapter extends BaseAdapter {
	
	private Activity activity;
	private Meal meal;
	private List<MealItem> mealItems;
	public ImageLoader imageLoader; 
	    
	public MealListAdapter(Activity a, Meal meal) {
		this.activity = a;
		this.meal = meal;
		this.mealItems = new ArrayList<MealItem>( (Collection<MealItem>) this.meal.getItems());
		this.imageLoader = new ImageLoader(this.activity);
	}
		
	@Override
	public int getCount() {
		return (mealItems == null) ? 0 : mealItems.size();
	}

	@Override
	public Object getItem(int position) {
		return (mealItems == null) ? 0 : mealItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Context context = MyPlateApplication.getContext();
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_food, null);			
		}
		
		// Retrieve food at position
		MealItem item = (MealItem) getItem(position);
		
		TextView foodNameTextField = (TextView)convertView.findViewById(R.id.foodNameTextView);
		foodNameTextField.setText(item.getTitle());
		
		TextView foodDescriptionTextField = (TextView)convertView.findViewById(R.id.foodDescriptionTextView);
		foodDescriptionTextField.setText(item.getDescription());
		
		ImageView imageView = (ImageView)convertView.findViewById(R.id.foodImageView);
		Log.d("D", "imageName: " + item.getSmallImage());
		
		TimeOfDay timeOfDay = MyPlateApplication.getWorkingTimeOfDay();		
		if (timeOfDay == TimeOfDay.BREAKFAST){
			imageView.setImageResource(R.drawable.icon_breakfast);
		} else if (timeOfDay == TimeOfDay.LUNCH){
			imageView.setImageResource(R.drawable.icon_lunch);
		} else if (timeOfDay == TimeOfDay.DINNER){
			imageView.setImageResource(R.drawable.icon_dinner);
		} else {
			imageView.setImageResource(R.drawable.icon_snacks);
		}
		
		this.imageLoader.DisplayImage(item.getSmallImage(), imageView);
		
		return convertView;
	}
}
