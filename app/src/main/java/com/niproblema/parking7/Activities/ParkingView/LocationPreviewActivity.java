package com.niproblema.parking7.Activities.ParkingView;

import android.content.Intent;
import android.graphics.BitmapFactory;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.niproblema.parking7.DataObjects.Location;
import com.niproblema.parking7.DataObjects.Parking;
import com.niproblema.parking7.DataObjects.Recurrence;
import com.niproblema.parking7.DataObjects.TimeSlot;
import com.niproblema.parking7.R;
import com.niproblema.parking7.Utils.PictureManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;


public class LocationPreviewActivity extends AppCompatActivity {
	ImageView mParkPreviewImageView;

	TextView mLocationTextView, mTimeSlotTextView, mDescriptionTextView, mPriceTextView, mInsuranceTextView, mOwnerTextView;
	FloatingActionButton mOccupyBtn, mMessageBtn, mDirectionsBtn, mReportBtn, mFavouriteBtn;
	FirebaseFunctions mFunctions;

	// Existing location
	private Parking mParking;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.park_preview_screen);
		mFunctions = FirebaseFunctions.getInstance("europe-west1");

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

	public void getAsyncData() {
		if (mParking.mImageURIs.size() > 0) {
			PictureManager.getImageByURL(mParking.mImageURIs.get(0), new PictureManager.OnImageFetchedListener() {
				@Override
				public void onSuccess(File image) {
					String imagePath = image.getPath();
					if (mParkPreviewImageView != null && imagePath != null)
						mParkPreviewImageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
				}

				@Override
				public void onFailure(@NonNull Exception e) {
					Log.e("IMAGE", "Could not load image from firebase: " + e.toString());
				}
			});
		}

		//TODO get parking's photo.
		FirebaseDatabase.getInstance().getReference("/users/"+mParking.mOwnerUID+"/loginEmail")
				.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						String loginEmail = dataSnapshot.getValue(String.class);
						mOwnerTextView.setText(loginEmail);
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
						Log.e("DB", "onCancelled", databaseError.toException());
					}
				});
	}


	@Override
	protected void onStart() {
		super.onStart();

	}


	private static String doubleToString(double time) {
		int hourStart = (int) Math.floor(time);
		int minuteStart = (int) Math.round((time - hourStart) * 60);
		return String.format("%02d:%02d", hourStart, minuteStart);
	}
}
