package com.niproblema.parking7;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;


public class ParkingPlace implements Serializable {
    public String id;
    public boolean available;
    public double lat;
    public double lon;
    public String owner;
    public double price;
    public String description;

    public ParkingPlace(String id, boolean availability, double lat, double lon, String owner, double price, String description) {
        this.id = id;
        this.available = availability;
        this.lat = lat;
        this.lon = lon;
        this.owner = owner;
        this.price = price;
        this.description = description;
    }

    @NonNull
    @Override
    public String toString() {
        return "Parking spot:" + id + "\n" + owner + " " + price + "\n" + lat + " " + lon;
    }

    public LatLng GetLocation() {
        return new LatLng(lat, lon);
    }
}
