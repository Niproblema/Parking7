package com.niproblema.parking7.DataObjects;

import java.io.Serializable;
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
	/// Transactions
	final public List<Transaction> mTransactions;

	// ServerSide
	/// Score
	public double mScore;
	/// Availability
	public boolean mAvailable;
	public String mTransactionUID;

	public Parking(List<Transaction> transactions, List<TimeSlot> timeSlots, Location location, String description, String access) {
		this.mTransactions = transactions;
		this.mTimeSlots = timeSlots;
		this.mLocation = location;
		this.mDescription = description;
		this.mAccessInstructions = access;
	}

	@Override
	public HashMap<String, Object> getSubmittableObject() {
		return new HashMap<String, Object>() {{
			put("transactions", mTransactions.stream().map(t -> t.getSubmittableObject()).collect(Collectors.toList()));
			put("timeSlots", mTimeSlots.stream().map(t -> t.getSubmittableObject()).collect(Collectors.toList()));
			put("location", mLocation.getSubmittableObject());
			put("accessInstructions", mAccessInstructions != null ? mAccessInstructions : "");
			put("description", mDescription != null ? mDescription : "");
		}};
	}
}
