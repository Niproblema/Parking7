package com.niproblema.parking7;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationPreviewActivity extends AppCompatActivity {
    ImageView mImageView;
    EditText mTitle, mDescription, mPrice;
    Button mButton1, mButton2;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_preview_screen);
        mImageView = findViewById(R.id.ivParkPlacPreview);
        mTitle = findViewById(R.id.etTitle);
        mDescription = findViewById(R.id.etDescription);
        mPrice = findViewById(R.id.etPrice);
        mButton1 = findViewById(R.id.bP1);
        mButton2 = findViewById(R.id.bP2);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent i = getIntent();
        if (i.hasExtra("new")) {

        } else {
            mTitle.setFocusable(false);
            mDescription.setFocusable(false);
            mPrice.setFocusable(false);

            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            ParkingPlace parkingPlace = (ParkingPlace) i.getSerializableExtra("parkingPlace");
            if (email.equals(parkingPlace.owner)) { // chnace to remove
                FirebaseDatabase.getInstance().getReference("parkings").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("DB", "onCancelled", databaseError.toException());
                    }
                });
            } else {  // chance to see and rent
                mTitle.setText(parkingPlace.lat + " " + parkingPlace.lon);
                mPrice.setText(parkingPlace.price + "€/h");
                mDescription.setText(parkingPlace.description);
            }
        }
    }
}
