package com.enavamaratha.enavamaratha.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.enavamaratha.enavamaratha.provider.FeedData;

public class DBManager {

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String name, String desc,String url,String UrlType) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.MESSAGE, name);
        contentValue.put(DatabaseHelper.TIME, desc);
        contentValue.put(DatabaseHelper.URL,url);
        contentValue.put(DatabaseHelper.URLTYPE,UrlType);
        database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.MESSAGE, DatabaseHelper.TIME ,DatabaseHelper.URL,DatabaseHelper.URLTYPE};
        String orderBy =  DatabaseHelper._ID + " DESC";
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, orderBy);

        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToNext();
        }
        return cursor;
    }

    public Cursor fetchUrl()
    {
        String [] columns = new String[]{DatabaseHelper._ID,DatabaseHelper.URL,DatabaseHelper.URLTYPE};
        String orderBy =  DatabaseHelper._ID + " DESC";
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, orderBy);

        if (cursor != null && cursor.moveToFirst())
        {
            cursor.moveToNext();
        }
        return cursor;
    }

    public int update(long _id, String name, String desc,String url,String UrlType) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.MESSAGE, name);
        contentValues.put(DatabaseHelper.TIME, desc);
        contentValues.put(DatabaseHelper.URL,url);
        contentValues.put(DatabaseHelper.URLTYPE,UrlType);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper._ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id)
    {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper._ID + "=" + _id, null);
    }


    public void deleteall()
    {

        database.execSQL("DELETE FROM "+DatabaseHelper.TABLE_NAME);
    }
}
