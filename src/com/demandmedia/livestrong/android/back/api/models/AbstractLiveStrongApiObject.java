package com.demandmedia.livestrong.android.back.api.models;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.demandmedia.livestrong.android.back.api.ISO8601DateParser;

public abstract class AbstractLiveStrongApiObject implements LiveStrongApiObject {

	protected static Map<String, String> units;

	public String getUnits(Field field) {
		return units.get(field.getName());
	}

	public double sanitizeDouble(String string, String units) {
		if (units == null) {
			units = "";
		}
		if (string == null) {
			return 0.0;
		}
		string = string.trim();
		if (string.length() == 0 || string.toLowerCase().equals("null")) {
			return 0.0;
		}
		try {
			return Double.parseDouble(string.replaceAll(",", "").replaceAll(units, ""));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return 0.0;
		}
	}
	
	public int sanitizeInteger(String string, String units) {
		if (units == null) {
			units = "";
		}
		if (string == null) {
			return 0;
		}
		string = string.trim();
		if (string.length() == 0 || string.toLowerCase().equals("null")) {
			return 0;
		}
		try {
			return Integer.parseInt(string.replaceAll(",", "").replaceAll(units, ""));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public boolean sanitizeBoolean(Integer number) {
		return number == 1;
	}
	
	public Date sanitizeDate(String string) {
	    try {
	    	return ISO8601DateParser.parse(string);
		} catch (ParseException e) {
		    try {
		    	return new SimpleDateFormat("yyyy-MM-dd").parse(string);	
			} catch (ParseException e2) {
				e.printStackTrace();
				e2.printStackTrace();
			}
		}
	    return null;
	}
	
	public String sanitizeString(String string) {
		if (string == null) {
			return null;
		}
		string = string.trim();
		if (string.length() == 0 || string.toLowerCase().equals("null")) {
			return null;
		}
		return string;
	}

}
