package com.niproblema.parking7.Activities.PreLogin;

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
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.niproblema.parking7.Activities.CoreActivity;
import com.niproblema.parking7.R;

public class LoginScreen extends AppCompatActivity {
	private boolean isLoggingIn = true;

	private FirebaseAuth mAuth;

	Button mMainButton;
	Button mSwitchButton;
	EditText mUsername;
	EditText mPassword;
	Toast mFeedbackToast;

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

	@Override
	protected void onStart() {
		super.onStart();
		FirebaseUser currentUser = mAuth.getCurrentUser();
		if (currentUser != null) {
			startActivity(new Intent(getApplicationContext(), CoreActivity.class));
		}
	}

	protected void onMainButtonClick() {
		final String username = mUsername.getText().toString();
		final String password = mPassword.getText().toString();

		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
			if(mFeedbackToast != null)
				mFeedbackToast.cancel();
			mFeedbackToast = Toast.makeText(getApplicationContext(), getString(R.string.login_exception_invalid), Toast.LENGTH_LONG);
			mFeedbackToast.show();
			return;
		}

		if (isLoggingIn) {
			mAuth.signInWithEmailAndPassword(username, password)
					.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {
							if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
								startActivity(new Intent(getApplicationContext(), CoreActivity.class));
							} else {
								String feedback = "";
								try {
									throw task.getException();
								} catch (FirebaseAuthInvalidUserException ex) {
									feedback = getString(R.string.login_exception_wrong_username);
								} catch (FirebaseAuthInvalidCredentialsException ex) {
									feedback = getString(R.string.login_exception_wrong_password);
								} catch (FirebaseTooManyRequestsException ex) {
									feedback = getString(R.string.login_exception_try_later);
								} catch (Exception e) {
									feedback = getString(R.string.login_exception_default);
								}
								if(mFeedbackToast != null)
									mFeedbackToast.cancel();
								mFeedbackToast = Toast.makeText(getApplicationContext(), feedback, Toast.LENGTH_LONG);
								mFeedbackToast.show();
							}
						}
					});
		} else {
			mAuth.createUserWithEmailAndPassword(username, password)
					.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {
							if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
								startActivity(new Intent(getApplicationContext(), CoreActivity.class));
							} else {
								String feedback = "";
								try {
									throw task.getException();
								} catch (FirebaseAuthWeakPasswordException ex) {
									feedback = getString(R.string.login_exception_weak_password);
								} catch (FirebaseAuthUserCollisionException ex) {
									feedback = getString(R.string.login_exception_existing_username);
								} catch (FirebaseTooManyRequestsException ex) {
									feedback = getString(R.string.login_exception_try_later);
								} catch (Exception e) {
									feedback = getString(R.string.login_exception_default);
								}
								if(mFeedbackToast != null)
									mFeedbackToast.cancel();
								mFeedbackToast = Toast.makeText(getApplicationContext(), feedback, Toast.LENGTH_LONG);
								mFeedbackToast.show();
							}
						}
					});
		}
	}
}
