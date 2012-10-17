package com.demandmedia.livestrong.android.fragment;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.demandmedia.livestrong.android.MyPlateApplication;
import com.demandmedia.livestrong.android.R;
import com.demandmedia.livestrong.android.activity.AddWeightActivity;
import com.demandmedia.livestrong.android.activity.TrackActivity;
import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.models.DiaryEntries;
import com.demandmedia.livestrong.android.back.models.UserProfile;
import com.demandmedia.livestrong.android.back.models.WeightDiaryEntry;
import com.demandmedia.livestrong.android.utilities.SimpleDate;

public class ProgressFragment extends FragmentDataHelperDelegate {

	private View weightChartView, caloriesChartView, nutrientsChartView;
	private TextView headerTextView;
	private ImageView caloriesHeaderImageView;
	private ProgressBar progressBar;

	private Date graphFromDate;
	private Date graphToDate;

	// Chart Container
	private LinearLayout layout, noDataContainer;
	private Button weightButton, caloriesButton, nutrientsButton, trackButton, trackWeightButton;

	private DiaryEntries diaryEntries;
	private Map<String, Double> nutrients;
	private Map<SimpleDate, Double> dailyWeight = new LinkedHashMap<SimpleDate, Double>();
	private Map<SimpleDate, Integer> dailyCalories = new LinkedHashMap<SimpleDate, Integer>();
	private Map<SimpleDate, Integer> dailyCaloriesGoals = new LinkedHashMap<SimpleDate, Integer>();
	
	private boolean loading = false; 
	private double _bmi;
	
	private class UserProfileTask extends AsyncTask<Void, Void, UserProfile>
	{
		
		@Override
		protected UserProfile doInBackground(Void... params) 
		{
			return DataHelper.getUserProfile(null);
		}
		
		protected void onPostExecute(UserProfile profile) 
		{
			if (profile != null) 
			{
				_bmi = profile.getBmi();
			}
			else
			{
				"".toString();
			}
		};

	};
	
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
		View view = (LinearLayout) inflater.inflate(R.layout.fragment_progress, container, false);
		
		new UserProfileTask().execute(new Void[]{});

