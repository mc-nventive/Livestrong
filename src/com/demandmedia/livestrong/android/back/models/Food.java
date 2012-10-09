package com.demandmedia.livestrong.android.back.models;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;

import com.demandmedia.livestrong.android.Constants;
import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.api.models.AbstractLiveStrongApiObject;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Food")
public class Food extends AbstractLiveStrongApiObject implements LiveStrongDisplayableListItem, Serializable {
	private static final long serialVersionUID = -1426601101239482961L;

	public static final String ID_FIELD_NAME = "id";
	public static final String ITEM_TITLE_FIELD_NAME = "itemTitle";
	public static final String FOOD_ID_FIELD_NAME = "foodId";
	public static final String CUSTOM_FIELD_NAME = "custom";

	public final static int CUSTOM_FOOD_ID = 256940;
	
	public final static int CALORIES_PER_FAT = 9;
	public final static int CALORIES_PER_CARBS = 4;
	public final static int CALORIES_PER_PROTEIN = 4;

	static {
		units = new HashMap<String, String>() {
			private static final long serialVersionUID = 6775390878060734545L;
			{
				this.put("fat", "g");
				this.put("satFat", "g");
				this.put("carbs", "g");
				this.put("protein", "g");
				this.put("cholesterol", "mg");
				this.put("sodium", "mg");
				this.put("dietaryFiber", "g");
				this.put("sugars", "g");
				this.put("calsPercFat", "%");
				this.put("calsPercCarbs", "%");
				this.put("calsPercProtein", "%");
			}
		};
	}

	@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
	private int id;

	@DatabaseField(columnName = FOOD_ID_FIELD_NAME)
	private int foodId; // Remote ID

	@DatabaseField(columnName = ITEM_TITLE_FIELD_NAME)
	private String itemTitle;

	@DatabaseField
	private String servingSize;

	@DatabaseField
	private String itemBrand;

	@DatabaseField
	private int cals = 0;

	@DatabaseField
	private int calsFromFat = 0;

	@DatabaseField
	private double fat = 0;

	@DatabaseField
	private double satFat = 0;

	@DatabaseField
	private double carbs = 0;

	@DatabaseField
	private double protein = 0;

	@DatabaseField
	private double cholesterol = 0;

	@DatabaseField
	private double sodium = 0;

	@DatabaseField
	private double dietaryFiber = 0;

	@DatabaseField
	private double sugars = 0;

	@DatabaseField
	private double calsPercFat = 0;

	@DatabaseField
	private double calsPercCarbs = 0;

	@DatabaseField
	private double calsPercProtein = 0;

	@DatabaseField
	private boolean verification = false;

	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private HashMap<Integer, String> images;

	@DatabaseField(columnName = CUSTOM_FIELD_NAME)
	private boolean custom = false; // User saved this as a custom food
	
	public Food() {
	}

	public Food(boolean isCustom) {
		this.custom = isCustom;
		this.foodId = CUSTOM_FOOD_ID;
		this.cals = 500;
		this.servingSize = "1 Serving";
		if (isCustom) {
			this.itemTitle = "My Custom Food " + getNextAvailableCustomId();
		} else {
			this.itemTitle = "My Manual Food " + getNextAvailableManualId();
		}
	}
	
	public Food(boolean isCustom, String name, String servingSize, int calories, int fat, int carbs, int protein) {
		this.custom = isCustom;
		this.foodId = CUSTOM_FOOD_ID;
		this.itemTitle = name;
		this.servingSize = servingSize;
		this.fat = fat;
		this.carbs = carbs;
		this.protein = protein;
		setCals(calories);
	}
	
	public void setServingSize(String string) {
		this.servingSize = string;
	}
	
	public String getTitle() {
		return this.itemTitle;
	}

	public String getDescription() {
		return this.servingSize + ", " + getCals() + " calories";
	}

	public int getId() {
		return this.id;
	}
	
