/* 
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.animation.*;
import android.widget.*;
/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * TrackTrackTrack App은 TabView로 구성되어 진다.
 * 따라서 TaHost를 관리할 메인 TabActivity가 존재한다. 
 */
public class TrackTrackTrack extends TabActivity {
	
	final String TAG = "TrackTrackTrack";
	TabHost mTabHost;
	Animation mTranslateLeftAnim;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        mTabHost = getTabHost();
        TabHost.TabSpec spec; 
        Intent intent; 
        
        intent = new Intent().setClass(this, ExerciseActivity.class);
        spec = mTabHost.newTabSpec("exercise").setIndicator(
                new MyView(this, R.drawable.run, "운동 정보")
                ).setContent(intent);
        mTabHost.addTab(spec);

        intent = new Intent().setClass(this, MapsActivity.class);
        spec = mTabHost.newTabSpec("maps").setIndicator(
                new MyView(this, R.drawable.map, "지도 정보")
                ).setContent(intent);
        mTabHost.addTab(spec);
        
        intent = new Intent().setClass(this, SaveListView.class);
        spec = mTabHost.newTabSpec("datasave").setIndicator(
                new MyView(this, R.drawable.save, "기록 정보")
                ).setContent(intent);
        mTabHost.addTab(spec);
        
        
        intent = new Intent().setClass(this, OptionsActivity.class);
        spec = mTabHost.newTabSpec("options").setIndicator(
                new MyView(this, R.drawable.option, "옵션 정보")
                ).setContent(intent);
        mTabHost.addTab(spec);
        
        intent = new Intent().setClass(this, GraphActivity.class);
        spec = mTabHost.newTabSpec("graph").setIndicator(
                new MyView(this, R.drawable.graph, "그래프")
                ).setContent(intent);
        mTabHost.addTab(spec);
        
        mTabHost.setCurrentTab(4);
        mTabHost.setCurrentTab(0);

    }
    private class MyView extends LinearLayout {

        public MyView(Context c, int drawable, String label) {
            super(c);
            ImageView iv = new ImageView(c);
            iv.setImageResource(drawable);

            TextView tv = new TextView(c);
            tv.setText(label);
            tv.setGravity(0x01); 

            setOrientation(LinearLayout.VERTICAL);
            addView(iv);
            addView(tv);			
        }
    }
}
