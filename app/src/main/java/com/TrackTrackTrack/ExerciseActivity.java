/*
 * Copyright (C) 2010 The Froyo Term Project 
 */

package com.TrackTrackTrack;

import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.hardware.*;
import android.location.*;
import android.os.*;
import android.preference.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import com.google.android.maps.*;

/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * 기본적으로 운동 정보 화면을 구성하는 Activity와 상호작용 하는 기능들이 정의 되어 있다.
 * 또한 GPS를 수신하여 실제적인 데이터를 처리하는 모든 기능이 포함되어있다.
 * 
 * DB에 접근하여 데이터를 실시간으로 저장하고 사용자의 선택에 의해서 영속적으로 저장한다.
 * TrackTrackTrack App의 기능에 있어서 가장 핵심적인 Class라 할 수 있다.
 *
 */
public class ExerciseActivity extends Activity {
	
	public static final String TAG = "TTT Exercise";	// Locat 변수

	private SensorManager mSensorManager;	// 하드웨어 자원을 사용하기위한 Manager

	private SQLiteDatabase mDb;	// SQLite DB에 접근하기위한 변수
	
	/* 결음 수 와 관련된 변수들  */
	private int mNum = 0;					// 총 걸음 수를 저장한다.
	private int mBeforValue[] = {0,0,0};	// 이전의 가속도 값을 저장한다. (해딩, 피치, 롤)
	private int mAfterValue[] = {0,0,0};	// 이후의 가속도 값을 저장한다. (해딩, 피치, 롤)
	private int mTime = 0;					//	이전과 이후를 구분 한다.
	private int mWalkCondition;				// 한 걸음이라고 판단되는 가속도 값이다.
	
	private LocationManager mLocMan;		// 위치값을 수신하는 하드웨어 자원을 사용하기 위한 Manager

	/* UI와 관련된 변수들 */
	private TextView mTextStatus;		// GPS 수신 상태를 표시하는 TextView
	private TextView mTextDistance;		// 총 운동 거리를 표시하는 TextView
	private TextView mTextNowSpeed;		// 현재 속력을 표시하는 TextView
	private TextView mTextAverageSpeed;	// 평균속력을 표시하는 TextView
	private TextView mTextTotalTime;	// 운동 총 시간을 표시하는 TextView
	private TextView mTextLatitude;		// 현재 위도를 표시하는 TextView
	private TextView mTextLongitude;	// 현재 경도를 표시하는 TextView
	private TextView mTextStep;			// 운동한 걸음 수를 표시하는 TextView
	private TextView mTextKcal;			// 소모된 총 칼로리를 표시하는 TextView
	private Button mStartButton;		// 측정 시작 Button
	private Button mPauseButton;		// 측정 중지 Button
	
	private String mProvider;		// 최적의 Provider를 저장하고 있다.
	private GeoPoint mGeoPoint;		// GPS 수신 상태를 표시하는 TextView

	/* 거리 측정을 위해 사용하는 위도 경도 시작값 */
	private double mStartLatitude;
	private double mStartLongitude;

	/*시간을 기록하기 위한 변수 */
	private long mStartTime;
	private long mNowTime;
	private long mStopTime;
	private long mTotalTime;
	
	/*거리 값이 저장되어질 배열 */
	private float [] mDistance;
	private float mTotalDistance=0;
	
	/*속력 값이 저장 되어질 변수 */
	private float mNowSpeed;
	private float mAvgSpped;
	
	/*고도를 저장 하는 변수*/
	private double mAltitude;
	
	private double mCal = 0.0;	// 칼로리 계산 값을 저장
	
	/* GPS 수신횟수를 저장할 변수 */
	private int mCount;

	/* 트랙의 ID 이다 (매우중요 DB의 모든 정보를 결정 한다.) */
	private int mTrackId;
	
	/* DB 실시간 save 여부를 판단하기 위한 조건 변수이다. */
	private boolean saveCondition = false;
	
	/*	시실간 DB 저장을 하는 쓰레드 이다. */
	private Thread saveThread;
	
	/* 환경 변수 저장 값. */
	private boolean acceleStatePrefe;
	private int acceleRatePrefe;
	private int gpsRatePrefe;
	private int agePrefe;
	private int weightPrefe;
	
	/* 최적의 Provider를 찾기 위해 사용되는 변수 이다. */
	private Criteria mCriteria;

	protected boolean isRouteDisplayed() {
		return false;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stats);
		LocationDB mLocationDB = new LocationDB(this);

