package com.niproblema.parking7.DataObjects;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.util.HashMap;

public class Transaction implements Serializable, DataObject {
	// ServerSide
	public double mCost;
	public long mStopTimestamp;
	public long mStartTimestamp;

	// ClientSide
	public final String mParkingSnapshotUID;
	public final String mProviderUID;
	public final String mRenterUID;

	public Transaction(String parkingSnapshotUID, String providerUID, String renterUID) {
		this.mParkingSnapshotUID = parkingSnapshotUID;
		this.mProviderUID = providerUID;
		mRenterUID = renterUID;
	}

	public static Transaction parse(DataSnapshot data) {
		try {
			// Parse
			double cost = (double) data.child("cost").getValue();                    	// TODO ? not set yewt?
			long stopTimeStamp = (long) data.child("stopTimestamp").getValue();    		// TODO ? not set yet?
			long startTimeStamp = (long) data.child("startTimestamp").getValue();
			String providerUID = (String) data.child("providerUID").getValue();
			String renterUID = (String) data.child("renterUID").getValue();
			String parkingSnapshotUID = (String) data.child("parkingSnapshotUID").getValue();

			// Init
			Transaction t = new Transaction(parkingSnapshotUID, providerUID, renterUID);
			t.mCost = cost;
			t.mStopTimestamp = stopTimeStamp;
			t.mStartTimestamp = startTimeStamp;
			return t;
		} catch (Exception e) {
			Log.e("TRANSACTION", "Error parsing transaction data: " + e.toString());
		}
		return null;
	}

	@Override
	public HashMap<String, Object> getSubmittableObject() {
		return new HashMap<String, Object>() {{
			put("parkingSnapshotUID", mParkingSnapshotUID);
			put("providerUID", mProviderUID);
			put("renterUID", mRenterUID);
		}};
	}
}