		// Get dates for which we will fetch graph data (2 months back)
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, -2);
		this.graphFromDate = calendar.getTime();
		
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, 1);
		this.graphToDate = calendar.getTime();
		
		// Hook up UI outlets
		this.layout 				= (LinearLayout) view.findViewById(R.id.chart);
		this.noDataContainer 		= (LinearLayout) view.findViewById(R.id.noDataContainer);
		this.headerTextView 		= (TextView) view.findViewById(R.id.headerTextView);
		this.weightButton 			= (Button) view.findViewById(R.id.weightButton);
		this.caloriesButton 		= (Button) view.findViewById(R.id.caloriesButton);
		this.nutrientsButton 		= (Button) view.findViewById(R.id.nutrientsButton);
		this.trackButton 			= (Button) view.findViewById(R.id.trackButton);
		this.trackWeightButton		= (Button) view.findViewById(R.id.trackWeightButton);
		this.caloriesHeaderImageView = (ImageView) view.findViewById(R.id.caloriesHeaderImageView);
		this.progressBar 			= (ProgressBar) view.findViewById(R.id.progressBar);
		
		this.initializeButtons();
		
		Integer selectedTab = (Integer) DataHelper.getPref(DataHelper.PREFS_PROGRESS_SELECTED_TAB, 0);
		switch (selectedTab) {
			case 0:
				this.weightButton.setSelected(true);
				break;
			case 1:
				this.caloriesButton.setSelected(true);
				break;
			case 2:
				this.nutrientsButton.setSelected(true);
				break;
		}
		
		return view;
	}
	
	@Override
	public void onResume() {
		loadData();
		initializeViews();
		
		super.onResume();
	}
	
	private void loadData(){
		if (this.loading) {
			return;
		}
		this.loading = true;

		this.diaryEntries = null;
		this.nutrients = null;
		this.dailyWeight = new LinkedHashMap<SimpleDate, Double>();
		this.dailyCalories = new LinkedHashMap<SimpleDate, Integer>();
		this.dailyCaloriesGoals = new LinkedHashMap<SimpleDate, Integer>();
		
		// Fetch data for all graphs!
		DataHelper.getDailyDiaryEntries(this.graphFromDate, this.graphToDate, this);
		DataHelper.getTodayNutrients(this);
	}
	
	private void initializeViews(){
		// Hide views to show later
		this.noDataContainer.setVisibility(View.INVISIBLE);
		this.caloriesHeaderImageView.setVisibility(View.INVISIBLE);
		this.trackWeightButton.getLayoutParams().height = 0;
		
		this.layout.removeAllViews();
		
		// Show Progress
		this.progressBar.setVisibility(View.VISIBLE);
	}
	
	private void initializeButtons(){
		View.OnClickListener onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View button) {
				if (button.isSelected()) {
					return;
				}
				unSelectTabButtons();
				caloriesHeaderImageView.setVisibility(View.INVISIBLE);
				trackWeightButton.getLayoutParams().height = 0;
				
				if (button.getId() == R.id.weightButton) {
					showWeightChart();
				} else if (button.getId() == R.id.caloriesButton) {
					showCaloriesChart();
				} else if (button.getId() == R.id.nutrientsButton) {
					showNutrientsChart();
				}
				
			}
		};

		weightButton.setOnClickListener(onClickListener);
		caloriesButton.setOnClickListener(onClickListener);
		nutrientsButton.setOnClickListener(onClickListener);
		
		trackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MyPlateApplication.setWorkingDateStamp(new Date());
				Intent intent = new Intent(getCurrentActivity(), TrackActivity.class);
				startActivity(intent);
			}
		});
		
		trackWeightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				WeightDiaryEntry weightDiaryEntry = diaryEntries.getWeightEntry(new Date());
				
				MyPlateApplication.setWorkingDateStamp(new Date());
				Intent intent = new Intent(getCurrentActivity(), AddWeightActivity.class);
				
				if (weightDiaryEntry != null){
					intent.putExtra(WeightDiaryEntry.class.getName(), weightDiaryEntry);
				}
				startActivity(intent);
			}
		});
	}
		
	public void showWeightChart(){
		this.headerTextView.setText("BMI: " + new DecimalFormat("#.#").format(_bmi));
		
		if (weightChartView == null){
			weightChartView = getWeightChart();
		}
		layout.removeAllViews();
		
		this.progressBar.setVisibility(View.INVISIBLE);
		
		if (weightChartView == null){
			this.noDataContainer.setVisibility(View.VISIBLE);
		} else {
			if (weightChartView.getParent() != null) {
				((LinearLayout) weightChartView.getParent()).removeAllViews();
			}
			layout.addView(weightChartView);
			this.trackWeightButton.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
			this.noDataContainer.setVisibility(View.INVISIBLE);
		}
		
		this.weightButton.setSelected(true);
		DataHelper.setPref(DataHelper.PREFS_PROGRESS_SELECTED_TAB, 0);
	}
	
	public void showCaloriesChart(){
		this.caloriesHeaderImageView.setVisibility(View.VISIBLE);
		this.headerTextView.setText("");
		
		if (caloriesChartView == null){
			caloriesChartView = getCaloriesChart();
		}
		layout.removeAllViews();
		
		this.progressBar.setVisibility(View.INVISIBLE);
		
		if (caloriesChartView == null){
			this.noDataContainer.setVisibility(View.VISIBLE);
		} else {
			if (caloriesChartView.getParent() != null) {
				((LinearLayout) caloriesChartView.getParent()).removeAllViews();
			}
			layout.addView(caloriesChartView);
			this.noDataContainer.setVisibility(View.INVISIBLE);
		}
		
		this.caloriesButton.setSelected(true);
		DataHelper.setPref(DataHelper.PREFS_PROGRESS_SELECTED_TAB, 1);
	}
	
	public void showNutrientsChart(){
		this.headerTextView.setText("TODAY");
		
		if (nutrientsChartView == null){
			nutrientsChartView = getNutrientsChart();	
		}
		
		layout.removeAllViews();
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(30, 0, 30, 0);
		
		this.progressBar.setVisibility(View.INVISIBLE);
				
		if (nutrientsChartView == null){
			this.noDataContainer.setVisibility(View.VISIBLE);
		} else {
			if (nutrientsChartView.getParent() != null) {
				((LinearLayout) nutrientsChartView.getParent()).removeAllViews();
			}
			layout.addView(nutrientsChartView, layoutParams);
			this.noDataContainer.setVisibility(View.INVISIBLE);
		}

		this.nutrientsButton.setSelected(true);
		DataHelper.setPref(DataHelper.PREFS_PROGRESS_SELECTED_TAB, 2);
	}
	
	public void unSelectTabButtons() {
                this.weightButton.setSelected(false);
		this.caloriesButton.setSelected(false);
		this.nutrientsButton.setSelected(false);
	}

	/**
	 * Builds an XY multiple time dataset using the provided values.
	 * 
	 * @param titles the series titles
	 * @param xValues the values for the X axis
	 * @param yValues the values for the Y axis
	 * @return the XY multiple time dataset
	 */
	protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues, List<Double[]> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			TimeSeries series = new TimeSeries(titles[i]);
			Date[] xV = xValues.get(i);
			Double[] yV = yValues.get(i);
			int seriesLength = xV.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(xV[k], yV[k]);
			}
			dataset.addSeries(series);
		}
		return dataset;
	}
	
	private void getWeightChartData() {
		this.dailyWeight = diaryEntries.getDailyWeightForSelectedUnits();
	}
	
	private View getWeightChart() {
		String[] titles = new String[] { "Weight" };

		List<SimpleDate> allDates = new ArrayList<SimpleDate>(dailyWeight.keySet());
		List<Date[]> dates = new ArrayList<Date[]>();
		dates.add(allDates.toArray(new SimpleDate[allDates.size()]));

		Collection<Double> allValues = dailyWeight.values();
		List<Double[]> values = new ArrayList<Double[]>();
		values.add(allValues.toArray(new Double[allValues.size()]));
		
		if (allValues.size() == 0) {
			return null;
		}
		
		double minWeight = Collections.min(allValues);
		double maxWeight = Collections.max(allValues);
		double delta = maxWeight - minWeight;
		if (delta == 0){
			delta = maxWeight / 2;
		}
		minWeight -= delta/2;
		maxWeight += delta/2;

		XYMultipleSeriesDataset dataSet = buildDateDataset(titles, dates, values);

		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		renderer.setLabelsTextSize(getPixelSize());
		renderer.setLabelsColor(getResources().getColor(android.R.color.black));
		renderer.setPointSize(5f);
		
		renderer.setShowLegend(false);
		renderer.setShowGrid(true);
		renderer.setGridColor(Color.parseColor("#E1E0D4"));
		renderer.setAxesColor(Color.parseColor("#E1E0D4"));
		renderer.setLabelsColor(Color.BLACK);
		
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.WHITE);
		renderer.setMargins(new int[] { 10, 30, 10, 30 });
		renderer.setMarginsColor(Color.WHITE);
		renderer.setPanEnabled(true, false);
		renderer.setInScroll(true);

		XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
		xySeriesRenderer.setPointStyle(PointStyle.CIRCLE);
		xySeriesRenderer.setLineWidth(3);
		xySeriesRenderer.setColor(Color.parseColor("#FCD235")); // Set to Yellow
		xySeriesRenderer.setFillBelowLine(false);
		xySeriesRenderer.setDisplayChartValues(false);
		renderer.addSeriesRenderer(xySeriesRenderer);
		
		// Format Y labels
		renderer.setYAxisMin(minWeight);
		renderer.setYAxisMax(maxWeight);
		renderer.setYLabelsAlign(Align.CENTER);
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTime(Collections.min(allDates));
		calendar.add(Calendar.DATE, -2);
		long minTime = calendar.getTimeInMillis();
		
		calendar.setTime(Collections.max(allDates));
		calendar.add(Calendar.DATE, 2);
		long maxTime = calendar.getTimeInMillis();
		
		calendar.add(Calendar.MONTH, -1);
		long oneMonthBack = calendar.getTimeInMillis();
		
		// Display one month back on load (if we have more than a month of data)
		if (minTime < oneMonthBack){
			renderer.setXAxisMin(oneMonthBack);
		} else {
			renderer.setXAxisMin(minTime);
		}
		renderer.setXAxisMax(maxTime);
		renderer.setPanLimits(new double[] { minTime, maxTime, 0, maxWeight });	
		
		return ChartFactory.getTimeChartView(getCurrentActivity(), dataSet, renderer, "d MMM");
	
	}
	
	private void getCaloriesChartData() {
		this.dailyCalories = DataHelper.getDailyCaloriesSum(this.graphFromDate, this.graphToDate);
		this.dailyCaloriesGoals = DataHelper.getDailyCaloriesGoals(this.graphFromDate, this.graphToDate);
	}

	private View getCaloriesChart() {
		XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();

		DateFormat dateFormater = new SimpleDateFormat("d MMM");

		if (diaryEntries.getFoodEntries().size() == 0 || dailyCaloriesGoals.size() == 0) {
			return null;
		}
		
		// Fill array with all dates
		List<String> allDates = new ArrayList<String>();
		int k = 0;
		int seriesStartsAt = dailyCalories.size();
		for (Date v : dailyCalories.keySet()) {
			allDates.add(dateFormater.format(v));
			if ((dailyCalories.get(v) > 0 || dailyCaloriesGoals.get(v) > 0) && k < seriesStartsAt) {
				seriesStartsAt = k;
			}
			k++;
		}
		
		// Create Calories data set
		List<Integer> yValues = new ArrayList<Integer>(dailyCalories.values());
		
		int seriesLength = allDates.size();

		XYSeries series = new XYSeries("Calories");
		for (k = seriesStartsAt; k < seriesLength; k++) {
			int x = k - seriesStartsAt;
			series.add(x, yValues.get(k));
		}
		dataSet.addSeries(series);

		// Create Calorie Goals data set
		List<Integer> yValues2 = new ArrayList<Integer>(dailyCaloriesGoals.values());

		XYSeries series2 = new XYSeries("Calories Goals");
		for (k = seriesStartsAt; k < seriesLength; k++) {
			int x = k - seriesStartsAt;
			series2.add(x, yValues2.get(k));
		}
		dataSet.addSeries(series2);

		double minCalories = Math.min(series.getMinY(), series2.getMinY());
		double maxCalories = Math.max(series.getMaxY(), series2.getMaxY());
		maxCalories += (maxCalories / 7);
		//double maxCaloriesGoal = series2.getMaxY();

		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		renderer.setBarSpacing(0.3);
		renderer.setPointSize(4f);
		renderer.setLabelsTextSize(getPixelSize());
		renderer.setLabelsColor(Color.parseColor("#383934"));
		renderer.setShowLegend(false);
		renderer.setShowGrid(true);
		renderer.setGridColor(Color.parseColor("#E1E0D4"));
		renderer.setBackgroundColor(Color.WHITE);
		renderer.setApplyBackgroundColor(true);
		renderer.setMargins(new int[] { 10, 30, 10, 30 });
		renderer.setMarginsColor(Color.WHITE);
		renderer.setPanEnabled(true, false);
		renderer.setPanLimits(new double[] { -1, allDates.size() - seriesStartsAt, 1, maxCalories});
		renderer.setZoomEnabled(false, false);
		//renderer.setZoomLimits(new double[] { 1, /*allDates.size() - seriesStartsAt*/14, minCalories, maxCalories});
		renderer.setInScroll(true);

		// Calorie series
		XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
		xySeriesRenderer.setPointStyle(PointStyle.CIRCLE);
		xySeriesRenderer.setFillPoints(true);
		xySeriesRenderer.setLineWidth(0);
		xySeriesRenderer.setColor(Color.parseColor("#FCD235"));
		xySeriesRenderer.setDisplayChartValues(false);
		renderer.addSeriesRenderer(xySeriesRenderer);

		// Calorie goals series
		xySeriesRenderer = new XYSeriesRenderer();
		xySeriesRenderer.setLineWidth(2);
		xySeriesRenderer.setColor(Color.parseColor("#FF0000"));
		renderer.addSeriesRenderer(xySeriesRenderer);

		// Format Y labels
		renderer.setYAxisMin(0);
		renderer.setYAxisMax(maxCalories);
		
		renderer.setYLabelsAlign(Align.CENTER);

		// Add X labels
		renderer.setXLabels(0);
		
		renderer.setXAxisMax(k - seriesStartsAt);
		// set xAxisMin to 7 days ago if possible
		int numPoints = allDates.size() - seriesStartsAt;
		if (numPoints > 7){
			renderer.setXAxisMin(k - seriesStartsAt - 7 - 0.5);
		} else if (numPoints == 1){
			renderer.setXAxisMin(-0.5);
			renderer.setXAxisMax(0.5);
		} else {
			renderer.setXAxisMin(k - seriesStartsAt - numPoints - 0.5);
		}
		
		// Set x-axis labels
		int labelMod = 2;// A label should be written every 2 ticks
		if (numPoints < 7){
			labelMod = 1; // A label should be written on every tick
		}
		for (k = seriesStartsAt; k < seriesLength; k++) {
			int x = k - seriesStartsAt;
			
			if (x % labelMod == 0) {
				renderer.addXTextLabel(x, allDates.get(k));
			} else {
				renderer.addXTextLabel(x, "");
			}
		}

		return ChartFactory.getCombinedXYChartView(getCurrentActivity(), dataSet, renderer, new String[] { BarChart.TYPE, LineChart.TYPE });
	}
	
	private View getNutrientsChart() {
		int[] colors = new int[] { Color.parseColor("#F35552"), Color.parseColor("#66CCE3"), Color.parseColor("#12CA28") };

		CategorySeries categorySeries = new CategorySeries("Nutrients");
		boolean hasData = false;
		for (String what : this.nutrients.keySet()) {
			double perc = this.nutrients.get(what);
			if (perc > 0) {
				hasData = true;
			}
			categorySeries.add(Math.round(perc * 100.0) + "% " + what, perc);
		}

		if (!hasData) {
			return null;
		}

		DefaultRenderer renderer = buildCategoryRenderer(colors);
		renderer.setInScroll(true);
		renderer.setBackgroundColor(Color.WHITE);
		renderer.setApplyBackgroundColor(true);
		renderer.setMargins(new int[] { 0, 0, 0, 0 });
		renderer.setShowLegend(true);
		renderer.setPanEnabled(false);
		renderer.setZoomEnabled(false);
		renderer.setLabelsColor(Color.BLACK);
		renderer.setLabelsTextSize(getPixelSize());
		renderer.setLegendTextSize(getPixelSize());
		renderer.setShowLabels(true);

		return ChartFactory.getPieChartView(getCurrentActivity(), categorySeries, renderer);
	}

	protected DefaultRenderer buildCategoryRenderer(int[] colors) {
		DefaultRenderer renderer = new DefaultRenderer();
		for (int color : colors) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(color);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void dataReceivedThreaded(final Method methodCalled, final Object data) {
		// Parse data
		if (methodCalled.equals(DataHelper.METHOD_GET_DAILY_DIARY_ENTRIES_FOR_DATES)) {
			this.diaryEntries = (DiaryEntries) data;
			getWeightChartData();
			getCaloriesChartData();
		} else if (methodCalled.equals(DataHelper.METHOD_GET_TODAY_NUTRIENTS)) {
			this.nutrients = (Map<String, Double>) data;
		}

		Activity activity = getCurrentActivity();
		activity.runOnUiThread(new Runnable() {
		    public void run() {
		    	dataReceived(methodCalled, data);
		    }
		});
	}
	
	private Activity getCurrentActivity() {
		Activity act = super.getActivity();
		if (act == null) {
			act = (Activity) MyPlateApplication.getFrontMostActivity();
		}
		return act;
	}

	@Override
	public void dataReceived(Method methodCalled, Object data) {
		if (getCurrentActivity() == null){
			return;
		}
		
		// Get selected tab
		final int selectedTab = DataHelper.getPref(DataHelper.PREFS_PROGRESS_SELECTED_TAB, 0);
		
		// Parse data
		if (methodCalled.equals(DataHelper.METHOD_GET_DAILY_DIARY_ENTRIES_FOR_DATES)) {
			this.weightChartView = getWeightChart();
			this.caloriesChartView = getCaloriesChart();
			if (selectedTab == 0) {
				showWeightChart();
			} else if (selectedTab == 1) {
				showCaloriesChart();
			}
		} else if (methodCalled.equals(DataHelper.METHOD_GET_TODAY_NUTRIENTS)) {
			this.nutrientsChartView = getNutrientsChart();
			if (selectedTab == 2) {
				showNutrientsChart();
			}
		}
		
		if (this.diaryEntries != null && this.nutrients != null) {
			this.loading = false;
		}
	}
	
	private int getPixelSize(){
		DisplayMetrics metrics = new DisplayMetrics();
		getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		switch(metrics.densityDpi){
			case DisplayMetrics.DENSITY_LOW:
				return 7;
			case DisplayMetrics.DENSITY_MEDIUM:
				return 12;
		    case DisplayMetrics.DENSITY_HIGH:
		    	return 17;
		    case DisplayMetrics.DENSITY_XHIGH:
		    	return 22;
		    default:
		    	return 20;
		}
	}
}
