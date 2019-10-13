package com.niproblema.parking7.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.niproblema.parking7.ParkingPlace;
import com.niproblema.parking7.R;

import java.util.HashMap;
import java.util.UUID;

public class LocationPreviewActivity extends AppCompatActivity {
    ImageView mImageView;
    TextView mTitle;
    EditText mDescription, mPrice;
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
            final LatLng position = i.getParcelableExtra("pos");
            mTitle.setText(position.latitude + " " + position.longitude);
            mButton1.setText(R.string.location_add);
            mButton1.setBackgroundColor(getColor(R.color.loc_green));
            mButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String id = UUID.randomUUID().toString();
                    FirebaseDatabase.getInstance().getReference("parkings/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            final String usermail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                            double costd = 0.1;
                            try {
                                costd = Double.parseDouble(mPrice.getText().toString());
                            } catch (Exception e) {
                            }
                            final double cost = costd;
                            final String description = mDescription.getText().toString();
                            final String lat = String.valueOf(position.latitude);
                            final String lon = String.valueOf(position.longitude);

                            dataSnapshot.getRef().setValue(new HashMap<String, Object>() {
                                {
                                    put("available", true);
                                    put("description", description);
                                    put("lat", lat);
                                    put("lon", lon);
                                    put("owner", usermail);
                                    put("price", cost);
                                }
                            });
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("DB", "onCancelled", databaseError.toException());
                        }
                    });
                }
            });
        } else {
            mTitle.setFocusable(false);
            mDescription.setFocusable(false);
            mPrice.setFocusable(false);

            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            final ParkingPlace parkingPlace = (ParkingPlace) i.getSerializableExtra("parkingPlace");

            mTitle.setText(parkingPlace.lat + " " + parkingPlace.lon);
            mPrice.setText(parkingPlace.price + "€/h");
            mDescription.setText(parkingPlace.description);
            if(parkingPlace.available) {
                mButton1.setText(R.string.location_rent_start);
                mButton1.setBackgroundColor(getColor(R.color.loc_green));
            }else{
                mButton1.setText(R.string.location_rent_end);
                mButton1.setBackgroundColor(getColor(R.color.loc_red));
            }
            mButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference("parkings/" + parkingPlace.id+"/available").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            dataSnapshot.getRef().setValue(!parkingPlace.available);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
        }
    }
}
