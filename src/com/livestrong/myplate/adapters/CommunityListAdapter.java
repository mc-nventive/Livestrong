package com.livestrong.myplate.adapters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplate.R;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.api.ApiHelper;
import com.livestrong.myplate.back.models.CommunityMessage;
import com.livestrong.myplate.utilities.EndlessScrollDelegate;
import com.livestrong.myplate.utilities.EndlessScrollListener;
import com.livestrong.myplate.utilities.ImageLoader;
import com.livestrong.myplate.utilities.PullToRefreshListView;

public class CommunityListAdapter extends AbstractBaseAdapterDataHelperDelegate implements EndlessScrollDelegate {

	private static enum ListType {COMMUNITY_MESSAGES, USER_MESSAGES};
	
	private ListType selectedList;
	private ListView listView;
	private Map<ListType, List<CommunityMessage>> messageMap = new Hashtable<CommunityListAdapter.ListType, List<CommunityMessage>>();
	public ImageLoader imageLoader; 
	private Boolean showLoadingItem = true;
	private Boolean showMessageItem = false;
	private Boolean reloading = false;
	private Map<ListType, Integer> currentPageMap = new Hashtable<ListType, Integer>();
	private Map<ListType, Integer> maxPageMap = new Hashtable<ListType, Integer>();
	    
	public CommunityListAdapter(Activity activity, ListView listView) {
		this.activity = activity;

		this.listView = listView;
		this.imageLoader = new ImageLoader(activity);
		
		this.selectedList = ListType.COMMUNITY_MESSAGES;
		
		this.messageMap.put(ListType.COMMUNITY_MESSAGES, new ArrayList<CommunityMessage>());
		this.currentPageMap.put(ListType.COMMUNITY_MESSAGES, 0);
		this.maxPageMap.put(ListType.COMMUNITY_MESSAGES, 6);
		
		this.messageMap.put(ListType.USER_MESSAGES, new ArrayList<CommunityMessage>());
		this.currentPageMap.put(ListType.USER_MESSAGES, 0);
		this.maxPageMap.put(ListType.USER_MESSAGES, 6);
	}
	
	/**** DataHelper Calls/Functions ****/
	
	public void reloadCommunityMessages() {
		this.selectedList = ListType.COMMUNITY_MESSAGES;
		
		this.reloading = true;
		Log.d("D", "COMMUNITY - reloadPage 1");
		DataHelper.getCommunityMessages(1, this);
	}

	public void reloadUserMessages(){
		this.selectedList = ListType.USER_MESSAGES;
		
		this.reloading = true;
		Log.d("D", "USER - reloadPage 1");
		DataHelper.getUserOwnCommunityMessages(1, this);
	}
	
	public void loadCommunityMessages(int page){
		if (this.selectedList != ListType.COMMUNITY_MESSAGES){
			this.selectedList = ListType.COMMUNITY_MESSAGES;
			((PullToRefreshListView) this.listView).onRefreshComplete();
			this.notifyDataSetChanged();
		}
		
		// Check if online
		if (ApiHelper.isOnline() == false && this.messageMap.get(this.selectedList).size() == 0){
			this.showMessageItem = true;
			this.showLoadingItem = false;
			notifyDataSetChanged();
			return;
		}
		
		this.showMessageItem = false;
		
		// if data has already been downloaded, show it
		if (page < this.currentPageMap.get(this.selectedList)){
			return;
		} else {
			this.showLoadingItem = true;
			DataHelper.getCommunityMessages(page, this);	
		}	
		notifyDataSetChanged();
	}

