package com.niproblema.parking7.Activities.PreLogin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.niproblema.parking7.Activities.CoreActivity;
import com.niproblema.parking7.DataObjects.User;
import com.niproblema.parking7.R;

import java.util.Map;

public class LoginScreen extends AppCompatActivity {
	private boolean isLoggingIn = true;

	private FirebaseAuth mAuth;

	Button mMainButton;
	Button mSwitchButton;
	EditText mUsername, mPassword, mPasswordRepeat, mFirstName, mLastName, mTaxNumber;
	Toast mFeedbackToast;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);

		mMainButton = (Button) findViewById(R.id.bMain);
		mSwitchButton = (Button) findViewById(R.id.bSwitch);
		mUsername = (EditText) findViewById(R.id.etLoginUsername);
		mPassword = (EditText) findViewById(R.id.etLoginPassword);
		mPasswordRepeat = findViewById(R.id.etLoginPasswordRepeat);
		mFirstName = findViewById(R.id.etLoginFirstName);
		mLastName = findViewById(R.id.etLoginLastName);
		mTaxNumber = findViewById(R.id.etLoginTaxNumber);

		mSwitchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isLoggingIn = !isLoggingIn;
				mMainButton.setText(isLoggingIn ? R.string.login_button_login : R.string.login_register_button);
				mSwitchButton.setText(isLoggingIn ? R.string.login_button_newUser : R.string.login_edittext_existingUser);

				mPasswordRepeat.setVisibility(isLoggingIn ? View.GONE : View.VISIBLE);
				mFirstName.setVisibility(isLoggingIn ? View.GONE : View.VISIBLE);
				mLastName.setVisibility(isLoggingIn ? View.GONE : View.VISIBLE);
				mTaxNumber.setVisibility(isLoggingIn ? View.GONE : View.VISIBLE);
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
			if (mFeedbackToast != null)
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
								if (mFeedbackToast != null)
									mFeedbackToast.cancel();
								mFeedbackToast = Toast.makeText(getApplicationContext(), feedback, Toast.LENGTH_LONG);
								mFeedbackToast.show();
							}
						}
					});
		} else {
			final String passwordRepeat = mPasswordRepeat.getText().toString();
			final String firstName = mFirstName.getText().toString();
			final String lastName = mLastName.getText().toString();
			final String taxNumber = mTaxNumber.getText().toString();

			if (TextUtils.isEmpty(passwordRepeat) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(taxNumber)) {
				if (mFeedbackToast != null)
					mFeedbackToast.cancel();
				mFeedbackToast = Toast.makeText(getApplicationContext(), getString(R.string.login_exception_invalid), Toast.LENGTH_LONG);
				mFeedbackToast.show();
				return;
			}

			if (!passwordRepeat.equals(password)) {
				if (mFeedbackToast != null)
					mFeedbackToast.cancel();
				mFeedbackToast = Toast.makeText(getApplicationContext(), getString(R.string.login_register_password_missmatch), Toast.LENGTH_LONG);
				mFeedbackToast.show();
				return;
			}

			if (!TextUtils.isDigitsOnly(taxNumber)) {
				if (mFeedbackToast != null)
					mFeedbackToast.cancel();
				mFeedbackToast = Toast.makeText(getApplicationContext(), getString(R.string.login_register_invalid_taxNumber), Toast.LENGTH_LONG);
				mFeedbackToast.show();
				return;
			}


			mAuth.createUserWithEmailAndPassword(username, password)
					.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {
							if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
								// Submit user  information!
								final User user = new User(firstName, lastName, username);
								finalizeRegistration(user);
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
								if (mFeedbackToast != null)
									mFeedbackToast.cancel();
								mFeedbackToast = Toast.makeText(getApplicationContext(), feedback, Toast.LENGTH_LONG);
								mFeedbackToast.show();
							}
						}
					});
		}
	}

	private void finalizeRegistration(final User user) {
		finalizeRegistrationResponse(user).addOnCompleteListener(new OnCompleteListener<String>() {
			@Override
			public void onComplete(@NonNull Task<String> task) {
				if (!task.isSuccessful()) {
					Exception e = task.getException();
					if (e instanceof FirebaseFunctionsException) {
						FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
						FirebaseFunctionsException.Code code = ffe.getCode();
						Object details = ffe.getDetails();
					}

					Log.e("FINALIZE", "finalizeRegistration:onFailure", e);
					return;
				}

				String result = task.getResult();
				Log.d("FINALIZE", result);
				startActivity(new Intent(getApplicationContext(), CoreActivity.class));
			}
		});
	}

	private Task<String> finalizeRegistrationResponse(final User user) {
		return FirebaseFunctions.getInstance()
				.getHttpsCallable("finalizeRegistration")
				.call(user.getSubmittableObject())
				.continueWith(new Continuation<HttpsCallableResult, String>() {
					@Override
					public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
						if (((Map<String, Boolean>) task.getResult().getData()).get("status")) {
							return "Success!";
						}

						return "Failiure";
					}
				});
	}
}
