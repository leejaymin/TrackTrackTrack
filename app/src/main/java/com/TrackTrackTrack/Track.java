/* 
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * GPX Format으로 Write하기 위해서는 임시로 Track의 정보를  가지고 있어야 하는
 * Class가 필요하다. 즉 인텐트를 통해서 객체를 넘겨 줘야할 필요성있다.
 * 그 작업 처리를 위해서 Parcelable를 상속받은다음 Class를 정의해 주었다.
 */
public class Track implements Parcelable {

	public static class Creator implements Parcelable.Creator<Track> {

		public Track createFromParcel(Parcel source) {
			ClassLoader classLoader = getClass().getClassLoader();
			Track track = new Track();
			track.id = source.readLong();
			track.name = source.readString();
			track.description = source.readString();
			track.mapId = source.readString();
			track.category = source.readString();
			track.startId = source.readLong();
			track.stopId = source.readLong();

			track.numberOfPoints = source.readInt();
			for (int i = 0; i < track.numberOfPoints; ++i) {
				Location loc = source.readParcelable(classLoader);
				track.locations.add(loc);
			}

			return track;
		}

		public Track[] newArray(int size) {
			return new Track[size];
		}
	}

	public static final Creator CREATOR = new Creator();

	private ArrayList<Location> locations = new ArrayList<Location>();

	private int numberOfPoints = 0;

	private long id = -1;
	private String name = "";
	private String description = "";
	private String mapId = "";
	private long startId = -1;
	private long stopId = -1;
	private String category = "";
	public Track() {
	}

	public void writeToParcel(Parcel dest, int flags) {
		 dest.writeLong(id);
		 dest.writeString(name);
		 dest.writeString(description);
		 dest.writeString(mapId);
		 dest.writeString(category);
		 dest.writeLong(startId);
		 dest.writeLong(stopId);

		 dest.writeInt(numberOfPoints);
		 for (int i = 0; i < numberOfPoints; ++i) {
			 dest.writeParcelable(locations.get(i), 0);
		 }
	 }
	 // Getters and setters:
	 //---------------------
	 public int describeContents() {
		 return 0;
	 }

	 public long getId() {
		 return id;
	 }

	 public void setId(long id) {
		 this.id = id;
	 }

	 public String getName() {
		 return name;
	 }

	 public void setName(String name) {
		 this.name = name;
	 }

	 public long getStartId() {
		 return startId;
	 }

	 public void setStartId(long startId) {
		 this.startId = startId;
	 }

	 public long getStopId() {
		 return stopId;
	 }

	 public void setStopId(long stopId) {
		 this.stopId = stopId;
	 }

	 public String getDescription() {
		 return description;
	 }

	 public void setDescription(String description) {
		 this.description = description;
	 }

	 public String getMapId() {
		 return mapId;
	 }

	 public void setMapId(String mapId) {
		 this.mapId = mapId;
	 }

	 public String getCategory() {
		 return category;
	 }

	 public void setCategory(String category) {
		 this.category = category;
	 }

	 public int getNumberOfPoints() {
		 return numberOfPoints;
	 }

	 public void setNumberOfPoints(int numberOfPoints) {
		 this.numberOfPoints = numberOfPoints;
	 }

	 public void addLocation(Location l) {
		 locations.add(l);
	 }

	 public ArrayList<Location> getLocations() {
		 return locations;
	 }

	 public void setLocations(ArrayList<Location> locations) {
		 this.locations = locations;
	 }
}
