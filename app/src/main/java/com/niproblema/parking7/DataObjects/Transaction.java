package com.niproblema.parking7.DataObjects;

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

	public Transaction(String parkingSnapshotUID, String providerUID, String renterUID){
		this.mParkingSnapshotUID = parkingSnapshotUID;
		this.mProviderUID = providerUID;
		mRenterUID = renterUID;
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
