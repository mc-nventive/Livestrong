package com.livestrong.myplate.adapters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplatelite.R;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.api.ApiHelper;
import com.livestrong.myplate.back.api.models.FoodSearchResponse;
import com.livestrong.myplate.back.models.Food;
import com.livestrong.myplate.back.models.FoodDiaryEntry.TimeOfDay;
import com.livestrong.myplate.back.models.LiveStrongDisplayableListItem;
import com.livestrong.myplate.utilities.ImageLoader;

public class FoodSelectorAdapter extends AbstractBaseAdapterDataHelperDelegate {

	private List<LiveStrongDisplayableListItem> listItems = new ArrayList<LiveStrongDisplayableListItem>();
	private ImageLoader imageLoader; 
	private TimeOfDay timeOfDay;
	private Boolean isLoading = false;
	private Boolean showSearchOnlinePromp = false;
	public Boolean showNoResultsMessage = false;
	
	public FoodSelectorAdapter(Activity activity, TimeOfDay timeOfDay) {
		this.activity = activity;
		this.imageLoader = new ImageLoader(this.activity);
		this.timeOfDay = timeOfDay;
		
		loadRecentlyEaten();
	}
	
	/**** Load list data functions ****/
	
	public void loadRecentlyEaten(){
		this.showSearchOnlinePromp = false;
		this.showNoResultsMessage = false;
		this.setListItems(DataHelper.getRecentFoods(this.timeOfDay, null));		
	}
	
	public void loadFrequentlyEaten(){
		this.showSearchOnlinePromp = false;
		this.showNoResultsMessage = false;
		this.setListItems(DataHelper.getFavoriteFoods(this.timeOfDay, null));
	}
	
	public void loadMyMeals(){
		this.showSearchOnlinePromp = false;
		this.showNoResultsMessage = false;
		this.setListItems(DataHelper.getMeals());
	}
	
	public void loadCustomFoods(){
		this.showSearchOnlinePromp = false;
		this.showNoResultsMessage = false;
		this.setListItems(DataHelper.getCustomFoods());
	}
	
	public void loadFoodFromLocalSearch(String searchStr){
		FoodSearchResponse foodSearchResponse = DataHelper.searchFoods(searchStr, true, null);
		this.setListItems((List<Food>)foodSearchResponse.getFoods());
		this.showSearchOnlinePromp = true;
		this.showNoResultsMessage = false;
	}
	
	public void loadFoodFromServerSearch(String searchStr){
		this.showSearchOnlinePromp = false;
		DataHelper.searchFoods(searchStr, false, this);
		this.isLoading = true;
		this.setListItems(null);
	}

	/**
	 * DataHelperDelegate call back
	 */
	@Override
	public void dataReceived(Method methodCalled, Object data) {
		this.isLoading = false;
		this.notifyDataSetChanged();
		if (data instanceof FoodSearchResponse) {
			FoodSearchResponse foodSearchResponse = (FoodSearchResponse) data;
			this.setListItems((List<Food>) foodSearchResponse.getFoods());
			if (foodSearchResponse.getFoods().size() == 0){
				this.showNoResultsMessage = true;
			}
		}
	}

	@Override
	public int getCount() {
		if (this.isLoading){
			return 1;
		}
		
		int count = 0;
		if (this.listItems != null){
			count = this.listItems.size();
		}
		
		if (this.showSearchOnlinePromp || this.showNoResultsMessage){
			count++;
		}
				
		return count;
	}

	@Override
	public Object getItem(int position) {
		if (this.isShowingSearchOnlinePrompt() && position >= this.listItems.size()){
			return null;
		}
		return (this.listItems == null) ? 0 : this.listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Context context = MyPlateApplication.getContext();
		
		// show custom views (loading, search prompt)
		if (this.isLoading){
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_loading, null);
			return convertView;
		} else if (this.showSearchOnlinePromp && position == this.listItems.size()){
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_message, null);
			TextView message = (TextView) convertView.findViewById(R.id.messageTextView);
			if (ApiHelper.isOnline()){
				message.setText("Tap to search online...");
			} else {
				message.setText("Offline - only previously tracked items available.");
			}			
			return convertView;
		} else if (this.showNoResultsMessage && position == this.listItems.size()){
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_message, null);
			((TextView)convertView.findViewById(R.id.messageTextView)).setText("No Results Found.");
			return convertView;
		}
		
		if (convertView == null || convertView.getId() != R.id.listItemFood) {
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_food, null);			
		}
		
		// Retrieve food at position
		LiveStrongDisplayableListItem item = (LiveStrongDisplayableListItem)getItem(position);
		if (item != null){
			TextView foodNameTextField = (TextView)convertView.findViewById(R.id.foodNameTextView);
			foodNameTextField.setText(item.getTitle());
			
			TextView foodDescriptionTextField = (TextView)convertView.findViewById(R.id.foodDescriptionTextView);
			foodDescriptionTextField.setText(item.getDescription());
			
			ImageView imageView = (ImageView)convertView.findViewById(R.id.foodImageView);
			if (timeOfDay == TimeOfDay.BREAKFAST){
				imageView.setImageResource(R.drawable.icon_breakfast);
			} else if (timeOfDay == TimeOfDay.LUNCH){
				imageView.setImageResource(R.drawable.icon_lunch);
			} else if (timeOfDay == TimeOfDay.DINNER){
				imageView.setImageResource(R.drawable.icon_dinner);
			} else {
				imageView.setImageResource(R.drawable.icon_snacks);
			}
			
			imageView.setTag(item.getSmallImage());
			this.imageLoader.DisplayImage(item.getSmallImage(), imageView);			
		}
		
		return convertView;
	}
	
	@SuppressWarnings("unchecked")
	public void setListItems(List<?> listItems){
		this.listItems = (List<LiveStrongDisplayableListItem>) listItems;
		notifyDataSetChanged();
	}
	
	public void setTimeOfDay(TimeOfDay timeOfDay){
		this.timeOfDay = timeOfDay;
	}
	
	public Boolean isShowingSearchOnlinePrompt(){
		return this.showSearchOnlinePromp;
	}
	
	public Boolean isLoading(){
		return this.isLoading;
	}
}
