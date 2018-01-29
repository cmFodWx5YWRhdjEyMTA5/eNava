
package  com.enavamaratha.enavamaratha.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enavamaratha.enavamaratha.service.ConnectionDetector;
import com.enavamaratha.enavamaratha.utils.DeleteFeeds;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.melnykov.fab.FloatingActionButton;

import  com.enavamaratha.enavamaratha.Constants;
import  com.enavamaratha.enavamaratha.R;
import  com.enavamaratha.enavamaratha.adapter.DrawerAdapter;
import  com.enavamaratha.enavamaratha.fragment.EntriesListFragment;
import  com.enavamaratha.enavamaratha.provider.FeedData;
import  com.enavamaratha.enavamaratha.provider.FeedData.EntryColumns;
import  com.enavamaratha.enavamaratha.provider.FeedData.FeedColumns;
import  com.enavamaratha.enavamaratha.service.FetcherService;
import  com.enavamaratha.enavamaratha.service.RefreshService;
import  com.enavamaratha.enavamaratha.utils.PrefUtils;
import  com.enavamaratha.enavamaratha.utils.UiUtils;
import com.enavamaratha.enavamaratha.provider.DatabaseHelper;

import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.APP_DELETE_FEEEDS_URL;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.protocol.BasicHttpContext;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.util.EntityUtils;


public class HomeActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, DatePickerDialog.OnDateSetListener {

    private static final String STATE_CURRENT_DRAWER_POS = "STATE_CURRENT_DRAWER_POS";

    private static final String FEED_UNREAD_NUMBER = "(SELECT " + Constants.DB_COUNT + " FROM " + EntryColumns.TABLE_NAME + " WHERE " +
            EntryColumns.IS_READ + " IS NULL AND " + EntryColumns.FEED_ID + '=' + FeedColumns.TABLE_NAME + '.' + FeedColumns._ID + ')';

    private static final String WHERE_UNREAD_ONLY = "(SELECT " + Constants.DB_COUNT + " FROM " + EntryColumns.TABLE_NAME + " WHERE " +
            EntryColumns.IS_READ + " IS NULL AND " + EntryColumns.FEED_ID + "=" + FeedColumns.TABLE_NAME + '.' + FeedColumns._ID + ") > 0" +
            " OR (" + FeedColumns.IS_GROUP + "=1 AND (SELECT " + Constants.DB_COUNT + " FROM " + FeedData.ENTRIES_TABLE_WITH_FEED_INFO +
            " WHERE " + EntryColumns.IS_READ + " IS NULL AND " + FeedColumns.GROUP_ID + '=' + FeedColumns.TABLE_NAME + '.' + FeedColumns._ID +
            ") > 0)";

    private static final int LOADER_ID = 0;
    private static final int SEARCH_DRAWER_POSITION = -1;

    private EntriesListFragment mEntriesFragment;
    private DrawerLayout mDrawerLayout;
    private View mLeftDrawer;
    private ListView mDrawerList;

