package com.enavamaratha.enavamaratha.activity;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enavamaratha.enavamaratha.Constants;
import com.enavamaratha.enavamaratha.R;

import com.enavamaratha.enavamaratha.adapter.NotificationAdapter;
import com.enavamaratha.enavamaratha.provider.FeedData;
import com.enavamaratha.enavamaratha.service.ConnectionDetector;
import com.enavamaratha.enavamaratha.utils.GetePaperUrlDateFormat;


public class GcmNotification extends AppCompatActivity {

    public DBManager dbManager;
    public ListView listView;


    private NotificationAdapter mAdapter;

    private DatabaseHelper dbHelper;
    private com.enavamaratha.enavamaratha.provider.DatabaseHelper DBHelper;

    private Context context;
    private SQLiteDatabase database;
    private SQLiteDatabase datab;

    String url;
    String urltype;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_notification_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initView();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor curs = (Cursor) parent.getItemAtPosition(position);

                String Url = curs.getString(curs.getColumnIndex(DatabaseHelper.URL));
                String UrlType = curs.getString(curs.getColumnIndex(DatabaseHelper.URLTYPE));
                if (UrlType != null)
                {

                    // if url contain web
                    if (UrlType.contains("Web"))
                    {
                        Intent i = new Intent(getApplicationContext(), PollActivity.class);
                        i.putExtra("poll", "Web");
                        i.putExtra("Notification", Url);
                        startActivity(i);

                    }
                    // if url contains app url
                    else if (UrlType.contains("App"))
                    {
                        Uri mUr = Uri.parse("content://com.enavamaratha.enavamaratha.provider.FeedData/all_entries");
                        if (Url != null)
                        {
                            int _idd;
                            long entryidd;
                            long entryy = 0;
                            String re;

                            String[] cols = new String[]{FeedData.EntryColumns._ID, FeedData.EntryColumns.FEED_ID, FeedData.EntryColumns.GUID};
                            String filter = FeedData.EntryColumns.GUID + "='" + Url + "'";
                            Cursor cursor = datab.query(FeedData.EntryColumns.TABLE_NAME, cols, filter, null, null, null, null, null);

                            // check url is exist in db
                            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst())
                            {
                                do {
                                    _idd = cursor.getColumnIndex(FeedData.EntryColumns._ID);
                                    re = cursor.getString(_idd);
                                    //convert string to long because our id is in long
                                    entryidd = Long.parseLong(re);
                                    entryy = entryidd;

                                } while (cursor.moveToNext());

                                startActivity(new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(mUr, entryy)));
                            }


                            else
                            {
                                // make clickable false
                                if (listView.getChildAt(position).isEnabled())
                                {
                                    listView.getChildAt(position).setEnabled(false);
                                }
                            }


                        }
                    }

                    // If Notification Urltype is Epaper it means on click That Download/View That Date ePaper

                    else if (UrlType.contains("ePaper")) {


                        if (Url != null) {

                            GetePaperUrlDateFormat getDate = new GetePaperUrlDateFormat();

                            // For ePaper Pdf files Activity
                            Intent i = new Intent(GcmNotification.this, EpaperPdfActivity.class);
                            i.putExtra("date", getDate.ePaperPdfUrl(Url));
                            i.putExtra("pdf", getDate.ePaperPdfName(Url));
                            startActivity(i);


                        } else {
                            // make clickable false
                            if (listView.getChildAt(position).isEnabled()) {
                                listView.getChildAt(position).setEnabled(false);
                            }
                        }


                    }
                    // not web and not app then listview clickable make false
                    else {

                        if (listView.getChildAt(position).isEnabled())
                        {
                            listView.getChildAt(position).setEnabled(false);
                        }

                    }

                }
            }
        });





        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GcmNotification.this);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete?");

                final Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                final long ID = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int ii) {

                        // Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                        dbManager.delete(ID);
                        cursor.requery();
                        mAdapter.notifyDataSetChanged();


                      /*  mAdapter.swapCursor(cursor);
                        listView.setAdapter(mAdapter);*/

                        //  adapter.notifyDataSetChanged();
                        // cursor.close();

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int ii) {
                                dialog.dismiss();
                            }
                        }
                );
                builder.show();
                //  cursor.close();
                cursor.requery();

                return true;
            }
        });



    }


    private void initView() {

        context = getApplicationContext();


        dbManager = new DBManager(this);
        dbManager.open();
        cursor = dbManager.fetch();


        // for get data from FeedEx.Db
        DBHelper = new com.enavamaratha.enavamaratha.provider.DatabaseHelper(new Handler(), getApplicationContext());
        datab = DBHelper.getWritableDatabase();

        // get Data from notification
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();


        listView = (ListView) findViewById(R.id.list_view);
        listView.setEmptyView(findViewById(R.id.empty));


        mAdapter = new NotificationAdapter(context, cursor);
        listView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

       /* adapter = new SimpleCursorAdapter(this, R.layout.activity_gcmnotification, cursor, from, to, 0);
        adapter.notifyDataSetChanged();

        listView.setAdapter(adapter);*/
        urltype = getIntent().getStringExtra("UrlType");
        url = getIntent().getStringExtra("url");


    }


    @Override
    protected void onResume() {

        super.onResume();


    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {

        super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // if (id == R.id.action_settings) {
        //   return true;
        // }
        switch (id) {
            case R.id.all:
                deleteall();
                break;

            case R.id.menu_hommee:
                Intent intee = new Intent(GcmNotification.this,HomeActivity.class);
                intee.putExtra("home","home");
                startActivity(intee);
                return true;


            case android.R.id.home:

                Intent intt = new Intent(GcmNotification.this,HomeActivity.class);
                intt.putExtra("home","home");
                startActivity(intt);
                    finish();
                   return true;
               /* finish();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }


    private void deleteall() {
        // AlertDialog.Builder builder = new AlertDialog.Builder(this);

        Cursor c = database.rawQuery(" select * from " + DatabaseHelper.TABLE_NAME, null);

        if (c != null && c.getCount() > 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete All");
            builder.setMessage("Are you sure you want to Delete All Notification?");

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing but close the dialog

                    dbManager.deleteall();
                    listView.setAdapter(null);
                    mAdapter.notifyDataSetChanged();
                    //   adapter.notifyDataSetChanged();
                    Toast.makeText(GcmNotification.this, "All notifications deleted", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }

            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();



        } else
        {
            Toast.makeText(this, "No Notification", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onBackPressed() {


        //  super.onBackPressed();

        //super.onBackPressed();
      /*  Intent intt = new Intent(GcmNotification.this,HomeActivity.class);
        intt.putExtra("home","home");
        startActivity(intt);
        finish();*/

    }
}