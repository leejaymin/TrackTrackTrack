/* 
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import java.util.*;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.widget.*;

import com.TrackTrackTrack.Vertex.*;
import com.google.android.maps.*;

/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * 운동경로를 보여주는 Activity 작업 처리를 담당하는 class이다.
 * Google Map과 Overlay를 이용해서 이 작업을 처리하여 준다.
 */
public class MapsActivity extends MapActivity {
	
	MapView mMap; 				// MapView 객체 저장 변수
	MyLocationPoint mLocation; 	// 자신의 위치 표시 오버레이 변수
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maps);	

		// MapView 객체를 얻어 온다.
		mMap= (MapView)findViewById(R.id.exercise_map);
		// 자기 자신을 표시 하는 오버레이
		mLocation = new MyLocationPoint(this, mMap);
		//경로 표시를 하는 오버레이
		MyLocationPath mPath = new MyLocationPath();		
		// Map Zoom 단계를 18단계로 한다. (1~19단계로 조절 가능하다.)
		MapController mapControl = mMap.getController();
		mapControl.setZoom(18);
		// Zoom 컨트롤러를 맵에 나오 도록 한다.
		mMap.setBuiltInZoomControls(true);

		/*
		 * 맵에 오버레이를 등록 해준다.
		 * 첫번째는 자신의 위치를 나타내는 오버레이 이다.
		 * 두번째는 자신의 이동경로를 나타내는 오버레이 이다.
		 */
		List<Overlay> overlays = mMap.getOverlays();
		overlays.add(mLocation);
		overlays.add(mPath);
		
		/*
		 * 다음 메서드는 처음 위치가 조사될 때 실행될 러너블을 등록한다. 
		 * 이 러너블은 분리된 스레드에서 실행되며, 첫 조사된 위치로 이동하는 등의 처리를 할 수 있다. 
		 */
		mLocation.runOnFirstFix(new Runnable() {
			public void run() {
				mMap.getController().animateTo(mLocation.getMyLocation());
			}
		});
	}

	/* 현재 자신의 위치를 표시하는 Overlay 클래스 정의 */
	class MyLocationPoint extends MyLocationOverlay {
		public MyLocationPoint(Context context, MapView mapView) {
			super(context, mapView);
		}
		// 자신의 위치 아이콘을 클릭할 경우 토스트 메시지를 출력 한다. 
		protected boolean dispatchTap() {
			Toast.makeText(MapsActivity.this, "여기가 현재 위치입니다.", 
					Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	
	/* 경로를 그려주는 Overlay 클래스 정의 */
	class MyLocationPath extends Overlay{
		public void draw(Canvas canvas, MapView mapView, boolean shadow){
			super.draw(canvas,mapView,shadow);

			/* draw할때 사용되는 변수 이다. */
			Point m_startPoint;
			Point m_endPoint;

			Paint pnt = new Paint();
			Projection projection;

			pnt.setColor(Color.BLUE);
			pnt.setStrokeWidth(3);
			pnt.setAntiAlias(true);

			/*좌표 변환을 위해 얻어온다. */
			projection = mapView.getProjection();	

			/* 
			 * 화면 변화에 대응 하기위해 매번 실시간으로 과거의 GeoPoint값을 불러와
			 * 현재 화면에 맞게 변환하여 표시해 준다.
			 * 이렇게 해야 화면이 변경 되었을때도 기에 맞게 올바라르게 표시 되어진다.
			 */
			for(int i=1; i<Vertex.arVertex.size(); i++) {		
				m_startPoint = projection.toPixels(new GeoPoint(Vertex.arVertex.get(i-1).mLatitudeE6,
						Vertex.arVertex.get(i-1).mLongitudeE6),null);
				m_endPoint = projection.toPixels(new GeoPoint(Vertex.arVertex.get(i).mLatitudeE6,
						Vertex.arVertex.get(i).mLongitudeE6),null);

				canvas.drawLine(m_startPoint.x,m_startPoint.y,
						m_endPoint.x,m_endPoint.y,pnt);
			}
		}
	}

	public void onResume() {
		super.onResume();

		if(Vertex.MyLocationState == 1) {
			// 현재 위치 활성화 
			mLocation.enableMyLocation();
			// 나침반 기능 활성화 
			mLocation.enableCompass();
		}
	}	

	public void onPause() {
		super.onPause();
		// 현재 위치 비활성화 
		mLocation.disableMyLocation();
		// 나침반 기능 비활성화
		mLocation.disableCompass();		
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
