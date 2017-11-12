package com.enavamaratha.enavamaratha.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.enavamaratha.enavamaratha.MainApplication;
import com.enavamaratha.enavamaratha.provider.DatabaseHelper;
import com.enavamaratha.enavamaratha.provider.FeedData;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by win7 on 2/27/2017.
 */

// class for delete feeds which are trash on server
public class DeleteFeeds
{
    DatabaseHelper dbhelp;
    SQLiteDatabase mDatabase;
    final String DATABASE_NAME = "FeedEx.db";
    ContentResolver conte;
    // DatabaseHelper mDBHelper;


    // method for getting all postid(guid) from local database --- SQLite
    // return all post id in arraylist format
    public ArrayList<String> getPostId()
    {
        ArrayList<String> PostGuid = new ArrayList<String>();
        int _idd, _guid;
        String temp_guid;

        // mDBHelper = new DatabaseHelper(new Handler(),MainApplication.getContext());


        dbhelp = new DatabaseHelper(new Handler(),MainApplication.getContext());
        mDatabase = dbhelp.getWritableDatabase();
        String[] columns = new String[]{FeedData.EntryColumns._ID, FeedData.EntryColumns.GUID};
        String Table = FeedData.EntryColumns.TABLE_NAME;

        Cursor cursor = mDatabase.query(Table, columns, null, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst())
        {
            do {

                _idd = cursor.getColumnIndex(FeedData.EntryColumns._ID);
                _guid = cursor.getColumnIndex(FeedData.EntryColumns.GUID);
                temp_guid = cursor.getString(_guid);
                PostGuid.add(temp_guid);

                //  Log.i(TAG,"Get Array of Post Id In RssParser : " + PostGuid);

            } while (cursor.moveToNext());


        } else
        {
            // Log.i(TAG, "Get Post Id In RssParser is Null ");
        }


        cursor.close();
        mDatabase.close();

        return PostGuid;
    }


    // convert response from php jsonArray to ListArray(String )
    public  ArrayList<String> ConvertJsonarray(String mJsonArray) throws JSONException
    {
        JSONArray jsonArray = new JSONArray(mJsonArray);
        ArrayList<String> list = new ArrayList<String>();
        if(jsonArray !=null)
        {
            for (int i = 0; i < jsonArray.length(); i++)
            {
                list.add(jsonArray.getString(i));
            }

        }

        return list;
        // Log.i(TAG,"Converted Response Json Array to List : "+list);
    }


    // Delete Feeds Array Whic Are not publish on server and delete in our local db
    public void DeleteFeed(ArrayList<String> tempArray)
    {

        String select= FeedData.EntryColumns.GUID +"=?";

        dbhelp = new DatabaseHelper(new Handler(),MainApplication.getContext());
        mDatabase = dbhelp.getWritableDatabase();
        String args = TextUtils.join(", ", tempArray);
       // mDatabase.execSQL(String.format("DELETE FROM entries WHERE guid IN (%s);", args));

        for(int i= 0;i<tempArray.size();i++)
        {
            long l = mDatabase.delete(FeedData.EntryColumns.TABLE_NAME,select,new String[]{tempArray.get(i).toString()});
            System.out.println("Delete" + l);
        }
        Log.i("Delete FEEDS ", "Deleted Array ");
        mDatabase.close();
                           /* long l = db.delete(EntryColumns.TABLE_NAME,select,new String[]{tempArray.get(i)});
                            Log.i(TAG,"Deleted Feed When Trash : "+tempArray.get(i));*/


    }
}
