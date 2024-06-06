package com.example.chatbot;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SignupScreen extends AppCompatActivity {
    TextView loginText;
    ImageButton backButton;
    Button signupButton;
    EditText signupPassword ,signupEmail ,signupName ,signupDateBirth;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_screen);

        loginText = findViewById(R.id.loginText);
        backButton = findViewById(R.id.backButton);
        signupEmail = findViewById(R.id.signupEmail);
        signupName = findViewById(R.id.signupName);
        signupPassword = findViewById(R.id.signupPassword);
        signupDateBirth = findViewById(R.id.signupDateBirth);
        signupButton = findViewById(R.id.signupButton);
        mAuth = FirebaseAuth.getInstance();

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = signupEmail.getText().toString();
                String password = signupPassword.getText().toString();
                String name = signupName.getText().toString();
                String dateOfBirth = signupDateBirth.getText().toString();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || TextUtils.isEmpty(dateOfBirth)) {
                    KToast.errorToast(SignupScreen.this, "Please fill in all fields to sign up.", Gravity.BOTTOM, 500);
                    return;
                }



                if(!isNetworkAvailable()){
                    KToast.errorToast(SignupScreen.this, "Please check you internet connection, and try again.", Gravity.BOTTOM, 500);
                    return;
                } else {

                    if (!isValidFullName(name)){
                        KToast.errorToast(SignupScreen.this, "Please enter a valid full name", Gravity.BOTTOM, 500);
                    } else {
                        if(!validEmail(email)) {
                            KToast.errorToast(SignupScreen.this, "Invalid email format.", Gravity.BOTTOM, 500);
                        } else {

                            if(!validPass(password)){
                                KToast.errorToast(SignupScreen.this, "Password must be at least 8 characters long and contain both uppercase and lowercase letters.", Gravity.BOTTOM, 800);
                            } else {

                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                                try {

                                    Date dob = sdf.parse(dateOfBirth);
                                    Calendar dobCalendar = Calendar.getInstance();
                                    dobCalendar.setTime(dob);
                                    Calendar minAge = Calendar.getInstance();
                                    minAge.add(Calendar.YEAR, -16); // Minimum age required (16 years old)

                                    if (dobCalendar.after(minAge)) {
                                        KToast.errorToast(SignupScreen.this, "You must be at least 16 years old to sign up.", Gravity.BOTTOM, 500);
                                        return;
                                    }

                                    mAuth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        signupName.setText("");
                                                        signupEmail.setText("");
                                                        signupPassword.setText("");
                                                        signupDateBirth.setText("");
                                                        KToast.successToast(SignupScreen.this, "Account created successfully!", Gravity.BOTTOM,500);
                                                    } else {
                                                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                                            KToast.errorToast(SignupScreen.this, "This email is already registered. Please use a different email.", Gravity.BOTTOM, 500);
                                                        }
                                                    }
                                                }
                                            })
                                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                                @Override
                                                public void onSuccess(AuthResult authResult) {
                                                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(name)
                                                            .build();

                                                    FirebaseUser user = authResult.getUser();
                                                    user.updateProfile(userProfileChangeRequest);
                                                }
                                            });
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    KToast.errorToast(SignupScreen.this, "Invalid date of birth format.", Gravity.BOTTOM, 500);
                                }
                            }
                        }
                    }

                }

                signupPassword.clearFocus();
                signupEmail.clearFocus();
                signupName.clearFocus();

            }
        });

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent it = new Intent(SignupScreen.this , LoginScreen.class);
                startActivity(it);
                finish();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent it = new Intent(SignupScreen.this , LoginScreen.class);
                startActivity(it);
                finish();

            }
        });

        signupDateBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select Date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        String date = new SimpleDateFormat("dd-MM-yyyy" , Locale.getDefault()).format(new Date(selection));
                        signupDateBirth.setText(date);
                    }
                });
                materialDatePicker.show(getSupportFragmentManager() , "tag");
            }
        });
    }
    public boolean validPass(String password){
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        return password.matches(passwordPattern);
    }
    public boolean validEmail(String email){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
    public boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }
        String[] parts = fullName.trim().split("\\s+");

        if (parts.length < 2 || parts.length > 3) {
            return false;
        }
        for (String part : parts) {
            if (!part.matches("[a-zA-Z]+")) {
                return false;
            }
        }

        return true;
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }


}