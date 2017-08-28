/* 
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import java.util.*;

import com.TrackTrackTrack.ExerciseActivity.*;
/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * 운동 정보와 기록정보의 데이터를 공유하기 정의된 Class 이다.
 */
public class Vertex
{
	public static ArrayList<Vertex> arVertex = new ArrayList<Vertex>();
	public static int MyLocationState=0;
	Vertex(int aLatitudeE6, int aLongitudeE6, int aCount)
	{
		mLatitudeE6 = aLatitudeE6;
		mLongitudeE6 = aLongitudeE6;
		Count = aCount;
	}
	
	int mLatitudeE6;
	int mLongitudeE6;
	int Count;
}



