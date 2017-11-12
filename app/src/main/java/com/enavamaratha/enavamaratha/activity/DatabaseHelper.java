package com.enavamaratha.enavamaratha.activity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.enavamaratha.enavamaratha.provider.FeedData;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Table Name
    public static final String TABLE_NAME = "NOTIFICATION";

    // Table columns
    public static final String _ID = "_id";
    public static final String MESSAGE= "message";
    public static final String TIME = "time";
    public static final String URL="url";
    public static final String URLTYPE="urltype";
    static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";
    static final String TYPE_TEXT = "TEXT";

    // Database Information
    static final String DB_NAME = "GcmNotification.db";

    // database version
    static final int DB_VERSION = 1;

    // Creating table query

  public static final String[][] COLUMNS = new String[][]{{_ID, TYPE_PRIMARY_KEY},  { MESSAGE, TYPE_TEXT},{TIME, TYPE_TEXT},{URL, TYPE_TEXT},{URLTYPE, TYPE_TEXT}};

 /*   private static final String CREATE_TABLE = "create table " + TABLE_NAME +
                                                 "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                    MESSAGE + " TEXT NOT NULL, " +
                                                    TIME + " TEXT, " +
                                                    URL + " TEXT, " +
                                                    URLTYPE + " TEXT);";*/




    public DatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      //  db.execSQL(CREATE_TABLE);
        db.execSQL(createTable(TABLE_NAME,COLUMNS));
       // database.execSQL(createTable(FeedData.FeedColumns.TABLE_NAME, FeedData.FeedColumns.COLUMNS));
    }
    private String createTable(String tableName, String[][] columns) {
        if (tableName == null || columns == null || columns.length == 0)
        {
            throw new IllegalArgumentException("Invalid parameters for creating table " + tableName);
        } else
        {
            StringBuilder stringBuilder = new StringBuilder("CREATE TABLE ");

            stringBuilder.append(tableName);
            stringBuilder.append(" (");
            for (int n = 0, i = columns.length; n < i; n++) {
                if (n > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(columns[n][0]).append(' ').append(columns[n][1]);
            }
            return stringBuilder.append(");").toString();
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
