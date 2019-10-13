package com.niproblema.parking7.Activities;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.niproblema.parking7.Activities.LocationPreviewActivity;
import com.niproblema.parking7.ParkingPlace;
import com.niproblema.parking7.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class MapViewFragment extends Fragment implements GoogleMap.OnMarkerClickListener {
    private MapView mMapView;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mParkingsRef;

    private HashMap<String, ParkingPlace> locationsMap = new HashMap<String, ParkingPlace>();
    private Stack<Marker> currentMarkers = new Stack<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.maps_fragment, container, false);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        Intent lauchIntent = new Intent(getActivity(), LocationPreviewActivity.class);
                        lauchIntent.putExtra("new", true);
                        lauchIntent.putExtra("pos", latLng);
                        startActivity(lauchIntent);
                    }
                });
                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Intent lauchIntent = new Intent(getActivity(), LocationPreviewActivity.class);
                        lauchIntent.putExtra("parkingPlace", locationsMap.get((String) marker.getTag()));
                        startActivity(lauchIntent);
                    }
                });
                // For showing a move to my GetLocation button
                googleMap.setMyLocationEnabled(true);
                Location location = googleMap.getMyLocation();
                if (location != null) {   // TODO
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
                }
                updateMapPins();
            }
        });

        mDatabase = FirebaseDatabase.getInstance();
        mParkingsRef = mDatabase.getReference("parkings");
        mParkingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this GetLocation is updated.

                Iterator<DataSnapshot> locations = dataSnapshot.getChildren().iterator();
                while (locations.hasNext()) {
                    DataSnapshot location = locations.next();
                    String id = (String) location.getKey();
                    if (location.getValue() == null) {
                        locationsMap.remove(id);
                    } else {
                        String owner = (String) location.child("owner").getValue();
                        double lat = Double.parseDouble((String) location.child("lat").getValue());
                        double lon = Double.parseDouble((String) location.child("lon").getValue());
                        boolean availability = (boolean) location.child("available").getValue();
                        double price = (double) location.child("price").getValue();
                        String description = (String) location.child("description").getValue();
                        ParkingPlace updatedPlace = new ParkingPlace(id, availability, lat, lon, owner, price, description);
                        locationsMap.put(id, updatedPlace);
                    }
                }
                updateMapPins();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w("DB", "Failed to read value.", databaseError.toException());
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void updateMapPins() {
        // Discard all pins
        while (!currentMarkers.empty()) {
            Marker marker = currentMarkers.pop();
            marker.remove();
        }

        // Remake all
        if (googleMap != null) {
            Iterator nLocations = locationsMap.entrySet().iterator();
            while (nLocations.hasNext()) {
                Map.Entry<String, ParkingPlace> entrySet = (Map.Entry<String, ParkingPlace>) nLocations.next();
                Marker m = googleMap.addMarker(new MarkerOptions().position(entrySet.getValue().GetLocation()).title(entrySet.getValue().lat + " " + entrySet.getValue().lon)
                        .icon(BitmapDescriptorFactory.defaultMarker(entrySet.getValue().available ? BitmapDescriptorFactory.HUE_BLUE : BitmapDescriptorFactory.HUE_RED)));
                m.setTag(entrySet.getKey());
                currentMarkers.push(m);
            }
        }
    }

    private long doubleClickTime;

    @Override
    public boolean onMarkerClick(Marker marker) {

        return true;
    }
}
