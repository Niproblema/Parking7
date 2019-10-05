package com.niproblema.parking7;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity {

    /** Duration of wait **/
    private static final int SPLASH_DISPLAY_LENGTH = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent firstScreen = new Intent(SplashScreen.this, LoginScreen.class);
                startActivity(firstScreen);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
