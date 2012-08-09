package com.livestrong.myplate.activity;

import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

import com.admarvel.android.ads.AdMarvelView;
import com.livestrong.myplate.Constants;
import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplate.constants.BuildValues;
import com.livestrong.myplate.fragment.CommunityFragment;
import com.livestrong.myplate.fragment.DiaryFragment;
import com.livestrong.myplate.fragment.MoreFragment;
import com.livestrong.myplate.fragment.MyPlateFragment;
import com.livestrong.myplate.fragment.ProgressFragment;
import com.livestrong.myplate.utilities.AdvertisementHelper;
import com.livestrong.myplate.utilities.SessionMHelper;
import com.livestrong.myplatelite.R;
import com.sessionm.api.SessionM;
import com.sessionm.core.SessionMAndroidConfig;

public class TabBarActivity extends LiveStrongFragmentActivity implements OnTabChangeListener {
	private TabHost tabHost;
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, TabInfo>();
	private TabInfo lastTab = null;
	public String SessionMAchievement;
	
	private class TabInfo {
		private int position;
		private String tag;
		private String label;
		private Class<?> clss;
        private Bundle args;
        private Fragment fragment;
        private int drawableId;
        public <D extends Fragment> TabInfo(int position, String tag, String label, int drawableId, Class<D> clazz, Bundle args) {
        	this.position = position;
        	this.tag = tag;
        	this.label = label;
        	this.drawableId = drawableId;
        	this.clss = clazz;
        	this.args = args;
        	this.fragment = null;
        }
	}
	
    /**
	 * A simple factory that returns dummy views to the Tabhost
	 * @author mwho
	 */
	class TabFactory implements TabContentFactory {

		private final Context context;

	    /**
	     * @param context
	     */
	    public TabFactory(Context context) {
	        this.context = context;
	    }

	    /** (non-Javadoc)
	     * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
	     */
	    public View createTabContent(String tag) {
	        View v = new View(context);
	        v.setMinimumWidth(0);
	        v.setMinimumHeight(0);
	        return v;
	    }

	}
	
	private void initializeAdvertisement() {
		// Request AdM
		AdvertisementHelper.requestAd((AdMarvelView) findViewById(R.id.ad), this);
	}
	
	/**
	 * Initialise the Tab Host
	 */
	private void initialiseTabHost(Bundle args) {
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();
        
        TabInfo tabInfo;
        addTab(tabInfo = new TabInfo(0, "tab1", "MyPlate", R.drawable.tab_item_my_plate, MyPlateFragment.class, args));
        this.lastTab = tabInfo;
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        addTab(tabInfo = new TabInfo(1, "tab2", "Diary", R.drawable.tab_item_diary, DiaryFragment.class, args));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        addTab(tabInfo = new TabInfo(2, "tab3", "Progress", R.drawable.tab_item_progress, ProgressFragment.class, args));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        addTab(tabInfo = new TabInfo(3, "tab4", "Community", R.drawable.tab_item_community, CommunityFragment.class, args));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        addTab(tabInfo = new TabInfo(4, "tab5", "More", R.drawable.tab_item_more, MoreFragment.class, args));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        
		tabHost.setOnTabChangedListener(this);
	}
	
	private void addTab(TabInfo tabInfo) {
		TabHost.TabSpec spec = tabHost.newTabSpec(tabInfo.tag);
		
		// Layout tab bar button
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, tabHost.getTabWidget(), false);
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(tabInfo.label);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setImageResource(tabInfo.drawableId);
		spec.setIndicator(tabIndicator);
		spec.setContent(this.new TabFactory(this));

        // Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
		FragmentManager fragmentManager = this.getSupportFragmentManager();
        tabInfo.fragment = fragmentManager.findFragmentByTag(spec.getTag());
        if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.detach(tabInfo.fragment);
            ft.commit();
            fragmentManager.executePendingTransactions();
        }
		
		tabHost.addTab(spec);
	}

	/* (non-Javadoc)
	 * @see android.widget.TabHost.OnTabChangeListener#onTabChanged(java.lang.String)
	 */
	
	public void onTabChanged(String tag) {
		//Log.d("TabBarActivity", "TabBarActivity - onTabChange: " + tag);
		TabInfo newTab = this.mapTabInfo.get(tag);
		if (lastTab == null || lastTab != newTab) {
			///Log.d("TabBarActivity", "TabBarActivity - create new Tab: " + tag);
			FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
            
    		if (newTab != null) {
                if (newTab.fragment == null) {
                	//Log.d("TabBarActivity", "TabBarActivity - no saved tab fragment instanciate it: " + tag);
                	newTab.fragment = Fragment.instantiate(this, newTab.clss.getName(), newTab.args);
                } else {
                	//Log.d("TabBarActivity", "TabBarActivity - tab fragment already exists: " + newTab.fragment.getClass().toString());
                }

                fragmentTransaction.replace(R.id.frame, newTab.fragment, newTab.tag);
            }
                        
            lastTab = newTab;
            fragmentTransaction.commit();
            this.getSupportFragmentManager().executePendingTransactions();
		}
    }
	
	/**
	 * 
	 * Lifecycle Functions
	 * 
	 */
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		// The activity is being created; create views, bind data to lists, etc.
		setContentView(R.layout.activity_tab_bar);
		
		// Light version contains ads
		if (BuildValues.IS_LIGHT)
			initializeAdvertisement();
		
		this.mapTabInfo = new HashMap<String, TabInfo>();
		this.initialiseTabHost(savedInstanceState);
		this.lastTab = null;
		
		//initialize the pager
		Fragment newFragment = null;
		String tag = null;
		
		if (savedInstanceState != null) {
			
			tag = savedInstanceState.getString("tab"); 
			this.mapTabInfo.get(tag).fragment = null; // set to null to ensure the tab is instantiated 
			Log.d("TabBarActivity", "TabBarActivity - savedInstanceState savedTab: " + tag);
			tabHost.setCurrentTabByTag(tag); //set the tab as per the saved state
        } 
        
		if (tag == null){
			Log.d("TabBarActivity", "TabBarActivity - no saved State. Load MyPlate Tab");
			tag = "tab1";
			tabHost.setCurrentTabByTag(tag);
        }
		
		this.onTabChanged(tag);

        // This is used to removed a banding effect caused when drawing gradients in list view items
        getWindow().setFormat(PixelFormat.RGBA_8888); 
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);   
        
	}
	// Initialize menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, "Track").setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, 2, 0, "Track Weight").setIcon(android.R.drawable.ic_menu_add);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		if (item.getItemId() == 1){
			Intent intent = new Intent(this, TrackActivity.class);
			startActivityForResult(intent, 1);
		} else if (item.getItemId() == 2) {
			Intent intent = new Intent(this, AddWeightActivity.class);
			startActivityForResult(intent, 1);
		}
		
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// An activity might post a SessionM event
		if (data != null && data.getExtras() != null) {
			SessionMAchievement = data.getExtras().getString(SessionMHelper.INTENT_SESSIONM);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Called before making the activity vulnerable to destruction; save your activity state in outState.
		// UI elements states are saved automatically by super.onSaveInstanceState()
		if (lastTab != null){
			outState.putString("tab", lastTab.tag);
		}
	}

	@Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        
        SessionM.getInstance().onActivityResume(this);
        
        if (SessionMAchievement != null)
		{
			SessionM.getInstance().presentActivity(SessionM.ActivityType.ACHIEVEMENT, SessionMAchievement);
			SessionMAchievement = null;
		}
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused"); commit unsaved changes to persistent data, etc.
        // -> onStop()
        
        SessionM.getInstance().onActivityPause(this);
    }
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	
    	SessionM.getInstance().onActivityStart(this);
    }
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    
    	SessionM.getInstance().onActivityStop(this);
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
		
		AdMarvelView adMarvelView = (AdMarvelView) findViewById(R.id.ad);
		if (adMarvelView != null) {
			adMarvelView.destroy();
		}
		
		// The activity is about to be destroyed.
		if (isFinishing()) {
			// Someone called finish()
		} else {
			// System is temporarily destroying this instance of the activity to save space
		}
	}
	
}
