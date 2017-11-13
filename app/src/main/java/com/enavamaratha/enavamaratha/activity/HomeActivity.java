
package  com.enavamaratha.enavamaratha.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enavamaratha.enavamaratha.service.ConnectionDetector;
import com.enavamaratha.enavamaratha.utils.DeleteFeeds;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

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
import java.util.Calendar;
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

public class HomeActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

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

    private boolean mCanQuit = false;
    ConnectionDetector cd;
    TextView txtmarquee;
    int titlepos, mFeedIdPos;
    ArrayList<String> arr, arre;
    DatabaseHelper dbb;
    private SQLiteDatabase database;
    int id;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private WebView wev;
    private SQLiteDatabase db;
    //AdView mAdView;

    private ImageView mImageAdvertise,mImageLeftAd,mImageRightAd;
    String[] imageUrl;
    private long FOOTER_DELAY;
    String footer_url;
    private static boolean Delete_flag = true;
    String devid,FooterAd,LeftAd,RightAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


         String Url = getIntent().getStringExtra("url");
        devid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        FooterAd= "A1";
        LeftAd = "A2";
        RightAd ="A3";
        mImageAdvertise = (ImageView) findViewById(R.id.imgAdvertise);
        mImageLeftAd = (ImageView) findViewById(R.id.imgLeftAd);
        mImageRightAd = (ImageView) findViewById(R.id.imgRighttAd);

        // Footer Advertise Code
        FOOTER_DELAY = 5000;
        footer_url = "https://dummyimage.com/320x50/5fada1/0011ff.jpg&text=SAMPLE+";





        /*
        *
        * Dexter.checkPermissions(new MultiplePermissionsListener() {
                    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                        List<String> grantedPermissions = new ArrayList<String>();
                        for(PermissionGrantedResponse response: report.getGrantedPermissionResponses()){
                            if(!grantedPermissions.contains(response.getPermissionName())){
                                grantedPermissions.add(response.getPermissionName());
                            }
                        }
                        Toast.makeText(getApplicationContext(), "Granted permissions:"+grantedPermissions.toString(), Toast.LENGTH_LONG).show();
                    }
                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }, Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.RECORD_AUDIO);
            }
        });
        * */


        // Mutiple Runtime Permission
        // using Gradle/library for Multiple Runtime permission
        // ---- https://github.com/Karumi/Dexter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


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


       /* Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.CALL_PHONE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {*//* ... *//*}
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {*//* ... *//*}
        }).check();*/



        if (cd.isConnectingToInternet(getApplicationContext()))
        {

           // new FooterAd(mImageAdvertise,devid,FooterAd).execute();
            //new LeftAd(mImageLeftAd,devid,LeftAd).execute();
            /// FINAL CODE
           /* Picasso.with(getApplicationContext())
                    .load(footer_url)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE).into(mImageAdvertise);

            Runnable runnable = new Runnable() {

                public void run() {
                    // mImageAdvertise.setImageResource(imageArray[i]);
                    footer_url = "https://dummyimage.com/320x50/5fada1/0011ff.jpg&text=POOJA+";
                    Picasso.with(getApplicationContext())
                            .load(footer_url)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .networkPolicy(NetworkPolicy.NO_CACHE).into(mImageAdvertise);
                }
                };


                mImageAdvertise.postDelayed(runnable, FOOTER_DELAY);*/

            /// END HERE



//            final Handler handler = new Handler();

            /*final Runnable runnable2 = new Runnable()
            {


                public void run()
                {

                  Log.i("HOME","Call To second Runnable");
                }


            };*/

                    //i++;
                /*if (i > imageUrl.length - 1)
                {
                    i = 0;
                }*/
                    //  handler.postDelayed(this, FOOTER_DELAY);

            //call delete method only once when open home activity
            if (Delete_flag) {
              //  Log.i("HOME", "DELETE FLAG : " + Delete_flag);
                new CheckDeleteTask().execute();
                Delete_flag = false;
              //  Log.i("Home", "Calling AsyncMethod");
            }
        }

        //Initialize the Google Mobile Ads SDK
        // MobileAds.initialize(getApplicationContext(), "ca-app-pub-4094279933655114~6258738984");

        /*mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("CE5BF23EF32893496DAAAEA8CBB1EB93")
                .build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
//                Toast.makeText(getApplicationContext(), "Ad is loaded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClosed() {
//                Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                mAdView.setVisibility(View.GONE);
//                Toast.makeText(getApplicationContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
//                Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
//                Toast.makeText(getApplicationContext(), "Ad is opened!", Toast.LENGTH_SHORT).show();
            }
        });*/

       /* // smal ads on left side
        RelativeLayout smallad=(RelativeLayout)findViewById(R.id.smallad_left);
        sAdview = new AdView(getApplicationContext());
        AdSize smallsize = new AdSize(50,50);
        sAdview.setAdSize(smallsize);
        sAdview.setAdUnitId("ca-app-pub-4094279933655114/3492658587");
        smallad.addView(sAdview);
        AdRequest adre=new AdRequest.Builder().build();
        sAdview.loadAd(adre);

        // small ads on right side
        RelativeLayout smallad_right=(RelativeLayout)findViewById(R.id.smallad_right);
        sAdview_right = new AdView(getApplicationContext());
        AdSize smalls = new AdSize(50,50);
        sAdview_right.setAdSize(smalls);
        sAdview_right.setAdUnitId("ca-app-pub-4094279933655114/2015925381");
        smallad_right.addView(sAdview_right);
        AdRequest adreq=new AdRequest.Builder().build();
        sAdview_right.loadAd(adreq);*/


        SharedPreferences prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        String eMailId = prefs.getString("eMailId", "");
        arre = new ArrayList<String>();

        db = openOrCreateDatabase("MyMb", MODE_PRIVATE, null);
        // String simple=getIntent().getStringExtra("home");


        // getting value of Intent from FetcherService.java class
        // when notification comes from update of news and click of notification then goto that news page
        Bundle b = getIntent().getExtras();
        String simple = b.getString("home");

        if (simple != null) {
            selectDrawerItem(3);
        }


        int entryid = b.getInt("entryid");
        // Uri -  content://com.enavamaratha.enavamaratha.provider.FeedData/feeds/1/entries
        // Id : 1,2,3,...
        // Feed_id is 1 because we just want to show first feeds i.e thalak batmya
        Uri mUri = Uri.parse("content://com.enavamaratha.enavamaratha.provider.FeedData/feeds/1/entries");
        long l;
        if (entryid != 0) {
            System.out.println("Inent Value in EntriesListFragemnt" + entryid);
            // coverting int to long for
            l = (long) entryid;
            System.out.println("Long value of Int" + l);
            // goto particular news when click on news intent
            startActivity(new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(mUri, l)));

        }


        // getting database for entryid
        dbb = new DatabaseHelper(new Handler(), getApplicationContext());
        database = dbb.getWritableDatabase();

        // getting notification from gcm(.net side) of news then goto that news o n clickof that notification intent


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
                    System.out.println("Id  in Country List" + re);
                    System.out.println("Long Id  in Country List" + entryidd);

                } while (cursor.moveToNext());

                startActivity(new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(mUr, entryy)));
            }

            // else go to home activity
            else {
                Intent i = new Intent(HomeActivity.this, GcmNotification.class);
//                i.putExtra("home","home");
                startActivity(i);
                finish();
            }


            cursor.close();
        }


        // Scroll News(MArquee Text)
        //txtmarquee=(TextView)findViewById(R.id.textView1);
      /*  int idfedd= 1;
        String limit="5"; // get top 5 thalak news
        String filter =EntryColumns.FEED_ID + "=" + Integer.toString(idfedd);
        String orderBy = EntryColumns.DATE + " DESC"; // order by desc
        String [] cols= new String[]{EntryColumns.FEED_ID,EntryColumns.TITLE};
        Cursor cursor = database.query(EntryColumns.TABLE_NAME,cols,filter,null,null,null,orderBy,limit);
       

        if(cursor != null && cursor.getCount() >0  && cursor.moveToFirst())
        {
            do {
                titlepos = cursor.getColumnIndex(EntryColumns.TITLE);
                String res = cursor.getString(titlepos);

                arre.add(res);

                System.out.println("Ranking the value" + res);

            }while (cursor.moveToNext());
        }


        txtmarquee.setText(" ");
        if (arre.size() > 0)
        {
            for (String str : arre)
            {
                txtmarquee.append(str+"  ");

            }
        }
        txtmarquee.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        txtmarquee.setSingleLine(true);
        txtmarquee.setMarqueeRepeatLimit(-1);
        txtmarquee.setSelected(true);


        cursor.close();
        database.close();


        txtmarquee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDrawerItem(3);

            }
        });*/


        if (!checkPlayServices()) {
            Toast.makeText(getApplicationContext(), "This device doesn't support Play services, App will not work normally", Toast.LENGTH_LONG).show();
        }

        mEntriesFragment = (EntriesListFragment) getFragmentManager().findFragmentById(R.id.entries_list_fragment);

        mTitle = getTitle();

        inte = getIntent().getStringExtra("1");

        mLeftDrawer = findViewById(R.id.left_drawer);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);


        mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if (position == SEARCH_DRAWER_POSITION) {
                    selectDrawerItem(SEARCH_DRAWER_POSITION);
                }

                if (position == 0) {

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
/*

                        System.out.println("Id of selected date is" + _id);
                        System.out.println("seletced Date in  database is " + gatedate);*/

                        Date d1 = null;
                        Date d2 = null;
                        String mytable1 = "mytable1";

                        try {

                            d1 = sdf.parse(gatedate);


                            d2 = sdf.parse(currentDateandTime);

                            //in milliseconds
                            long diff = d2.getTime() - d1.getTime();

                            diffDays = diff / (24 * 60 * 60 * 1000);

                           // System.out.print(diffDays + " days, ");


                            // if diiference is greater than 7
                            if (diffDays >= 7 && (isTableExists(mytable1)) == true) {
                                // delete mytable1
                                db.execSQL("delete from mytable1");
                              //  System.out.println("IN DATE DIFF IS 7");

                                // clear directory of cache
                                clear();

                                // update date field in cache table
                                ContentValues args = new ContentValues();
                                args.put("time", currentDateandTime);
                                int updatev = db.update("cache", args, ROWID + "=" + _id, null);
                                Log.i("Updated Value is :", "" + updatev);
                               // System.out.println("Updated Value is :" + currentDateandTime);

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

                    c.close();


                    //  DialogFragment dFragment = new DatePickerFragment();

                    // Show the date picker dialog fragment
                    // dFragment.show(getFragmentManager(), "Date Picker");
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
                        showAlertDialog(HomeActivity.this, "No Internet Connection", "You don't have internet connection..Please Try Again Later. ", false);

                    }
                }


                if (position == 20) {
                    if (cd.isConnectingToInternet(getApplicationContext())) {

                        Intent i = new Intent(getApplicationContext(), PollActivity.class);
                        i.putExtra("poll", "quiz");
                        startActivity(i);

                    } else {
                        showAlertDialog(HomeActivity.this, "No Internet Connection", "You don't have internet connection..Please Try Again Later. ", false);

                    }
                }


                if (position == 21) {
                    if (cd.isConnectingToInternet(getApplicationContext())) {

                        Intent i = new Intent(getApplicationContext(), Game.class);
//                        i.putExtra("poll", "games");
                        startActivity(i);

                    } else {
                        Intent i = new Intent(getApplicationContext(), Game.class);
//                        i.putExtra("poll", "games");
                        startActivity(i);
                        // showAlertDialog(HomeActivity.this, "No Internet Connection", "You don't have internet connection..Please Try Again Later ", false);

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
                        showAlertDialog(HomeActivity.this, "No Internet Connection", "You don't have internet connection..Please Try Again Later. ", false);
                    }
                }

                if (position == 29) {
                    Intent shareIntent =
                            new Intent(android.content.Intent.ACTION_SEND);

                    //set the type
                    shareIntent.setType("text/plain");

                    //add a subject
                    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                            "Nava Maratha App");

                    //build the body of the message to be shared
                    String shareMessage = "Track Ahmednagar Local News with NavaMaratha App. Click this link to download & install" +
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

        //  mLeftDrawer.setBackgroundColor((ContextCompat.getColor(getApplicationContext(), PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_primary_color : R.color.dark_primary_color)));
        mLeftDrawer.setBackgroundColor((ContextCompat.getColor(getApplicationContext(), R.color.light_primary_color)));
        //mDrawerList.setBackgroundColor((ContextCompat.getColor(getApplicationContext(), PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_background : R.color.dark_primary_color_light)));
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

       /* mDrawerHideReadButton = (FloatingActionButton) mLeftDrawer.findViewById(R.id.hide_read_button);
        if (mDrawerHideReadButton != null) {
            mDrawerHideReadButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    UiUtils.displayHideReadButtonAction(HomeActivity.this);
                    return true;
                }
            });
            UiUtils.updateHideReadButton(mDrawerHideReadButton);
            UiUtils.addEmptyFooterView(mDrawerList, 90);
        }*/

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
       /* AlertDialog alertDialog = new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
        alertDialog.setIcon((status) ? R.drawable.ic_error_outline : R.drawable.ic_error_outline);


        // Showing Alert Message
        alertDialog.show();*/
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "This device doesn't support Play services, App will not work normally",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_DRAWER_POS, mCurrentDrawerPos);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onResume() {

        super.onResume();
      /*  if (mAdView != null)
        {
            mAdView.resume();
        }*/
      /*  if( sAdview!= null ||  sAdview_right!= null)
        {

            sAdview.resume();
            sAdview_right.resume();
        }*/
        PrefUtils.registerOnPrefChangeListener(mShowReadListener);

        //Show the AdView if the data connection is available

        if (cd.isConnectingToInternet(getApplicationContext())) {

            //mAdView.setVisibility(View.VISIBLE);
         /*   sAdview.setVisibility(View.VISIBLE);
            sAdview_right.setVisibility(View.VISIBLE);*/


        }
       /* else
        {
            mAdView.setVisibility(View.GONE);
            sAdview.setVisibility(View.GONE);
            sAdview_right.setVisibility(View.GONE);
        }*/


      /* sAdview.resume();
       sAdview_right.resume();*/

    }

    @Override
    protected void onPause() {

       /* if (mAdView != null)
        {
            mAdView.pause();
        }*/

       /* if(sAdview!=null ||  sAdview_right!=null)
        {

            sAdview.pause();
            sAdview_right.pause();
        }*/
        PrefUtils.unregisterOnPrefChangeListener(mShowReadListener);


        super.onPause();
    }

    @Override
    protected void onDestroy() {
       /* if (mAdView != null)
        {
                mAdView.destroy();
        }*/
       /* if( sAdview!=null ||  sAdview_right!=null)
        {

            sAdview.destroy();
            sAdview_right.destroy();
        }*/
        super.onDestroy();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // We reset the current drawer position
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

                    selectDrawerItem(3);


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

    private void selectDrawerItem(int position) {
        mCurrentDrawerPos = position;

        if (mDrawerAdapter == null)
            return;

        mDrawerAdapter.setSelectedItem(position);
        mIcon = null;

        Uri newUri = null;
        boolean showFeedInfo = true;

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
          /*  default:
                long feedOrGroupId = mDrawerAdapter.getItemId(position);
                if (mDrawerAdapter.isItemAGroup(position)) {
                    newUri = EntryColumns.ENTRIES_FOR_GROUP_CONTENT_URI(feedOrGroupId);
                }
                else {
                    byte[] iconBytes = mDrawerAdapter.getItemIcon(position);
                    Bitmap bitmap = UiUtils.getScaledBitmap(iconBytes, 24);
                    if (bitmap != null)
                    {
                        mIcon = new BitmapDrawable(getResources(), bitmap);
                    }

                    newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(feedOrGroupId);
                    showFeedInfo = false;
                }

                mTitle = mDrawerAdapter.getItemName(position);
                break;*/
        }

        if (!newUri.equals(mEntriesFragment.getUri())) {
            mEntriesFragment.setData(newUri, showFeedInfo);
        }

        mDrawerList.setItemChecked(position, true);

        // First open => we open the drawer for you
        if (PrefUtils.getBoolean(PrefUtils.FIRST_OPEN, true)) {
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
                getSupportActionBar().setTitle(android.R.string.search_go);
                getSupportActionBar().setIcon(R.drawable.ic_search);
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




           /* default:
                getSupportActionBar().setTitle(mTitle);
                if (mIcon != null) {
                    getSupportActionBar().setIcon(mIcon);
                } else {
                    getSupportActionBar().setIcon(null);
                }
                break;*/
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

            //System.out.println("CLEAR METHOD CALLED");
            deleteDir(cache);
            //  DELDir(cache);
            // Toast.makeText(context, "Your All Cache is clear", Toast.LENGTH_SHORT).show();


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
                    //System.out.println("DELETE METHOD CALLED ");

                    if (!success) {
                        return false;
                    }
                }
            }
            // The directory is now empty so delete it
            return dir.delete();
        }


    }

    public boolean isTableExists(String tableName) {


        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }


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
                            System.out.println("" + formattedDate);

                            // For Pdf Name we required this format
                            DateFormat dateFormat1= new SimpleDateFormat("dd_MM_yyyy");
                            String formattedDate1 = dateFormat1.format(date2);
                            System.out.println("DATE FORMATEEDDD " + formattedDate1);


                            // For ePaper Pdf files
                            Intent i = new Intent(getActivity(), EpaperPdfActivity.class);
                            i.putExtra("date", formattedDate);
                            i.putExtra("pdf",formattedDate1);
                            startActivity(i);


                            // For ePaper Images Activity
                           /* Intent i = new Intent(getActivity(), Epaper.class);
                            i.putExtra("date", formattedDate);
                            startActivity(i);*/

                        }
                    });
            dpd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("Picker", "Cancel!");
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

    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure want to exit?");

        // ok Button
        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });

        // cancel Button
        String NegativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(NegativeText, null);

        // home Button
        String NeturalText = "Home";
        builder.setNeutralButton(NeturalText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // positive button logic
                Intent i = new Intent(HomeActivity.this, HomeActivity.class);
                i.putExtra("home", "home");
                startActivity(i);
            }
        });

        android.support.v7.app.AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();

          /*  new AlertDialog.Builder(this).setIcon(R.drawable.ic_error_outline)
                    .setTitle("Exit")
                    .setMessage("Are you sure want to exit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .setNeutralButton("Home", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Intent i = new Intent(HomeActivity.this,HomeActivity.class);
                            i.putExtra("home","home");
                            startActivity(i);


                        }
                    })
                    .show();*/

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
                    HttpPost httpPost = new HttpPost("http://web1.abmra.in/custom/GetDeletedGuid.php");

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
                //Log.i("Home", "First Array : " + mGetGuid);
                //Log.i("Home", "Second Array : " + mConvertedArray);
                deleteFeeds.DeleteFeed(mConvertedArray);
            }


        }
    }

    // Footer Advertise Backdround Request
    private class FooterAd extends AsyncTask<String, Void, String>
    {
        ImageView mFooter;
        String mDevice,mAd;
        String response;

        public FooterAd(ImageView mImage,String mDeviceId,String mAdType) {
            this.mFooter = mImage;
            this.mDevice = mDeviceId;
            this.mAd = mAdType;

        }

        @Override
        protected String doInBackground(String... params) {

            try {

                URL url = new URL("http://192.168.1.21/Json/Footer.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = convertStreamToString(in);
               // Log.i("HOME", " Response for footer : " + response);
                return response;
            } catch (Exception e) {

                return null;
            }
        }


        @Override
        protected void onPostExecute(String resp)
        {

            if (resp != null)
            {

                try {

                    JSONObject jsonObj = new JSONObject(resp);

                    // Getting JSON Array node
                    JSONObject contacts = jsonObj.getJSONObject("footer");

                    String AssetId = contacts.getString("AssetId");
                    String ImageUrl = contacts.getString("url");
                    String ImageDelay = contacts.getString("delay");
                    String ImageClickUrl = contacts.getString("clickurl");


                    long timdelay = Long.parseLong(ImageDelay);
                   // Log.i("Home :", " Json Array : " + contacts);
                   // Log.i("HOME :", "JSON Image Url : " + ImageUrl);


                    // If Advertise Type is BottomFooter
                    if(AssetId.equals("A1"))
                    {
                        //SetFooter(ImageUrl,timdelay,ImageClickUrl,mFooter);
                    }




                } catch (final JSONException e)
                {

                }

            }


        }



        }// end of async task Footer


    // Left Advertise Background RequestMethod
    private class LeftAd extends AsyncTask<String, Void, String>
    {
        ImageView mFooter;
        String mDevice,mAd;
        String response;

        public LeftAd(ImageView mImage,String mDeviceId,String mAdType) {
            this.mFooter = mImage;
            this.mDevice = mDeviceId;
            this.mAd = mAdType;

        }

        @Override
        protected String doInBackground(String... params) {

            try {

                URL url = new URL("http://192.168.1.21/Json/Footer.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = convertStreamToString(in);
              //  Log.i("HOME", " Response for footer : " + response);
                return response;
            } catch (Exception e) {

                return null;
            }
        }


        @Override
        protected void onPostExecute(String resp) {

            if (resp != null)
            {

                try {

                    JSONObject jsonObj = new JSONObject(resp);

                    // Getting JSON Array node
                    JSONObject contacts = jsonObj.getJSONObject("footer");

                    String AssetId = contacts.getString("AssetId");
                    String ImageUrl = contacts.getString("url");
                    String ImageDelay = contacts.getString("delay");
                    String ImageClickUrl = contacts.getString("clickurl");


                    // String to Long Delay
                    long timdelay = Long.parseLong(ImageDelay);
                   // Log.i("Home :", " Json Array : " + contacts);
                   // Log.i("HOME :", "JSON Image Url : " + ImageUrl);


                    if(AssetId.equals("A2"))
                    {
                       // SetLeftAd(ImageUrl,timdelay,ImageClickUrl,mFooter);
                    }



                } catch (final JSONException e)
                {

                }

            }


        }



    }// end of asynctask Left Ad



    // Footer Advertise
    // Parameters : Footer Url - Image Url of Footer , FooterDelay - Delay for Footer advertise , FooterClickUrl - footer Click Url ,
    private void SetFooter(String footerurl, long footerdelay, final String footerClickurl, final ImageView mImage)
    {

        if (cd.isConnectingToInternet(getApplicationContext()))
        {
            Picasso.with(getApplicationContext())
                    .load(footerurl)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE).into(mImage);

            if (footerClickurl!=null)
            {
                mImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setData(Uri.parse(footerClickurl));
                        startActivity(intent);
                    }
                });
            }
        }



        Runnable runnable = new Runnable()
        {
            public void run()
            {

                //Log.i("SETFOOTER ", "Called Method In RUN FOR A1: ");
                //*/ mImageAdvertise.setImageResource(imageArray[i]);
                   /*String sample = "https://dummyimage.com/320x100/5fada1/0011ff.jpg&text=POOJA+";
                    Picasso.with(getApplicationContext())
                            .load(sample)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .networkPolicy(NetworkPolicy.NO_CACHE).into(mImageAdvertise);
*/
                //new FooterAd(mImage).execute();

               // new FooterAd(mImage,devid,FooterAd).execute();


            }

        };


        mImage.postDelayed(runnable, footerdelay);
    }


    // left ad
    private void SetLeftAd(String footerurl, long footerdelay, final String footerClickurl, final ImageView mImage)
    {

        if (cd.isConnectingToInternet(getApplicationContext()))
        {
            Picasso.with(getApplicationContext())
                    .load(footerurl)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE).into(mImage);

            if (footerClickurl!=null)
            {
                mImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setData(Uri.parse(footerClickurl));
                        startActivity(intent);
                    }
                });
            }
        }



        Runnable runnable = new Runnable()
        {
            public void run()
            {
               // Log.i("SETFOOTER ", "Called Method In RUN For A2: ");
                //*/ mImageAdvertise.setImageResource(imageArray[i]);
                   /*String sample = "https://dummyimage.com/320x100/5fada1/0011ff.jpg&text=POOJA+";
                    Picasso.with(getApplicationContext())
                            .load(sample)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .networkPolicy(NetworkPolicy.NO_CACHE).into(mImageAdvertise);
*/
                //new FooterAd(mImage).execute();

               //new LeftAd(mImage,devid,LeftAd).execute();

            }

        };


        mImage.postDelayed(runnable, footerdelay);
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


}



