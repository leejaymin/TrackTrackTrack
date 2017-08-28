/* 
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import android.net.*;
import android.provider.*;
/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * TrackTrackTrack.db 중 Trackpoints Table의 columns 값들을 정의한
 * class 이다.
 */
public interface TrackPointsColumns extends BaseColumns {
	public static final Uri CONTENT_URI =
	      Uri.parse("content://com.TrackTrackTrack/trackpoints");
	  public static final String CONTENT_TYPE =
	      "vnd.android.cursor.dir/vnd.TrackTrackTrack.trackpoint";
	  public static final String CONTENT_ITEMTYPE =
	      "vnd.android.cursor.item/vnd.TrackTrackTrack.trackpoint";
	  public static final String DEFAULT_SORT_ORDER = "_id";

	  /* All columns */
	  public static final String TRACKID = "trackid";
	  public static final String LATITUDE = "latitude";
	  public static final String LONGITUDE = "longitude";
	  public static final String ALTITUDE = "elevation";
	  public static final String BEARING = "bearing";
	  public static final String TIME = "time";
	  public static final String ACCURACY = "accuracy";
	  public static final String SPEED = "speed";

	  /** Columns that go into the backup. */
	  public static final String[] BACKUP_COLUMNS =
	      { _ID, TRACKID, LATITUDE, LONGITUDE, ALTITUDE, BEARING, TIME,
	        ACCURACY, SPEED };

}
