package com.niproblema.parking7.Activities.ParkingView;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.niproblema.parking7.DataObjects.Location;
import com.niproblema.parking7.DataObjects.Parking;
import com.niproblema.parking7.DataObjects.Recurrence;
import com.niproblema.parking7.DataObjects.TimeSlot;
import com.niproblema.parking7.R;
import com.niproblema.parking7.Utils.PathResolver;
import com.niproblema.parking7.Utils.PictureManager;
import com.niproblema.parking7.Utils.ToastManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LocationAddModifyActivity extends AppCompatActivity {
	// Image
	ImageView mImage;
	String mImagePath;
	String mImageGUID; // new image is loaded.
	String mImageURI_keepSame;    // old has been used at some point

	// View fields
	EditText mStreetNameET, mCityNameET, mCountryNameET, mPriceET, mInsuranceET, mDescriptionET;
	TextView mTimeStartTV, mTimeEndTV;
	FloatingActionButton mAcceptModifyButton, mDeleteButton;

	// Firebase
	FirebaseStorage mFBStorage;
	FirebaseDatabase mFBDatabase;
	FirebaseFunctions mFBFunctions;

	// State
	boolean mIsEditing;
	Parking mParking;
	LatLng mNewLocation;
	boolean mIsBlocked = false;

	// TimeSlots
	double mSlotStart = 0D;
	double mSlotEnd = 24D;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.park_modify_screen);

		// Link fields
		mFBStorage = FirebaseStorage.getInstance();
		mFBDatabase = FirebaseDatabase.getInstance();
		mFBFunctions = FirebaseFunctions.getInstance("europe-west1");
		mImage = findViewById(R.id.ivSelectableImage);
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
		if (i.hasExtra("parkingPlace")) {    // Editing existing place	// TODO: move to own fucntion that watches db field.
			mParking = (Parking) i.getSerializableExtra("parkingPlace");
			mIsEditing = true;
			// Set existing fields
			// TODO fetch image
			mStreetNameET.setText(mParking.mLocation.mStreet);
			mCityNameET.setText(mParking.mLocation.mCity);
			mCountryNameET.setText(mParking.mLocation.mCountry);
			mSlotStart = mParking.mTimeSlots.get(0).mTimeStart;
			mSlotEnd = mParking.mTimeSlots.get(0).mTimeEnd;
			mTimeStartTV.setText(doubleToString(mSlotStart));
			mTimeEndTV.setText(doubleToString(mSlotEnd));
			mPriceET.setText(mParking.mTimeSlots.get(0).mPrice + "");
			mInsuranceET.setText(mParking.mTimeSlots.get(0).mInsuranceBail + "");
			mDescriptionET.setText(mParking.mDescription);

			mAcceptModifyButton.setImageResource(R.drawable.ic_edit_50);
			mDeleteButton.show();
			mAcceptModifyButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onEditButtonClick_P1();
				}
			});

			if (mParking.mImageURIs.size() > 0) {
				PictureManager.getImageByURL(mParking.mImageURIs.get(0), new PictureManager.OnImageFetchedListener() {
					@Override
					public void onSuccess(File image) {
						if (mImagePath != null)    // User has already selected new image.
							return;
						mImagePath = image.getPath();
						mImageURI_keepSame = mParking.mImageURIs.get(0);

						if (mImage != null && mImagePath != null)
							mImage.setImageBitmap(BitmapFactory.decodeFile(mImagePath));
					}

					@Override
					public void onFailure(@NonNull Exception e) {
						Log.e("IMAGE", "Could not load image from firebase: " + e.toString());
					}
				});
			}
		} else if (i.hasExtra("newLocation")) {
			mIsEditing = false;
			mNewLocation = i.getParcelableExtra("newLocation");
			mTimeStartTV.setText(doubleToString(mSlotStart));
			mTimeEndTV.setText(doubleToString(mSlotEnd));
			//TODO: async location suggestion.
			mAcceptModifyButton.setImageResource(R.drawable.ic_check_50dp);
			mDeleteButton.hide();
			mAcceptModifyButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onAcceptButtonClick_P1();
				}
			});
		} else {
			Log.e("PREVIEW", "Error launching activity, no intent set");
			finish();
		}
		mDeleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onDeleteButtonClick();
			}
		});
		mImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectImage();
			}
		});
		View.OnClickListener timeSpanChange = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onTimeSlotClicked(v);
			}
		};
		mTimeStartTV.setOnClickListener(timeSpanChange);
		mTimeEndTV.setOnClickListener(timeSpanChange);
	}

	/*============BUTTONS============*/
	double parsedPrice;
	double parsedInsurance;

	private void onAcceptButtonClick_P1() {
		try {
			parsedPrice = Double.parseDouble(mPriceET.getText().toString());
			if (parsedPrice < 0) throw new Exception("Negative price");
		} catch (Exception e) {
			Log.d("DOUBLE PARSE", e.toString());
			ToastManager.showToast(getString(R.string.location_invalid_price));
			return;
		}

		try {
			parsedInsurance = Double.parseDouble(mInsuranceET.getText().toString());
			if (parsedInsurance < 0) throw new Exception("Negative insurance");
		} catch (Exception e) {
			Log.d("DOUBLE PARSE", e.toString());
			ToastManager.showToast(getString(R.string.location_invalid_price));
			return;
		}

		mIsBlocked = true;
		mAcceptModifyButton.setEnabled(false);
		ToastManager.showToast(getString(R.string.general_loading));
		if (mImagePath == null) {
			onAcceptButtonClick_P2(null);
		} else {
			uploadImage();
		}
	}

	private void onAcceptButtonClick_P2(Uri imagePath) {
		String accessInstructions = "";
		String description = mDescriptionET.getText().toString();
		// Location
		String streetName = mStreetNameET.getText().toString();
		String cityName = mCityNameET.getText().toString();
		String countryName = mCountryNameET.getText().toString();
		double lat = mIsEditing ? mParking.mLocation.mLat : mNewLocation.latitude;
		double lon = mIsEditing ? mParking.mLocation.mLon : mNewLocation.longitude;
		Location location = new Location(cityName, countryName, streetName, lat, lon);
		// Time slot
		double price = Double.parseDouble(mPriceET.getText().toString());

		TimeSlot timeSlot = new TimeSlot(parsedInsurance
				, parsedPrice
				, Recurrence.DAILY
				, 0
				, mSlotStart
				, mSlotEnd);
		List<TimeSlot> timeSlots = new ArrayList<TimeSlot>() {{
			add(timeSlot);
		}};
		List<String> imageURIs = new ArrayList<String>();
		if (imagePath != null) imageURIs.add(imagePath.toString());

		Parking parking = new Parking(timeSlots, location, description, accessInstructions, imageURIs);

		addParkResponse(parking).addOnCompleteListener(new OnCompleteListener<String>() {
			@Override
			public void onComplete(@NonNull Task<String> task) {
				if (!task.isSuccessful()) {
					Exception e = task.getException();
					if (e instanceof FirebaseFunctionsException) {
						FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
						FirebaseFunctionsException.Code code = ffe.getCode();
						Object details = ffe.getDetails();

					}
					mIsBlocked = false;
					mAcceptModifyButton.setEnabled(true);
					Log.e("ADD", "addMessage:onFailure", e);
					ToastManager.showToast(getString(R.string.general_error));
					return;
				}

				String result = task.getResult();
				Log.d("ADD", result);
				ToastManager.showToast(getString(R.string.general_success));
				finish();
			}
		});


		mIsBlocked = false;
	}

	private void onEditButtonClick_P1() {
		try {
			parsedPrice = Double.parseDouble(mPriceET.getText().toString());
			if (parsedPrice < 0) throw new Exception("Negative price");
		} catch (Exception e) {
			Log.d("DOUBLE PARSE", e.toString());
			ToastManager.showToast(getString(R.string.location_invalid_price));
			return;
		}

		try {
			parsedInsurance = Double.parseDouble(mInsuranceET.getText().toString());
			if (parsedInsurance < 0) throw new Exception("Negative insurance");
		} catch (Exception e) {
			Log.d("DOUBLE PARSE", e.toString());
			ToastManager.showToast(getString(R.string.location_invalid_price));
			return;
		}

		mIsBlocked = true;
		ToastManager.showToast(getString(R.string.general_loading));
		// If image hasn't chnaged or if no image is set at all.
		if (mImageGUID == null || mImagePath == null) {
			onEditButtonClick_P2(null);
		} else {
			editUploadImage();
		}
	}

	private void onEditButtonClick_P2(Uri imagePath) {
		String accessInstructions = "";
		String description = mDescriptionET.getText().toString();
		// Location
		String streetName = mStreetNameET.getText().toString();
		String cityName = mCityNameET.getText().toString();
		String countryName = mCountryNameET.getText().toString();
		double lat = mIsEditing ? mParking.mLocation.mLat : mNewLocation.latitude;
		double lon = mIsEditing ? mParking.mLocation.mLon : mNewLocation.longitude;
		Location location = new Location(cityName, countryName, streetName, lat, lon);
		// Time slot
		double price = Double.parseDouble(mPriceET.getText().toString());

		TimeSlot timeSlot = new TimeSlot(parsedInsurance
				, parsedPrice
				, Recurrence.DAILY
				, 0
				, mSlotStart
				, mSlotEnd);
		List<TimeSlot> timeSlots = new ArrayList<TimeSlot>() {{
			add(timeSlot);
		}};
		List<String> imageURIs = new ArrayList<String>();
		if (imagePath != null)
			imageURIs.add(imagePath.toString());
		else if (mImageURI_keepSame != null)
			imageURIs.add(mImageURI_keepSame);

		Parking parking = new Parking(timeSlots, location, description, accessInstructions, imageURIs);

		editParkResponse(mParking.mUID, parking).addOnCompleteListener(new OnCompleteListener<String>() {
			@Override
			public void onComplete(@NonNull Task<String> task) {
				if (!task.isSuccessful()) {
					Exception e = task.getException();
					if (e instanceof FirebaseFunctionsException) {
						FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
						FirebaseFunctionsException.Code code = ffe.getCode();
						Object details = ffe.getDetails();

					}
					mIsBlocked = false;
					mAcceptModifyButton.setEnabled(true);
					Log.e("EDIT", "addMessage:onFailure", e);
					ToastManager.showToast(getString(R.string.general_error));
					return;
				}

				String result = task.getResult();
				Log.d("EDIT", result);
				ToastManager.showToast(getString(R.string.general_success));
				finish();
			}
		});

		mIsBlocked = false;
	}

	private void onDeleteButtonClick() {
		removeParkResponse(mParking).addOnCompleteListener(new OnCompleteListener<String>() {
			@Override
			public void onComplete(@NonNull Task<String> task) {
				if (!task.isSuccessful()) {
					Exception e = task.getException();
					if (e instanceof FirebaseFunctionsException) {
						FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
						FirebaseFunctionsException.Code code = ffe.getCode();
						Object details = ffe.getDetails();

					}
					mIsBlocked = false;
					mAcceptModifyButton.setEnabled(true);
					Log.e("DELETE", "addMessage:onFailure", e);
					ToastManager.showToast(getString(R.string.general_error));
					return;
				}

				String result = task.getResult();
				Log.d("DELETE", result);
				ToastManager.showToast(getString(R.string.general_success));
				finish();
			}
		});
	}

	/*============IMAGES============*/
	private void selectImage() {
		Intent getImg = new Intent(Intent.ACTION_GET_CONTENT);
		getImg.setType("image/*");
		Intent pickFromGallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		pickFromGallery.setType("image/*");
		Intent chooserIntent = Intent.createChooser(getImg, "Select Image");
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickFromGallery});
		startActivityForResult(chooserIntent, 69);    //TODO: add multiple images
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Uri selectedImage = data.getData();
			mImagePath = PathResolver.getPathFromUri(this, selectedImage);//getPath(selectedImage);
			if (mImage != null && mImagePath != null)
				mImage.setImageBitmap(BitmapFactory.decodeFile(mImagePath));
			mImageGUID = UUID.randomUUID().toString();
		}
	}

	protected void uploadImage() {
		if (mImagePath == null)
			return;

		StorageReference storageReference = mFBStorage.getReferenceFromUrl("gs://parkings7.appspot.com").child("parkingImages/" + mImageGUID);

		storageReference.putFile(Uri.fromFile(new File(mImagePath)))
				.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
					@Override
					public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
						if (taskSnapshot.getMetadata().getReference() != null) {
							Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
							result.addOnSuccessListener(new OnSuccessListener<Uri>() {
								@Override
								public void onSuccess(Uri uri) {
									Log.d("IMAGE", "Successfully uploaded image " + uri.toString());
									onAcceptButtonClick_P2(uri);
								}
							});
						} else {
							Log.e("IMAGE", "Could not retrieve download link");
							mIsBlocked = false;
							mAcceptModifyButton.setEnabled(true);
							ToastManager.showToast(getString(R.string.general_error));
						}
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						mIsBlocked = false;
						mAcceptModifyButton.setEnabled(true);
						ToastManager.showToast(getString(R.string.general_error));
					}
				});
	}

	protected void editUploadImage() {
		if (mImagePath == null || mImageGUID == null)
			return;

		StorageReference storageReference = mFBStorage.getReferenceFromUrl("gs://parkings7.appspot.com").child("parkingImages/" + mImageGUID);

		storageReference.putFile(Uri.fromFile(new File(mImagePath)))
				.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
					@Override
					public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
						if (taskSnapshot.getMetadata().getReference() != null) {
							Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
							result.addOnSuccessListener(new OnSuccessListener<Uri>() {
								@Override
								public void onSuccess(Uri uri) {
									Log.d("IMAGE", "Successfully uploaded image " + uri.toString());
									onEditButtonClick_P2(uri);
								}
							});
						} else {
							Log.e("IMAGE", "Could not retrieve download link");
							mIsBlocked = false;
							mAcceptModifyButton.setEnabled(true);
							ToastManager.showToast(getString(R.string.general_error));
						}
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						mIsBlocked = false;
						mAcceptModifyButton.setEnabled(true);
						ToastManager.showToast(getString(R.string.general_error));
					}
				});
	}


	/*============OTHER============*/

	private void onTimeSlotClicked(View v) {
		TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				if (v == mTimeStartTV) {
					mSlotStart = hourOfDay + (((double) minute) / 60);
					mTimeStartTV.setText(doubleToString(mSlotStart));
				} else if (v == mTimeEndTV) {
					mSlotEnd = hourOfDay + (((double) minute) / 60);
					mTimeEndTV.setText(doubleToString(mSlotEnd));
				}
			}
		};
		double time = v == mTimeStartTV ? mSlotStart : mSlotEnd;
		int hourStart = (int) Math.floor(time);
		int minuteStart = (int) Math.round((time - hourStart) * 60);
		TimePickerDialog newFragment = new TimePickerDialog(LocationAddModifyActivity.this, timeListener, hourStart, minuteStart, true);
		newFragment.show();
	}


	@Override
	public void onBackPressed() {
		if (!mIsBlocked)        //TODO two backs for cancel operation?
			super.onBackPressed();
		else
			ToastManager.showToast(getString(R.string.general_wait));
	}

	private static String doubleToString(double time) {
		int hourStart = (int) Math.floor(time);
		int minuteStart = (int) Math.round((time - hourStart) * 60);
		return String.format("%02d:%02d", hourStart, minuteStart);
	}


	private Task<String> addParkResponse(Parking park) {
		return mFBFunctions
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

	// This just deactivates it, removing it from selection
	private Task<String> removeParkResponse(Parking park) {
		return mFBFunctions
				.getHttpsCallable("removeParking")
				.call(new HashMap<String, Object>() {{
					put("parkingUID", park.mUID);
				}})
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

	private Task<String> editParkResponse(String existingParkUID, Parking newPark) {
		return mFBFunctions
				.getHttpsCallable("editParking")
				.call(new HashMap<String, Object>() {{
					put("parkingUID", existingParkUID);
					put("newPark", newPark.getSubmittableObject());
				}})
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
