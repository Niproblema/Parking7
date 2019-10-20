package com.niproblema.parking7.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.niproblema.parking7.R;

import java.text.DecimalFormat;
import java.util.Random;

public class CoreActivity extends AppCompatActivity {
	public static final long BACK_DOUBLEPRESS_INTERVAL = 500;
	private long mBackPressTimeLast = 0l;

	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;
	private TextView mUsername, mBalance;

	private ChildEventListener mListenForBalanceChanges;
	private DatabaseReference mDatabaseReference;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.core_screen);
		mAuth = FirebaseAuth.getInstance();
		mDatabase = FirebaseDatabase.getInstance();
		mUsername = findViewById(R.id.tvUsername);
		mBalance = findViewById(R.id.tvBalance);
		mUsername.setText(mAuth.getCurrentUser().getEmail());

		mDatabaseReference = mDatabase.getReference("/users/" + mAuth.getUid());// + "/balance");
		mListenForBalanceChanges = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				if (dataSnapshot.getKey().equals("balance")) {
					mBalance.setText(String.format("%.2f€", dataSnapshot.getValue(Double.class)));
				}
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				if (dataSnapshot.getKey().equals("balance")) {
					mBalance.setText(String.format("%.2f€", dataSnapshot.getValue(Double.class)));
				}
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		};
	}

	@Override
	protected void onStart() {
		super.onStart();
		mDatabaseReference.addChildEventListener(mListenForBalanceChanges);

	}

	@Override
	protected void onStop() {
		super.onStop();
		mDatabaseReference.removeEventListener(mListenForBalanceChanges);
	}

	@Override
	public void onBackPressed() {
		long time = System.currentTimeMillis();
		if (time - mBackPressTimeLast < BACK_DOUBLEPRESS_INTERVAL) {
			mAuth.signOut();
			super.onBackPressed();
		} else {
			Toast.makeText(getApplicationContext(), "Dvojni nazaj gumb za odjavo", Toast.LENGTH_SHORT).show();
		}
		mBackPressTimeLast = time;
	}
}
