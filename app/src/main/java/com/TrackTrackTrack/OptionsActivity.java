/* 
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import android.os.*;
import android.preference.*;
/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * 옵션정보 Activity를 담당하는 Class이다.
 * Native Preference를 사용하기 때문에 xml파일만 구성해주면 내부적으로 모두 
 * android에서 처리해 준다. 따라서 코드는 매우 간단하다.
 */
public class OptionsActivity extends PreferenceActivity {

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);	
	}
}
