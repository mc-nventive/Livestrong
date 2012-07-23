package com.livestrong.myplate.adapters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
import com.livestrong.myplatelite.R;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.models.CommunityMessage;
import com.livestrong.myplate.back.models.CommunityMessageComment;
import com.livestrong.myplate.utilities.EndlessScrollDelegate;
import com.livestrong.myplate.utilities.EndlessScrollListener;
import com.livestrong.myplate.utilities.ImageLoader;

public class CommunityCommentsAdapter extends AbstractBaseAdapterDataHelperDelegate implements EndlessScrollDelegate {

	private ListView listView;
	private List<CommunityMessageComment> messages = new ArrayList<CommunityMessageComment>();
	public ImageLoader imageLoader;
	private Boolean showLoadingItem = true;
	private CommunityMessage message;
	private Boolean loadingNewPost = false;
	int messageId;

	public CommunityCommentsAdapter(Activity activity, ListView listView, CommunityMessage message) {
		this.activity = activity;
		this.listView = listView;
		this.message = message;
		this.messageId = message.getPostId();
		this.imageLoader = new ImageLoader(activity);
	}

	/**** DataHelper Calls/Functions ****/

	public void loadComments(int page) {
		if (this.messages.size() >= this.message.getComments()){
			this.showLoadingItem = false;
			this.notifyDataSetChanged();
		} else {
			Log.d("D", "loadPage " + page);
			DataHelper.getCommunityMessageComments(this.messageId, page, this);	
		}
	}

	public void reloadComments() {
		Log.d("D", "reload");
		//this.loadingNewPost = true;
		this.messages.clear();
		setShowLoadingItem(true);
		this.notifyDataSetChanged();

		DataHelper.getCommunityMessageComments(this.messageId, 1, this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void dataReceived(Method methodCalled, Object data) {
		if (data != null) {
			// Initialize EndlessScroll Listener if list is empty
			boolean shouldAddOnScrollListener = false;
			if (this.messages.size() == 0) {
				shouldAddOnScrollListener = true;
			}

			List<CommunityMessageComment> newComments = (List<CommunityMessageComment>) data;
			Log.d("D", "num comments: " + newComments.size());
			if (newComments.size() > 0) {
				this.addMessages(newComments);
				if (shouldAddOnScrollListener) {
					this.listView.setOnScrollListener(new EndlessScrollListener(2, this));
				}
			} else {
				this.setShowLoadingItem(false);
				this.listView.setOnScrollListener(null);
				this.notifyDataSetChanged();
			}
			
//			if (this.loadingNewPost){
//				this.listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
//				this.loadingNewPost = false;
//				this.notifyDataSetChanged();
//			}

		} else {
			this.setShowLoadingItem(false);
			this.listView.setOnScrollListener(null);
		}
	}

	/**
	 *  EndlessScrollListener callback
	 */
	@Override
	public void loadListDataForPage(int page) {
		this.loadComments(page);
	}

	@Override
	public int getCount() {
		int count = 0;
		if (this.messages != null) {
			count = this.messages.size();
		}
		if (this.showLoadingItem) {
			count = count + 1;
		}
		
		count++; // To include original message
		
		return count;
	}

	@Override
	public Object getItem(int position) {
		if (position == 0){
			return this.message;
		} else {
			return (this.messages.size() == 0) ? 0 : this.messages.get(position - 1);	
		}
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Context context = MyPlateApplication.getContext();

		// Return loading cell is last item
		if (this.messages == null || position == this.messages.size() + 1) {
			View view = LayoutInflater.from(context).inflate(R.layout.list_item_loading, null);
			return view;
		}

		if (convertView == null || convertView.getId() == R.id.listItemLoading) {
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_community, null);
			convertView.setBackgroundResource(R.drawable.list_item_background);
		}

		TextView userNameTextField = (TextView) convertView.findViewById(R.id.userNameTextView);
		TextView messageTextField = (TextView) convertView.findViewById(R.id.messageTextView);
		TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
		TextView commentsTextView = (TextView) convertView.findViewById(R.id.commentsLabelTextView);
		TextView numCommentsTextView = (TextView) convertView.findViewById(R.id.numCommentsTextView);
		ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
		
		if (this.message == null){
			return convertView;
		}
			
		if (position == 0){
			userNameTextField.setText(this.message.getUser().getUsername());
			messageTextField.setText(this.message.getPost());
			dateTextView.setText(this.message.getPrettyDate());
			numCommentsTextView.setText(this.message.getComments()+"");
			commentsTextView.setVisibility(View.VISIBLE);
			numCommentsTextView.setVisibility(View.VISIBLE);
			this.imageLoader.DisplayImage(this.message.getUser().getAvatar(), imageView);
		} else {
			// Retrieve food at position
			CommunityMessageComment message = (CommunityMessageComment) getItem(position);
			userNameTextField.setText(message.getUser().getUsername());
			messageTextField.setText(message.getPost());
			dateTextView.setText(message.getPrettyDate());
			commentsTextView.setVisibility(View.INVISIBLE);
			numCommentsTextView.setVisibility(View.INVISIBLE);
			this.imageLoader.DisplayImage(message.getUser().getAvatar(), imageView);
		}	

		return convertView;
	}

	private void addMessages(List<CommunityMessageComment> messages) {
		this.messages.addAll(messages);
		this.notifyDataSetChanged();
	}
	
	private void addMessage(CommunityMessageComment message){
		this.messages.add(message);
		this.notifyDataSetChanged();
	}

	public List<CommunityMessageComment> getMessages() {
		if (this.messages == null) {
			return new ArrayList<CommunityMessageComment>();
		}

		return this.messages;
	}

	public void setShowLoadingItem(Boolean show) {
		this.showLoadingItem = show;
	}
}
