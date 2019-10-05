package com.niproblema.parking7;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class CoreActivity extends AppCompatActivity {
    private long mBackPressTimeLast = 0l;
    private static final long BACK_DOUBLEPRESS_INTERVAL = 500;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.core_screen);
    }

    @Override
    public void onBackPressed() {
        long time = System.currentTimeMillis();
        if (time - mBackPressTimeLast < BACK_DOUBLEPRESS_INTERVAL) {
            super.onBackPressed();
        }else{
            Toast.makeText(getApplicationContext(), "Dvojni nazaj gumb za odjavo", Toast.LENGTH_SHORT).show();
        }
    }
}
