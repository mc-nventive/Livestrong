package com.demandmedia.livestrong.android.back.models;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import android.database.Cursor;

import com.demandmedia.livestrong.android.back.api.ISO8601DateParser;
import com.demandmedia.livestrong.android.back.api.models.AbstractLiveStrongApiObject;
import com.demandmedia.livestrong.android.utilities.SimpleDate;
import com.j256.ormlite.field.DatabaseField;

public abstract class DiaryEntry extends AbstractLiveStrongApiObject implements LiveStrongDisplayableListItem, Serializable {
	private static final long serialVersionUID = -6596467512973861414L;

	public final static String DELETED_FIELD_NAME = "deleted";
	public final static String DATESTAMP_FIELD_NAME = "datestamp";
	public final static String MODIFIED_FIELD_NAME = "modified";
	public final static String DIRTY_FIELD_NAME = "dirty";
	
	@DatabaseField (index = true, columnName = DELETED_FIELD_NAME)
	private boolean deleted = false;
	
	@DatabaseField(columnName = MODIFIED_FIELD_NAME)
	private Date modified = new Date();

	@DatabaseField (index = true, columnName = DATESTAMP_FIELD_NAME)
	private Date datestamp;
	
	@DatabaseField(id = true)
	private String guid;
	
	@DatabaseField (index = true)
	private boolean dirty = true;
	
	public DiaryEntry() {
		this.guid = UUID.randomUUID().toString().toLowerCase();
	}
	
	public DiaryEntry(Cursor cloningDatabaseCursor)
	{
		try 
		{
			this.datestamp = new SimpleDateFormat("yyyy-MM-dd").parse(cloningDatabaseCursor.getString(cloningDatabaseCursor.getColumnIndex(DATESTAMP_FIELD_NAME)));
		} catch (ParseException e) 
		{
			e.printStackTrace();
		}
		this.deleted = cloningDatabaseCursor.getShort(cloningDatabaseCursor.getColumnIndex(DELETED_FIELD_NAME)) > 0;
		this.dirty = cloningDatabaseCursor.getShort(cloningDatabaseCursor.getColumnIndex(DIRTY_FIELD_NAME)) > 0;
		this.guid = cloningDatabaseCursor.getString(cloningDatabaseCursor.getColumnIndex("guid"));
		try 
		{
			this.modified = ISO8601DateParser.parse(cloningDatabaseCursor.getString(cloningDatabaseCursor.getColumnIndex(MODIFIED_FIELD_NAME)));
		} catch (ParseException e) 
		{
			e.printStackTrace();
		}
	}
	
	public DiaryEntry(Date datestamp, Object didWhat) {
		this();
		this.datestamp = new SimpleDate(datestamp);
	}
	
	@Override
	public String toString() {
		return getTitle() + ", " + getDescription();
	}
	
	protected void wasModified() {
		this.modified = new Date();
		this.dirty = true;
	}

	public void wasSynced() {
		this.dirty = false;
	}
	
	public void setDeleted(Boolean isDeleted) {
		this.deleted = isDeleted;
		wasModified();
	}

	public void setDirty(boolean isDirty) {
		this.dirty = isDirty;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isDirty() {
		return dirty;
	}

	public Date getModified() {
		return modified;
	}

	public SimpleDate getDatestamp() {
		return new SimpleDate(datestamp);
	}

	public String getGuid() {
		return guid;
	}
	
	// For serialization
	public String getSerializableDatestamp() {
		return new SimpleDateFormat("yyyy-MM-dd").format(this.datestamp);
	}

	public String getSerializableModified() {
		return ISO8601DateParser.toString(this.modified);
	}

	public int getSerializableDeleted() {
		return this.deleted ? 1 : 0;
	}

	public Boolean getSerializableDirty() {
		// Don't serialize this field, when it's sent to the API
		return null;
	}
}
