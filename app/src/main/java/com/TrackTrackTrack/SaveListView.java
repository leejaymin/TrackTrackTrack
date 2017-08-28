/* 
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import java.text.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.*;
import android.os.*;
import android.provider.ContactsContract.*;
import android.util.*;
import android.view.*;
import android.view.WindowManager.*;
import android.widget.*;

/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * 기록정보 Activity를 담당하는 Class이다.
 */
public class SaveListView extends Activity {

	static final SimpleDateFormat TIMESTAMP_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static {
		TIMESTAMP_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	// 다이얼로그를 출력하기 위한 변수.
	public static final int DIALOG_WRITE_PROGRESS = 3;

	// 프로그레스바 변수이다.
	private ProgressDialog mWriteProgressDialog;

	// 트랙 객체를 생성하여준다. 파일에 쓰기위해 데이터를 총 관리 하기 위해서 이다.
	Track mTrack;

	LocationDB mLocationDB;
	Cursor mCursor;
	SQLiteDatabase mDb;

	// 롱 클릭시 발생하는 이벤트를 저장 한다.
	int mPositionLongClick;

	// contextMenu 처리를 위해 새롭게 ListView를 설정하고 공유한다.
	ListView mMyList;

	// 커스터 마이징 한 어뎁터이다.
	ContactListItemAdapter mAdapter;
	private static final String TAG = "SaveListActivity";
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loadsave);

		mLocationDB = new LocationDB(this);
		mDb = mLocationDB.getReadableDatabase();
		mMyList = (ListView)findViewById(R.id.save_list);

		Log.v(TAG,"success 1");

		/* _id 가 반드시 포함되어야 하고 trackid가 중복 되지 않아야 하기 때문에 다음과 같은 쿼리문을 작성 하였다. */
		mCursor = mDb.rawQuery("select _id, name, starttime, totaltime, " +
				"totaldistance, description,startid, stopid, numpoints, " +
				"category from tracks", null);

		Log.v(TAG,"success 2");

		/*
		 * 커서를 Activity의 라이프 사이클과 맞춰 준다.
		 * 이것을 통해서 커서를 닫아주고 생성해 주고를 생각할 필요가없다.
		 * 단지 최종적으로 프로그래머는 onDestroy 때 에만 커서를 닫아 주면 된다. 
		 */
		startManagingCursor(mCursor);

		Log.v(TAG,"success 3");

		mMyList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mMyList.setOnItemClickListener(mItemClickListener);
		registerForContextMenu(mMyList);

