package com.niproblema.parking7.Utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.HashMap;

public class PictureManager {
	private static final HashMap<String, File> mImageMap = new HashMap<String, File>();

	public static void getImageByURL(final String url, final OnImageFetchedListener onImageFetchedListener) {
		if (mImageMap.containsKey(url)) {
			onImageFetchedListener.onSuccess(mImageMap.get(url));
		}

		try {
			File newFile = File.createTempFile("img", null);
			newFile.deleteOnExit();    // TODO: delete no guaranteed
			FirebaseStorage.getInstance().getReferenceFromUrl(url).getFile(newFile)
					.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
						@Override
						public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
							mImageMap.put(url, newFile);
							onImageFetchedListener.onSuccess(newFile);
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							onImageFetchedListener.onFailure(e);
						}
					});
		} catch (Exception e) {
			Log.e("PIC_MANAGER", e.toString());
		}
	}

	public interface OnImageFetchedListener {
		void onSuccess(File image);

		void onFailure(@NonNull Exception e);
	}
}
