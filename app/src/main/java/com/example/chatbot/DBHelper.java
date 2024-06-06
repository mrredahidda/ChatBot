package com.example.chatbot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "UserProfileDB";
    private static final String TABLE_NAME = "UserProfile";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_UID = "UserID";
    private static final String COLUMN_PROFILE_PIC = "ProfilePic";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_UID + " TEXT,"
                + COLUMN_PROFILE_PIC + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void updateProfilePicture(String userId, String profilePicPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE_PIC, profilePicPath);
        int rowsAffected = db.update(TABLE_NAME, values, COLUMN_UID + "=?", new String[]{userId});
        db.close();

        if (rowsAffected > 0) {
            Log.d("DBHelper", "Profile picture path updated successfully for user: " + userId);
        } else {
            Log.e("DBHelper", "Failed to update profile picture path for user: " + userId);
        }
    }


    public String getProfilePicture(String userId) {
        String profilePicPath = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_PROFILE_PIC},
                COLUMN_UID + "=?", new String[]{userId}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            profilePicPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_PIC));
            cursor.close();
        }
        return profilePicPath;
    }

    public void insertProfilePicture(String userId, String profilePicPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, userId);
        values.put(COLUMN_PROFILE_PIC, profilePicPath);
        long result = db.insert(TABLE_NAME, null, values);
        db.close();

        if (result != -1) {
            Log.d("DBHelper", "Profile picture inserted successfully for user: " + userId);
        } else {
            Log.e("DBHelper", "Failed to insert profile picture for user: " + userId);
        }
    }


}
