package com.niproblema.parking7.DataObjects;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimeSlot implements Serializable, DataObject {
	// ClientSide
	public final double mInsuranceBail;
	public final double mPrice;
	public final Recurrence mRecurrence;
	public final long mStartDate;    // Calendar date
	public final double mTimeStart;    // Time at which parking opens for parking
	public final double mTimeEnd;    // Time at which parking closes for parking.

	public TimeSlot(double insuranceBail, double price, Recurrence recurrence, long startDate, double timeStart, double timeEnd) {
		this.mInsuranceBail = insuranceBail;
		this.mPrice = price;
		this.mRecurrence = recurrence;
		this.mStartDate = startDate;
		this.mTimeStart = timeStart;
		this.mTimeEnd = timeEnd;
	}

	public static TimeSlot parse(DataSnapshot data) {
		try {
			// Parse
			double insuranceBail = (double) data.child("insuranceBail").getValue();
			double price = (double) data.child("price").getValue();
			Recurrence recurrence = Recurrence.valueOf(((String) data.child("recurrence").getValue()).toUpperCase());
			long startDate = (long) data.child("startDate").getValue();
			double timeStart = (double) data.child("timeStart").getValue();
			double timeEnd = (double) data.child("timeEnd").getValue();

			// Init
			return new TimeSlot(insuranceBail, price, recurrence, startDate, timeStart, timeEnd);
		} catch (Exception e) {
			Log.e("TIMESLOT", "Error parsing time slot data: " + e.toString());
		}
		return null;
	}


	@Override
	public HashMap<String, Object> getSubmittableObject() {
		return new HashMap<String, Object>() {{
			put("insuranceBail", mInsuranceBail);
			put("price", mPrice);
			put("recurrence", mRecurrence.toString());
			put("startDate", mStartDate);
			put("timeStart", mTimeStart);
			put("timeEnd", mTimeEnd);
		}};
	}
}