		/*LBS Manager를 얻어온다. */
		mLocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		/* Sensor Manager를 얻어온다. */
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		/**
		 * DB를 자주 OPEN 하는것은 성능에 안좋은 영향을 끼친다.
		 * 따라서 CREATE함수에서 한번 열어주고 지속적으로 이것을 사용 한다.
		 */
		mDb = mLocationDB.getWritableDatabase();	

		Log.v(TAG, "Track ID catched out");

		/*
		 * 표시 해야할것 , 상태 , 이동거리, 현재속력, 총시간, 평균속도, L, Lo, 걸음수
		 * 소모된 칼로리
		 */
		mTextStatus = (TextView)findViewById(R.id.state_register);
		mTextDistance = (TextView)findViewById(R.id.total_distance_register);
		mTextNowSpeed = (TextView)findViewById(R.id.now_speed_register);
		mTextAverageSpeed = (TextView)findViewById(R.id.average_speed_register);
		mTextTotalTime = (TextView)findViewById(R.id.total_time_register);
		mTextLatitude = (TextView)findViewById(R.id.latitude_register);
		mTextLongitude = (TextView)findViewById(R.id.longitude_register);
		mTextStep = (TextView)findViewById(R.id.step_register);
		mTextKcal = (TextView)findViewById(R.id.total_calorie_register);

		//버튼 객체 생성
		mStartButton = (Button)findViewById(R.id.start_button);
		mPauseButton = (Button)findViewById(R.id.pause_button);

		/*서비스 시작 중지를 위해 클릭 리스너를 붙인다. */
		mStartButton.setOnClickListener(pOnClickListener);
		mPauseButton.setOnClickListener(pOnClickListener);

		// 새로 시작 버튼 처음에는 비활성화 시킨다.
		mPauseButton.setVisibility(View.INVISIBLE);

		mCriteria = new Criteria();
        mCriteria.setAccuracy(Criteria.NO_REQUIREMENT);
        mCriteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        
        // 고도정보 받아오게함
        mCriteria.setAltitudeRequired(true); 
        // 속도정보 받아오게함
        mCriteria.setSpeedRequired(true);
		/* 최적의 Provider를 찾아온다. */
		mProvider = mLocMan.getBestProvider(new Criteria(),true);

