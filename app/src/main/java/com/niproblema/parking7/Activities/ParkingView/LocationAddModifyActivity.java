package com.niproblema.parking7.Activities.ParkingView;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.niproblema.parking7.DataObjects.Parking;
import com.niproblema.parking7.R;

public class LocationAddModifyActivity extends AppCompatActivity {
	EditText mStreetNameET, mCityNameET, mCountryNameET, mPriceET, mInsuranceET, mDescriptionET;
	TextView mTimeStartTV, mTimeEndTV;
	FloatingActionButton mAcceptModifyButton, mDeleteButton;

	FirebaseStorage mFBStorage;
	FirebaseDatabase mFBDatabase;

	boolean mIsEditing;
	Parking mParking;
	LatLng mNewLocation;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.park_modify_screen);

		// Link fields
		mFBStorage = FirebaseStorage.getInstance();
		mFBDatabase = FirebaseDatabase.getInstance();
		mStreetNameET = findViewById(R.id.etStreetName);
		mCityNameET = findViewById(R.id.etCityName);
		mCountryNameET = findViewById(R.id.etCountryName);
		mTimeStartTV = findViewById(R.id.tvTimeSlotStart);
		mTimeEndTV = findViewById(R.id.tvTimeSlotEnd);
		mPriceET = findViewById(R.id.etPrice);
		mInsuranceET = findViewById(R.id.etInsurance);
		mDescriptionET = findViewById(R.id.etDescription);
		mAcceptModifyButton = findViewById(R.id.fabAccept);
		mDeleteButton = findViewById(R.id.fabRemove);

		Intent i = getIntent();
		if (i.hasExtra("parkingPlace")) {    // Editing existing place
			mParking = (Parking) i.getSerializableExtra("parkingPlace");
			mIsEditing = true;
			// Set existing fields
			// TODO fetch image
			mStreetNameET.setText(mParking.mLocation.mStreet);
			mCityNameET.setText(mParking.mLocation.mCity);
			mCountryNameET.setText(mParking.mLocation.mCountry);
			mTimeStartTV.setText(doubleToString(mParking.mTimeSlots.get(0).mTimeStart));
			mTimeEndTV.setText(doubleToString(mParking.mTimeSlots.get(0).mTimeEnd));
			mPriceET.setText(mParking.mTimeSlots.get(0).mPrice + "");
			mPriceET.setText(mParking.mTimeSlots.get(0).mInsuranceBail + "");
			mDescriptionET.setText(mParking.mDescription);

			mAcceptModifyButton.setImageResource(R.drawable.ic_edit_50);
			mDeleteButton.show();
		} else if (i.hasExtra("newLocation")) {
			mIsEditing = false;
			mNewLocation = i.getParcelableExtra("newLocation");
			mAcceptModifyButton.setImageResource(R.drawable.ic_check_50dp);
			mDeleteButton.hide();
		} else {
			Log.e("PREVIEW", "Error launching activity, no intent set");
			finish();
		}
	}

	private static String doubleToString(double time) {
		int hourStart = (int) Math.floor(time);
		int minuteStart = (int) Math.round((time - hourStart) * 60);
		return hourStart + ":" + minuteStart;
	}
}
