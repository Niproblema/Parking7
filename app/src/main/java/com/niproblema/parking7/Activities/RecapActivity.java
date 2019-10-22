package com.niproblema.parking7.Activities;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.niproblema.parking7.DataObjects.Transaction;
import com.niproblema.parking7.DataObjects.User;
import com.niproblema.parking7.R;
import com.niproblema.parking7.Utils.PictureManager;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;

public class RecapActivity extends AppCompatActivity {

	ImageView mRecapImage, mRecapStar1, mRecapStar2, mRecapStar3, mRecapStar4, mRecapStar5;
	TextView mRecapTime, mRecapCost;

	String mImageURL;

	FirebaseAuth mAuth;
	FirebaseDatabase mDatabase;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recap_screen);
		if (getIntent().hasExtra("imageURL"))
			mImageURL = (String) getIntent().getSerializableExtra("imageURL");

		mAuth = FirebaseAuth.getInstance();
		mDatabase = FirebaseDatabase.getInstance();

		mRecapImage = findViewById(R.id.ivRecapPicture);
		mRecapStar1 = findViewById(R.id.ivStar1);
		mRecapStar2 = findViewById(R.id.ivStar2);
		mRecapStar3 = findViewById(R.id.ivStar3);
		mRecapStar4 = findViewById(R.id.ivStar4);
		mRecapStar5 = findViewById(R.id.ivStar5);
		mRecapTime = findViewById(R.id.tvRecapTime);
		mRecapCost = findViewById(R.id.tvRecapCost);

		getAsyncData();
	}


	public void getAsyncData() {
		if (mImageURL != null) {
			PictureManager.getImageByURL(mImageURL, new PictureManager.OnImageFetchedListener() {
				@Override
				public void onSuccess(File image) {
					String imagePath = image.getPath();
					if (mRecapImage != null && imagePath != null)
						mRecapImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
				}

				@Override
				public void onFailure(@NonNull Exception e) {
					Log.e("IMAGE", "Could not load image from firebase: " + e.toString());
				}
			});
		}

		mDatabase.getReference("/users/" + mAuth.getUid())
				.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						User user = User.parse(dataSnapshot);
						if (user.mTransactionUIDs != null && user.mTransactionUIDs.size() > 0) {
							mDatabase.getReference("/transactions/" + user.mTransactionUIDs.get(user.mTransactionUIDs.size() - 1))
									.addListenerForSingleValueEvent(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
											Transaction transaction = Transaction.parse(dataSnapshot);
											if (transaction != null) {
												mRecapCost.setText(getString(R.string.recap_cost_recap) + "\n" + String.format("%.2f€", transaction.mCost));
												long duration = (transaction.mStopTimestamp - transaction.mStartTimestamp) / 1000;
												int hours = (int) Math.floor(duration / 3600);
												int minutes = (int) Math.floor((duration % 3600) / 60);
												int seconds = (int) Math.floor((duration % 60));
												mRecapTime.setText(getString(R.string.recap_time_recap) + "\n" + String.format("%02d:%02d:%02d", hours, minutes, seconds));
											}
										}

										@Override
										public void onCancelled(@NonNull DatabaseError databaseError) {
											Log.e("DB", "onCancelled", databaseError.toException());
										}
									});
						}
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
						Log.e("DB", "onCancelled", databaseError.toException());
					}
				});
	}

}