	private long getNextAvailableCustomId() {
		long numCustomFoods = 0;
		try {
			RuntimeExceptionDao<Food, Integer>dao = DataHelper.getDatabaseHelper().getFoodDao();
			numCustomFoods = dao.countOf(dao.queryBuilder()
					.setCountOf(true)
					.where()
					.eq(FOOD_ID_FIELD_NAME, CUSTOM_FOOD_ID)
					.and()
					.eq(CUSTOM_FIELD_NAME, true)
					.prepare()
			);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return numCustomFoods + 1;
	}

	private long getNextAvailableManualId() {
		long numCustomFoods = 0;
		try {
			RuntimeExceptionDao<Food, Integer>dao = DataHelper.getDatabaseHelper().getFoodDao();
			numCustomFoods = dao.countOf(dao.queryBuilder()
					.setCountOf(true)
					.where()
					.eq(FOOD_ID_FIELD_NAME, CUSTOM_FOOD_ID)
					.and()
					.eq(CUSTOM_FIELD_NAME, false)
					.prepare()
			);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return numCustomFoods + 1;
	}
	
	// For deserialization (from API)
	
	public void setCals(int cals) {
		this.cals = cals;
		setCalsPercFat(-1);
		setCalsPercCarbs(-1);
		setCalsPercProtein(-1);
	}

	public void setFat(double fat) {
		this.fat = fat;
		setCalsPercFat(-1);
	}

	public void setCarbs(double carbs) {
		this.carbs = carbs;
		setCalsPercCarbs(-1);
	}

	public void setProtein(double protein) {
		this.protein = protein;
		setCalsPercProtein(-1);
	}

	public void setCalsPercFat(double calsPercFat) {
		if (calsPercFat > 0) {
			// New value from API
			this.calsPercFat = calsPercFat;
		} else if (this.calsPercFat > 0) {
			// Already calculated
			return;
		} else if (this.cals > 0) {
			// Calculates from this.cals
			this.calsPercFat = this.fat * CALORIES_PER_FAT * 100.0 / this.cals;
		}
	}

	public void setCalsPercCarbs(double calsPercCarbs) {
		if (calsPercCarbs > 0) {
			// New value from API
			this.calsPercCarbs = calsPercCarbs;
		} else if (this.calsPercCarbs > 0) {
			// Already calculated
			return;
		} else if (this.cals > 0) {
			// Calculates from this.cals
			this.calsPercCarbs = this.carbs * CALORIES_PER_CARBS * 100.0 / this.cals;
		}
	}

	public void setCalsPercProtein(double calsPercProtein) {
		if (calsPercProtein > 0) {
			// New value from API
			this.calsPercProtein = calsPercProtein;
		} else if (this.calsPercProtein > 0) {
			// Already calculated
			return;
		} else if (this.cals > 0) {
			// Calculates from this.cals
			this.calsPercProtein = this.protein * CALORIES_PER_PROTEIN * 100.0 / this.cals;
		}
	}

	public void setVerification(String string) {
		this.verification = "approved".equalsIgnoreCase(string);
	}

	public int getFoodId() {
		return this.foodId;
	}

	public String getServingSize() {
		return this.servingSize;
	}

	public String getItemBrand() {
		return this.itemBrand;
	}

	public int getCals() {
		return this.cals;
	}

	public int getCalsFromFat() {
		if (this.calsFromFat == 0) {
			this.calsFromFat = (int) Math.round(getCals() * getCalsPercFat() / 100);
		}
		return this.calsFromFat;
	}

	public double getFat() {
		return this.fat;
	}

	public double getSatFat() {
		return this.satFat;
	}

	public double getCarbs() {
		return this.carbs;
	}

	public double getProtein() {
		return this.protein;
	}

	public double getCholesterol() {
		return this.cholesterol;
	}

	public double getSodium() {
		return this.sodium;
	}

	public double getDietaryFiber() {
		return this.dietaryFiber;
	}

	public double getSugars() {
		return this.sugars;
	}

	public double getCalsPercFat() {
		if (this.calsPercFat == 0) {
			setCalsPercFat(-1);
		}
		return this.calsPercFat;
	}

	public double getCalsPercCarbs() {
		if (this.calsPercCarbs == 0) {
			setCalsPercCarbs(-1);
		}
		return this.calsPercCarbs;
	}

	public double getCalsPercProtein() {
		if (this.calsPercProtein == 0) {
			setCalsPercProtein(-1);
		}
		return this.calsPercProtein;
	}

	public boolean isVerified() {
		return this.verification;
	}

	public boolean isCustom() {
		return this.custom;
	}
	
	public boolean isManual() {
		 return this.foodId == CUSTOM_FOOD_ID;
	}

	public boolean isGeneric() {
		return isManual() && !isCustom() && getCals() == 1;
	}

	public HashMap<Integer, String> getImages() {
		return this.images;
	}

	public String getSmallImage() {
		if (this.images != null) {
			String url = this.images.get(Constants.FOOD_IMAGE_SMALL);
			if (url.contains("nologo.gif")) {
				return "";
			}
			return url;
		} else {
			return "";
		}
	}

	// For serialization (to API)
	public Integer getSerializableId() {
		// Don't serialize this field, when it's sent to the API
		return null;
	}
	
	public Boolean getSerializableCustom() {
		// Don't serialize this field, when it's sent to the API
		return null;
	}
}
