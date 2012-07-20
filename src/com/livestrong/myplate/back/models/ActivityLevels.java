package com.livestrong.myplate.back.models;

import java.util.HashMap;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.livestrong.myplate.back.api.models.AbstractLiveStrongApiObject;

@DatabaseTable(tableName = "Activity_Levels")
public class ActivityLevels extends AbstractLiveStrongApiObject {
	@DatabaseField(generatedId = true)
	@SuppressWarnings("unused")
	private int id;

	@SerializedName("default")	// This field is called 'default' in JSON, but we can't use that variable name in Java. @SerializedName() is useful to use different names in Java and JSON.
	@DatabaseField(columnName="default")
	private float _default = 1.2f;
	
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private HashMap<Float, String> levels;
	
	public ActivityLevels() {
		// Default values, in case we can't get them from the server on first start 
		this.levels = new HashMap<Float, String>();
		this.levels.put(1.2f, "Sedentary");
		this.levels.put(1.375f, "Light Activity");
		this.levels.put(1.55f, "Moderate Activity");
		this.levels.put(1.725f, "Very Active");
	}
	
	public float getDefault() {
		return _default;
	}
	
	public HashMap<Float, String> getLevels() {
		return levels;
	}
}
