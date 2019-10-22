package com.niproblema.parking7.Activities.ParkingView;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.niproblema.parking7.Activities.CoreActivity;
import com.niproblema.parking7.Activities.PreLogin.LoginScreen;
import com.niproblema.parking7.Activities.PreLogin.SplashScreen;
import com.niproblema.parking7.Activities.RecapActivity;
import com.niproblema.parking7.DataObjects.Location;
import com.niproblema.parking7.DataObjects.Parking;
import com.niproblema.parking7.DataObjects.Recurrence;
import com.niproblema.parking7.DataObjects.TimeSlot;
import com.niproblema.parking7.R;
import com.niproblema.parking7.Utils.PictureManager;
import com.niproblema.parking7.Utils.ToastManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class LocationPreviewActivity extends AppCompatActivity {
	ImageView mParkPreviewImageView;

	TextView mLocationTextView, mTimeSlotTextView, mDescriptionTextView, mPriceTextView, mInsuranceTextView, mOwnerTextView;
	FloatingActionButton mOccupyBtn, mMessageBtn, mDirectionsBtn, mReportBtn, mFavouriteBtn;
	FirebaseFunctions mFunctions;

	// Existing location
	private Parking mParking;

	private boolean mBlocked = false;

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

		mOccupyBtn.setImageResource(CoreActivity.mIsRenting ? R.drawable.ic_stop_50dp : R.drawable.ic_start_50dp);

		mLocationTextView.setText(mParking.mLocation.toString());
		mTimeSlotTextView.setText(mParking.mTimeSlots.get(0).toString());
		mPriceTextView.setText(mParking.mTimeSlots.get(0).mPrice + "€/" + getString(R.string.general_hour));
		mInsuranceTextView.setText(mParking.mTimeSlots.get(0).mInsuranceBail + "€");
		mDescriptionTextView.setText(mParking.mDescription);

		View.OnClickListener notImplementedListner = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastManager.showToast(getString(R.string.general_notImplemented));
			}
		};
		mMessageBtn.setOnClickListener(notImplementedListner);
		mDirectionsBtn.setOnClickListener(notImplementedListner);
		mReportBtn.setOnClickListener(notImplementedListner);
		mFavouriteBtn.setOnClickListener(notImplementedListner);
		mOccupyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBlocked) {
					ToastManager.showToast(getString(R.string.general_wait));
					return;
				}

				if (CoreActivity.mIsRenting) {
					stopRent();
				} else {
					startRent();
				}
			}
		});

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

		FirebaseDatabase.getInstance().getReference("/users/" + mParking.mOwnerUID + "/loginEmail")
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


	private void startRent() {
		ToastManager.showToast(getString(R.string.general_loading));
		mBlocked = true;
		startRentResponse(mParking).addOnCompleteListener(new OnCompleteListener<String>() {
			@Override
			public void onComplete(@NonNull Task<String> task) {
				mBlocked = false;
				if (!task.isSuccessful()) {
					Exception e = task.getException();
					if (e instanceof FirebaseFunctionsException) {
						FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
						FirebaseFunctionsException.Code code = ffe.getCode();
						Object details = ffe.getDetails();

					}
					Log.e("STARTRENT", "addMessage:onFailure", e);
					ToastManager.showToast(getString(R.string.general_error));
					return;
				}

				String result = task.getResult();
				Log.d("STARTRENT", result);
				ToastManager.showToast(getString(R.string.general_success));
				finish();
			}
		});
	}

	private void stopRent() {
		ToastManager.showToast(getString(R.string.general_loading));
		mBlocked = true;
		stopRentResponse(mParking).addOnCompleteListener(new OnCompleteListener<String>() {
			@Override
			public void onComplete(@NonNull Task<String> task) {
				mBlocked = false;
				if (!task.isSuccessful()) {
					Exception e = task.getException();
					if (e instanceof FirebaseFunctionsException) {
						FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
						FirebaseFunctionsException.Code code = ffe.getCode();
						Object details = ffe.getDetails();

					}
					Log.e("STOPRENT", "addMessage:onFailure", e);
					ToastManager.showToast(getString(R.string.general_error));
					return;
				}

				String result = task.getResult();
				Log.d("STOPRENT", result);
				//ToastManager.showToast(getString(R.string.general_success));
				Intent recapScreen = new Intent(getApplicationContext(), RecapActivity.class);
				if (mParking.mImageURIs.size() > 0)
					recapScreen.putExtra("imageURL", mParking.mImageURIs.get(0));
				startActivity(recapScreen);
				finish();
			}
		});
	}


	private static String doubleToString(double time) {
		int hourStart = (int) Math.floor(time);
		int minuteStart = (int) Math.round((time - hourStart) * 60);
		return String.format("%02d:%02d", hourStart, minuteStart);
	}

	private Task<String> startRentResponse(Parking park) {
		return mFunctions
				.getHttpsCallable("startRent")
				.call(new HashMap<String, Object>() {{
					put("parkingUID", park.mUID);
				}})
				.continueWith(new Continuation<HttpsCallableResult, String>() {
					@Override
					public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
						if (((Map<String, Boolean>) task.getResult().getData()).get("status")) {
							return "Success!";
						}
						throw new Exception();
					}
				});
	}

	private Task<String> stopRentResponse(Parking park) {
		return mFunctions
				.getHttpsCallable("stopRent")
				.call(new HashMap<String, Object>() {{
					put("parkingUID", park.mUID);
				}})
				.continueWith(new Continuation<HttpsCallableResult, String>() {
					@Override
					public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
						if (((Map<String, Boolean>) task.getResult().getData()).get("status")) {
							return "Success!";
						}
						throw new Exception();
					}
				});
	}

	@Override
	public void onBackPressed() {
		if (mBlocked) {
			ToastManager.showToast(getString(R.string.general_wait));
			return;
		}
		super.onBackPressed();
	}
}
