package com.niproblema.parking7.DataObjects;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
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
	public String mTransactionUID;
	/// Transactions
	public List<Transaction> mTransactions;

	public Parking(List<TimeSlot> timeSlots, Location location, String description, String access) {
		this.mTimeSlots = timeSlots;
		this.mLocation = location;
		this.mDescription = description;
		this.mAccessInstructions = access;
	}

	public static Parking parse(DataSnapshot data) {
		try {
			// Parse
			String accessInstruction = (String) data.child("accessInstructions").getValue();
			boolean available = (boolean) data.child("availability").child("available").getValue();
			String transactionUID = (String) data.child("availability").child("transactionUID").getValue();
			String description = (String) data.child("description").getValue();
			Location location = Location.parse(data.child("location"));
			if (location == null) return null;
			String ownerUID = (String) data.child("ownerUID").getValue();
			long scoreSum = (long) data.child("publicScore").child("sum").getValue();
			long votes = (long) data.child("publicScore").child("votes").getValue();
			double score = ((double) scoreSum) / ((double) votes);
			List<TimeSlot> slots = new ArrayList<TimeSlot>();
			for (DataSnapshot slotData : data.child("timeSlots").getChildren()) {
				TimeSlot parsedSlot = TimeSlot.parse(slotData);
				if (parsedSlot == null) return null;
				slots.add(parsedSlot);
			}
			List<Transaction> transactions = new ArrayList<Transaction>(); // TODO: chnage to UIDS, not objects!
			for (DataSnapshot transactionData : data.child("transactions").getChildren()) {
				Transaction parsedTransaction = Transaction.parse(transactionData);
				if (parsedTransaction == null) return null;
				transactions.add(parsedTransaction);
			}

			// Init
			Parking parsedPark = new Parking(slots, location, description, accessInstruction);
			parsedPark.mAvailable = available;
			parsedPark.mTransactionUID = transactionUID;
			parsedPark.mOwnerUID = ownerUID;
			parsedPark.mScore = score;
			parsedPark.mTransactions = transactions;
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
