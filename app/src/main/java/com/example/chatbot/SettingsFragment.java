package com.example.chatbot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.onurkaganaldemir.ktoastlib.KToast;

public class SettingsFragment extends Fragment {
    FirebaseAuth mAuth;
    Button logout, delete_acc;
    TextView txtEmail, txtFullName;
    FirebaseUser user;
    ImageView profile_pic , edit_profile;


    private static final int PICK_IMAGE = 1;
    private DBHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        txtEmail = view.findViewById(R.id.txtEmail);
        txtFullName = view.findViewById(R.id.txtFullName);
        profile_pic = view.findViewById(R.id.profile_pic);
        logout = view.findViewById(R.id.logout);
        delete_acc = view.findViewById(R.id.delete_acc);
        edit_profile = view.findViewById(R.id.edit_profile);

        edit_profile.setOnClickListener(v -> {
            Intent it = new Intent(getActivity() , EditProfile.class);
            startActivity(it);
            getActivity().finish();
        });

        dbHelper = new DBHelper(getActivity());

        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            txtEmail.setText(email);
            txtFullName.setText(name);

            String profilePicPath = dbHelper.getProfilePicture(user.getUid());
            if (profilePicPath != null) {
                Uri profilePicUri = Uri.parse(profilePicPath);
                profile_pic.setImageURI(profilePicUri);
            } else {
                profile_pic.setImageResource(R.drawable.avatar);
            }
        }

        logout.setOnClickListener(v -> showLogoutConfirmationDialog());
        delete_acc.setOnClickListener(v -> showDeleteAccConfirmationDialog());
        profile_pic.setOnClickListener(v -> openGallery());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.rifle_green));
        }

        return view;
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            if (data != null) {
                String imagePath = getRealPathFromURI(data.getData());
                if (imagePath != null) {
                    KToast.infoToast(getActivity(), "Updating profile picture. Please wait...", Gravity.BOTTOM, 500);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String profilePicPath = dbHelper.getProfilePicture(user.getUid());
                            if (profilePicPath != null) {
                                dbHelper.updateProfilePicture(user.getUid(), imagePath);
                            } else {
                                dbHelper.insertProfilePicture(user.getUid(), imagePath);
                            }
                            profile_pic.setImageURI(data.getData());
                            KToast.successToast(getActivity(), "Profile picture updated successfully", Gravity.BOTTOM, 500);
                        }
                    }, 2000);
                } else {
                    Log.e("SettingsFragment", "Image path is null");
                }
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor == null || cursor.getCount() == 0) return null;

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_logout_dialog, null);
        builder.setView(view);

        TextView textTitle = view.findViewById(R.id.textTitle);
        textTitle.setText("Logging out");

        TextView textMessage = view.findViewById(R.id.textMessage);
        textMessage.setText("Do you want to logout?");

        Button buttonYes = view.findViewById(R.id.buttonYes);
        buttonYes.setText("Logout");

        Button buttonNo = view.findViewById(R.id.buttonNo);
        buttonNo.setText("Cancel");

        final AlertDialog alertDialog = builder.create();
        buttonYes.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginScreen.class);
            startActivity(intent);
            getActivity().finish();
        });

        buttonNo.setOnClickListener(v -> alertDialog.dismiss());

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private void showDeleteAccConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_delete_acc_dialog, null);
        builder.setView(view);

        TextView textTitle = view.findViewById(R.id.textTitle);
        textTitle.setText("Delete Account");

        TextView textMessage = view.findViewById(R.id.textMessage);
        textMessage.setText("Current Password");

        EditText confirm_password = view.findViewById(R.id.confirm_password);

        Button buttonYes = view.findViewById(R.id.buttonYes);
        buttonYes.setText("Delete");

        Button buttonNo = view.findViewById(R.id.buttonNo);
        buttonNo.setText("Cancel");

        final AlertDialog alertDialog = builder.create();
        buttonYes.setOnClickListener(v -> {
            String password = confirm_password.getText().toString().trim();

            if (TextUtils.isEmpty(password)) {
                KToast.errorToast(getActivity(), "Please enter your password.", Gravity.BOTTOM, 500);
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.delete()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Intent intent = new Intent(getActivity(), LoginScreen.class);
                                            startActivity(intent);
                                            getActivity().finish();
                                            KToast.successToast(getActivity(), "Account deleted successfully", Gravity.BOTTOM, 500);
                                        } else {
                                            KToast.errorToast(getActivity(), "Failed to delete account. Please try again later.", Gravity.BOTTOM, 500);
                                        }
                                    });
                        } else {
                            alertDialog.dismiss();
                            KToast.errorToast(getActivity(), "Incorrect password.", Gravity.BOTTOM, 500);
                        }
                    });
        });

        buttonNo.setOnClickListener(v -> alertDialog.dismiss());

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }
}