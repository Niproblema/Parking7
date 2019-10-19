package com.niproblema.parking7.DataObjects;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Parking implements Serializable, DataObject {
	// ClientSide
	/// Information
	public String mOwnerUID;
	final public String mAccessInstructions;
	final public String mDescription;
	/// Location
	final public Location mLocation;
	/// TimeSlots
	final public List<TimeSlot> mTimeSlots;

	// ServerSide
	/// Score
	public double mScore;
	/// Availability
	public boolean mAvailable;
	public String mTransactionUID;            // Current transaction active on parking.
	/// Transactions
	public List<String> mTransactionUIDs;    // List of all previous transactions. Added by server when transaction ends.

	public Parking(List<TimeSlot> timeSlots, Location location, String description, String access) {
		this.mTimeSlots = timeSlots;
		this.mLocation = location;
		this.mDescription = description;
		this.mAccessInstructions = access;
	}

	@Nullable
	public static Parking parse(DataSnapshot data) {
		try {
			// Parse
			String accessInstruction = data.child("accessInstructions").getValue(String.class);
			boolean available = data.child("availability").child("available").getValue(Boolean.class);
			String transactionUID = data.child("availability").child("transactionUID").getValue(String.class);
			String description = data.child("description").getValue(String.class);
			Location location = Location.parse(data.child("location"));
			if (location == null) return null;
			String ownerUID = data.child("ownerUID").getValue(String.class);
			long scoreSum = data.child("publicScore").child("sum").getValue(Long.class);
			long votes = data.child("publicScore").child("votes").getValue(Long.class);
			double score = votes == 0 ? 0D : ((double) scoreSum) / ((double) votes);
			List<TimeSlot> slots = new ArrayList<TimeSlot>();
			for (DataSnapshot slotData : data.child("timeSlots").getChildren()) {
				TimeSlot parsedSlot = TimeSlot.parse(slotData);
				if (parsedSlot == null) return null;
				slots.add(parsedSlot);
			}
			List<String> transactions = new ArrayList<String>();
			for (DataSnapshot transactionData : data.child("transactions").getChildren()) {
				String parsedTransactionUID = transactionData.getValue(String.class);
				transactions.add(parsedTransactionUID);
			}

			// Init
			Parking parsedPark = new Parking(slots, location, description, accessInstruction);
			parsedPark.mAvailable = available;
			parsedPark.mTransactionUID = transactionUID;
			parsedPark.mOwnerUID = ownerUID;
			parsedPark.mScore = score;
			parsedPark.mTransactionUIDs = transactions;
			return parsedPark;
		} catch (Exception e) {
			Log.e("PARKING", "Error parsing parking data: " + e.toString());
		}
		return null;
	}


	@Override
	public HashMap<String, Object> getSubmittableObject() {
		return new HashMap<String, Object>() {{
			put("timeSlots", mTimeSlots.stream().map(t -> t.getSubmittableObject()).collect(Collectors.toList()));
			put("location", mLocation.getSubmittableObject());
			put("accessInstructions", mAccessInstructions != null ? mAccessInstructions : "");
			put("description", mDescription != null ? mDescription : "");
		}};
	}
}