		mAdapter = new ContactListItemAdapter(this, R.layout.track_list_item, mCursor);
		mMyList.setAdapter(mAdapter);
	}

	AdapterView.OnItemClickListener mItemClickListener = new
	AdapterView.OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			int mStartPoint=0;
			int mStopPoint=0;
			// 총 포인트
			int mNumPoint = 0; 
			String mes="";
			if(mCursor.moveToPosition(position)){
				/* 
				 * 시작 포인트와 끝나는 포인트를 넘겨 준다.
				 * 단 트랙의 포인트 값이 0라면 토스트 메시지를
				 * 띠워서 표시할 경로가 없다고 처리해준다. 
				 */
				mStartPoint = mCursor.getInt(6);
				mStopPoint = mCursor.getInt(7);
				mNumPoint = mCursor.getInt(8);
			}
			if(mNumPoint == 0)
			{
				// 0 이라는것은 저장된 파일이 포인트 값이 하나도 없다는것 이다.
				// 그것은 즉 표시할 내용이 하나도 없다는 것이다.
				Toast.makeText(SaveListView.this,"표시할 포인트 값이 하나도 없습니다. 저장이 잘못되었거나 손실 되었습니다." +
						"", Toast.LENGTH_SHORT).show();
				return;
			}
			Toast.makeText(SaveListView.this,String.format("S:%d~E:%d",
					mStartPoint,mStopPoint),Toast.LENGTH_SHORT).show();

			Intent intent = new Intent(SaveListView.this,LoadMapActivity.class);
			intent.putExtra("TrackStartPoint",mStartPoint);
			intent.putExtra("TrackStopPoint",mStopPoint);
			startActivity(intent);
		}
	};
	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG,"onResume !!");
		// 매번 다시 커서 값을 읽어온다. 그래야 갱신된 정보가 누락 되지 않는다. 
		mCursor = mDb.rawQuery("select _id, name, starttime, totaltime, " +
				"totaldistance, description, startid, stopid, numpoints, category from tracks", null);
		mAdapter.changeCursor(mCursor);
	}	
	@Override
	public void onPause() {
		super.onPause();
		Log.v(TAG,"onPayse !!");
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG,"onDestroy !!");
		mCursor.close();
	}
	//커서 어뎁터의 커스터 마이징 원하는 값을 출력 해주기 위해서 한다.
	private final class ContactListItemAdapter extends ResourceCursorAdapter {
		public ContactListItemAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final ContactListItemCache cache = (ContactListItemCache) view.getTag();
			//캐쉬에서 꺼내는 작업
			TextView bitemName = cache.itemName;
			TextView bitemCategory = cache.itemCategory;
			TextView bitemTime = cache.itemTime;
			TextView bitemStats = cache.itemStats;
			TextView bitemDescription = cache.itemDescription;
			bitemName.setText(cursor.getString(1));
			bitemCategory.setText(cursor.getString(9));

			//	시간을 변환하여 준다.
			if ( cursor.getInt(2) != 0){
				Date myDate = new Date(cursor.getLong(2));
				bitemTime.setText(TIMESTAMP_FORMAT.format(myDate));
			}
			else{
				bitemTime.setText("시작 시간:"+cursor.getString(2));
			}
			bitemStats.setText("시간/거리:"+cursor.getString(3)+"sec/"+cursor.getString(4)+"m");
			bitemDescription.setText(cursor.getString(5));

		}
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = super.newView(context, cursor, parent);
			ContactListItemCache cache = new ContactListItemCache();
			cache.imageView = (ImageView) view.findViewById(R.id.trackdetails_item_icon);
			cache.itemName = (TextView) view.findViewById(R.id.trackdetails_item_name);
			cache.itemCategory = (TextView) view.findViewById(R.id.trackdetails_item_category);
			cache.itemTime = (TextView) view.findViewById(R.id.trackdetails_item_time);
			cache.itemStats = (TextView) view.findViewById(R.id.trackdetails_item_stats);
			cache.itemDescription = (TextView) view.findViewById(R.id.trackdetails_item_description);	
			view.setTag(cache);
			return view;
		}
	}
	final static class ContactListItemCache {
		public ImageView imageView;
		public TextView itemName;
		public TextView itemCategory;
		public TextView itemTime;
		public TextView itemStats;
		public TextView itemDescription;
	}

	//	컨텍스트 메뉴를 등록하여 준다.
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		Log.v(TAG, "onCreateContextMenu !");
		if(v == mMyList){
			/*
			 * 포지션 위치값을 얻어와야한다.
			 * 리스트뷰를 커스텀 해야되는줄 알고 식겁했지만.. 다행이도 변환만 해주면 되었다.
			 * 이거 블로그에있어서 다행이지.. 아니었다면 엄청 고생 할 문제였던것 같다. 
			 */
			mPositionLongClick = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			menu.setHeaderTitle("long click menu");
			menu.add(0,1,0,"GPX Write to SD card...");
			menu.add(0,2,0,"선택 데이터 삭제");
			menu.add(0,3,0,"모든 데이터 삭제");
		}
	}
	// 컨텍스트 메뉴에 따른 처리를 해준다.
	public boolean onContextItemSelected (MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			// 존의 커서로 현재 포지션 즉 선택되었을때의 ID만 얻어온다.
			mCursor.moveToPosition(mPositionLongClick);
			long mTrackId = mCursor.getLong(0);
			
			// Track 객체를 생성하기 위한 임시 커서를 만들고 선택된 trackId의 모든 속성값을 추출해 온다.
			Cursor icursor = mDb.rawQuery("select * from tracks where _id = "
					+mTrackId, null);
			icursor.moveToFirst();

			mTrack = createTrack(icursor);
			
			// GPX 저장을 위해 saveTrack을 호출 한다.
			saveTrack(mTrack);
			return true;
			
		case 2:
			// 기존의 커서로 현재 포지션 즉 선택되었을때의 ID만 얻어온다.
			mCursor.moveToPosition(mPositionLongClick);
			mDb.execSQL("delete from tracks where _id = "+mCursor.getLong(0));
			mDb.execSQL("delete from trackpoints where trackid = "+mCursor.getLong(0));
			Toast.makeText(SaveListView.this,String.format("선택 데이터를 삭제 하였습니다.."), Toast.LENGTH_SHORT)
			.show();
			
			// 삭제된 결과를 즉시 반영해 준다.
			mCursor = mDb.rawQuery("select _id, name, starttime, totaltime, " +
					"totaldistance, description, startid, stopid, numpoints, category from tracks", null);
			mAdapter.changeCursor(mCursor);
			return true;
			
		case 3:
			// 삭제시 포지션 값을 화면에 출력 하여 준다.
			Toast.makeText(SaveListView.this,String.format("모든 데이터를 삭제 하셨습니다."), Toast.LENGTH_SHORT)
			.show();
			mDb.delete("tracks",null,null);
			mDb.delete("trackpoints",null,null);
			// 삭제된 결과를 즉시 반영해 준다.
			mCursor = mDb.rawQuery("select _id, name, starttime, totaltime, " +
					"totaldistance, description, startid, stopid, numpoints, category from tracks", null);
			mAdapter.changeCursor(mCursor);
			return true;
		}
		return false;
	}
	/*
	 * Dialog events:
	 * ==============
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_WRITE_PROGRESS:
			mWriteProgressDialog = new ProgressDialog(this);
			mWriteProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
			mWriteProgressDialog.setTitle(getString(R.string.progress_title));
			mWriteProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mWriteProgressDialog.setMessage(
					getString(R.string.write_progress_message));
			return mWriteProgressDialog;
		}
		return null;
	}
	public void showDialogSafely(final int id) {
		// 강제적으로 UI Thread와 동기화 시키는 쓰레드 이다.
		// 즉 화면의 표시를 결정하기 위해 사용되어 진다.
		runOnUiThread(new Runnable() {
			public void run() {
				try {
					// 다이얼로그 인스턴스를 한번 생성하고 지속시켜서 매번 그것을 재사용 한다.
					// 어떤 다이얼로그가 불려지는지 정확히 알기 위해서는 onCreateDialog 핸들러를 봐야 한다.
					showDialog(id);
				} catch (BadTokenException e) {
					Log.w(TAG,
							"Could not display dialog with id " + id, e);
				} catch (IllegalStateException e) {
					Log.w(TAG,
							"Could not display dialog with id " + id, e);
				}
			}
		});
	}
	/**
	 * Saves the track with the given id to the SD card.
	 * 실제적으로 SD 카드에 지정된 포멧을 저장을 한다.
	 * 선택 되어진 TrackId와 그것을 어떤 포멧으로 변환 할지에대한 정보가 인자로 들어 온다.
	 * @param trackId The id of the track to be sent
	 */
	public void saveTrack(Track track) {
		showDialogSafely(DIALOG_WRITE_PROGRESS);
		// 기록을 위해 기록 클래스를 생성 한다.
		final TrackWriter writer = new TrackWriter(this,track,mDb);
		// 작업 진행을 알려주기위한 상태표시창을 제거할 수있는 기능과 결과를 알려줄 수 있는 기능을 Runnable로 넘겨준다.
		writer.setOnCompletion(new Runnable() {
			public void run() {
				dismissDialogSafely(DIALOG_WRITE_PROGRESS);
				// 결과 메시지를 처리하여 준다. 성공 했는지 실패 했는지를..
				showMessageDialog(writer.getErrorMessage(), writer.wasSuccess());
			}
		});
		writer.writeTrackAsync();

	}
	/**
	 * GPX 처리 결과에 대한 메시지를 띠워 준다.
	 */
	public void showMessageDialog(final int message, final boolean success) {
		runOnUiThread(new Runnable() {
			public void run() {
				AlertDialog dialog = null;
				AlertDialog.Builder builder = new AlertDialog.Builder(SaveListView.this);
				builder.setMessage(SaveListView.this.getString(message));
				builder.setNegativeButton(SaveListView.this.getString(R.string.ok), null);
				builder.setIcon(success ? android.R.drawable.ic_dialog_info :
					android.R.drawable.ic_dialog_alert);
				builder.setTitle(success ? R.string.success : R.string.error);
				dialog = builder.create();
				dialog.show();
			}
		});
	}
	/* Dialog를 종료해 준다.*/
	public void dismissDialogSafely(final int id) {
		runOnUiThread(new Runnable() {
			public void run() {
				try {
					dismissDialog(id);
				} catch (IllegalArgumentException e) {
					// 에러 핸들링 코드를 작성 한다.
				}
			}
		});
	}
	public Track createTrack(Cursor cursor) {
		int idxId = cursor.getColumnIndexOrThrow(TracksColumns._ID);
		int idxName = cursor.getColumnIndexOrThrow(TracksColumns.NAME);
		int idxDescription =
			cursor.getColumnIndexOrThrow(TracksColumns.DESCRIPTION);
		int idxMapId = cursor.getColumnIndexOrThrow(TracksColumns.MAPID);
		int idxCategory = cursor.getColumnIndexOrThrow(TracksColumns.CATEGORY);
		int idxStartId = cursor.getColumnIndexOrThrow(TracksColumns.STARTID);
		int idxStartTime = cursor.getColumnIndexOrThrow(TracksColumns.STARTTIME);
		int idxStopTime = cursor.getColumnIndexOrThrow(TracksColumns.STOPTIME);
		int idxStopId = cursor.getColumnIndexOrThrow(TracksColumns.STOPID);
		int idxNumPoints = cursor.getColumnIndexOrThrow(TracksColumns.NUMPOINTS);
		int idxMaxlat = cursor.getColumnIndexOrThrow(TracksColumns.MAXLAT);
		int idxMinlat = cursor.getColumnIndexOrThrow(TracksColumns.MINLAT);
		int idxMaxlon = cursor.getColumnIndexOrThrow(TracksColumns.MAXLON);
		int idxMinlon = cursor.getColumnIndexOrThrow(TracksColumns.MINLON);
		int idxTotalDistance =
			cursor.getColumnIndexOrThrow(TracksColumns.TOTALDISTANCE);
		int idxTotalTime = cursor.getColumnIndexOrThrow(TracksColumns.TOTALTIME);
		int idxMovingTime = cursor.getColumnIndexOrThrow(TracksColumns.MOVINGTIME);
		int idxMaxSpeed = cursor.getColumnIndexOrThrow(TracksColumns.MAXSPEED);
		int idxMinElevation =
			cursor.getColumnIndexOrThrow(TracksColumns.MINELEVATION);
		int idxMaxElevation =
			cursor.getColumnIndexOrThrow(TracksColumns.MAXELEVATION);
		int idxElevationGain =
			cursor.getColumnIndexOrThrow(TracksColumns.ELEVATIONGAIN);
		int idxMinGrade = cursor.getColumnIndexOrThrow(TracksColumns.MINGRADE);
		int idxMaxGrade = cursor.getColumnIndexOrThrow(TracksColumns.MAXGRADE);

		Track track = new Track();
		if (!cursor.isNull(idxId)) {
			track.setId(cursor.getLong(idxId));
		}
		if (!cursor.isNull(idxName)) {
			track.setName(cursor.getString(idxName));
		}
		if (!cursor.isNull(idxDescription)) {
			track.setDescription(cursor.getString(idxDescription));
		}
		if (!cursor.isNull(idxMapId)) {
			track.setMapId(cursor.getString(idxMapId));
		}
		if (!cursor.isNull(idxCategory)) {
			track.setCategory(cursor.getString(idxCategory));
		}
		if (!cursor.isNull(idxStartId)) {
			track.setStartId(cursor.getInt(idxStartId));
		}
		if (!cursor.isNull(idxStopId)) {
			track.setStopId(cursor.getInt(idxStopId));
		}
		if (!cursor.isNull(idxNumPoints)) {
			track.setNumberOfPoints(cursor.getInt(idxNumPoints));
		}
		return track;
	}
}

