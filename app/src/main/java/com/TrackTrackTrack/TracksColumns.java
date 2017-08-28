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
 * TrackTrackTrack.db 중 TracksColumns Table의 columns 값들을 정의한
 * class 이다.
 */
public interface TracksColumns extends BaseColumns{
	 public static final Uri CONTENT_URI =
	      Uri.parse("content://com.TrackTrackTrack/tracks");
	  public static final String CONTENT_TYPE =
	      "vnd.android.cursor.dir/vnd.TrackTrackTrack.track";
	  public static final String CONTENT_ITEMTYPE =
	      "vnd.android.cursor.item/vnd.TrackTrackTrack.track";
	  public static final String DEFAULT_SORT_ORDER = "_id";

	  /* All columns */
	  public static final String NAME = "name";
	  public static final String DESCRIPTION = "description";
	  public static final String CATEGORY = "category";
	  public static final String STARTID = "startid";
	  public static final String STOPID = "stopid";
	  public static final String STARTTIME = "starttime";
	  public static final String STOPTIME = "stoptime";
	  public static final String NUMPOINTS = "numpoints";
	  public static final String TOTALDISTANCE = "totaldistance";
	  public static final String TOTALTIME = "totaltime";
	  public static final String MOVINGTIME = "movingtime";
	  public static final String AVGSPEED = "avgspeed";
	  public static final String AVGMOVINGSPEED = "avgmovingspeed";
	  public static final String MAXSPEED = "maxspeed";
	  public static final String MINELEVATION = "minelevation";
	  public static final String MAXELEVATION = "maxelevation";
	  public static final String ELEVATIONGAIN = "elevationgain";
	  public static final String MINGRADE = "mingrade";
	  public static final String MAXGRADE = "maxgrade";
	  public static final String MINLAT = "minlat";
	  public static final String MAXLAT = "maxlat";
	  public static final String MINLON = "minlon";
	  public static final String MAXLON = "maxlon";
	  public static final String MAPID = "mapid";

	  public static final String[] BACKUP_COLUMNS = {
	      _ID, NAME, DESCRIPTION, CATEGORY, STARTID, STOPID, STARTTIME, STOPTIME,
	      NUMPOINTS, TOTALDISTANCE, TOTALTIME, MOVINGTIME, AVGSPEED, AVGMOVINGSPEED,
	      MAXSPEED, MINELEVATION, MAXELEVATION, ELEVATIONGAIN, MINGRADE, MAXGRADE,
	      MINLAT, MAXLAT, MINLON, MAXLON };
}