    private DrawerAdapter mDrawerAdapter;
    private String inte;
    private ActionBarDrawerToggle mDrawerToggle;
    private FloatingActionButton mDrawerHideReadButton;
    private final SharedPreferences.OnSharedPreferenceChangeListener mShowReadListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (PrefUtils.SHOW_READ.equals(key)) {
                getLoaderManager().restartLoader(LOADER_ID, null, HomeActivity.this);

                if (mDrawerHideReadButton != null) {
                    UiUtils.updateHideReadButton(mDrawerHideReadButton);
                }
            }
        }
    };
    private CharSequence mTitle;
    private BitmapDrawable mIcon;
    private int mCurrentDrawerPos;
    ConnectionDetector cd;
    DatabaseHelper dbb;
    private SQLiteDatabase database;
    int id;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private SQLiteDatabase db;
    //AdView mAdView;

    private static boolean Delete_flag = true;
    String devid, mButtonlanding_selected;
    ImageView imgAdLeft, imgAdRight;
    private int doubleBackCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initView();

        if (ConnectionDetector.isConnectingToInternet(getApplicationContext())) {


            imgAdLeft.setVisibility(View.VISIBLE);
            imgAdRight.setVisibility(View.VISIBLE);

            showSmallAds();


        } else {
            imgAdLeft.setVisibility(View.GONE);
            imgAdRight.setVisibility(View.GONE);

        }






        // Mutiple Runtime Permission
        // using Gradle/library for Multiple Runtime permission
        // ---- https://github.com/Karumi/Dexter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            showMultiplePermissionView();

        }




        if (cd.isConnectingToInternet(getApplicationContext()))
        {

            //call delete method only once when open home activity
            if (Delete_flag) {

                new CheckDeleteTask().execute();
                Delete_flag = false;

            }
        }




        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if (position == SEARCH_DRAWER_POSITION) {
                    selectDrawerItem(SEARCH_DRAWER_POSITION);
                }

                if (position == 0) {


                    deleteOldePaeprPdfs();

                    showDatePicker();

                   /* // OLD Date Picker CODE
                    // Show Date Picker
                    DialogFragment dFragment = new DatePickerFragment();

                    // Show the date picker dialog fragment
                    dFragment.show(getFragmentManager(), "Date Picker");
*/


                    // Epaper Delete Cache Logic for epaper images cache
                    // If Days Diff greater than 7 days then delete all cache

/*
                    // current date
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    String currentDateandTime = sdf.format(new Date());
                    long diffDays;

                    Cursor c = db.rawQuery("select * from cache where id = 1 ", null);

                    // if value is already in database then update
                    if (c != null && c.getCount() > 0) {
                        final String ROWID = "id";
                        c.moveToFirst();
                        //PID Found
                        int _id = c.getInt(c.getColumnIndex("id"));
                        String gatedate = c.getString(c.getColumnIndex("time"));

                        Date d1 = null;
                        Date d2 = null;
                        String mytable1 = "mytable1";

                        try {

                            d1 = sdf.parse(gatedate);


                            d2 = sdf.parse(currentDateandTime);

                            //in milliseconds
                            long diff = d2.getTime() - d1.getTime();

                            diffDays = diff / (24 * 60 * 60 * 1000);


                            // if diiference is greater than 7
                            if (diffDays >= 7 && (isTableExists(mytable1)) == true) {
                                // delete mytable1
                                db.execSQL("delete from mytable1");
                               // clear directory of cache
                                clear();

                                // update date field in cache table
                                ContentValues args = new ContentValues();
                                args.put("time", currentDateandTime);
                                int updatev = db.update("cache", args, ROWID + "=" + _id, null);

                                DialogFragment dFragment = new DatePickerFragment();
                                // Show the date picker dialog fragment
                                dFragment.show(getFragmentManager(), "Date Picker");

                                // call clear method


                            } else {
                                DialogFragment dFragment = new DatePickerFragment();

                                // Show the date picker dialog fragment
                                dFragment.show(getFragmentManager(), "Date Picker");
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }

                    c.close();*/

                }

                if (position == 1) {

                    selectDrawerItem(1);

                }

                if (position == 2) {
                    selectDrawerItem(2);
                }

                if (position == 3) {
                    selectDrawerItem(3);
                }

                if (position == 4) {

                    selectDrawerItem(4);
                }

                if (position == 5) {
                    selectDrawerItem(5);
                }

                if (position == 6) {
                    selectDrawerItem(6);
                }
                if (position == 7) {
                    selectDrawerItem(7);
                }

                if (position == 8) {
                    selectDrawerItem(8);
                }
                if (position == 9) {
                    selectDrawerItem(9);
                }
                if (position == 10) {
                    selectDrawerItem(10);
                }
                if (position == 11) {
                    selectDrawerItem(11);
                }

                if (position == 12) {
                    selectDrawerItem(12);
                }
                if (position == 13) {
                    selectDrawerItem(13);
                }

                if (position == 14) {
                    selectDrawerItem(14);
                }
                if (position == 15) {
                    selectDrawerItem(15);
                }

                if (position == 16) {

                    selectDrawerItem(16);

                }

                if (position == 17) {
                    selectDrawerItem(17);
                }

                if (position == 18) {
                    selectDrawerItem(18);
                }

                if (position == 19) {

                    if (cd.isConnectingToInternet(getApplicationContext())) {

                        Intent i = new Intent(getApplicationContext(), PollActivity.class);
                        i.putExtra("poll", "poll");
                        startActivity(i);

                    } else {
                        showAlertDialog(HomeActivity.this, getResources().getString(R.string.no_internet), getResources().getString(R.string.no_internet_msg), false);


                    }
                }


                if (position == 20) {
                    if (cd.isConnectingToInternet(getApplicationContext())) {

                        Intent i = new Intent(getApplicationContext(), PollActivity.class);
                        i.putExtra("poll", "quiz");
                        startActivity(i);

                    } else {
                        showAlertDialog(HomeActivity.this, getResources().getString(R.string.no_internet), getResources().getString(R.string.no_internet_msg), false);

                    }
                }


                if (position == 21) {
                    if (cd.isConnectingToInternet(getApplicationContext())) {

                        Intent i = new Intent(getApplicationContext(), Game.class);

                        startActivity(i);

                    } else {
                        Intent i = new Intent(getApplicationContext(), Game.class);

                        startActivity(i);


                    }

                }
                if (position == 22) {
                    Intent intee = new Intent(getApplicationContext(), GcmNotification.class);
                    intee.putExtra("url", "");
                    startActivity(intee);
                }

                if (position == 23) {

                    Intent i = new Intent(getApplicationContext(), Expandable.class);
                    i.putExtra("contact", "emergency");
                    startActivity(i);
                }

                if (position == 24) {
                    Intent i = new Intent(getApplicationContext(), Feedback.class);
                    startActivity(i);
                }

                if (position == 25) {
                    Intent i = new Intent(getApplicationContext(), Expandable.class);
                    i.putExtra("contact", "contact");
                    startActivity(i);
                }

                if (position == 26) {
                    Intent i = new Intent(getApplicationContext(), AboutActivity.class);
                    i.putExtra("intent", "ahmednagar");
                    startActivity(i);
                }

                if (position == 27) {
                    Intent i = new Intent(getApplicationContext(), GeneralPrefsActivity.class);
                    startActivity(i);
                }


                if (position == 28) {
                    if (cd.isConnectingToInternet(getApplicationContext())) {
                        launchmarket();
                    } else {
                        showAlertDialog(HomeActivity.this, getResources().getString(R.string.no_internet), getResources().getString(R.string.no_internet_msg), false);
                    }
                }

                if (position == 29) {
                    Intent shareIntent =
                            new Intent(android.content.Intent.ACTION_SEND);

                    //set the type
                    shareIntent.setType("text/plain");

                    //add a subject
                    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                            "NavaMaratha App");

                    //build the body of the message to be shared
                    String shareMessage = "Track Ahmednagar Local News with NavaMaratha App. Click this link to Download & Install Android App" +
                            "\n" + "https://goo.gl/sZE6kz";

                    //add the message
                    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                            shareMessage);

                    //start the chooser for sharing
                    startActivity(Intent.createChooser(shareIntent,
                            "Share Application Via"));
                }

                if (mDrawerLayout != null) {
                    mDrawerLayout.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    mDrawerLayout.closeDrawer(mLeftDrawer);
                                }
                            }, 50);
                }
            }
        });


        mLeftDrawer.setBackgroundColor((ContextCompat.getColor(getApplicationContext(), R.color.light_primary_color)));

        mDrawerList.setBackgroundColor((ContextCompat.getColor(getApplicationContext(), R.color.light_background)));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    super.onDrawerSlide(drawerView, 0);
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            if (PrefUtils.getBoolean(PrefUtils.LEFT_PANEL, false)) {
                mDrawerLayout.openDrawer(mLeftDrawer);
            }
        }


        if (savedInstanceState != null) {
            mCurrentDrawerPos = savedInstanceState.getInt(STATE_CURRENT_DRAWER_POS);
        }

        getLoaderManager().initLoader(LOADER_ID, null, this);

        if (PrefUtils.getBoolean(PrefUtils.REFRESH_ENABLED, true)) {
            // starts the service independent to this activity
            startService(new Intent(this, RefreshService.class));
        } else {
            stopService(new Intent(this, RefreshService.class));
        }
        if (PrefUtils.getBoolean(PrefUtils.REFRESH_ON_OPEN_ENABLED, false)) {
            if (!PrefUtils.getBoolean(PrefUtils.IS_REFRESHING, false)) {
                startService(new Intent(HomeActivity.this, FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS));
            }
        }
    }


    public void open() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }


    private void initView() {

        String Url = getIntent().getStringExtra("url");


        devid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Small Advertise Code

        imgAdLeft = (ImageView) findViewById(R.id.img_ad_Left);
        imgAdRight = (ImageView) findViewById(R.id.img_ad_Right);


        //  db = openOrCreateDatabase("MyMb", MODE_PRIVATE, null);
        // String simple=getIntent().getStringExtra("home");


        // getting value of Intent from FetcherService.java class
        // when notification comes from update of news and click of notification then goto that news page
        Bundle b = getIntent().getExtras();
        String simple = b.getString("home");

        if (simple != null) {
            selectDrawerItem(3);
        }


        // Landing page selected button goto that news
        mButtonlanding_selected = b.getString("land");
        if (mButtonlanding_selected != null) {
            // Log.e("HOME", "initView: Landing Button Value  ----------"+mButtonlanding_selected);
            // if epeper

            if (mButtonlanding_selected.equals("0")) {
                deleteOldePaeprPdfs();
                showDatePicker();

            } else {
                selectDrawerItem(Integer.parseInt(mButtonlanding_selected));
            }


        }


        int entryid = b.getInt("entryid");
        // Uri -  content://com.enavamaratha.enavamaratha.provider.FeedData/feeds/1/entries
        // Id : 1,2,3,...
        // Feed_id is 1 because we just want to show first feeds i.e thalak batmya
        Uri mUri = Uri.parse("content://com.enavamaratha.enavamaratha.provider.FeedData/feeds/1/entries");
        long l;
        if (entryid != 0) {

            // coverting int to long for
            l = (long) entryid;

            // goto particular news when click on news intent
            startActivity(new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(mUri, l)));

        }


        // getting database for entryid
        dbb = new DatabaseHelper(new Handler(), getApplicationContext());
        database = dbb.getWritableDatabase();


        // getting notification from gcm(.net side) of news then goto that news on click of that notification intent


        Uri mUr = Uri.parse("content://com.enavamaratha.enavamaratha.provider.FeedData/all_entries");
        if (Url != null) {
            int _idd;
            long entryidd;
            long entryy = 0;
            String re;

            String[] cols = new String[]{FeedData.EntryColumns._ID, FeedData.EntryColumns.FEED_ID, FeedData.EntryColumns.GUID};
            String filter = FeedData.EntryColumns.GUID + "='" + Url + "'";
            Cursor cursor = database.query(FeedData.EntryColumns.TABLE_NAME, cols, filter, null, null, null, null, null);

            // check url is exist in db
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    _idd = cursor.getColumnIndex(FeedData.EntryColumns._ID);
                    re = cursor.getString(_idd);
                    //convert string to long because our id is in long
                    entryidd = Long.parseLong(re);
                    entryy = entryidd;


                } while (cursor.moveToNext());


                startActivity(new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(mUr, entryy)));
                cursor.close();
                database.close();
            }


            // else go to home activity
            else {
                Intent i = new Intent(HomeActivity.this, GcmNotification.class);
//                i.putExtra("home","home");
                startActivity(i);
                finish();
            }


        }


        mEntriesFragment = (EntriesListFragment) getFragmentManager().findFragmentById(R.id.entries_list_fragment);

        mTitle = getTitle();

        inte = getIntent().getStringExtra("1");

        mLeftDrawer = findViewById(R.id.left_drawer);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);


        mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


    }


    // show Small advertise on Toolbar
    private void showSmallAds() {


        Picasso.with(this)
                .load("http://paper.enavamaratha.com//images/Advt/left.png")
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(imgAdLeft, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        // If Image have Error then make image not visible/gone
                        imgAdLeft.setVisibility(View.GONE);

                    }
                });


        Picasso.with(this)
                .load("http://paper.enavamaratha.com//images/Advt/right.png")
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imgAdRight, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        // If Image have Error then make image not visible/gone

                        imgAdRight.setVisibility(View.GONE);

                    }
                });


        imgAdLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBigAdvertise("http://paper.enavamaratha.com//images/Advt/leftbig.png");
            }
        });


        imgAdRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openBigAdvertise("http://paper.enavamaratha.com//images/Advt/rightbig.png");

            }
        });


    }


    private void showMultiplePermissionView() {

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {


                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                        token.continuePermissionRequest();


                    }
                }).check();
    }


    private void deleteOldePaeprPdfs() {


        // Delete Pdf files only save top 5 pdf files by files date

        String path = Environment.getExternalStorageDirectory() + "/" + "NavaMaratha/";

        File directory = new File(path);

        if (directory.exists()) {

            try {


                if (directory.listFiles() != null) {


                    File[] files = directory.listFiles();

                    // Save only top 5 (By Date of file modified date) files
                    if (files.length > 5) {

                        // Sorting Array By files's last Modified Date and Get files array by Descending Date
                        Arrays.sort(files, new Comparator<File>() {
                            public int compare(File f1, File f2) {
                                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                            }
                        });


                        // If Sorted Files Array size is grater than 5 then
                        // Save top 5 pdf files(By Last Modified date of File) and delete all pdf files
                        for (int i = 5; i < files.length; i++) {

                            // Delete Pdf files one by one
                            files[i].delete();

                        }


                    }
                } else {
                    //
                }
            } catch (Exception e) {

            }
        }


    }







    private void launchmarket() {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
        }
    }

    public void showAlertDialog(final Context context, String title, String message, Boolean status) {
        // material dialog
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(HomeActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);

        android.support.v7.app.AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_DRAWER_POS, mCurrentDrawerPos);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onResume() {

        super.onResume();

        PrefUtils.registerOnPrefChangeListener(mShowReadListener);


    }

    @Override
    protected void onPause() {

        PrefUtils.unregisterOnPrefChangeListener(mShowReadListener);


        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // We reset the current drawer position
        // Comment Code for Landing Page
        selectDrawerItem(3);

        setIntent(intent);

        // Uri : content://com.enavamaratha.enavamaratha.provider.FeedData/feeds/1/entries
        // Id : 1 , 2,3
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

        }

        return (super.onOptionsItemSelected(item));
    }


    public void onClickHideRead(View view) {
        if (!PrefUtils.getBoolean(PrefUtils.SHOW_READ, true)) {
            PrefUtils.putBoolean(PrefUtils.SHOW_READ, true);
        } else {
            PrefUtils.putBoolean(PrefUtils.SHOW_READ, false);
        }
    }


    public void onClickSearch(View view) {
        selectDrawerItem(SEARCH_DRAWER_POSITION);
        if (mDrawerLayout != null) {
            mDrawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawer(mLeftDrawer);
                }
            }, 50);
        }
    }


    public void onClickHome(View view) {
        Intent i = new Intent(HomeActivity.this, HomeActivity.class);
        i.putExtra("home", "home");
        startActivity(i);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader cursorLoader = new CursorLoader(this, FeedColumns.GROUPED_FEEDS_CONTENT_URI, new String[]{FeedColumns._ID, FeedColumns.URL, FeedColumns.NAME,
                FeedColumns.IS_GROUP, FeedColumns.ICON, FeedColumns.LAST_UPDATE, FeedColumns.ERROR, FEED_UNREAD_NUMBER},
                PrefUtils.getBoolean(PrefUtils.SHOW_READ, true) ? "" : WHERE_UNREAD_ONLY, null, null
        );
        cursorLoader.setUpdateThrottle(Constants.UPDATE_THROTTLE_DELAY);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (mDrawerAdapter != null) {
            mDrawerAdapter.setCursor(cursor);
        } else {
            mDrawerAdapter = new DrawerAdapter(this, cursor);
            mDrawerList.post(new Runnable() {
                public void run() {

                    mDrawerList.setAdapter(mDrawerAdapter);
                    // Comment Code for Landing Page
                    //selectDrawerItem(3);

                    if (mButtonlanding_selected != null) {

                        selectDrawerItem(Integer.parseInt(mButtonlanding_selected));
                    } else {
                        selectDrawerItem(3);
                    }

                    //  Log.e("Home", "run:----------- "+mButtonlanding_selected);



                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (mDrawerAdapter == null)
            return;

        mDrawerAdapter.setCursor(null);
    }

    public void selectDrawerItem(int position) {
        mCurrentDrawerPos = position;

        if (mDrawerAdapter == null)
            return;

        mDrawerAdapter.setSelectedItem(position);
        mIcon = null;

        Uri newUri = null;
        boolean showFeedInfo = true;

        doubleBackCount = 0;
        switch (position) {
            case SEARCH_DRAWER_POSITION:
                newUri = EntryColumns.SEARCH_URI(mEntriesFragment.getCurrentSearch());
                break;
            case 0:
                newUri = EntryColumns.ALL_ENTRIES_CONTENT_URI;
                break;

            case 1:
                newUri = EntryColumns.ALL_ENTRIES_CONTENT_URI;
                break;


            case 2:
                newUri = EntryColumns.FAVORITES_CONTENT_URI;
                break;


            case 3:

                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(1);
                showFeedInfo = false;
                break;

            case 4:

                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(2);
                showFeedInfo = false;
                break;

            case 5:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(3);
                showFeedInfo = false;
                break;

            case 6:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(4);
                showFeedInfo = false;
                break;

            case 7:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(5);
                showFeedInfo = false;
                break;


            case 8:

                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(6);
                showFeedInfo = false;
                break;

            case 9:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(7);
                showFeedInfo = false;
                break;


            case 10:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(8);
                showFeedInfo = false;
                break;

            case 11:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(9);
                showFeedInfo = false;
                break;

            case 12:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(10);
                showFeedInfo = false;
                break;

            case 13:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(11);
                showFeedInfo = false;
                break;

            case 14:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(12);
                showFeedInfo = false;
                break;

            case 15:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(13);
                showFeedInfo = false;
                break;

            case 16:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(14);
                showFeedInfo = false;
                break;


            case 17:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(15);
                showFeedInfo = false;
                break;


            case 18:
                newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(16);
                showFeedInfo = false;
                break;

        }

        if (!newUri.equals(mEntriesFragment.getUri())) {
            mEntriesFragment.setData(newUri, showFeedInfo);
        }

        mDrawerList.setItemChecked(position, true);

        // First open => we open the drawer for you
        if (PrefUtils.getBoolean(PrefUtils.FIRST_OPEN, true)) {


            // On first Open Refresh Feeds Automatically

            startService(new Intent(HomeActivity.this, FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS));


            PrefUtils.putBoolean(PrefUtils.FIRST_OPEN, false);
            if (mDrawerLayout != null) {
                mDrawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.openDrawer(mLeftDrawer);
                    }
                }, 500);
            }
        }
        refreshTitle(0);
    }


    public void refreshTitle(int mNewEntriesNumber) {
        switch (mCurrentDrawerPos) {
            case SEARCH_DRAWER_POSITION:
                // Not to show search icon and search name in toolbar
                // Change from 19/01/2018
                getSupportActionBar().setTitle("");
                getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

             /*   getSupportActionBar().setTitle(android.R.string.search_go);
                getSupportActionBar().setIcon(R.drawable.ic_search);*/
                break;
            case 0:
                getSupportActionBar().setTitle(R.string.epaper);
                getSupportActionBar().setIcon(R.drawable.epaper);
                break;
            case 1:
                getSupportActionBar().setTitle(R.string.all);
                getSupportActionBar().setIcon(R.drawable.menu_home);
                break;


            case 2:
                getSupportActionBar().setTitle(R.string.favorites);
                getSupportActionBar().setIcon(R.drawable.favnews);
                break;


            case 3:
                getSupportActionBar().setTitle(R.string.latestnews);
                getSupportActionBar().setIcon(R.drawable.impnews);
                break;


            case 4:
                getSupportActionBar().setTitle(R.string.economics);
                getSupportActionBar().setIcon(R.drawable.economics);

                break;

            case 5:
                getSupportActionBar().setTitle(R.string.health);
                getSupportActionBar().setIcon(R.drawable.health);

                break;

            case 6:

                getSupportActionBar().setTitle(R.string.science);
                getSupportActionBar().setIcon(R.drawable.science);
                break;

            case 7:
                getSupportActionBar().setTitle(R.string.enter);
                getSupportActionBar().setIcon(R.drawable.entertain);
                break;

            case 8:
                getSupportActionBar().setTitle(R.string.religious);
                getSupportActionBar().setIcon(R.drawable.religious);
                break;

            case 9:
                getSupportActionBar().setTitle(R.string.sign);
                getSupportActionBar().setIcon(R.drawable.astrology);
                break;


            case 10:
                getSupportActionBar().setTitle(R.string.interview);
                getSupportActionBar().setIcon(R.drawable.interview);
                break;

            case 11:
                getSupportActionBar().setTitle(R.string.travel);
                getSupportActionBar().setIcon(R.drawable.travel);
                break;

            case 12:
                getSupportActionBar().setTitle(R.string.build);
                getSupportActionBar().setIcon(R.drawable.home);
                break;

            case 13:
                getSupportActionBar().setTitle(R.string.pakk);
                getSupportActionBar().setIcon(R.drawable.meal);
                break;


            case 14:
                getSupportActionBar().setTitle(R.string.child);
                getSupportActionBar().setIcon(R.drawable.child);
                break;

            case 15:
                getSupportActionBar().setTitle(R.string.gk);
                getSupportActionBar().setIcon(R.drawable.gekk);
                break;

            case 16:
                getSupportActionBar().setTitle(R.string.thought);
                getSupportActionBar().setIcon(R.drawable.thoughts);
                break;

            case 17:
                getSupportActionBar().setTitle(R.string.jobs);
                getSupportActionBar().setIcon(R.drawable.jobs);
                break;

            case 18:
                getSupportActionBar().setTitle(R.string.property);
                getSupportActionBar().setIcon(R.drawable.property);
                break;


        }
        if (mNewEntriesNumber != 0) {
            getSupportActionBar().setTitle(getSupportActionBar().getTitle().toString() + " (" + String.valueOf(mNewEntriesNumber) + ")");
        }
        invalidateOptionsMenu();
    }


    private void clear() {
        // TODO Auto-generated method stub
        File cache = new File(getApplicationContext().getFilesDir(), "/Epaper/");
        if (cache.exists() && cache.isDirectory()) {


            deleteDir(cache);


        }


    }

    private boolean deleteDir(File dir) {
        // TODO Auto-generated method stub
        {
            if (dir.isDirectory()) {
                // last modified date

                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));


                    if (!success) {
                        return false;
                    }
                }
            }
            // The directory is now empty so delete it
            return dir.delete();
        }


    }

   /* public boolean isTableExists(String tableName) {


        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        db.close();
        return false;
    }
*/




