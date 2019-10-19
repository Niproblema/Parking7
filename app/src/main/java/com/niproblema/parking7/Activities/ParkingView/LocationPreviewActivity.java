package com.niproblema.parking7.Activities.ParkingView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.niproblema.parking7.DataObjects.Location;
import com.niproblema.parking7.DataObjects.Parking;
import com.niproblema.parking7.DataObjects.Recurrence;
import com.niproblema.parking7.DataObjects.TimeSlot;
import com.niproblema.parking7.R;

import java.util.ArrayList;
import java.util.Map;


public class LocationPreviewActivity extends AppCompatActivity {
	ImageView mParkPreviewImageView;
	TextView mLocationTextView, mTimeSlotTextView, mDescriptionTextView, mPriceTextView, mInsuranceTextView, mOwnerTextView;
	Button mOccupyBtn, mMessageBtn, mDirectionsBtn, mReportBtn, mFavouriteBtn;
	FirebaseFunctions mFunctions;

	// Existing location
	private Parking mParking;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.park_preview_screen);
		mFunctions = FirebaseFunctions.getInstance();

		Intent i = getIntent();
		if (i.hasExtra("parkingPlace")) {    // Viewing existing place
			mParking = (Parking) i.getSerializableExtra("parkingPlace");

		} else {
			Log.e("PREVIEW", "Error launching activity, no intent set");
			finish();
		}

		mParkPreviewImageView = findViewById(R.id.ivParkPreview);
		mLocationTextView = findViewById(R.id.tvLocationDescription);
		mTimeSlotTextView = findViewById(R.id.tvTimeSlot);
		mPriceTextView = findViewById(R.id.tvPrice);
		mInsuranceTextView = findViewById(R.id.tvInsurance);
		mOwnerTextView = findViewById(R.id.tvOwner);
		mDescriptionTextView = findViewById(R.id.etDescription);
		mOccupyBtn = findViewById(R.id.fabOccupy);
		mMessageBtn = findViewById(R.id.fabMessage);
		mDirectionsBtn = findViewById(R.id.fabDirections);
		mReportBtn = findViewById(R.id.fabReport);
		mFavouriteBtn = findViewById(R.id.fabFavourite);

		mLocationTextView.setText(mParking.mLocation.toString());
		mTimeSlotTextView.setText(mParking.mTimeSlots.get(0).toString());
		mPriceTextView.setText(mParking.mTimeSlots.get(0).mPrice + "€/" + getString(R.string.general_hour));
		mInsuranceTextView.setText(mParking.mTimeSlots.get(0).mInsuranceBail + "€");
		mDescriptionTextView.setText(mParking.mDescription);
		getAsyncData();
	}

	public void getAsyncData(){
		//TODO get owner's username
		//TODO get parking's photo.
		//FirebaseDatabase.getInstance().getReference("/users")
	}


	@Override
	protected void onStart() {
		super.onStart();


//		mButton2.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				addParking();
//			}
//		});


//		if (i.hasExtra("new")) {
//			final LatLng position = i.getParcelableExtra("pos");
//			mTitle.setText(position.latitude + " " + position.longitude);
//			mButton1.setText(R.string.location_add);
//			mButton1.setBackgroundColor(getColor(R.color.loc_green));
//			mButton1.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					final String id = UUID.randomUUID().toString();
//
//					FirebaseDatabase.getInstance().getReference("parkings/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
//						@Override
//						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//							final String usermail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//							double costd = 0.1;
//							try {
//								costd = Double.parseDouble(mPrice.getText().toString());
//							} catch (Exception e) {
//							}
//							final double cost = costd;
//							final String description = mDescription.getText().toString();
//							final String lat = String.valueOf(position.latitude);
//							final String lon = String.valueOf(position.longitude);
//
//							dataSnapshot.getRef().setValue(new HashMap<String, Object>() {
//								{
//									put("available", true);
//									put("description", description);
//									put("lat", lat);
//									put("lon", lon);
//									put("owner", usermail);
//									put("price", cost);
//								}
//							});
//							finish();
//						}
//
//						@Override
//						public void onCancelled(@NonNull DatabaseError databaseError) {
//							Log.e("DB", "onCancelled", databaseError.toException());
//						}
//					});
//				}
//			});
//		} else {
//			mTitle.setFocusable(false);
//			mDescription.setFocusable(false);
//			mPrice.setFocusable(false);

//            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//            final ParkingPlace parkingPlace = (ParkingPlace) i.getSerializableExtra("parkingPlace");
//
//            mTitle.setText(parkingPlace.lat + " " + parkingPlace.lon);
//            mPrice.setText(parkingPlace.price + "€/h");
//            mDescription.setText(parkingPlace.description);
//            if(parkingPlace.available) {
//                mButton1.setText(R.string.location_rent_start);
//                mButton1.setBackgroundColor(getColor(R.color.loc_green));
//            }else{
//                mButton1.setText(R.string.location_rent_end);
//                mButton1.setBackgroundColor(getColor(R.color.loc_red));
//            }
//            mButton1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    FirebaseDatabase.getInstance().getReference("parkings/" + parkingPlace.id+"/available").addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            dataSnapshot.getRef().setValue(!parkingPlace.available);
//                            finish();
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
//                }
//            });
	}


	private void addParking() {
		// TODO: validate input.

		// Test data;
		Location p = new Location("Test City", "Test Country", "Test Street", 46.1262181, 14.4411588);
		ArrayList<TimeSlot> slots = new ArrayList<TimeSlot>() {
			{
				add(new TimeSlot(80, 0.8, Recurrence.DAILY, 1571415332, 19.5, 3.3));
			}
		};
		Parking park = new Parking(slots, p, "Test park", "Pls work...");
		///

		addParkResponse(park).addOnCompleteListener(new OnCompleteListener<String>() {
			@Override
			public void onComplete(@NonNull Task<String> task) {
				if (!task.isSuccessful()) {
					Exception e = task.getException();
					if (e instanceof FirebaseFunctionsException) {
						FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
						FirebaseFunctionsException.Code code = ffe.getCode();
						Object details = ffe.getDetails();
					}

					Log.e("ADD", "addMessage:onFailure", e);
					return;
				}

				String result = task.getResult();
				Log.d("ADD", result);
			}
		});
	}

	private Task<String> addParkResponse(Parking park) {
		return mFunctions
				.getHttpsCallable("addParking")
				.call(park.getSubmittableObject())
				.continueWith(new Continuation<HttpsCallableResult, String>() {
					@Override
					public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
						if (((Map<String, Boolean>) task.getResult().getData()).get("status")) {
							return "Success!";
						}

						return "Failiure";
					}
				});
	}
}
