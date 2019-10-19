package com.niproblema.parking7.DataObjects;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.niproblema.parking7.Activities.CoreActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Location implements Serializable, DataObject {
	final public String mCity;
	final public String mCountry;
	final public String mStreet;
	final public double mLat;
	final public double mLon;

	// Local only
	public List<Address> mNearbyAdresses;

	public Location(String city, String country, String street, double lat, double lon) {
		mCity = city;
		mCountry = country;
		mStreet = street;
		mLat = lat;
		mLon = lon;
	}

	@Nullable
	public static Location parse(DataSnapshot data) {
		try {
			// Parse
			String city = data.child("city").getValue(String.class);
			String country = data.child("country").getValue(String.class);
			String street = data.child("street").getValue(String.class);
			double lat = data.child("lat").getValue(Double.class);
			double lon = data.child("lon").getValue(Double.class);

			// Init
			return new Location(city, country, street, lat, lon);
		} catch (Exception e) {
			Log.e("LOCATION", "Error parsing location data: " + e.toString());
		}
		return null;
	}

	public static Location fromLatLng(Context context, LatLng latLng) {
/*		Location location = new Location()

		try {
			Geocoder geocoder = new Geocoder(context, Locale.getDefault());
			 = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
		}catch (Exception e){
			Log.e("LOCATION", "Error retrieving possible addresses: " + e.toString());
		}*/    // TODO
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

	@NonNull
	@Override
	public String toString() {
		return mStreet + "\n"
				+ mCity + "\n"
				+ mCountry + "\n"
				+ mLat + " " + mLon;
	}
}
