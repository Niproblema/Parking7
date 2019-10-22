package com.niproblema.parking7.DataObjects;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User implements DataObject {
	// ClientSide
	public final String mFirstName;
	public final String mLastName;
	public final String mLoginEmail;

	// ServerSide
	public double mBalance;
	public List<String> mChatsUIDs;
	public List<String> mParkingUIDs;
	public List<String> mTransactionUIDs;
	public boolean mIsRenting = false;

	public User(String firstName, String lastName, String loginEmail) {
		this.mFirstName = firstName;
		this.mLastName = lastName;
		this.mLoginEmail = loginEmail;
	}

	@Nullable
	public static User parse(DataSnapshot data) {
		try {
			// Parse
			Double balance = data.child("balance").getValue(Double.class);
			String firstName = data.child("firstName").getValue(String.class);
			String lastName = data.child("lastName").getValue(String.class);
			String loginEmail = data.child("loginEmail").getValue(String.class);
			boolean isRenting = data.child("isRenting").getValue(Boolean.class);
			List<String> chatUIDs = new ArrayList<String>();
			for (DataSnapshot chatUID : data.child("chats").getChildren()) {
				String parsedChatUID = chatUID.getValue(String.class);
				chatUIDs.add(parsedChatUID);
			}

			List<String> parkingUIDs = new ArrayList<String>();
			for (DataSnapshot parkingUID : data.child("parkings").getChildren()) {
				String parsedParkingUID = parkingUID.getValue(String.class);
				parkingUIDs.add(parsedParkingUID);
			}

			List<String> transactionUIDs = new ArrayList<String>();
			for (DataSnapshot transactionUID : data.child("transactions").getChildren()) {
				String parsedTransactionUID = transactionUID.getValue(String.class);
				transactionUIDs.add(parsedTransactionUID);
			}

			// Init
			User u = new User(firstName, lastName, loginEmail);
			u.mBalance = balance;
			u.mChatsUIDs = chatUIDs;
			u.mParkingUIDs = parkingUIDs;
			u.mTransactionUIDs = transactionUIDs;
			u.mIsRenting = isRenting;
			return u;
		} catch (Exception e) {
			Log.e("USER", "Error parsing user data: " + e.toString());
		}
		return null;
	}

	@Override
	public HashMap<String, Object> getSubmittableObject() {
		return new HashMap<String, Object>() {{
			put("firstName", mFirstName);
			put("lastName", mLastName);
			put("loginEmail", mLoginEmail);
		}};
	}
}
