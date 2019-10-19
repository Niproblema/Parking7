package com.niproblema.parking7.Activities.PreLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.niproblema.parking7.Utils.Permissions.Permissions;
import com.niproblema.parking7.R;

public class SplashScreen extends AppCompatActivity {

	/**
	 * Duration of wait
	 **/
	private static final int SPLASH_DISPLAY_LENGTH = 1000;

	private Permissions mLocation;
	private TextView mPermissionNotificationTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.splash_screen);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				checkPermissions();
			}
		}, SPLASH_DISPLAY_LENGTH);
		mLocation = new Permissions(this, Manifest.permission.ACCESS_FINE_LOCATION);
		mPermissionNotificationTextView = (TextView) findViewById(R.id.tvPermissionsInfo);
	}

	protected void checkPermissions() {
		mLocation.forcePermissions(new Permissions.IPCallBack() {
			@Override
			public void func() {
				toLogin();
			}
		});
	}

	protected void toLogin() {
		Intent firstScreen = new Intent(SplashScreen.this, LoginScreen.class);
		startActivity(firstScreen);
		finish();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		mLocation.requestPermissionsResult(requestCode, permissions, grantResults);
		mPermissionNotificationTextView.setVisibility(View.VISIBLE);
	}
}
