package com.niproblema.parking7.Permissions;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.atomic.AtomicInteger;

public class Permissions {
	private static class PermissionCodeGen {
		private static AtomicInteger gen = new AtomicInteger(50);

		public static final int getUniqueCode() {
			return gen.addAndGet(1);
		}
	}

	public interface IPCallBack {
		void func();
	}

	private final int TARGET_PERMISSIONS_REQUEST_CODE = PermissionCodeGen.getUniqueCode();

	private Context context;
	private String TARGET_PERMISSION;
	private IPCallBack callBack;
	private boolean isBlocking = false;


	public Permissions(Context context, String target) {
		this.context = context;
		this.TARGET_PERMISSION = target;
	}

	private boolean checkPermission(Context context) {
		int result = context.checkSelfPermission(TARGET_PERMISSION);
		return result == PackageManager.PERMISSION_GRANTED;
	}

	private void requestPermission(Context context) {
		((AppCompatActivity) context).requestPermissions(new String[]{TARGET_PERMISSION}, TARGET_PERMISSIONS_REQUEST_CODE);
	}

	public void requestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == TARGET_PERMISSIONS_REQUEST_CODE) {
			forcePermissions(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);

		}
		return;
	}


	public void forcePermissions(IPCallBack callback) {
		if (!isBlocking) {
			if (callback != null)
				this.callBack = callback;
			if (!checkPermission(context)) {
				isBlocking = true;
				requestPermission(context);
			} else {
				this.callBack.func();
			}
		}
	}

	private void forcePermissions(boolean response) {
		isBlocking = false;
		if (!response)
			forcePermissions(null);
		else if (this.callBack != null)
			this.callBack.func();
	}
}
