package com.demandmedia.livestrong.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.demandmedia.livestrong.android.MyPlateApplication;
import com.demandmedia.livestrong.android.R;
import com.demandmedia.livestrong.android.adapters.FoodSelectorAdapter;
import com.demandmedia.livestrong.android.animations.DropDownAnimation;
import com.demandmedia.livestrong.android.back.api.ApiHelper;
import com.demandmedia.livestrong.android.back.models.Food;
import com.demandmedia.livestrong.android.back.models.Meal;
import com.demandmedia.livestrong.android.back.models.FoodDiaryEntry.TimeOfDay;
import com.demandmedia.livestrong.android.views.ClearableEditText;

public class FoodSelectorActivity extends LiveStrongActivity implements OnItemClickListener {
	
	ListView list;
	private FoodSelectorAdapter foodSelectorAdapter;
	private EditText searchEditText;
	private LinearLayout toolBarContainer, messageContainer;
	private int toolBarHeight = -1;
	private Button recentlyEatenBtn, frequentlyEatenBtn, myMealsBtn, customFoodsButton, addManualButton;
	private ImageButton backBtn, forwardBtn;
	private TextView timeOfDayTextView, messageTextView;
	private Boolean isSearching = false;
	
	protected void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
	        
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_food_selector);
        
        // Hook up outlets
        this.list 				= (ListView) findViewById(R.id.listView);
        this.searchEditText 	= ((ClearableEditText) findViewById(R.id.editTextClearable)).getEditText();
        this.toolBarContainer	= (LinearLayout) findViewById(R.id.toolBarContainer);
        this.recentlyEatenBtn 	= (Button) findViewById(R.id.recentlyEatenButton);
        this.frequentlyEatenBtn = (Button) findViewById(R.id.frequentlyEatenButton);
        this.myMealsBtn 		= (Button) findViewById(R.id.myMealsButton);
        this.customFoodsButton 	= (Button) findViewById(R.id.customFoodsButton);
        this.addManualButton	= (Button) findViewById(R.id.addManualButton);
        this.backBtn 			= (ImageButton) findViewById(R.id.backButton);
        this.forwardBtn			= (ImageButton) findViewById(R.id.forwardButton);
        this.timeOfDayTextView	= (TextView) findViewById(R.id.timeOfDayTextView);
        this.messageTextView 	= (TextView) findViewById(R.id.messageTextView);
        this.messageContainer 	= (LinearLayout) findViewById(R.id.messageContainer);
        
        // Initialize Text Views
        this.timeOfDayTextView.setText(MyPlateApplication.getWorkingTimeOfDayString());
        
        // Initialize list view
        this.list.setOnItemClickListener(this);
        this.foodSelectorAdapter = new FoodSelectorAdapter(this, MyPlateApplication.getWorkingTimeOfDay());
        this.list.setAdapter(this.foodSelectorAdapter);
        this.list.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyboard();
				searchEditText.clearFocus();
				return false;
			}
		});
     
        this.initializeSearchBar();
        this.initializeTabButtons();
        this.initializeTimeOfDayButtons();
        
        // This is used to removed a banding effect caused when drawing gradients in list view items
        getWindow().setFormat(PixelFormat.RGBA_8888); 
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        
        this.loadListFromSelectedButton();
	}
	
	// Initialize menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, "Add Food Manually").setIcon(android.R.drawable.ic_menu_add);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		this.showAddManualActivity();
		
		return true;
	}
	
	private void initializeSearchBar(){
		this.searchEditText.setHint("Search for food...");
		this.searchEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		
        this.searchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {				
				if (s.length() == 0){
					// Hide toolbar when field is empty
					showToolBar();
					isSearching = false;
					// Reload list with the appropriate data depending on the selected button
					loadListFromSelectedButton();
				} else {
					hideToolBar();
					
					isSearching = true;
					
					// Perform local food search
					String searchStr = String.valueOf(s);
					foodSelectorAdapter.loadFoodFromLocalSearch(searchStr);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {	}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		});
        
        this.searchEditText.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {				
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						// Search for value in Edit Field
						FoodSelectorActivity.this.performServerSearch();
						
						// Hide Keyboard
						hideKeyboard();
						
						return true;
					} 
				}
				return false;
			}
        });
	}
	
	private void initializeTabButtons(){
		View.OnClickListener onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View button) {
				if (button.isSelected()){
					return;
				} 
				unSelectTabButtons();
				button.setSelected(true);
				
				int btnId = button.getId();
				switch (btnId){
					case R.id.recentlyEatenButton:
						//foodSelectorAdapter.loadRecentlyEaten();
						messageTextView.setText("No recently eaten food items.");
						break;
					case R.id.frequentlyEatenButton:
						//foodSelectorAdapter.loadFrequentlyEaten();
						messageTextView.setText("No frequently eaten food items.");
						break;					
					case R.id.myMealsButton:
						//foodSelectorAdapter.loadMyMeals();
						messageTextView.setText("No meals.");
						break;
					case R.id.customFoodsButton:
						messageTextView.setText("No custom foods.");
						break;
				}
				loadListFromSelectedButton();
				list.setSelectionAfterHeaderView();
				hideKeyboard();
			}
		};

		this.recentlyEatenBtn.setSelected(true);
		this.recentlyEatenBtn.setOnClickListener(onClickListener);
		this.frequentlyEatenBtn.setOnClickListener(onClickListener);
		this.myMealsBtn.setOnClickListener(onClickListener);
		this.customFoodsButton.setOnClickListener(onClickListener);
	}
	
	private void initializeTimeOfDayButtons(){
		View.OnClickListener onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View button) {
				TimeOfDay timeOfDay = MyPlateApplication.getWorkingTimeOfDay();
				TimeOfDay[] timesOfDay = TimeOfDay.values();
				
				// Get index of current TimeOfDay
				int index = -1;
				for (int i = 0; i < timesOfDay.length; i++) {
					if (timeOfDay == timesOfDay[i]){
						index = i;
						break;
					}
				}
				
				int btnId = button.getId();
				switch (btnId) {
				case R.id.backButton:
					index--;
					if (index < 0){
						index = timesOfDay.length - 1;
					}
					break;
				case R.id.forwardButton:
					index++;
					if (index >= timesOfDay.length){
						index = 0;
					}
					break;
				}
				
				MyPlateApplication.setWorkingTimeOfDay(timesOfDay[index]);
				timeOfDayTextView.setText(MyPlateApplication.getWorkingTimeOfDayString());
				foodSelectorAdapter.setTimeOfDay(MyPlateApplication.getWorkingTimeOfDay());
				loadListFromSelectedButton();
				list.setSelectionAfterHeaderView(); // Scroll to top of list
			}
		};
		
		this.backBtn.setOnClickListener(onClickListener);
		this.forwardBtn.setOnClickListener(onClickListener);
		
		this.addManualButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showAddManualActivity();
			}
		});
	}
	
	private void loadListFromSelectedButton(){
		if (this.recentlyEatenBtn.isSelected()){
			this.foodSelectorAdapter.loadRecentlyEaten();
		} else if (this.frequentlyEatenBtn.isSelected()){
			this.foodSelectorAdapter.loadFrequentlyEaten();
		} else if (this.myMealsBtn.isSelected()){
			this.foodSelectorAdapter.loadMyMeals();
		} else if (this.customFoodsButton.isSelected()){
			this.foodSelectorAdapter.loadCustomFoods();
		}
		
		if (this.foodSelectorAdapter.getCount() == 0){
			this.messageContainer.setVisibility(View.VISIBLE);
		} else {
			this.messageContainer.setVisibility(View.INVISIBLE);
		}
	}
	
	private void unSelectTabButtons(){
		this.recentlyEatenBtn.setSelected(false);
		this.frequentlyEatenBtn.setSelected(false);
		this.myMealsBtn.setSelected(false);
		this.customFoodsButton.setSelected(false);
	}
	
	private void showAddManualActivity(){
		Intent intent = new Intent(FoodSelectorActivity.this, CreateFoodActivity.class);
		startActivityForResult(intent, 1);
	}
	
	private void performServerSearch(){
		if (ApiHelper.isOnline()){
			String searchString = searchEditText.getText().toString();
			foodSelectorAdapter.loadFoodFromServerSearch(searchString);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		if (this.foodSelectorAdapter.showNoResultsMessage || this.foodSelectorAdapter.isLoading()){
			return;
		}
		
		// If User is searching
		if (this.isSearching){
			Object object = this.foodSelectorAdapter.getItem(position);
			if (object == null && this.foodSelectorAdapter.isShowingSearchOnlinePrompt()){
				this.performServerSearch();
				return;
			} else {
				Food food = (Food) object;
				Intent intent = new Intent(this, AddFoodActivity.class);
				intent.putExtra(food.getClass().getName(), food);
				startActivityForResult(intent, 1);
				return;
			}	
		} 
	
		// Regular food item selected
		if (this.recentlyEatenBtn.isSelected() || this.frequentlyEatenBtn.isSelected() || this.customFoodsButton.isSelected()){
			Food food = (Food) this.foodSelectorAdapter.getItem(position);
			Intent intent = new Intent(this, AddFoodActivity.class);
			intent.putExtra(food.getClass().getName(), food);
			startActivityForResult(intent, 1);	
		} else if (this.myMealsBtn.isSelected()){
			Meal meal = (Meal) this.foodSelectorAdapter.getItem(position);
			Intent intent = new Intent(this, AddMealActivity.class);
			intent.putExtra(meal.getClass().getName(), meal);
			startActivityForResult(intent, 1);	
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Activity.RESULT_OK){
			if (this.searchEditText.getText().length() > 0){
				this.searchEditText.setText("");
			} else {
				loadListFromSelectedButton();
			}
			
			String foodName = data.getExtras().getString(AddFoodActivity.INTENT_FOOD_NAME);
			this.displayNotification(foodName + " was added to your diary.");
			
			setResult(Activity.RESULT_OK);
		}
	}
	
	public void hideToolBar() {
		if (this.toolBarContainer.getHeight() == 0){
			return;
		}

		if (this.toolBarHeight == -1){
			this.toolBarHeight = this.toolBarContainer.getHeight();
		}
		
		isSearching = true;
		
		DropDownAnimation animation = new DropDownAnimation(this.toolBarContainer, 0);
		animation.setDuration(300);
		this.toolBarContainer.startAnimation(animation);
		
		this.messageContainer.setVisibility(View.INVISIBLE);
	}
	
	public void showToolBar() {
		if (this.toolBarContainer.getHeight() != 0){
			return;
		}
		
		isSearching = false;
		
		DropDownAnimation animation = new DropDownAnimation(this.toolBarContainer, this.toolBarHeight);
		animation.setDuration(300);
		this.toolBarContainer.startAnimation(animation);
	}
	
	private void hideKeyboard(){
		// Hide Keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
		searchEditText.clearFocus();
	}
	
	private void displayNotification(String notificationString){
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		
		Toast toast = Toast.makeText(context, notificationString, duration);
		//toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}
}