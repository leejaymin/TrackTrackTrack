/* 
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import java.io.*;

import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.location.*;
import android.util.*;
/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * 이 클래스는 SD card로 DB의 데이터를 exprots 하는 기능을 담당한다. 
 * 포멧 형태는 GPX 표준 1.1 을 따른다.
 */
public class TrackWriter {

	//UI 프로그레스 바를 종료 시키기위해 호출한 context를 가지고 있는다.
	private final Context context;
	
	// exprot를 위해서 선택되어진 트랙의 정보를 가지고 있는다.
	private final Track track;
	
	// 트랙 정보에서 트랙 포인트 정보를 추출하기 위해 db 접근 객체를 가지고 있는다.
	SQLiteDatabase db;

	// GPX 포멧에 대한 구체적인 정보를 담고 있는 클래스이다.
	private final GpxTrackWriter writer;
	
	// 디렉토리가 존재할떄, 없을때, 파일이 중복 되엇을때 등 모든 파일 관련 입출력을 담당하는 클래스이다.
	private final FileUtils fileUtils;
	
	// 프로그레스 바의 완료 여부를 담당하는 쓰레드 이다.
	private Runnable onCompletion = null;
	
	// 성공적으로 파일이 생성 되었는지 여부를 판단한다.
	private boolean success = false;
	private int errorMessage = -1;
	private File directory = null;
	private File file = null;
	public static final String TAG = "TrackTrackTrack";
	
	// 생성자를 통해 필요한 객체 값을 모두 얻어온다.
	public TrackWriter(SaveListView context ,Track track,SQLiteDatabase db) {
		this.context = context;
		this.track = track;
		this.fileUtils = new FileUtils();
		this.writer = new GpxTrackWriter();
		this.db = db;
	}

	/*
	 * 파일 생성 완료시 호출되어져 프로그레스 바를 종료하는 쓰레드이다..
	 * @param 프로그레스    바를 종료하는 Runnable을 인자로 받는다.
	 */
	public void setOnCompletion(Runnable onCompletion) {
		this.onCompletion = onCompletion;
	}

	/* SD card에 생성 되어질 디렉토리 이름을 저장 한다. */
	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

	/*
	 * 실제적으로 파일을 기록하는 쓰레드 이다.
	 * non-blocking 상태에서 실행 되어 진다.
	 */
	public void writeTrackAsync() {
		Thread t = new Thread() {
			@Override
			public void run() {
				writeTrack();
			}
		};
		t.start();
	}

	public void writeTrack() {
		// Open the input and output
		success = false;
		errorMessage = R.string.error_track_does_not_exist;
		if (track != null) {
			if (openFile()) {
				writeDocument();
			}
		}
		finished();
	}

	public int getErrorMessage() {
		return errorMessage;
	}

	public boolean wasSuccess() {
		return success;
	}

	/* 프로그레스 바를 실제적으로 죵료 하는 메소드 이다. */
	private void finished() {
		if (onCompletion != null) {
			runOnUiThread(onCompletion);
			return;
		}
	}

	/* 프로그레스바의 내용을 출력하여 준다. */
	protected void runOnUiThread(Runnable runnable) {
		if (context instanceof Activity) {
			((Activity) context).runOnUiThread(runnable);
		}
	}
	
	/* 파일을 생성하는 모든 예외적인 상황을 담당하여 반드시 파일을 생성하여 준다. */
	protected boolean openFile() {
		if (!canWriteFile()) {
			return false;
		}

		// 중복된 이름이 있을경우 파일의 이름에 (1) , (2) 를 순서대로 증가 시켜 준다.
		String fileName = fileUtils.buildUniqueFileName(
				directory, track.getName(), writer.getExtension());
		if (fileName == null) {
			Log.e(TAG,
					"Unable to get a unique filename for " + fileName);
			return false;
		}

		//  정상적인 파일 기록을 위해 데이터 처리 스트림을 데이터 싱크 스트림에게 넘겨준다. 트랙 정보와 같이
		Log.i(TAG, "Writing track to: " + fileName);
		try {
			writer.prepare(track, newOutputStream(fileName));
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to open output file.", e);
			errorMessage = R.string.io_write_failed;
			return false;
		}
		return true;
	}

	/* 디렉토리의 여부를 체크한다음 없으면 디렉토리를 생성 한다. */
	protected boolean canWriteFile() {
		if (directory == null) {
			String dirName =
				fileUtils.buildExternalDirectoryPath(writer.getExtension());
			directory = newFile(dirName);
		}

		if (!fileUtils.isSdCardAvailable()) {
			Log.i(TAG, "Could not find SD card.");
			errorMessage = R.string.io_no_external_storage_found;
			return false;
		}
		if (!fileUtils.ensureDirectoryExists(directory)) {
			Log.i(TAG, "Could not create export directory.");
			errorMessage = R.string.io_create_dir_failed;
			return false;
		}

		return true;
	}

	/* 파일 이름으로 데이터 처리 스트림을 생성하여 준다. */
	protected OutputStream newOutputStream(String fileName)
	throws FileNotFoundException {
		file = new File(directory, fileName);
		return new FileOutputStream(file);
	}

	/* 해당 경로로 파일을 생성 한다. */
	protected File newFile(String path) {
		return new File(path);
	}

