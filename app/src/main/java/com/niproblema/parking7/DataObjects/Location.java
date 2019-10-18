package com.niproblema.parking7.DataObjects;

import java.io.Serializable;
import java.util.HashMap;

public class Location implements Serializable, DataObject {
	final public String mCity;
	final public String mCountry;
	final public String mStreet;
	final public double mLat;
	final public double mLon;

	public Location (String city, String country, String street, double lat, double lon){
		mCity = city;
		mCountry = country;
		mStreet = street;
		mLat = lat;
		mLon = lon;
	}

	@Override
	public HashMap<String, Object> getSubmittableObject(){
		return new HashMap<String, Object>(){{
			put("city", mCity);
			put("country", mCountry);
			put("street", mStreet);
			put("lon", mLon);
			put("lat", mLat);
		}};
	}
}
