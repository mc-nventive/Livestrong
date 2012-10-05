package com.demandmedia.livestrong.android.adapters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.demandmedia.livestrong.android.MyPlateApplication;
import com.demandmedia.livestrong.android.R;
import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.models.DiaryEntries;
import com.demandmedia.livestrong.android.back.models.ExerciseDiaryEntry;
import com.demandmedia.livestrong.android.back.models.FoodDiaryEntry;
import com.demandmedia.livestrong.android.back.models.LiveStrongDisplayableListItem;
import com.demandmedia.livestrong.android.back.models.WaterDiaryEntry;
import com.demandmedia.livestrong.android.back.models.WeightDiaryEntry;
import com.demandmedia.livestrong.android.back.models.FoodDiaryEntry.TimeOfDay;
import com.demandmedia.livestrong.android.utilities.ImageLoader;
import com.demandmedia.livestrong.android.utilities.PinnedHeaderListView;
import com.demandmedia.livestrong.android.utilities.PinnedHeaderListView.PinnedHeaderAdapter;

public class DiaryAdapter extends AbstractBaseAdapterDataHelperDelegate implements SectionIndexer, OnScrollListener, PinnedHeaderAdapter {
		
	private List<LiveStrongDisplayableListItem> mEntries = new ArrayList<LiveStrongDisplayableListItem>();
	private List<Integer> mEntrySection = new ArrayList<Integer>();
	private String[] mSections = new String[]{"Breakfast", "Lunch", "Dinner", "Snack", "Exercise", "Water", "Weight", "Net Calories"};
	private List<Integer> mSectionCalorieCounts = new ArrayList<Integer>();
	private Date mDate;
	private DiaryEntries mDiaryEntry;
	private ImageLoader imageLoader;
	
	// cached values for performance improvement
	private int[] mPositionForSection;
	private int[] mPositionForNextSection;
	
	public DiaryAdapter(Activity activity) {
		this.activity = activity;
		this.imageLoader = new ImageLoader(activity);
	}

	private void loadDiaryEntry(){
		this.mEntries.clear();
		this.mEntrySection.clear();
		this.mSectionCalorieCounts.clear();
		
		// TODO This can be made async by sending this as the 2nd argument
		this.mDiaryEntry = DataHelper.getDailyDiaryEntries(this.mDate, null);
		
		// Get Food entries
		Map<TimeOfDay, List<FoodDiaryEntry>> foodEntries = this.mDiaryEntry.getFoodEntriesPerTimeOfDay();
		
		// Add Breakfast entries
		List<FoodDiaryEntry> breakfastEntries = foodEntries.get(TimeOfDay.BREAKFAST);
		Integer count = 0;
		for (FoodDiaryEntry foodDiaryEntry : breakfastEntries) {
			this.mEntries.add(foodDiaryEntry);
			this.mEntrySection.add(0);
			count += foodDiaryEntry.getCals();
		}
		this.mSectionCalorieCounts.add(0, count);
		
		// Add Lunch entries
		List<FoodDiaryEntry> lunchEntries = foodEntries.get(TimeOfDay.LUNCH);
		count = 0;
		for (FoodDiaryEntry foodDiaryEntry : lunchEntries) {
			this.mEntries.add(foodDiaryEntry);
			this.mEntrySection.add(1);
			count += foodDiaryEntry.getCals();
		}
		this.mSectionCalorieCounts.add(1, count);
		
		// Add Dinner entries
		List<FoodDiaryEntry> dinnerEntries = foodEntries.get(TimeOfDay.DINNER);
		count = 0;
		for (FoodDiaryEntry foodDiaryEntry : dinnerEntries) {
			this.mEntries.add(foodDiaryEntry);
			this.mEntrySection.add(2);
			count += foodDiaryEntry.getCals();
		}
		this.mSectionCalorieCounts.add(2, count);
		
		// Add Snack entries
		List<FoodDiaryEntry> snackEntries = foodEntries.get(TimeOfDay.SNACKS);
		count = 0;
		for (FoodDiaryEntry foodDiaryEntry : snackEntries) {
			this.mEntries.add(foodDiaryEntry);
			this.mEntrySection.add(3);
			count += foodDiaryEntry.getCals();
		}
		this.mSectionCalorieCounts.add(3, count);
		
		// Add Exercise Entries
		List<ExerciseDiaryEntry> exerciseEntries = this.mDiaryEntry.getExerciseEntries();
		count = 0;
		for (ExerciseDiaryEntry exerciseDiaryEntry: exerciseEntries) {
			this.mEntries.add(exerciseDiaryEntry);
			this.mEntrySection.add(4);
			count += exerciseDiaryEntry.getCals();
		}
		this.mSectionCalorieCounts.add(4, count);
		
		// Add Water Entry
		WaterDiaryEntry waterDiaryEntry = this.mDiaryEntry.getWaterEntry(this.mDate);
		if (waterDiaryEntry != null) {
			this.mEntries.add(waterDiaryEntry);
			this.mEntrySection.add(5);
		}
		this.mSectionCalorieCounts.add(5, null);
		
		// Add Weight Entry
		WeightDiaryEntry weightDiaryEntry = this.mDiaryEntry.getWeightEntry(this.mDate);
		if (weightDiaryEntry != null) {
			this.mEntries.add(weightDiaryEntry);
			this.mEntrySection.add(6);
		}
		this.mSectionCalorieCounts.add(6, null);
		
		// Add entry for Net Calories entry
		if (this.mEntrySection.size() > 0){
			this.mEntrySection.add(7);
		}
		this.mSectionCalorieCounts.add(7, this.mDiaryEntry.getNetCalories(this.mDate));
		
		this.loadPositionForSectionArray();
		this.loadPositionForNextSectionArray();
	}
	