	/* 실제적으로 GpxTrackWriter를 이용해서 파일에 내용을 기록하는 메소드 이다. */
	void writeDocument() {
		Log.d(TAG, "Started writing track.");
		writer.writeHeader();
		// TODO: Fix ordering (in GPX waypoints should come first)
		writeLocations();
		writer.writeFooter();
		writer.close();
		success = true;
		Log.d(TAG, "Done writing track.");
		errorMessage = R.string.io_write_finished;
	}

	/* 
	 * 가장 핵심적인 메소드 이다. 
	 * Track을 통해서 해당 startId와 stopId를 기준으로 
	 * 모든 Location값을 얻어와 순차적으로 파일에 기록을 하여 준다. 
	 */
	private void writeLocations() {
		boolean wroteFirst = false;
		boolean segmentOpen = false;
		Location lastLoc = null, loc = null;
		boolean isLastValid = false;
		Cursor locationsCursor =
			db.rawQuery("select * from trackpoints " +
					"where trackid = "+track.getId()+" order by _id ASC", null);

		if (locationsCursor == null || !locationsCursor.moveToFirst()) {
			Log.w(TAG, "Unable to get any points to write");
			return;
		}

		do {
			if (loc == null) loc = new Location("");
			fillLocation(locationsCursor, loc);

			boolean isValid = isValidLocation(loc);
			boolean validSegment = isValid && isLastValid;
			//    요효화지 않은 트랙 포인트 값이고 포인트의 끝이라면 트랙 포인트 기록을 중지 한다.
			if (!wroteFirst && validSegment) {
				writer.writeBeginTrack(lastLoc);
				wroteFirst = true;
			}

			if (validSegment) {
				//    	맨 처음일 때만 <trkseg> 새겨주기 위해서 출력하여 준다.
				if (!segmentOpen) {

					writer.writeOpenSegment();
					segmentOpen = true;

					writer.writeLocation(lastLoc);
				}

				// 현재의 location 값을 기록 하여 준다.
				writer.writeLocation(loc);
			} else {
				if (segmentOpen) {
					writer.writeCloseSegment();
					segmentOpen = false;
				}
			}

			// location 값을 증가 시키기 위해서 리셋 해준다.
			Location tmp = lastLoc;
			lastLoc = loc;
			loc = tmp;
			if (loc != null) loc.reset();

			isLastValid = isValid;
		} while (locationsCursor.moveToNext());

		if (segmentOpen) {
			writer.writeCloseSegment();
			segmentOpen = false;
		}
		if (wroteFirst) {
			writer.writeEndTrack(lastLoc);
		}
	}
	/**
	 * DB에 저장된 위도, 경도는 모두 GeoPoint 값이다. 하지만 GPX 표준은 00.000000 형식의 순수한
	 * 위도 경도 값이다. 이밖에도 시간 또한 UTC 방식을 채택하고 있다. 이러한 포멧 규격을 맞추기 위해서
	 * 변환을 반드시 해주어야 한다.
	 * 
	 * @param cursor : 현재의 로케이션 값이 들어있는 커서이다.
	 * @param location : 로케이션 값이 실제로 저장 되어져야할 객체이다.
	 */
	public void fillLocation(Cursor cursor, Location location) {
		location.reset();

		int idxLatitude = cursor.getColumnIndexOrThrow(TrackPointsColumns.LATITUDE);
		int idxLongitude =
			cursor.getColumnIndexOrThrow(TrackPointsColumns.LONGITUDE);
		int idxAltitude = cursor.getColumnIndexOrThrow(TrackPointsColumns.ALTITUDE);
		int idxTime = cursor.getColumnIndexOrThrow(TrackPointsColumns.TIME);
		int idxBearing = cursor.getColumnIndexOrThrow(TrackPointsColumns.BEARING);
		int idxAccuracy = cursor.getColumnIndexOrThrow(TrackPointsColumns.ACCURACY);
		int idxSpeed = cursor.getColumnIndexOrThrow(TrackPointsColumns.SPEED);

		if (!cursor.isNull(idxLatitude)) {
			location.setLatitude(1. * cursor.getInt(idxLatitude) / 1E6);
		}
		if (!cursor.isNull(idxLongitude)) {
			location.setLongitude(1. * cursor.getInt(idxLongitude) / 1E6);
		}
		if (!cursor.isNull(idxAltitude)) {
			location.setAltitude(cursor.getFloat(idxAltitude));
		}
		if (!cursor.isNull(idxTime)) {
			location.setTime(cursor.getLong(idxTime));
		}
		if (!cursor.isNull(idxBearing)) {
			location.setBearing(cursor.getFloat(idxBearing));
		}
		if (!cursor.isNull(idxSpeed)) {
			location.setSpeed(cursor.getFloat(idxSpeed));
		}
		if (!cursor.isNull(idxAccuracy)) {
			location.setAccuracy(cursor.getFloat(idxAccuracy));
		}
	}
	/*
	 * 변환된 순수한 로케이션값일 경우 위도는 90도를 넘지 못하고 경도는 180도를 넘지 못한다.
	 * 이것을 체크하여 유효한 값인지를 판별한다.
	 */
	public static boolean isValidLocation(Location location) {
		return location != null && Math.abs(location.getLatitude()) <= 90
		&& Math.abs(location.getLongitude()) <= 180;
	}
}
