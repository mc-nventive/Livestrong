package com.demandmedia.livestrong.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.demandmedia.livestrong.android.R;

public class WebViewActivity extends Activity {
	
	public static String INTENT_URL = "URL";
	public static String INTENT_TITLE = "TITLE";
	
	private WebView webView;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);
 
		Bundle extras = getIntent().getExtras();
		if (extras != null){
			String url = (String)extras.getString("URL");
			String title = (String)extras.getString("TITLE");
			
			final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
			
			webView = (WebView) findViewById(R.id.webView);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.loadUrl(url);
			webView.setWebChromeClient(new WebChromeClient() {
			    public void onProgressChanged(WebView view, int progress) {
			        if (progress > 90){
			            progressBar.setVisibility(View.INVISIBLE);
			        }
			    }
			});
			
			
			TextView header = (TextView) findViewById(R.id.headerTextView);
			header.setText(title);
		}
	}
}