	public void setDate(Date date){
		this.mDate = date;
		this.loadDiaryEntry();
		notifyDataSetChanged();			
	}
	
	@Override
	public int getCount() {
		return (this.mEntrySection == null) ? 0 : mEntrySection.size();
	}

	@Override
	public Object getItem(int position) {
		if (position >= 0 && position < mEntries.size()){
			return mEntries.get(position);	
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Context context = MyPlateApplication.getContext();
		
		LiveStrongDisplayableListItem entryObject = (LiveStrongDisplayableListItem)getItem(position);
		
		if (entryObject != null){
			if (convertView == null || convertView.getId() != R.id.listItemDiary){
				convertView = LayoutInflater.from(context).inflate(R.layout.list_item_diary, null);				
			}
			TextView nameLabel = (TextView)convertView.findViewById(R.id.foodNameTextView);
			TextView descriptionLabel = (TextView)convertView.findViewById(R.id.foodDescriptionTextView);	
			nameLabel.setText(entryObject.getTitle());
			descriptionLabel.setText(entryObject.getDescription());
			
			ImageView imageView = (ImageView) convertView.findViewById(R.id.foodImageView);
			
			switch (getSectionForPosition(position)) {
				case 0:
					imageView.setImageResource(R.drawable.icon_breakfast);
					break;
				case 1:
					imageView.setImageResource(R.drawable.icon_lunch);				
					break;
				case 2:
					imageView.setImageResource(R.drawable.icon_dinner);
					break;
				case 3:
					imageView.setImageResource(R.drawable.icon_snacks);
					break;
				case 4:
					imageView.setImageResource(R.drawable.icon_fitness);
					break;
				case 5:
					imageView.setImageResource(R.drawable.icon_water);
					break;
				case 6:
					imageView.setImageResource(R.drawable.icon_weight);
					break;
			}
			
			imageView.setTag(entryObject.getSmallImage());
			this.imageLoader.DisplayImage(entryObject.getSmallImage(), imageView);
						
			RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.listItemFood);
			layout.setBackgroundResource(R.drawable.list_item_diary_background_selector);
		} else {
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_diary_totals, null);
			
			int caloriesConsumed = this.mDiaryEntry.getCaloriesConsumed(this.mDate);
			int caloriesBurned = this.mDiaryEntry.getCaloriesBurned(this.mDate);
			int netCalories = this.mDiaryEntry.getNetCalories(this.mDate);
			
			TextView caloriesConsummedTextView = (TextView) convertView.findViewById(R.id.caloriesConsumed);
			TextView caloriesBurnedTextView = (TextView) convertView.findViewById(R.id.caloriesBurned);
			TextView netCaloriesTextView = (TextView) convertView.findViewById(R.id.netCalories);
			
			caloriesConsummedTextView.setText(caloriesConsumed + "");
			caloriesBurnedTextView.setText(caloriesBurned + "");
			netCaloriesTextView.setText(netCalories + "");
		}
		
		bindSectionHeader(convertView, position);
		