/*
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);

            final DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                    AlertDialog.THEME_HOLO_LIGHT, this, year, month, day);


            // set max date as tommorow date
            calendar.add(Calendar.DATE, 1);
            // Set the Calendar new date(current date) as maximum date of date picker
            //   dpd.getDatePicker().setMaxDate(new Date().getTime());
            dpd.getDatePicker().setMaxDate(calendar.getTimeInMillis());

            // Subtract 365 days from Calendar updated date
            calendar.add(Calendar.DATE, -365);

            // Set the Calendar new date as minimum date of date picker
            dpd.getDatePicker().setMinDate(calendar.getTimeInMillis());

            dpd.setTitle("Select Date");
            dpd.setCancelable(true);
            dpd.setCanceledOnTouchOutside(true);
            dpd.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            long date2 = dpd.getDatePicker().getCalendarView().getDate();

                            // For ePaper Url we required this date format
                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                            String formattedDate = dateFormat.format(date2);


                            // For Pdf Name we required this format
                            DateFormat dateFormat1= new SimpleDateFormat("dd_MM_yyyy");
                            String formattedDate1 = dateFormat1.format(date2);


                            // For ePaper Pdf files Activity
                            Intent i = new Intent(getActivity(), EpaperPdfActivity.class);
                            i.putExtra("date", formattedDate);
                            i.putExtra("pdf",formattedDate1);
                            startActivity(i);


                            // For ePaper Images Activity
                           *//* Intent i = new Intent(getActivity(), Epaper.class);
                            i.putExtra("date", formattedDate);
                            startActivity(i);*//*

                        }
                    });
            dpd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                        }
                    });

            // So, now date picker selectable date range is 7 days only

            // Return the DatePickerDialog
            return dpd;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the chosen date


            // Create a Date variable/object with user chosen date
            Calendar cal = Calendar.getInstance();


        }

    }*/


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub


        backCalling();

