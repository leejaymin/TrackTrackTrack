/*
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import android.content.*;
import android.database.sqlite.*;
import android.util.*;
/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * Databases를 관리하는 Class이다. 작업의 편리를 위해 android에서 제공해주는
 * Helper class를 상속받아 재정의 하였다.
 */
public class LocationDB extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "TrackTrackTrack.db";
	private static final int DATABASE_VERSION = 2;

	public static final String TRACKPOINTS_TABLE = "trackpoints";
	public static final String TRACKS_TABLE = "tracks";
	public static final String TAG = "TrackTrackTrackDB";
	  
	public LocationDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/* DB를 생성하는 코드이다. */
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + TRACKPOINTS_TABLE + " ("
				+ TrackPointsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ TrackPointsColumns.TRACKID + " INTEGER, "
				+ TrackPointsColumns.LATITUDE + " INTEGER, "
				+ TrackPointsColumns.LONGITUDE + " INTEGER, "
				+ TrackPointsColumns.TIME + " INTEGER, "
				+ TrackPointsColumns.SPEED + " FLOAT, "
				+ TrackPointsColumns.ALTITUDE + " FLOAT, "
				+ TrackPointsColumns.ACCURACY + " FLOAT, "
				+ TrackPointsColumns.BEARING + " FLOAT);");

		db.execSQL("CREATE TABLE " + TRACKS_TABLE + " ("
				+ TracksColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ TracksColumns.NAME + " STRING, "
				+ TracksColumns.DESCRIPTION + " STRING, "
				+ TracksColumns.CATEGORY + " STRING, "
				+ TracksColumns.STARTID + " INTEGER, "
				+ TracksColumns.STOPID + " INTEGER, "
				+ TracksColumns.STARTTIME + " INTEGER, "
				+ TracksColumns.STOPTIME + " INTEGER, "
				+ TracksColumns.NUMPOINTS + " INTEGER, "
				+ TracksColumns.TOTALDISTANCE + " FLOAT, "
				+ TracksColumns.TOTALTIME + " INTEGER, "
				+ TracksColumns.MOVINGTIME + " INTEGER, "
				+ TracksColumns.MINLAT + " INTEGER, "
				+ TracksColumns.MAXLAT + " INTEGER, "
				+ TracksColumns.MINLON + " INTEGER, "
				+ TracksColumns.MAXLON + " INTEGER, "
				+ TracksColumns.AVGSPEED + " FLOAT, "
				+ TracksColumns.AVGMOVINGSPEED + " FLOAT, "
				+ TracksColumns.MAXSPEED + " FLOAT, "
				+ TracksColumns.MINELEVATION + " FLOAT, "
				+ TracksColumns.MAXELEVATION + " FLOAT, "
				+ TracksColumns.ELEVATIONGAIN + " FLOAT, "
				+ TracksColumns.MINGRADE + " FLOAT, "
				+ TracksColumns.MAXGRADE + " FLOAT, "
				+ TracksColumns.MAPID + " STRING);");

	}	
	/* DB를 갱신하는 코드이다. */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TRACKPOINTS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + TRACKS_TABLE);
		onCreate(db);
	}
}

