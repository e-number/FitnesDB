package com.example.fitnesdb.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.fitnesdb.data.FitnessClubContract.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FitnessClubContentProvider extends ContentProvider {

    FitnessClubDbOpenHelper dbOpenHelper;

    private static final int MEMBERS = 111;
    private static final int MEMBER_ID = 222;

    // Creates a UriMatcher object.
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(FitnessClubContract.AUTHORITY, FitnessClubContract.PATH_MEMBERS, MEMBERS);
        uriMatcher.addURI(FitnessClubContract.AUTHORITY, FitnessClubContract.PATH_MEMBERS + "/#", MEMBER_ID);
    }


    @Override
    public boolean onCreate() {
        dbOpenHelper = new FitnessClubDbOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor;

        int match = uriMatcher.match(uri);

        switch (match) {
            case MEMBERS:
                cursor = db.query(MemberEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case MEMBER_ID:
                selection = MemberEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(MemberEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw  new IllegalArgumentException("Can't query incorrect URI " + uri);

        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        String firstName = values.getAsString(MemberEntry.COLUMN_FIRST_NAME);
        if (firstName == null) {
            throw new IllegalArgumentException("You have to input first name");
        }

        String lastName = values.getAsString(MemberEntry.COLUMN_LAST_NAME);
        if (lastName == null) {
            throw new IllegalArgumentException("You have to input last name");
        }

        Integer gender = values.getAsInteger(MemberEntry.COLUMN_GENDER);
        if (gender == null || !(gender == MemberEntry.GENDER_UNKNOWN || gender == MemberEntry.GENDER_MALE ||
                gender == MemberEntry.GENDER_FEMALE)) {
            throw new IllegalArgumentException("You have to input correct gender");
        }

        String sport = values.getAsString(MemberEntry.COLUMN_SPORT);
        if (sport == null) {
            throw new IllegalArgumentException("You have to input correct data");
        }

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);

        switch (match) {
            case MEMBERS:
                long id = db.insert(MemberEntry.TABLE_NAME, null, values);
                if (id == -1) {
                    Log.e("insertMethod", "Insertion of data in the table failed for " + uri);
                    return null;
                }

                getContext().getContentResolver().notifyChange(uri, null);

                return ContentUris.withAppendedId(uri, id);

            default:
                throw  new IllegalArgumentException("Insertion of data in the table failed for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case MEMBERS:
                rowsDeleted = db.delete(MemberEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case MEMBER_ID:
                selection = MemberEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(MemberEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw  new IllegalArgumentException("Can't delete this URI " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        if (values.containsKey(MemberEntry.COLUMN_FIRST_NAME)) {
            String firstName = values.getAsString(MemberEntry.COLUMN_FIRST_NAME);
            if (firstName == null) {
                throw new IllegalArgumentException("You have to input first name");
            }
        }

        if (values.containsKey(MemberEntry.COLUMN_LAST_NAME)) {
            String lastName = values.getAsString(MemberEntry.COLUMN_LAST_NAME);
            if (lastName == null) {
                throw new IllegalArgumentException("You have to input last name");
            }
        }

        if (values.containsKey(MemberEntry.COLUMN_GENDER)) {
            Integer gender = values.getAsInteger(MemberEntry.COLUMN_GENDER);
            if (gender == null || !(gender == MemberEntry.GENDER_UNKNOWN || gender == MemberEntry.GENDER_MALE ||
                    gender == MemberEntry.GENDER_FEMALE)) {
                throw new IllegalArgumentException("You have to input correct gender");
            }
        }

        if (values.containsKey(MemberEntry.COLUMN_SPORT)) {
            String sport = values.getAsString(MemberEntry.COLUMN_SPORT);
            if (sport == null) {
                throw new IllegalArgumentException("You have to input correct data");
            }
        }

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MEMBERS:
                rowsUpdated = db.update(MemberEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case MEMBER_ID:
                selection = MemberEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = db.update(MemberEntry.TABLE_NAME, values, selection, selectionArgs);
                if (rowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                break;

            default:
                throw  new IllegalArgumentException("Can't update this URI " + uri);
        }
        return rowsUpdated;
    }

    @Override
    public String getType(@NonNull Uri uri) {

        int match = uriMatcher.match(uri);

        switch (match) {
            case MEMBERS:
                return MemberEntry.CONTENT_MULTIPLE_ITEMS;

            case MEMBER_ID:
                return MemberEntry.CONTENT_SINGLE_ITEMS;

            default:
                throw  new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}
