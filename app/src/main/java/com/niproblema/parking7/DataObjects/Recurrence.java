package com.niproblema.parking7.DataObjects;

import androidx.annotation.NonNull;

public enum Recurrence {
	ONCE,
	DAILY,
	WEEKLY,
	MONTHLY;

	@NonNull
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