		// 실시간 DB 저장 thread를 생성한다.
		SaveCurrentDB saveRunnable = new SaveCurrentDB();
		saveThread = new Thread(saveRunnable);
		saveThread.setDaemon(true);
		saveThread.start();
	}
	
	/*
	 * 버튼 클릭 리스너이다. 이 리스너가 서비스의 시작과 중지를 처리 한다.
	 */
	OnClickListener pOnClickListener = new OnClickListener() {
		ContentValues row;
		public void onClick(View v) {
			// TODO Auto-generated method stub

			switch (v.getId())
			{
			case R.id.start_button :
				/*
				 * 트랙 ID를 구한다. 새로운 시작일 경우 매번 증가를 시켜 주기도 한다.
				 */
				Cursor cursor = mDb.rawQuery("select max(_id) from tracks",null);
				cursor.moveToFirst();
				if(cursor == null)
					mTrackId = 1;
				else{
					mTrackId = cursor.getInt(0);
					mTrackId++;
				}
				// 최종적으로 다쓴 커서를 닫는다.
				cursor.close();

				mLocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER,gpsRatePrefe,0,mListener);

				/*
				 * 환경설정에서 걸음 수 측정이 활성화 되어야 동작한다.
				 */
				if(acceleStatePrefe){
					/* 가속도 센서 활성화*/
					mSensorManager.registerListener(Listner,
							mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
							SensorManager.SENSOR_DELAY_NORMAL);	
				}
				mTextStatus.setText("서비스 시작");

				// 현재 위치를 표시하도록 Enable 한다. (전역 변수를 통한 제어)
				Vertex.MyLocationState = 1;	
				mStartButton.setVisibility(View.INVISIBLE);
				mPauseButton.setVisibility(View.VISIBLE);
				break;
			case R.id.pause_button :
				/*
				 * 환경설정에서 걸음 수 측정이 활성화 되어야 동작한다.
				 */
				if(acceleStatePrefe){
					/*가속도 센서 작업을 정지 시킨다. */
					mSensorManager.unregisterListener(Listner,
							mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
				}
				/* GPS 센서 작업을 정지 시킨다. */
				mLocMan.removeUpdates(mListener);
				// 현재 위치를 표시하도록 Disable 한다. (전역 변수를 통한 제어)
				Vertex.MyLocationState = 0;
				mTextStatus.setText("서비스 정지");

				/*
				 * 저장을 하기 위한 대화상자를 화면에 표시한다.
				 */
				
				// 대화상자에 관한 처리 코드
				final LinearLayout linear = (LinearLayout)View.inflate(ExerciseActivity.this
						, R.layout.search, null);
				new AlertDialog.Builder(ExerciseActivity.this)
				.setTitle("저장할 식별자를 입력하세요.")
				.setIcon(R.drawable.icon)
				.setView(linear)
				.setPositiveButton("확인",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub						
						EditText eTrackName = (EditText)linear.findViewById(R.id.track_name);
						EditText eTrackType = (EditText)linear.findViewById(R.id.exercise_type);
						EditText eTrackDescription = (EditText)linear.findViewById(R.id.track_decription);

						String sName = eTrackName.getText().toString();
						String sType = eTrackType.getText().toString();
						String sDescroption = eTrackDescription.getText().toString();

						// 사용자에 의사에 의해서 저장을 해준다.
						// 저장을 안한 모든 데이터는 사라진다. 이것을 명심 해야한다.
						ContentValues row = new ContentValues();
						// 트랙의 Name을 저장한다. Track + trackId인 것이다.
						row.put("name",sName);
						// 카테고리
						row.put("category",sType);
						row.put("description",sDescroption);

						// 현재 트랙ID로 시작하는 최초의 트랙포인트 _id를 얻어온다.
						Cursor dbcursor = mDb.rawQuery("select min(_id) from trackpoints " +
								"where trackid ="+mTrackId, null);
						dbcursor.moveToFirst();
						int mStartTrackPoints = dbcursor.getInt(0);
						row.put("startid",mStartTrackPoints);

						//현재 트랙ID로 시작하는 최후의 트랙포인트 _id를 얻어온다.
						dbcursor = mDb.rawQuery("select max(_id) from trackpoints " +
								"where trackid ="+mTrackId, null);
						dbcursor.moveToFirst();
						int mStopTrackPoints = dbcursor.getInt(0);
						
						// 최종적으로 다쓴 커서를 닫는다.
						dbcursor.close();
						row.put("stopid",mStopTrackPoints);
						//운동 시작 시간 저장
						row.put("starttime",mStartTime);
						//운동 종료 시간 저장
						row.put("stoptime",mStopTime);
						//총 포인트 값을 저장한다.
						row.put("numpoints",mStopTrackPoints-mStartTrackPoints);
						//총 거리를 저장 한다.
						row.put("totaldistance",mTotalDistance);
						//총 시간을 저장 한다.
						row.put("totaltime",(int)mTotalTime);
						//평균 속력을 저장 한다.
						row.put("avgspeed",mAvgSpped);

						//최종적으로 Track Table에 저장 한다.
						mDb.insert("tracks", null, row);
						Toast.makeText(ExerciseActivity.this,sName +"을 성공적으로 저장 하였습니다.", Toast.LENGTH_SHORT)
						.show();
					}
				})
				.show();

				/*
				 * 트랙의 point를 저장하고 있는 Vertex객체를 클리어 한다.
				 * 그밖에 화면에 표시되고 있는 각종 모든 정보를 클리어 해야한다.
				 */
				Vertex.arVertex.clear();
				
				//GPS 수신횟수를 초기화 한다. 이로써 거리나 시간이 초기화가 자동으로 이루어 진다.
				mCount = 0; 
				//화면에 표시되는 운동 정보를 리셋 한다.
				mTextStatus.setText(R.string.unknown);
				mTextDistance.setText(R.string.unknown);
				mTextNowSpeed.setText(R.string.unknown);
				mTextAverageSpeed.setText(R.string.unknown);
				mTextTotalTime.setText(R.string.unknown);
				mTextLatitude.setText(R.string.unknown);
				mTextLongitude.setText(R.string.unknown);
				mTextStep.setText(R.string.unknown);
				mTextKcal.setText(R.string.unknown);

				/*
				 * 정지 버튼을 누르면 센서와 GPX 수신 모두 비활성 화가 되어 진다.
				 * 시작 버튼은 비 화성화 된다.
				 */
				mStartButton.setVisibility(View.VISIBLE);
				mPauseButton.setVisibility(View.INVISIBLE);

				break;
			}	
		}
	};
	
	/* 가속도 센서를 위한 무명 클래스로 리스너를 만들 었다. */
	private SensorEventListener Listner = new SensorEventListener(){

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub	
		}
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			synchronized(this)
			{
				switch (event.sensor.getType())
				{
				case Sensor.TYPE_ACCELEROMETER:
					if(mTime == 0)
					{
						mTime = 1;
						mBeforValue[0] = (int)event.values[0];
						mBeforValue[1] = (int)event.values[1];
						mBeforValue[2] = (int)event.values[2];
					}
					else
					{
						mTime = 0;
						mAfterValue[0] = (int)event.values[0];
						mAfterValue[1] = (int)event.values[1];
						mAfterValue[2] = (int)event.values[2];
					}
					mWalkCondition += Math.abs(mBeforValue[0] -mAfterValue[0]);
					mWalkCondition += Math.abs(mBeforValue[1] -mAfterValue[1]);

					if(mWalkCondition > acceleRatePrefe)
					{   
						mNum = mNum + 1;
						mTextStep.setText(String.format("%d",mNum));	
						mWalkCondition = 0;
					}
					break;
				}
			}
		}	
	};
	/*
	 * Activity Life Cycle에 의해서 Activity가 호출될 때 매번 수행 되어진다.
	 * 따라서 이곳에서 Preferences를 사용하여 사용자의 설정값을 반영하여 준다.
	 */
	public void onResume() {
		super.onResume();
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		gpsRatePrefe = Integer.parseInt(prefs.getString("GpsRate","3000"));
		acceleStatePrefe = prefs.getBoolean("AcceleState", false);
		acceleRatePrefe = Integer.parseInt(prefs.getString("AcceleRate", "40"));
		agePrefe = Integer.parseInt(prefs.getString("Age", "20"));
		weightPrefe = Integer.parseInt(prefs.getString("Weight", "60"));
	}	

	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v(TAG,"TTT Exercise onDestroy");
		
		// 완전히 종료되어질때 전역변수를 클리어 한다.
		Vertex.arVertex.clear();
		/*
		 * 데이터 베이스 무결성을 확보하기 위해서 종료시에
		 * 저장되지 않능 Track정보는 DB에서 삭제 하여 준다.
		 */
		mDb.execSQL("delete from trackpoints where _id > (select max(stopid) from tracks)");
		mDb.close();


	}
	/* 
	 * double 좌표 값을 e6좌표 값으로 변경한다. 
	 * 즉 double을 int로 바꾸는 것이다. 이렇게 해야 
	 * 화면 좌표로 변환하는 함수를 실행 할 수 있다. 
	 */
	private GeoPoint getGeoPoint(Location location) {
		if (location == null) {
			return null;
		}
		Double lat = location.getLatitude() * 1E6;
		Double lng = location.getLongitude() * 1E6;
		return new GeoPoint(lat.intValue(), lng.intValue());
	}
	/*
	 * 칼로리를 계산하기위한 함수 이다.
	 */
	private double CalAc()
	{
		double mets;
		double cal;
		
		// Vo2max 즉 최대 산소 소비량을 먼저 구한다.
		mets = 0.1 * Math.round(mAvgSpped) + 3.5;
		// Met로 환산을 한다.
		mets = mets / 3.5;
		// 이제 칼로리로 변환을 한다.
		cal = 0.0175 * mets * weightPrefe;
		mCal += cal;	
		return mCal;
	}

	/*
	 * Location의 변화에 따른 작업을 수행해 주기위해 리스너를 정의해준다.
	 */
	LocationListener mListener = new LocationListener() {

		/*
		 * Location의 상태가 변화할 경우 자동으로 호출되어 진다.
		 * 따라서 그에따른 작업을 처리해 주는 코드가 작성되어 있다.
		 */
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			String sStatus = "";
			switch(status)
			{
			case LocationProvider.OUT_OF_SERVICE:
				sStatus = "범위 벗어남";
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				sStatus = "일시적 불능";
				break;
			case LocationProvider.AVAILABLE:
				sStatus = "사용 가능";
				break;
			}
			mTextStatus.setText(sStatus);
		}


		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			mTextStatus.setText("서비스 사용 가능");
		}


		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			mTextStatus.setText("서비스 사용 불가");
		}


		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub

			/* GPS 수신 횟수를 기록하는 변수 */
			mCount++;
			mDistance = new float[2];
			mDistance[0] = (float)0.0;
			
			/** 
			 * GPS 수신 횟수를 처음으로 돌린다는것은 리셋 하라는 것이다.
			 * 이때 모든 디스플레이 되어지는 변수를 초기화 해주어야 한다.
			 */
			if(mCount == 1)
			{
				mStartLatitude = location.getLatitude();
				mStartLongitude = location.getLongitude();
				//최초의 시작 시간을 재설정 한다.
				mStartTime = location.getTime();
				//총 거리를 재 설정 한다.
				mTotalDistance = 0;
				//총 운동 시간을 초기화 한다.
				mTotalTime = 0;
				//소비 칼로리를 초기화 한다. 
				mCal = 0.0;
				mAltitude = location.getAltitude();
			}
			
			/** 
			 * 거리를 측정하는 알고리즘 설명
			 * 위에서 mCount를 이용해서 맨 처음일 때에만 수행이 되어 지는 것이다.
			 * 따라서 맨 처음에는 처음-처음 이기때문에 이 된다 그다음 부터는
			 * 나중 - 처음 으로 동작한다. 처음을 현재값으로 지속적으로 변경 하기 때문이다.
			 */
			Location.distanceBetween(mStartLatitude,mStartLongitude,location.getLatitude(),
					location.getLongitude(),mDistance);
			mStartLatitude = location.getLatitude();
			mStartLongitude = location.getLongitude();
			mTotalDistance += mDistance[0];

			/**
			 * DB 저장을 위해 현재 시간을 매순간 로컬 변수에 저장하여 준다.
			 */
			
			// 현재 시간 값을 얻어 온다.
			mNowTime = location.getTime();
			// 거리 값을 변환한다.
			String sloc = String.format("%d",(int)mTotalDistance);
			mTextDistance.setText(sloc);
			// 순간 속력 값
			mNowSpeed = location.getSpeed();
			mTextNowSpeed.setText(String.format("%.2f", mNowSpeed));
			// 운동을 한 총 시간이다. 단위는 초 이다.
			mTotalTime = (mNowTime - mStartTime)/1000;
			mTextTotalTime.setText(String.format("%d",mTotalTime));
			// 평균 속력 값
			mAvgSpped = mTotalDistance/mTotalTime;
			mTextAverageSpeed.setText(String.format("%.2f",mAvgSpped));
			// 실시간 위도 값 출력
			sloc = String.format("%f",location.getLatitude());
			mTextLatitude.setText(sloc);
			// 실시간 경도 값 출력
			sloc = String.format("%f",location.getLongitude());
			mTextLongitude.setText(sloc);
			// 실시간 고도 값을 얻어 온다.
			mAltitude = location.getAltitude();
			
			/*
			 * 칼로리 값을 계산 한다.
			 */
			mTextKcal.setText(String.format("%.3f",CalAc()));

			/*
			 * 위치 값을 실시간으로 GeoPoint 값으로 반영해 준다. 
			 * GeoPoint로 변경하는이유는 double형이 아니라 int형이어야 
			 * 화면 좌표로 변환이 가능하기 때문이다.
			 */
			mGeoPoint = getGeoPoint(location);
			Vertex.arVertex.add(new Vertex(mGeoPoint.getLatitudeE6(),mGeoPoint.getLongitudeE6(),mCount));
			
			// DB 저장을 허용을 설정한다.
			saveCondition = true;
		}
	};
	
	//	실시가 DB 저장을 위한 Thread를 정의 한다.
	class SaveCurrentDB implements Runnable {

		public void run() {
			// TODO Auto-generated method stub
			/*
			 * DB TrackPoints Table에 실시간 데이터 삽입
			 */
			while(true)
			{
				if(saveCondition == true)
				{
					ContentValues row = new ContentValues();			
					row.put("trackid",mTrackId);
					row.put("latitude",mGeoPoint.getLatitudeE6());
					row.put("longitude",mGeoPoint.getLongitudeE6());
					row.put("time", mNowTime);
					row.put("speed", mNowSpeed);
					row.put("elevation", mAltitude);
					mDb.insert("trackpoints",null,row);
					
					// 5초마다 그래프 최신화. 몇초마다 바꿀지는 숫자를 바꾸면 된다.
					if(mTotalTime % 5 == 0){ 
						GraphActivity.mCurrentSeries.add((double)mTotalTime, (int)(mAltitude*10)/10F);
						if (GraphActivity.getGraphicalView() != null) {
							GraphActivity.getGraphicalView().repaint();
						}
					}
					saveCondition = false;
				}
				else{
					// 과부하를 막기위해 0.1초 단위로 체크한다 GPS 수신 변화는 아무리 빨라봐야 1초이다. 
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}



