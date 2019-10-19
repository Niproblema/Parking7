package com.niproblema.parking7.Activities;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
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
import com.niproblema.parking7.DataObjects.Parking;
import com.niproblema.parking7.R;

import java.util.HashMap;
import java.util.Iterator;

public class MapViewFragment extends Fragment implements GoogleMap.OnMarkerClickListener {
	private MapView mMapView;
	private GoogleMap googleMap;
	private LocationManager locationManager;
	private FirebaseDatabase mDatabase;
	private DatabaseReference mParkingsRef;

	private HashMap<String, Pair<Parking, Marker>> mLocationsMap = new HashMap<String, Pair<Parking, Marker>>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.maps_fragment, container, false);

		mDatabase = FirebaseDatabase.getInstance();
		mParkingsRef = mDatabase.getReference("parkings");

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
						lauchIntent.putExtra("location", latLng);
						startActivity(lauchIntent);
					}
				});
				googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
					@Override
					public void onInfoWindowClick(Marker marker) {
						Intent lauchIntent = new Intent(getActivity(), LocationPreviewActivity.class);
						lauchIntent.putExtra("parkingPlace", mLocationsMap.get((String) marker.getTag()).first);
						startActivity(lauchIntent);
					}
				});
				// For showing a move to my GetLocation button
				googleMap.setMyLocationEnabled(true);
				Location location = googleMap.getMyLocation();
				if (location != null) {   // TODO
					googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
				}

				addDBListener();
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


	@Override
	public boolean onMarkerClick(Marker marker) {

		return true;
	}


	private void addDBListener() {
		// Add DB listener.
		mParkingsRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				Iterator<DataSnapshot> dbLocationsList = dataSnapshot.getChildren().iterator();
				while (dbLocationsList.hasNext()) {
					DataSnapshot dbLocation = dbLocationsList.next();
					String id = (String) dbLocation.getKey();

					// Remove old information
					if (mLocationsMap.containsKey(id)) {
						Marker marker = mLocationsMap.remove(id).second;
						marker.remove();
					}

					// Add new, if it's an update or new data.
					if (dbLocation.exists()) {
						Parking parking = Parking.parse(dbLocation);
						if (parking != null) {
							Marker m = googleMap.addMarker(new MarkerOptions().position(new LatLng(parking.mLocation.mLat, parking.mLocation.mLon))
									.title("TODO")    //TODO
									.icon(BitmapDescriptorFactory.defaultMarker(parking.mAvailable ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED)));
							m.setTag(id);
							mLocationsMap.put(id, new Pair<Parking, Marker>(parking, m));
						}
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				// Failed to read value
				Log.w("DB", "Failed to read value.", databaseError.toException());
			}
		});
	}
}
