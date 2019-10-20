package com.niproblema.parking7.Utils;

import android.widget.Toast;

import com.niproblema.parking7.R;

public class ToastManager {
	private static Toast mFeedbackToast;

	public static void showToast(String textToShow) {
		if (mFeedbackToast != null)
			mFeedbackToast.cancel();
		mFeedbackToast = Toast.makeText(ContextManager.applicationContext, textToShow, Toast.LENGTH_LONG);
		mFeedbackToast.show();
	}
}