		return convertView;
	}
	
    private void bindSectionHeader(View itemView, int position) {
    	final View headerView = itemView.findViewById(R.id.listItemDiaryHeader);
    	final View dividerView = itemView.findViewById(R.id.list_divider);
    	   	
		final int section = getSectionForPosition(position);
        if (getPositionForSection(section) == position) {
        	this.configurePinnedHeader(itemView, position, 1); // layout content of header
            headerView.setVisibility(View.VISIBLE);
	    	dividerView.setVisibility(View.GONE);
        } else {
        	headerView.setVisibility(View.GONE);
	    	dividerView.setVisibility(View.VISIBLE);
        }

        // remove the divider for the last item in a section
        if (getPositionForNextSection(section) - 1 == position) {
        	dividerView.setVisibility(View.GONE);
        } else {
	    	dividerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadPositionForSectionArray(){
    	this.mPositionForSection = new int[this.mSections.length];
    	for (int sectionIndex = 0; sectionIndex < this.mSections.length; sectionIndex++){
    		this.mPositionForSection[sectionIndex] = -1; // Initialize entry
    		
    		for (int i = 0; i < this.mEntrySection.size(); i++){ // Search for for entry with current sectionIndex
    			if (this.mEntrySection.get(i) == sectionIndex){ 
    				this.mPositionForSection[sectionIndex] = i;
    				break;
    			}
    		}    		
    	}
    }
    
	public int getPositionForSection(int sectionIndex) {
		return this.mPositionForSection[sectionIndex];
    }
	
	private void loadPositionForNextSectionArray(){
		this.mPositionForNextSection = new int[this.mSections.length];
    	for (int sectionIndex = 0; sectionIndex < this.mSections.length; sectionIndex++){
    		this.mPositionForNextSection[sectionIndex] = -1; // Initialize entry
    		
    		int nextSectionIndex = sectionIndex + 1;
    		
    		while (nextSectionIndex < this.mSections.length){ // Make sure nextSectionIndex has elements in list, if not get next section
    			if (this.mEntrySection.contains(nextSectionIndex)){
    				break;
    			} 
    			nextSectionIndex++;
    		}
    		    		
    		for (int i = 0; i < this.mEntrySection.size(); i++){ // Search for for entry with nextSectionIndex
    			if (this.mEntrySection.get(i) == nextSectionIndex){
    				this.mPositionForNextSection[sectionIndex] = i;
    				break;
    			}
    		}
    		 		
    	}
	}
	
	public int getPositionForNextSection(int sectionIndex) {
		return this.mPositionForNextSection[sectionIndex];
	}
	
	public int getSectionForPosition(int position) {
		if (position < 0 || position >= this.mEntrySection.size()){
			return -1;
		}
		
		return this.mEntrySection.get(position);
	}
	
	@Override
	public Object[] getSections() {
		return this.mSections;
	}
      
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (view instanceof PinnedHeaderListView) {
            ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
        }		
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
	}

	@Override
	public int getPinnedHeaderState(int position) {
        if (getCount() == 0) {
            return PINNED_HEADER_GONE;
        }

        if (position < 0) {
            return PINNED_HEADER_GONE;
        }

        // The header should get pushed up if the top item shown
        // is the last item in a section for a particular letter.
        int section = getSectionForPosition(position);
        int nextSectionPosition = getPositionForNextSection(section);
        
        if (nextSectionPosition != -1 && position == nextSectionPosition - 1) {
            return PINNED_HEADER_PUSHED_UP;
        }

        return PINNED_HEADER_VISIBLE;
	}

	@Override
	public void configurePinnedHeader(View v, int position, int alpha) {
		TextView sectionLabel = (TextView)v.findViewById(R.id.sectionHeaderTextView);
		TextView caloriesTextView = (TextView)v.findViewById(R.id.caloriesTextView);
		TextView caloriesLabel = (TextView)v.findViewById(R.id.caloriesLabel);
		
		final int section = getSectionForPosition(position);
		if (section >= 0 && section < this.mSections.length){
			final String title = (String) getSections()[section];
			sectionLabel.setText(title);
			Integer caloriesCount = this.mSectionCalorieCounts.get(section);
			if (caloriesCount != null){
				caloriesTextView.setVisibility(View.VISIBLE);
				caloriesTextView.setText(caloriesCount + "");
				caloriesLabel.setVisibility(View.VISIBLE);
			} else {
				caloriesTextView.setVisibility(View.INVISIBLE);
				caloriesLabel.setVisibility(View.INVISIBLE);
			}
		}
	}	
}
