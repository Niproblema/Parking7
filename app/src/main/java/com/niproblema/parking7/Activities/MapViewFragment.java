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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.niproblema.parking7.Activities.ParkingView.LocationAddModifyActivity;
import com.niproblema.parking7.Activities.ParkingView.LocationPreviewActivity;
import com.niproblema.parking7.DataObjects.Parking;
import com.niproblema.parking7.R;
import com.niproblema.parking7.Utils.ToastManager;

import java.util.HashMap;
import java.util.Iterator;

public class MapViewFragment extends Fragment implements GoogleMap.OnMarkerClickListener {
	private MapView mMapView;
	private GoogleMap googleMap;
	private LocationManager locationManager;
	private FirebaseDatabase mDatabase;
	private FirebaseAuth mAuth;
	private DatabaseReference mParkingsRef;
	private ChildEventListener mParkingsEventListener;

	private HashMap<String, Pair<Parking, Marker>> mLocationsMap = new HashMap<String, Pair<Parking, Marker>>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.maps_fragment, container, false);

		mDatabase = FirebaseDatabase.getInstance();
		mParkingsRef = mDatabase.getReference("parkings");
		mAuth = FirebaseAuth.getInstance();

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
						Intent lauchIntent = new Intent(getActivity(), LocationAddModifyActivity.class);
						lauchIntent.putExtra("newLocation", latLng);
						startActivity(lauchIntent);
					}
				});
				googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
					@Override
					public void onInfoWindowClick(Marker marker) {
						int editAccessible = Math.round(marker.getZIndex());
						if (editAccessible == 0) {
							ToastManager.showToast(getString(R.string.general_wait_forRelease));
							return;
						} else if (CoreActivity.mIsRenting && editAccessible == 1) {
							ToastManager.showToast(getString(R.string.general_release_first));
							return;
						}

						Parking parking = mLocationsMap.get((String) marker.getTag()).first;
						Intent launchIntent = null;
						if (parking.mOwnerUID.equals(mAuth.getCurrentUser().getUid())) {    // Editing own parking
							launchIntent = new Intent(getActivity(), LocationAddModifyActivity.class);
							launchIntent.putExtra("parkingPlace", parking);
						} else {                                                                // Viewing someone else's parking
							launchIntent = new Intent(getActivity(), LocationPreviewActivity.class);
							launchIntent.putExtra("parkingPlace", parking);
						}

						startActivity(launchIntent);
					}
				});
				// For showing a move to my GetLocation button
				googleMap.setMyLocationEnabled(true);
				//Location location = googleMap.getMyLocation();
				//if (location != null) {   // TODO
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.0815718, 14.4719724), 11));
				//}
			}
		});

		mParkingsEventListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				String id = (String) dataSnapshot.getKey();

				if (mLocationsMap != null && mLocationsMap.containsKey(id)) {
					onChildChanged(dataSnapshot, s);
					return;
				}

				if (dataSnapshot.exists() && mLocationsMap != null) {
					Parking parking = Parking.parse(dataSnapshot);
					if (parking != null) {
						setupMapPlace(id, parking);
					}
				}
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				String id = (String) dataSnapshot.getKey();

				if (dataSnapshot.exists() && mLocationsMap != null) {
					Pair<Parking, Marker> previous = mLocationsMap.get(id);
					Parking parking = Parking.parse(dataSnapshot);

					if (parking != null) {
						setupMapPlace(id, parking);
					}
				}
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				String id = (String) dataSnapshot.getKey();
				if (mLocationsMap != null && mLocationsMap.containsKey(id)) {
					Marker marker = mLocationsMap.remove(id).second;
					if (marker != null)
						marker.remove();
				}
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				// wtf is this
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.w("DB", "Failed to read value.", databaseError.toException());
			}
		};

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
	public void onStart() {
		super.onStart();
		mParkingsRef.addChildEventListener(mParkingsEventListener);
	}

	@Override
	public void onStop() {
		super.onStop();
		mParkingsRef.removeEventListener(mParkingsEventListener);
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

	protected void setupMapPlace(String id, Parking parking) {
		boolean isOwnParking = parking.mOwnerUID.equals(mAuth.getUid());
		boolean isTakenParking = !parking.mAvailable;
		boolean isRentingIt = parking.mRenterUID == null ? false : parking.mRenterUID.equals(mAuth.getUid());

		if (!mLocationsMap.containsKey(id)) {
			// New parking
			if (parking.mActive) {
				Marker m = googleMap.addMarker(new MarkerOptions().position(new LatLng(parking.mLocation.mLat, parking.mLocation.mLon))
						.title(detetmineMarkerTitle(isOwnParking, isTakenParking, isRentingIt, parking))
						.icon(determineMarkerColor(isOwnParking, isTakenParking, isRentingIt)));
				m.setTag(id);
				m.setZIndex((isTakenParking && !isRentingIt) ? 0 : isRentingIt ? 2 : 1);
				mLocationsMap.put(id, new Pair<Parking, Marker>(parking, m));
			} else {
				mLocationsMap.put(id, new Pair<Parking, Marker>(parking, null));
			}
		} else {
			Pair<Parking, Marker> previous = mLocationsMap.get(id);
			if (parking.mActive) {
				Marker m = previous.second;
				if (m != null) {
					m.setPosition(new LatLng(parking.mLocation.mLat, parking.mLocation.mLon));
					m.setTitle(detetmineMarkerTitle(isOwnParking, isTakenParking, isRentingIt, parking));
					m.setIcon(determineMarkerColor(isOwnParking, isTakenParking, isRentingIt));
					m.setTag(id);
					m.setZIndex((isTakenParking && !isRentingIt) ? 0 : isRentingIt ? 2 : 1);
					Pair<Parking, Marker> newPair = new Pair<Parking, Marker>(parking, m);
					mLocationsMap.replace(id, newPair);
				} else {
					Marker mm = googleMap.addMarker(new MarkerOptions().position(new LatLng(parking.mLocation.mLat, parking.mLocation.mLon))
							.title(detetmineMarkerTitle(isOwnParking, isTakenParking, isRentingIt, parking))    //TODO
							.icon(determineMarkerColor(isOwnParking, isTakenParking, isRentingIt)));
					mm.setTag(id);
					mm.setZIndex((isTakenParking && !isRentingIt) ? 0 : isRentingIt ? 2 : 1);
					mLocationsMap.replace(id, new Pair<Parking, Marker>(parking, mm));
				}
			} else {
				if (previous.second != null) {
					previous.second.remove();
				}
				Pair<Parking, Marker> newPair = new Pair<Parking, Marker>(parking, null);
				mLocationsMap.replace(id, newPair);
			}
		}
	}

	public BitmapDescriptor determineMarkerColor(boolean isOwnParking, boolean isTakenParking, boolean isRentingIt) {
		return BitmapDescriptorFactory.defaultMarker(isOwnParking ? (isTakenParking ? 190f : 240f)
				: (isRentingIt ? BitmapDescriptorFactory.HUE_YELLOW
				: (!isTakenParking ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED)));
	}

	public String detetmineMarkerTitle(boolean isOwnParking, boolean isTakenParking, boolean isRentingIt, Parking parking) {
		return (isOwnParking ? getString(R.string.map_marker_own) + ", " : "")
				+ (isRentingIt ? getString(R.string.map_marker_renting) : (isTakenParking ? getString(R.string.map_marker_taken) : getString(R.string.map_marker_free)))
				+ ", " + parking.mTimeSlots.get(0).toString()
				+ ", " + parking.mTimeSlots.get(0).mPrice + "€/h";
	}
}