	public void loadUserMessages(int page){
		if (this.selectedList != ListType.USER_MESSAGES){
			this.selectedList = ListType.USER_MESSAGES;
			((PullToRefreshListView)this.listView).onRefreshComplete();
			this.notifyDataSetChanged();
		}

		// Check if logged in
		if (DataHelper.isLoggedIn() == false){
			this.resetList();
			this.showMessageItem = true;
			this.showLoadingItem = false;
			notifyDataSetChanged();
			return;
		}
		
		// Check if online and there are no messages already loaded show offline message
		if (ApiHelper.isOnline() == false && this.messageMap.get(this.selectedList).size() == 0){
			this.showMessageItem = true;
			this.showLoadingItem = false;
			notifyDataSetChanged();
			return;
		}
		
		this.showMessageItem = false;
		
		// if data has already been downloaded, show it
		if (this.messageMap.get(this.selectedList).size() > 0 && page < this.currentPageMap.get(this.selectedList)){
			notifyDataSetChanged();
		} else {
			this.showLoadingItem = true;
			notifyDataSetChanged();
			DataHelper.getUserOwnCommunityMessages(page, this);	
		}			
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void dataReceived(Method methodCalled, Object data) {
		if (this.selectedList == ListType.COMMUNITY_MESSAGES  && methodCalled != DataHelper.METHOD_GET_COMMUNITY_MESSAGES){
			return;
		}
		if (this.selectedList == ListType.USER_MESSAGES && methodCalled != DataHelper.METHOD_GET_USER_OWN_COMMUNITY_MESSAGES){
			return;
		}
		
		if (reloading){ // User is reloading via pull to refresh
			if (data != null){ // empty the list if we received data
				this.resetList();	
			} else if (ApiHelper.isOnline() == false){ // if we are offline show offline message
				this.resetList();
				this.showMessageItem = true;
			}			
		}
		
		List<CommunityMessage> messages = this.messageMap.get(this.selectedList); // Fetch stored messages
		int currentPage = this.currentPageMap.get(this.selectedList); // Fetch current page
		
		if (messages.size() == 0 && currentPage == 0) { // Initialize EndlessScroll Listener if list is empty
			this.listView.setOnScrollListener(new EndlessScrollListener(2, this));
		} 

		currentPage++;
		
		if (data != null){ // if we received a response
			List<CommunityMessage> newMessages = (List<CommunityMessage>)data;			
			if (newMessages.size() > 0){ 
				messages.addAll(newMessages);				
			} else { // No more messages, stop endless scrolling
				this.setShowLoadingItem(false);
				this.listView.setOnScrollListener(null);
			}	
		}
		
		// If user reached the max number of pages
		if (currentPage == this.maxPageMap.get(this.selectedList) || data == null){
			this.setShowLoadingItem(false);
			this.listView.setOnScrollListener(null); 
		}
		
		// If there are no messages in selectedList and device is offline
		if (messages.size() == 0 /*&& ApiHelper.isOnline() == false*/){
			this.showMessageItem = true;
		} else {
			this.showMessageItem = false;
		}
		
		// Update member variables
		this.messageMap.put(this.selectedList, messages);
		this.currentPageMap.put(this.selectedList, currentPage);
		
		if (reloading){ // notify that reload was completed (for pull to refresh)
			((PullToRefreshListView)this.listView).onRefreshComplete();
			reloading = false;
		}
		
		this.notifyDataSetChanged();
	}

	/**
	 *  EndlessScrollListener callback
	 */	
	@Override
	public void loadListDataForPage(int page) {
		Log.d("CommunityListAdapter", "loadListDataForPage: "+ page);
		if (page <= this.maxPageMap.get(this.selectedList)){
			if (this.selectedList == ListType.COMMUNITY_MESSAGES){
				Log.d("D", "Community - loadListDataForPage: " + page);
				this.loadCommunityMessages(page);
			} else if (this.selectedList == ListType.USER_MESSAGES){
				Log.d("D", "USER - loadListDataForPage: " + page);
				this.loadUserMessages(page);
			}
		} 
	}
	
	@Override
	public int getCount() {
		if ((showMessageItem && DataHelper.isLoggedIn() == false) ||
				(showMessageItem && ApiHelper.isOnline() == false) ||
				(showMessageItem && this.messageMap.get(this.selectedList).size() == 0)){
			return 1;
		}
		
		int count = this.messageMap.get(this.selectedList).size();
				
		if (showLoadingItem){
			count++;
		}
				
		return count;
	}

	@Override
	public CommunityMessage getItem(int position) {
		if (this.showMessageItem){
			return null;
		}
		
		if (this.showLoadingItem && position == this.messageMap.get(this.selectedList).size()){
			return null;
		}
		
		return (this.messageMap.get(this.selectedList) == null) ? null : this.messageMap.get(this.selectedList).get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Context context = MyPlateApplication.getContext();
		
		// Return loading cell is last item
		if (this.messageMap.get(this.selectedList) == null || position == this.messageMap.get(this.selectedList).size()){
			if (this.showLoadingItem){
				View view = LayoutInflater.from(context).inflate(R.layout.list_item_loading, null);
				return view;
			}
		}
				
		// show message cell 
		if (showMessageItem){
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_message, null);
			TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
			if (DataHelper.isLoggedIn() == false){
				messageTextView.setText("You must be signed in to view your messages.");
			} else if (ApiHelper.isOnline() == false){
				messageTextView.setText("Offline - cannot retrieve messages.");
			} else if (this.messageMap.get(this.selectedList).size() == 0){
				messageTextView.setText("You have no messages.");
			}
			return convertView;
		}
		
		// Inflate community list item
		if (convertView == null || convertView.getId() == R.id.listItemLoading || convertView.getId() == R.id.listItemMessage) {
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_community, null);			
		}
		
		// Retrieve food at position	
		CommunityMessage message = getItem(position);
		if (message != null){
			TextView userNameTextField = (TextView)convertView.findViewById(R.id.userNameTextView);
			userNameTextField.setText(message.getUser().getUsername());
			
			TextView messageTextField = (TextView)convertView.findViewById(R.id.messageTextView);
			messageTextField.setText(message.getPost());
			
			TextView numberOfCommentsTextView = (TextView)convertView.findViewById(R.id.numCommentsTextView);
			numberOfCommentsTextView.setText(message.getComments() + "");
			
			TextView dateTextView = (TextView)convertView.findViewById(R.id.dateTextView);
			dateTextView.setText(message.getPrettyDate());
			
			ImageView imageView = (ImageView)convertView.findViewById(R.id.imageView);
			imageView.setTag(message.getUser().getAvatar());
			
			this.imageLoader.DisplayImage(message.getUser().getAvatar(), imageView);	
		}
		
		return convertView;
	}
	
	private void resetList(){
		this.clearMessages();
		this.listView.setOnScrollListener(null);
		this.currentPageMap.put(this.selectedList, 0);
		//this.setShowLoadingItem(true);
	}
		
	private void clearMessages(){
		this.messageMap.get(this.selectedList).clear();
		this.notifyDataSetChanged();
	}
	
	public void setShowLoadingItem(Boolean show){
		this.showLoadingItem = show;
	}
	
	public void setListView(ListView listView){
		this.listView = listView;
	}
}
