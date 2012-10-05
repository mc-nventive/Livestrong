package com.demandmedia.livestrong.android.utilities;

import java.util.Calendar;
import java.util.Date;

public class SimpleDate extends Date {
	private static final long serialVersionUID = -5466784423904908326L;

	public SimpleDate() {
		super();

		Calendar day = Calendar.getInstance();
		day.setTime(this);
		day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);
		
		setTime(day.getTime().getTime());
	}

	public SimpleDate(Date date) {
		super();

		Calendar day = Calendar.getInstance();
		day.setTime(date);
		day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);
		
		setTime(day.getTime().getTime());
	}
}
