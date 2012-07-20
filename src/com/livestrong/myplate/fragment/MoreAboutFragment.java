package com.livestrong.myplate.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livestrong.myplate.Constants;
import com.livestrong.myplate.R;

public class MoreAboutFragment extends FragmentDataHelperDelegate {
	
	private LinearLayout view;
	
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
		
		// Hook up outlets
		this.view = (LinearLayout) inflater.inflate(R.layout.fragment_more_about, container, false);	
		
		TextView versionTextView = (TextView) this.view.findViewById(R.id.versionTextView);
		versionTextView.setText("Version " + Constants.APPLICATION_VERSION);
		
		return this.view;
	}
}
