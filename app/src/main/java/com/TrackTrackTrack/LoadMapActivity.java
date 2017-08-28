/*
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import java.util.*;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.*;
import android.os.*;
import android.widget.*;

import com.TrackTrackTrack.TrackTrackTrack.*;
import com.google.android.maps.*;

/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * 저장된 운동 기록정보의 과거 운동 경로를 보여주는 Activity를 담당하는 class이다.
 */
public class LoadMapActivity extends MapActivity {

	private String mTrackid;
	private int mStartPoint;
	private int mStopPoint;
	private Intent mIntent;
	private TextView mInfo;
	private LocationDB mLocationDB;
	private Cursor mCursor;
	private ArrayList<Vertex> mArVertex;
	private int mPosition = 0;
	private MapView mMap;
	private MyLocationPath mPath;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loadmap);	

		mInfo = (TextView)findViewById(R.id.datein);
		mMap = (MapView)findViewById(R.id.mapview);
		
		//인텐트를 통하여 시작 포인트와 끝나는 포인트를 얻오 온다.
		mIntent = getIntent();
		mStartPoint = mIntent.getIntExtra("TrackStartPoint",-1);
		mStopPoint = mIntent.getIntExtra("TrackStopPoint",-1);
		mInfo.setText(String.format("S:%d~E:%d",mStartPoint,mStopPoint));

		// 출력을 위한 어레이 리스트 선언 이다. 전역과는 다른 개념이다.
		mArVertex = new ArrayList<Vertex>();
		
		/* Location DB를 읽기 모드로 Open 한다. */
		mLocationDB = new LocationDB(this);
		SQLiteDatabase db = mLocationDB.getReadableDatabase();
		mCursor = db.rawQuery("select latitude, longitude from trackpoints " +
				"where _id >= " +mStartPoint+" and "+" _id <= "+mStopPoint,null);

		while(mCursor.moveToPosition(mPosition)) {
			mArVertex.add(new Vertex(mCursor.getInt(0),mCursor.getInt(1),mPosition));
			mPosition++;
		}
		
		mPath = new MyLocationPath();
		
		/*
		 * Map에 대한 처리를 해준다.
		 * 1) 줌 상태를 18단계로 한다.
		 * 2) 줌/아웃 컨트롤러를 등록한다.
		 * 3) 포커스 Center를 트랙의 시작 점으로 설정 한다. (이래야 전혀 상관 없는 곳을 불러와도 적절하게 볼 수 있다.)
		 */
		MapController mapControl = mMap.getController();
		mapControl.setZoom(18);
		mMap.setBuiltInZoomControls(true);
		GeoPoint pt = new GeoPoint(mArVertex.get(0).mLatitudeE6,
				mArVertex.get(0).mLongitudeE6);
		mapControl.setCenter(pt);
		
		List<Overlay> overlays = mMap.getOverlays();
		overlays.add(mPath);
	}
	public class Vertex {
		Vertex(int aLatitudeE6, int aLongitudeE6, int aCount) {
			mLatitudeE6 = aLatitudeE6;
			mLongitudeE6 = aLongitudeE6;
			Count = aCount;
		}
		int mLatitudeE6;
		int mLongitudeE6;
		int Count;
	}

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

			/* 화면 변화에 대응 하기위해 매번 실시간으로 과거의 GeoPoint값을 불러와
			 * 현재 화면에 맞게 변환하여 표시해 준다.
			 * 이렇게 해야 화면이 변경 되었을때도 기에 맞게 올바라르게 표시 되어진다.
			 */
			for(int i=1; i<mArVertex.size(); i++) {		
				m_startPoint = projection.toPixels(new GeoPoint(mArVertex.get(i-1).mLatitudeE6,
						mArVertex.get(i-1).mLongitudeE6),null);
				m_endPoint = projection.toPixels(new GeoPoint(mArVertex.get(i).mLatitudeE6,
						mArVertex.get(i).mLongitudeE6),null);

				canvas.drawLine(m_startPoint.x,m_startPoint.y,
						m_endPoint.x,m_endPoint.y,pnt);
			}
		}
	}
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
