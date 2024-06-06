package com.example.chatbot;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onurkaganaldemir.ktoastlib.KToast;

public class EditProfile extends AppCompatActivity {

    private EditText editName, editEmail, editOldPassword, editNewPassword;
    private Button saveButton;
    ImageButton backButton;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editOldPassword = findViewById(R.id.editOldPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);

        editName.setText(currentUser.getDisplayName());
        editEmail.setText(currentUser.getEmail());

        editEmail.setOnClickListener(v -> {
            showEmailChangeDialog();
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileChanges();
            }
        });

        backButton.setOnClickListener(v -> {
            Intent it = new Intent(EditProfile.this , MainActivity.class);
            startActivity(it);
            finish();
        });

    }

    private void saveProfileChanges() {

        final String name = editName.getText().toString().trim();
        String oldPassword = editOldPassword.getText().toString().trim();
        String newPassword = editNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            KToast.errorToast(EditProfile.this, "Please enter your name.", Gravity.BOTTOM, 500);
            editName.setText(currentUser.getDisplayName());
            editName.clearFocus();
            return;
        }

        if(isValidFullName(name)){
            if(!currentUser.getDisplayName().equals(name)){
                currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build());
                KToast.successToast(EditProfile.this , "Name updated successfully." , Gravity.BOTTOM , 500);
            }
        } else {
            KToast.errorToast(EditProfile.this, "Please enter a valid full name.", Gravity.BOTTOM, 500);
        }

        if (!TextUtils.isEmpty(oldPassword) && !TextUtils.isEmpty(newPassword)) {
            currentUser.reauthenticate(EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                currentUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mAuth.signOut();
                                            Intent it = new Intent(EditProfile.this , LoginScreen.class);
                                            startActivity(it);
                                            finish();
                                            KToast.successToast(EditProfile.this , "Password updated successfully, Please login again." , Gravity.BOTTOM , 500);
                                        } else {
                                            KToast.errorToast(EditProfile.this,"Failed to update password,", Gravity.BOTTOM , 500);
                                        }
                                    }
                                });
                            } else {
                                KToast.errorToast(EditProfile.this,"Incorrect old password.", Gravity.BOTTOM , 500);
                            }
                        }
                    });
        }

        userRef.child("name").setValue(name);

        editName.clearFocus();
        editOldPassword.clearFocus();
        editNewPassword.clearFocus();

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

    private void showEmailChangeDialog(){
        AlertDialog.Builder builder =
                new AlertDialog.Builder
                        (EditProfile.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(EditProfile.this).inflate(
                R.layout.layout_you_cant_change_mail,
                (ConstraintLayout)findViewById(R.id.layoutDialogContainer)
        );

        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle))
                .setText("Email changes unavailable");
        ((TextView) view.findViewById(R.id.textMessage))
                .setText("Email changes currently unavailable !");
        ((Button) view.findViewById(R.id.buttonAction))
                .setText("Okay");

        final AlertDialog alertDialog = builder.create();
        view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

}