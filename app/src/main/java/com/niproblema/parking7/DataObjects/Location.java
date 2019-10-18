package com.niproblema.parking7.DataObjects;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Location implements Serializable, DataObject {
	final public String mCity;
	final public String mCountry;
	final public String mStreet;
	final public double mLat;
	final public double mLon;

	public Location(String city, String country, String street, double lat, double lon) {
		mCity = city;
		mCountry = country;
		mStreet = street;
		mLat = lat;
		mLon = lon;
	}

	public static Location parse(DataSnapshot data) {
		try {
			// Parse
			String city = (String) data.child("city").getValue();
			String country = (String) data.child("country").getValue();
			String street = (String) data.child("street").getValue();
			double lat = (double) data.child("lat").getValue();
			double lon = (double) data.child("lon").getValue();

			// Init
			return new Location(city, country, street, lat, lon);
		} catch (Exception e) {
			Log.e("LOCATION", "Error parsing location data: " + e.toString());
		}
		return null;
	}

	@Override
	public HashMap<String, Object> getSubmittableObject() {
		return new HashMap<String, Object>() {{
			put("city", mCity);
			put("country", mCountry);
			put("street", mStreet);
			put("lon", mLon);
			put("lat", mLat);
		}};
	}
}
