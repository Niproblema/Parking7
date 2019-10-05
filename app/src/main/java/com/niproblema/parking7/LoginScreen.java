package com.niproblema.parking7;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginScreen extends AppCompatActivity {
    private boolean isLoggingIn = true;

    private FirebaseAuth mAuth;

    Button mMainButton;
    Button mSwitchButton;
    EditText mUsername;
    EditText mPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        mMainButton = (Button) findViewById(R.id.bMain);
        mSwitchButton = (Button) findViewById(R.id.bSwitch);
        mUsername = (EditText) findViewById(R.id.etLoginUsername);
        mPassword = (EditText) findViewById(R.id.etLoginPassword);

        mSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingIn = !isLoggingIn;
                mMainButton.setText(isLoggingIn ? R.string.login_button_login : R.string.login_register_button);
                mSwitchButton.setText(isLoggingIn ? R.string.login_button_newUser : R.string.login_edittext_existingUser);
            }
        });
        mMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMainButtonClick();
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }


    protected void onMainButtonClick() {
        String username = mUsername.getText().toString();
        String password = mUsername.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Preveri vnos", Toast.LENGTH_LONG).show();
        }

        if (isLoggingIn) {
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(getApplicationContext(), CoreActivity.class));
                            } else {
                                Toast.makeText(getApplicationContext(), "Uporabniško ime(email) ali geslo je neustrezeno", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            mAuth.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(getApplicationContext(), CoreActivity.class));
                            } else {
                                Toast.makeText(getApplicationContext(), "Uporabniško ime(email) ali geslo je neustrezeno", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

}
