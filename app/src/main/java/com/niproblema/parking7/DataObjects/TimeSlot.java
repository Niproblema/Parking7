package com.niproblema.parking7.DataObjects;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

	@Nullable
	public static TimeSlot parse(DataSnapshot data) {
		try {
			// Parse
			double insuranceBail = data.child("insuranceBail").getValue(Double.class);
			double price = data.child("price").getValue(Double.class);
			Recurrence recurrence = Recurrence.valueOf(data.child("recurrence").getValue(String.class).toUpperCase());
			long startDate = data.child("startDate").getValue(Long.class);
			double timeStart = data.child("timeStart").getValue(Double.class);
			double timeEnd = data.child("timeEnd").getValue(Double.class);

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

	@NonNull
	@Override
	public String toString() {
		int hourStart = (int) Math.floor(mTimeStart);
		int minuteStart = (int) Math.round((mTimeStart - hourStart) * 60);

		int hourEnd = (int) Math.floor(mTimeEnd);
		int minuteEnd = (int) Math.round((mTimeEnd - hourEnd) * 60);

		return String.format(hourStart + ":" + minuteStart + " - " + hourEnd + ":" + minuteEnd);
	}
}
