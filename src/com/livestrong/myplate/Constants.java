package com.livestrong.myplate;

public class Constants {

	/** Application name */
	public static final String APPLICATION_NAME = "Livestrong";
	public static final double APPLICATION_VERSION = 1.2;
	
	/**
	 * The application package of the paid app.
	 * This is used to link to the Android market in the light app.
	 * TODO: Replace with final package ID.
	 */
	public static final String APPLICATION_PACKAGE_ID_PAID = "com.livestrong.myplate";
	
	/** AdMarvel Site ID constant */
	public static final String ADMARVEL_SITE_ID = "36399";
	/** AdMarvel Partner ID constant */
	public static final String ADMARVEL_PARTNER_ID = "d6be43944c067f30";
	/** SessionM ID constant */
	// public static final String SESSIONM_ID = "b3fb63aec2a9c5402095a51b0f17068a2a041bc7"; // iOS ID
	public static final String SESSIONM_ID = "cafaa3528fdaa9219081a52c530bf3b0b43547e2";	//Android ID
	
	/** Use this as a field name in your Model if you want to be able to get rowId*/
	public static final String ROW_ID_COLUMN = "rowId";
	
	/**log title for back*/
	public static final String LOG_MYPLATE = "BACK_LOG";
	
	/** Food image size keys */
	public static final Integer FOOD_IMAGE_SMALL 	= 60;
	public static final Integer FOOD_IMAGE_MEDIUM 	= 100;
	public static final Integer FOOD_IMAGE_LARGE 	= 190;
	
	/** Avatar image size keys */
	public static final String AVATAR_IMAGE_SMALL 	= "small";
	public static final String AVATAR_IMAGE_MEDIUM 	= "medium";
	public static final String AVATAR_IMAGE_LARGE 	= "large";
	
}
