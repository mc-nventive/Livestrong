package com.demandmedia.livestrong.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.demandmedia.livestrong.android.Constants;
import com.demandmedia.livestrong.android.R;
import com.demandmedia.livestrong.android.adapters.ExerciseSelectorAdapter;
import com.demandmedia.livestrong.android.animations.DropDownAnimation;
import com.demandmedia.livestrong.android.back.api.ApiHelper;
import com.demandmedia.livestrong.android.back.models.Exercise;
import com.demandmedia.livestrong.android.views.ClearableEditText;
import com.flurry.android.FlurryAgent;

public class ExerciseSelectorActivity extends LiveStrongActivity implements OnItemClickListener {
	
	ListView list;
	private ExerciseSelectorAdapter exerciseSelectorAdapter;
	private EditText searchEditText;
	private LinearLayout toolBarContainer, messageContainer;
	private int toolBarHeight;
	private Button recentlyPerformedBtn, frequentlyPerformedBtn, customExercisesBtn, addManualButton;
	private TextView messageTextView;
	private Boolean isSearching = false;
	
	protected void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
	        
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_exercise_selector);
        
        // Hook up outlets
        this.list 				    = (ListView)findViewById(R.id.listView);
        this.searchEditText 	    = ((ClearableEditText)findViewById(R.id.edit_text_clearable)).getEditText();
        this.toolBarContainer	    = (LinearLayout)findViewById(R.id.toolBarContainer);
        this.recentlyPerformedBtn 	= (Button)findViewById(R.id.recentlyPerformedButton);
        this.frequentlyPerformedBtn = (Button)findViewById(R.id.frequentlyPerformedButton);
        this.customExercisesBtn     = (Button)findViewById(R.id.customExercisesButton);
        this.addManualButton		= (Button)findViewById(R.id.addManualButton);
        this.messageTextView 		= (TextView)findViewById(R.id.messageTextView);
        this.messageContainer 		= (LinearLayout)findViewById(R.id.messageContainer);
        
        // Initialize list view
        this.list.setOnItemClickListener(this);
        this.exerciseSelectorAdapter = new ExerciseSelectorAdapter(this, null);
        this.list.setAdapter(this.exerciseSelectorAdapter);
        this.list.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyboard();
				return false;
			}
		});
     
        this.initializeSearchBar();
        this.initializeButtons();
        
        // This is used to removed a banding effect caused when drawing gradients in list view items
        getWindow().setFormat(PixelFormat.RGBA_8888); 
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER); 
        
        loadListFromSelectedButton();
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
			
			String exerciseName = data.getExtras().getString(AddExerciseActivity.INTENT_EXERCISE_NAME);
			this.displayNotification(exerciseName + " was added to your diary.");
			
			setResult(Activity.RESULT_OK);
		}
	}
	
	// Initialize menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, "Add Exercise Manually").setIcon(android.R.drawable.ic_menu_add);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		this.showCreateExerciseActivity();
		
		return true;
	}
	
	private void showCreateExerciseActivity(){
		Intent intent = new Intent(this, CreateExerciseActivity.class);
		startActivityForResult(intent, 1);
	}
	
	private void initializeSearchBar(){
		this.searchEditText.setHint("Search for exercise...");
		this.searchEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		
        this.searchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {				
				if (s.length() == 0){
					// Hide toolbar when field is empty
					showToolBar();
					// Reload list with the appropriate data depending on the selected button
					loadListFromSelectedButton();
					isSearching = false;
				} else {
					hideToolBar();
					isSearching = true;
					// Perform local exercise search
					String searchStr = String.valueOf(s);
					ExerciseSelectorActivity.this.exerciseSelectorAdapter.loadExercisesFromLocalSearch(searchStr);
					
					messageContainer.setVisibility(View.INVISIBLE);
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
						performServerSearch();
						
						// Hide Keyboard
						hideKeyboard();
						
						return true;
					} 
				}
				return false;
			}
        });
	}
	
	private void initializeButtons(){
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
					case R.id.recentlyPerformedButton:
						messageTextView.setText("No recently performed exercises.");
						break;
					case R.id.frequentlyPerformedButton:
						messageTextView.setText("No frequently performed exercises.");
						break;					
					case R.id.customExercisesButton:
						messageTextView.setText("No custom exercises.");
						break;
				}
				loadListFromSelectedButton();
				ExerciseSelectorActivity.this.list.setSelectionAfterHeaderView();
				hideKeyboard();
			}
		};

		this.recentlyPerformedBtn.setSelected(true);
		this.recentlyPerformedBtn.setOnClickListener(onClickListener);
		this.frequentlyPerformedBtn.setOnClickListener(onClickListener);
		this.customExercisesBtn.setOnClickListener(onClickListener);
		
		this.addManualButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showCreateExerciseActivity();
			}
		});
	}
	
	private void loadListFromSelectedButton(){
		if (this.recentlyPerformedBtn.isSelected()){
			this.exerciseSelectorAdapter.loadRecentlyPerformed();
		} else if (this.frequentlyPerformedBtn.isSelected()){
			this.exerciseSelectorAdapter.loadFrequentlyPerformed();
		} else if (this.customExercisesBtn.isSelected()){
			this.exerciseSelectorAdapter.loadCustomExercises();
		}
		
		if (this.exerciseSelectorAdapter.getCount() == 0){
			this.messageContainer.setVisibility(View.VISIBLE);
		} else {
			this.messageContainer.setVisibility(View.INVISIBLE);
		}
	}
	
	private void unSelectTabButtons(){
		this.recentlyPerformedBtn.setSelected(false);
		this.frequentlyPerformedBtn.setSelected(false);
		this.customExercisesBtn.setSelected(false);
	}
	
	private void performServerSearch(){
		if (ApiHelper.isOnline()){
			String searchString = searchEditText.getText().toString();
			this.exerciseSelectorAdapter.loadExercisesFromServerSearch(searchString);
			FlurryAgent.logEvent(Constants.Flurry.EXERCISE_SEARCH_EVENT);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		if (this.exerciseSelectorAdapter.showNoResultsMessage || this.exerciseSelectorAdapter.isLoading()){
			return;
		}
		
		// If user is searchign
		if (this.isSearching){
			Object object = this.exerciseSelectorAdapter.getItem(position);
			if (object == null && this.exerciseSelectorAdapter.isShowingSearchOnlinePrompt()){ // If user selects online search list item
				this.performServerSearch();
				return;
			} 
		}
	
		// Regular exercise item selected
		Exercise exercise = (Exercise) this.exerciseSelectorAdapter.getItem(position);
		Intent intent = new Intent(this, AddExerciseActivity.class);
		intent.putExtra(exercise.getClass().getName(), exercise);
		startActivityForResult(intent, 1);	
	}
	
	public void hideToolBar() {
		if (this.toolBarContainer.getHeight() == 0){
			return;
		}
		
		isSearching = true;
		
		this.toolBarHeight = this.toolBarContainer.getHeight();
		DropDownAnimation animation = new DropDownAnimation(this.toolBarContainer, 0);
		this.toolBarContainer.startAnimation(animation);
	}
	
	public void showToolBar() {
		Log.d("ExerciseSelector","showToolbar");
		if (this.toolBarContainer.getHeight() != 0){
			return;
		}
		
		isSearching = false;
		
		DropDownAnimation animation = new DropDownAnimation(this.toolBarContainer, this.toolBarHeight);
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
	
	@Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        // -> onResume()
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    	// Called before making the activity vulnerable to destruction; save your activity state in outState.
        // UI elements states are saved automatically by super.onSaveInstanceState()
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused"); commit unsaved changes to persistent data, etc.
        // -> onStop()
    }

    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // The activity was stopped, and is about to be started again. It was not destroyed, so all members are intact.
        // -> onStart()
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        if (isFinishing()) {
        	// Someone called finish()
        } else {
        	// System is temporarily destroying this instance of the activity to save space
        }
    }
}