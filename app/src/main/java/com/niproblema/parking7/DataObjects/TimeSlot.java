package com.niproblema.parking7.DataObjects;

import java.io.Serializable;
import java.util.HashMap;

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
