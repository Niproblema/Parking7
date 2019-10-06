package com.niproblema.parking7;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;

public class CoreActivity extends AppCompatActivity {
    public static final long BACK_DOUBLEPRESS_INTERVAL = 500;
    private long mBackPressTimeLast = 0l;
    private MapView mMap;
    private GoogleMap gMap;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.core_screen);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onBackPressed() {
        long time = System.currentTimeMillis();
        if (time - mBackPressTimeLast < BACK_DOUBLEPRESS_INTERVAL) {
            mAuth.signOut();
            super.onBackPressed();
        } else {
            Toast.makeText(getApplicationContext(), "Dvojni nazaj gumb za odjavo", Toast.LENGTH_SHORT).show();
        }
        mBackPressTimeLast = time;
    }
}