/*

        if (doubleBackCount == 1)
        {
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        }


        if(doubleBackCount == 2)
        {
            Log.e("HOME ", "onBackPressed: double Back--------- "+doubleBackCount);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackCount =0;
                }
            }, 2000);

            super.onBackPressed();
                return;

        }


        mDrawerLayout.openDrawer(mLeftDrawer);

        doubleBackCount++;
*/










      /*  android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure want to exit?");

        // New Dialouge
        // Interchange ok and home button // 12 th jan 2018


        String neutralText = getString(android.R.string.ok);
        String positiveText = "Home";
        String negativeText = getString(android.R.string.cancel);

        // Onclick Home open Navigation Drawer
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mDrawerLayout.openDrawer(mLeftDrawer);

            }
        });



        // OnClick Ok Finish All Activity's
        builder.setNeutralButton(neutralText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }
        });



        // OnClick Cancel Do Nothing

        builder.setNegativeButton(negativeText,null);

        android.support.v7.app.AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
*/

    }


    public void backCalling() {
        super.onBackPressed();
/*
        if (doubleBackCount == 1)
        {
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        }


        if(doubleBackCount == 2)
        {
            Log.e("HOME ", "onBackPressed: double Back--------- "+doubleBackCount);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackCount =0;
                }
            }, 2000);

            super.onBackPressed();
            return;

        }


        mDrawerLayout.openDrawer(mLeftDrawer);

        doubleBackCount++;*/
    }


    // get deleted and trash posts post id from server

    private class CheckDeleteTask extends AsyncTask<Void, Void, ArrayList<String>> {
        Exception error;
        boolean flag = false;
        DeleteFeeds deleteFeeds = new DeleteFeeds();

        // get all post id from our sqlite database;
        ArrayList<String> mGetGuid = deleteFeeds.getPostId();

        ArrayList<String> mConvertedArray = null;


        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            try {
                if (mGetGuid.size() > 0) {
                    // convert arraylist to json array
                    JSONArray mGuidJsonArray = new JSONArray(mGetGuid);

                    JSONObject mJsonObj = new JSONObject();

                    try {
                        mJsonObj.put("jsonarray", mGuidJsonArray);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //   Log.i(TAG,"JsonObject To String  : "+mJsonObj.toString());


                    // http request
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpContext httpContext = new BasicHttpContext();
                    HttpPost httpPost = new HttpPost(APP_DELETE_FEEEDS_URL);

                    try {

                        // sending json Array to Server
                        StringEntity se = new StringEntity(mJsonObj.toString());

                        httpPost.setEntity(se);
                        httpPost.setHeader("Accept", "application/json");
                        httpPost.setHeader("Content-type", "application/json");


                        HttpResponse response = httpClient.execute(httpPost, httpContext); //execute your request and parse response
                        HttpEntity entity = response.getEntity();

                        String jsonString = EntityUtils.toString(entity); //if response in JSON format
                        //Log.i("Home Activity", "Json Response " + jsonString);
                        // convert json Array to string array

                        mConvertedArray = deleteFeeds.ConvertJsonarray(jsonString);
                        if (mConvertedArray.size() > 0) {
                            flag = true;
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
                return mConvertedArray;
            } catch (Exception e) {
                error = e;
                flag = false;
                return null;

            }


        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
           // Log.i("HOME", " Flag Value For Delete : " + flag);
            if (flag) {

                deleteFeeds.DeleteFeed(mConvertedArray);
            }


        }
    }


    // Open Big Advertise Popup
    private void openBigAdvertise(String imageUrl) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(HomeActivity.this);


        LayoutInflater inflater = getLayoutInflater();

        View BigAdLayout = inflater.inflate(R.layout.layout_big_advertise, null);

        ImageView BigAdImage = (ImageView) BigAdLayout.findViewById(R.id.img_big_adv);
        ImageButton AdClose = (ImageButton) BigAdLayout.findViewById(R.id.img_big_adv_close);


        builder.setView(BigAdLayout);


        final android.support.v7.app.AlertDialog alD = builder.show();


        // Load Big Image with Url
        Picasso.with(this)
                .load(imageUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.loaderror)
                .into(BigAdImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        // OnSuccess Load Image

                    }

                    @Override
                    public void onError() {
                        // OnError Show Other Image/Url


                    }
                });


        // close Big Advertise
        AdClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                alD.dismiss();
            }
        });


    }


    private void showDatePicker() {


        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog dpd = DatePickerDialog.newInstance(HomeActivity.this, year, month, day);

        Calendar maxdate = Calendar.getInstance();

        // set max date as tommorow date
        maxdate.add(Calendar.DATE, 1);

        // Set the Calendar new date(current date) as maximum date of date picker
        dpd.setMaxDate(maxdate);


        Calendar mindate = Calendar.getInstance();


        // Subtract 365 days from Calendar updated date
        mindate.add(Calendar.DATE, -365);

        // Set the Calendar new date as minimum date of date picker
        dpd.setMinDate(mindate);


        dpd.setAccentColor(getResources().getColor(R.color.Indigo_800));
        // no Dark theme
        dpd.setThemeDark(false);


        dpd.setCancelable(true);

        dpd.show(getFragmentManager(), "Datepickerdialog");


    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {


        Calendar newDate = Calendar.getInstance();
        newDate.set(year, monthOfYear, dayOfMonth);


        // For ePaper Url we required this date format
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String formattedDate = dateFormat.format(newDate.getTime());

        // For Pdf Name we required this format
        DateFormat dateFormat1 = new SimpleDateFormat("dd_MM_yyyy");
        String formattedDate1 = dateFormat1.format(newDate.getTime());


        // For ePaper Pdf files Activity
        Intent i = new Intent(HomeActivity.this, EpaperPdfActivity.class);
        i.putExtra("date", formattedDate);
        i.putExtra("pdf", formattedDate1);
        startActivity(i);


    }






}



