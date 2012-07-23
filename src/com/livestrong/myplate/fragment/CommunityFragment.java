package com.livestrong.myplate.fragment;

import java.lang.reflect.Method;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.livestrong.myplate.activity.CommunityCommentsActivity;
import com.livestrong.myplate.adapters.CommunityListAdapter;
import com.livestrong.myplate.animations.DropDownAnimation;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.models.CommunityMessage;
import com.livestrong.myplate.constants.BuildValues;
import com.livestrong.myplate.utilities.PullToRefreshListView;
import com.livestrong.myplate.utilities.PullToRefreshListView.OnRefreshListener;
import com.livestrong.myplate.utilities.Utils;
import com.livestrong.myplatelite.R;

public class CommunityFragment extends FragmentDataHelperDelegate implements OnItemClickListener {

	private ListView communityListView;
	private CommunityListAdapter communityListAdapter;
	private View view;
	private Button communityButton, myMessagesButton, sendButton;
	private ImageButton composeMessageButton;
	private RelativeLayout messageContainer;
	private LinearLayout toolbarContainer;
	private int messageContainerHeight;
	private ProgressBar progressBar;
	private EditText messageEditText;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			// We have different layouts, and in one of them this
			// fragment's containing frame doesn't exist. The fragment
			// may still be created from its saved state, but there is
			// no reason to try to create its view hierarchy because it
			// won't be displayed. Note this is not needed -- we could
			// just run the code below, where we would create and return
			// the view hierarchy; it would just never be used.
			return null;
		}
		this.view = (LinearLayout) inflater.inflate(R.layout.fragment_community, container, false);
		
		// Hook up UI outlets
		this.toolbarContainer = (LinearLayout) this.view.findViewById(R.id.toolbarContainer);
		this.messageContainer = (RelativeLayout) this.view.findViewById(R.id.messageContainer);
		this.progressBar = (ProgressBar) this.view.findViewById(R.id.progressBar);
		this.messageEditText = (EditText) this.view.findViewById(R.id.messageEditText);
		
		// Initialize views
		this.messageContainerHeight = dpToPixels(57);
		this.hideMessageContainer(false);
		this.progressBar.setVisibility(View.INVISIBLE);
		
		this.communityListView = (ListView) view.findViewById(R.id.communityListView);
		if (this.communityListAdapter == null){
			this.communityListAdapter = new CommunityListAdapter(getActivity(), this.communityListView);
		} else {
			this.communityListAdapter.setListView(this.communityListView);
		}
		this.communityListView.setAdapter(this.communityListAdapter);
		this.communityListView.setOnItemClickListener(this);

		((PullToRefreshListView) this.communityListView).setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (communityButton.isSelected()){
					communityListAdapter.reloadCommunityMessages();
				} else {
					if (DataHelper.isLoggedIn()){
						communityListAdapter.reloadUserMessages();
					} else {
						((PullToRefreshListView) communityListView).onRefreshComplete();
					}
				}
			}
		});
				
		this.initializeButtons();
		
		this.unselectToolbarButtons();
		
		Integer selectedTab = (Integer) DataHelper.getPref(DataHelper.PREFS_COMMUNITY_SELECTED_TAB, 0);
		switch (selectedTab) {
			case 0:
				loadCommunityMessages();
				break;
			case 1:
				loadUserMessages();
				break;
		}
		
		return view;
	}
	
	private void initializeButtons() {
		View.OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View button) {
				if (button.isSelected()) {
					return;
				}

				int btnId = button.getId();
				switch (btnId) {
				case R.id.communityButton:
					loadCommunityMessages();
					break;
				case R.id.myMessagesButton:
					loadUserMessages();
					break;
				case R.id.composeMessageButton:
					writeMessageButtonPressed();
					break;
				}
			}
		};

		this.communityButton = (Button) this.view.findViewById(R.id.communityButton);
		this.myMessagesButton = (Button) this.view.findViewById(R.id.myMessagesButton);
		this.composeMessageButton = (ImageButton) this.view.findViewById(R.id.composeMessageButton);

		this.communityButton.setOnClickListener(onClickListener);
		this.myMessagesButton.setOnClickListener(onClickListener);
		this.composeMessageButton.setOnClickListener(onClickListener);
		
		this.sendButton = (Button) this.view.findViewById(R.id.sendButton);
		this.sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (DataHelper.isLoggedIn()){
					String message = CommunityFragment.this.messageEditText.getText().toString();
					if (message.length() > 0){
						progressBar.setVisibility(View.VISIBLE);
						sendButton.setVisibility(View.INVISIBLE);
						messageEditText.setEnabled(false);
						hideKeyboard();
						DataHelper.postNewMessage(message, CommunityFragment.this);
					}
				} else {
					new AlertDialog.Builder(getActivity())
				      .setMessage("You must be signed in to post messages.")
				      .setNeutralButton(android.R.string.ok,
				         new DialogInterface.OnClickListener() {
				         public void onClick(DialogInterface dialog, int whichButton){}
				         })
				      .show();
				}
			}
		});
	}

	private void unselectToolbarButtons() {
		this.communityButton.setSelected(false);
		this.myMessagesButton.setSelected(false);
		this.composeMessageButton.setSelected(false);
	}

	private void loadCommunityMessages() {
		this.unselectToolbarButtons();
		this.communityButton.setSelected(true);
		this.communityListAdapter.loadCommunityMessages(1);
		DataHelper.setPref(DataHelper.PREFS_COMMUNITY_SELECTED_TAB, 0);
	}

	private void loadUserMessages() {
		if (DataHelper.isLoggedIn() == false || BuildValues.IS_LIGHT){
			new AlertDialog.Builder(getActivity())
		      .setMessage("This function is not available in the lite version. Would you like to visit the Play store now?")
		      .setTitle("")
		      .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Utils.openPlayStore(getActivity());
				}
			  })
			  .setNegativeButton(android.R.string.no, null)
		      .show();
			return;
		}
		
		this.unselectToolbarButtons();
		this.myMessagesButton.setSelected(true);
		this.communityListAdapter.loadUserMessages(1);
		DataHelper.setPref(DataHelper.PREFS_COMMUNITY_SELECTED_TAB, 1);
	}

	private void writeMessageButtonPressed() {
		if (DataHelper.isLoggedIn() == false || BuildValues.IS_LIGHT){
			new AlertDialog.Builder(getActivity())
		      .setMessage("This function is not available in the lite version. Would you like to visit the Play store now?")
		      .setTitle("")
		      .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Utils.openPlayStore(getActivity());
				}
			  })
			  .setNegativeButton(android.R.string.no, null)
		      .show();
			return;
		}
		
		if (this.messageContainer.getLayoutParams().height == 0){
			this.showMessageContainer(true);
		} else {
			this.hideMessageContainer(true);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		// We subtrack one here because of the PullToRefresh cell that increments the size list by 1
		CommunityMessage message = this.communityListAdapter.getItem(position - 1);
		if (message == null){
			return;
		}
		
		Intent intent = new Intent(getActivity(), CommunityCommentsActivity.class);
		intent.putExtra(message.getClass().getName(), message);
		startActivity(intent);
	}
	
	private void hideMessageContainer(Boolean animated){
		if (animated){
			DropDownAnimation animation = new DropDownAnimation(this.messageContainer, 0);
			animation.setDuration(300);
			animation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					toolbarContainer.setBackgroundResource(android.R.color.transparent);
					//LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) messageContainer.getLayoutParams();
					//params.setMargins(0, -3, 0, 0);
					toolbarContainer.requestLayout();
				}
			});
			this.messageContainer.startAnimation(animation);
		} else {
			this.messageContainer.getLayoutParams().height = 0;
			this.messageContainer.requestLayout();
		}
	}
	
	private void showMessageContainer(Boolean animated){
		if (animated){
			//LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) messageContainer.getLayoutParams();
			//params.setMargins(0, 0, 0, 0);
			toolbarContainer.setBackgroundResource(R.color.black);
			toolbarContainer.requestLayout();
			
			this.messageContainer.getLayoutParams().height = 1;
			this.messageContainer.requestLayout();
			
			DropDownAnimation animation = new DropDownAnimation(this.messageContainer, this.messageContainerHeight);
			animation.setDuration(300);
			animation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
									
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					final Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) messageContainer.getLayoutParams();
							params.height = LayoutParams.WRAP_CONTENT;
							messageContainer.requestLayout();
						}
					}, 100);
				}
			});
			this.messageContainer.setAnimation(animation);
			this.messageContainer.invalidate();
			animation.startNow();
		} else {
			this.messageContainer.getLayoutParams().height = this.messageContainerHeight;
			this.messageContainer.requestLayout();
		}
	}
	
	private int dpToPixels(int dp){
		Resources r = getResources();
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
	}
	
	@Override
	protected void dataReceived(Method methodCalled, Object data) {
		this.progressBar.setVisibility(View.INVISIBLE);
		sendButton.setVisibility(View.VISIBLE);
		this.messageEditText.setEnabled(true);
		this.messageEditText.setText("");
		this.hideMessageContainer(true);
		
		if (this.communityButton.isSelected()){
			this.communityListAdapter.reloadCommunityMessages();
		} else if (this.myMessagesButton.isSelected()){
			this.communityListAdapter.reloadUserMessages();
		}
		
		super.dataReceived(methodCalled, data);
	}
	
	
	private void hideKeyboard(){
		// Hide Keyboard
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.messageEditText.getWindowToken(), 0);
	}
}
