/*
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import android.location.Location;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * GPX 포멧에 대한 정보가 들어있는 클래스이다. 
 * TrackWriter 클래스에 의해서 이 포멧정보가 순서대로 호출되어진다.
 */
public class GpxTrackWriter{

	static final SimpleDateFormat TIMESTAMP_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static {
		TIMESTAMP_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private PrintWriter mPw = null;

	private Track mTrack;

	public void prepare(Track track, OutputStream out) {
		this.mTrack = track;
		this.mPw = new PrintWriter(out);
	}

	public String getExtension() {
		return "GPX";
	}

	public void writeHeader() {
		if (mPw != null) {
			mPw.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ");
			mPw.println("standalone=\"yes\"?>");
			mPw.println("<?xml-stylesheet type=\"text/xsl\" href=\"details.xsl\"?>");
			mPw.println("<gpx");
			mPw.println(" version=\"1.0\"");
			mPw.println(" creator=\"TrackTrackTrack for the G1 running Android\"");
			mPw.println(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
			mPw.println(" xmlns=\"http://www.topografix.com/GPX/1/1\"");
			mPw.print(" xmlns:topografix=\"http://www.topografix.com/GPX/Private/"
					+ "TopoGrafix/0/1\"");
			mPw.print(" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 ");
			mPw.print("http://www.topografix.com/GPX/1/0/gpx.xsd ");
			mPw.print("http://www.topografix.com/GPX/Private/TopoGrafix/0/1 ");
			mPw.println("http://www.topografix.com/GPX/Private/TopoGrafix/0/1/"
					+ "topografix.xsd\">");
		}
	}

	public void writeFooter() {
		if (mPw != null) {
			mPw.println("</gpx>");
		}
	}

	public void writeBeginTrack(Location firstPoint) {
		if (mPw != null) {
			mPw.println("<trk>");
			mPw.println("<name>" + stringAsCData(mTrack.getName())
					+ "</name>");
			mPw.println("<desc>" + stringAsCData(mTrack.getDescription())
					+ "</desc>");
			mPw.println("<number>" + mTrack.getId() + "</number>");
			/*
			 * DDMS에서 GPX File이 불러와 지지 않은 문제로 인해 소스를 변경 하였다.
			 * 추후에 분석의 여지가 있어 일단 주석 처리 하였다.
			 */
			// pw.println("<topografix:color>c0c0c0</topografix:color>");
		}
	}
	public static String stringAsCData(String unescaped) {
		/*
		 * DDMS에서 GPX File이 불러와 지지 않은 문제로 인해 소스를 변경 하였다.
		 * 추후에 분석의 여지가 있어 일단 주석 처리 하였다.
		 */
		// "]]>" needs to be broken into multiple CDATA segments, like:
		// "Foo]]>Bar" becomes "<![CDATA[Foo]]]]><![CDATA[>Bar]]>"
		// (the end of the first CDATA has the "]]", the other has ">")
		String escaped = unescaped.replaceAll("]]>", "]]]]><![CDATA[>");
		return "<![CDATA[" + escaped + "]]>";
	}
	public void writeEndTrack(Location lastPoint) {
		if (mPw != null) {
			mPw.println("</trk>");
		}
	}

	public void writeOpenSegment() {
		mPw.println("<trkseg>");
	}

	public void writeCloseSegment() {
		mPw.println("</trkseg>");
	}

	public void writeLocation(Location l) {
		if (mPw != null) {
			mPw.println("<trkpt lat=\"" + l.getLatitude() + "\" lon=\""
					+ l.getLongitude() + "\">");
			Date d = new Date(l.getTime());
			mPw.println("<ele>" + l.getAltitude() + "</ele>");
			mPw.println("<time>" + TIMESTAMP_FORMAT.format(d) + "</time>");
			mPw.println("</trkpt>");
		}
	}

	public void close() {
		if (mPw != null) {
			mPw.close();
			mPw = null;
		}
	}

	/*
	 * Waypoint는 현재 지원하지 않으므로 사용하지 않는다.  
	 */
	/*
  public void writeWaypoint(Waypoint waypoint) {
    if (pw != null) {
      // TODO: The gpx spec says waypoints should come *before* tracks
      Location l = waypoint.getLocation();
      if (l != null) {
        pw.println("<wpt lat=\"" + l.getLatitude() + "\" lon=\""
            + l.getLongitude() + "\">");
        pw.println("<name>" + StringUtils.stringAsCData(waypoint.getName())
            + "</name>");
        pw.println("<desc>"
            + StringUtils.stringAsCData(waypoint.getDescription()) + "</desc>");
        pw.println("<time>" + TIMESTAMP_FORMAT.format(l.getTime()) + "</time>");
        pw.println("<ele>" + l.getAltitude() + "</ele>");
        pw.println("</wpt>");
      }
    }
  }
	 */
}
