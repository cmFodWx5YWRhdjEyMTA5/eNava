package  com.enavamaratha.enavamaratha.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.enavamaratha.enavamaratha.OnTaskCompleted;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;


import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import  com.enavamaratha.enavamaratha.R;
import  com.enavamaratha.enavamaratha.service.ConnectionDetector;
import  com.enavamaratha.enavamaratha.view.TouchImageView;

public class Epaper extends AppCompatActivity  {

    private final String TAG = "Archieve Paper";
    public Context context;

    //public ImagePagerAdapter mAdapter;
    ArrayList<String> urls;
    public int finalcount;
    public int count;
    private String selectdate;
    public boolean getval;
    SQLiteDatabase db;
    private int mProgressStatus = 0;

    int finalc,countc = 0;

    // flag for Internet connection status
    Boolean isInternetPresent = false;
    DetailOnPageChangeListener listener;
    // Connection detector class
    ConnectionDetector cd;

    boolean success = false;

    private ViewPager pager;
    TouchImageView imageView;
    ImagePagerAdapter adapter;
    TextView txtdate,txtpagno;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epaper);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = getApplicationContext();

        txtdate =(TextView)findViewById(R.id.txtdate);
        txtpagno=(TextView)findViewById(R.id.txtpageno);

        //get the selected date fromdatepiceker
        selectdate = getIntent().getStringExtra("date");


//        simpleZoomControls.hide();
       // System.out.println("" + selectdate);

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        String date = formatter.format(Date.parse(selectdate));
        // set select date
        txtdate.setText(date);



        // link of eapepr
        final  String url = "http://paper.enavamaratha.com/images/";
        final String zero = "/";
        final String exten = ".jpg";
        finalcount = 0;
        pager = (ViewPager) findViewById(R.id.pager);

        db = openOrCreateDatabase("MyMb", MODE_PRIVATE, null);


        // check internet connection
        if (cd.isConnectingToInternet(context))
        {

            // Its Available...
            // showAlertDialog(Archieve.this, "Internet Connection",  "You have internet connection", true);
	 		/*check
	 		 * http:///paper.enavamaratha.com/images/selecteddate/page no.
	 		 *
	 		 * is avaliable or not in server
	 		 *       if avaliable
	 		 *           then add count by 1
	 		 *            else
	 		 *               do nothing
	 		 */


            for (int i = 1; i <= 15; i++)
            {
                final String URLName = url + selectdate + zero + i + exten.trim();
                // background method
                final MyTask task = new MyTask(new OnTaskCompleted()
                {
                    @Override
                    public void onTaskCompleted(boolean val)
                    {

                        getval = val;
                        if (getval == true)
                        {
                            finalc++;
                            finalcount = finalc;

                          //  Log.i("Value Of Boolean count", "" + finalcount + "Value of finalc" + finalc);

                            // if paper not upload
                            if (finalcount == 0)
                            {
                              //  Log.i("If Final count", "final count is Zero means no paper uploded yet");
                                showAlertDialog(Epaper.this, "ePaper Not Avaliable", "ePaper Not Avaliable Yet..Please Try Again Later ", false);
                            }

                            // if upload
                            else {

                                db.execSQL("create table if not exists mytable1(id integer primary key autoincrement, time varchar , totalurl integer)");
                              //  Log.i(TAG, "Create Table in  archieve paper");

                                Cursor c = db.rawQuery("select * from mytable1 where time  ='" + selectdate + "'", null);

                                // if value is already in database then update
                                if (c != null && c.getCount() > 0)
                                {
                                    final String ROWID = "id";
                                    c.moveToFirst();
                                    //PID Found
                                    int _id = c.getInt(c.getColumnIndex("id"));
                                    String gatedate = c.getString(c.getColumnIndex("time"));
                                    int gatecount = c.getInt(c.getColumnIndex("totalurl"));

                                  //  System.out.println("Id of selected date is" + _id);
                                    //System.out.println("seletced Date in  database is " + gatedate);
                                    //System.out.println("seletced Date have no  " + gatecount);

                                    ContentValues args = new ContentValues();
                                    args.put("totalurl", finalcount);
                                    int updatev = db.update("mytable1", args, ROWID + "=" + _id, null);
                                    //Log.i("Updated Value is :", "" + updatev);

                                  //  System.out.println("Updated Value is :" + updatev);

                                    int gat = c.getInt(c.getColumnIndex("totalurl"));
                                   // System.out.println("seletced Date have no  " + gat);
                                }

                                // else insert that values in database
                                else
                                {
                                    db.execSQL("INSERT INTO mytable1 (time,totalurl)VALUES ( '" + selectdate + "', '" + finalcount + "')");

                                    //Log.i(TAG, "INSERT into DATABASE Values " + selectdate + "TotalPage:" + finalcount);
                                }
                                // insert urls in arraylist
                                urls = new ArrayList<String>();
                                for (int i = 1; i <= finalcount; i++)
                                {
                                    String finalurl = url + selectdate + zero + i + exten.trim();
                                    urls.add(finalurl);
                                   // System.out.println("" + urls);
                                }

                                //Converting arraylist to array of string
                                String[] images = new String[urls.size()];
                                images = urls.toArray(images);
                              //  System.out.println("" + images);

                                 adapter= new ImagePagerAdapter(images);

                                 pager.setAdapter(adapter);
                                listener = new DetailOnPageChangeListener();
                                //Log.i(TAG, "set Adapter");

                                CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);

                                final float density = getResources().getDisplayMetrics().density;
                                indicator.setViewPager(pager);
                                indicator.setRadius(5 * density);
                                indicator.setBackgroundColor(0xFF888888);
                                indicator.setPageColor(0x88FF0000);
                                indicator.setFillColor(0xFF888888);
                                indicator.setStrokeColor(0xFF000000);
                                indicator.setStrokeWidth(2 * density);
                                indicator.setOnPageChangeListener(listener);


                            }

                        }

                        else
                        {
                            count++;
                            countc = count;
                            //Log.i("No Value", "No Urls Are False" + count);
                            if (countc == 15  )
                            {
                                //Log.i("If Final count", "final count is Zero means no paper uploded yet");
                                showAlertDialog(Epaper.this, "ePaper Not Avaliable", "ePaper Not Avaliable Yet..Please Try Again Later ", false);
                            }
                        }

                    }

                });

                task.execute(URLName);


            }


        }



        // If Internet connection not avaliable
        else {


            // Not Available...
            String mytable1 = "mytable1";
            boolean check = isTableExists(mytable1, true);

            if (check == true) {

                //Checking in database papers date is exist or not

                Cursor c = db.rawQuery("select time, totalurl from mytable1 where time  ='" + selectdate + "'", null);

                //if exist
                if (c != null && c.getCount() > 0) {
                    c.moveToFirst();
                    //PID Found
                  //  System.out.println("Getting Value from database");

                    String getdate = c.getString(c.getColumnIndex("time"));
                    int getcount = c.getInt(c.getColumnIndex("totalurl"));

                  //  System.out.println("seletced Date in  database is match" + getdate);
                  //  System.out.println("seletced Date have no of ulrs " + getcount);


                    urls = new ArrayList<String>();
                    for (int i = 1; i <= getcount; i++) {
                        String finalurl = url + selectdate + zero + i + exten.trim();
                        urls.add(finalurl);
                      //  System.out.println("" + urls);
                    }

                    //Converting arraylist to array of string
                    String[] images = new String[urls.size()];
                    images = urls.toArray(images);
                  //  System.out.println("" + images);

                    pager.setAdapter(new ImagePagerAdapter(images));


                    listener = new DetailOnPageChangeListener();

                    CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
                    final float density = getResources().getDisplayMetrics().density;
                    indicator.setViewPager(pager);
                    indicator.setRadius(5 * density);
                    indicator.setBackgroundColor(0xFF888888);
                    indicator.setPageColor(0x88FF0000);
                    indicator.setFillColor(0xFF888888);
                    indicator.setStrokeColor(0xFF000000);
                    indicator.setStrokeWidth(2 * density);
                    indicator.setOnPageChangeListener(listener);
                }

                // if not exist then display alert dialog
                else

                {

                    showAlertDialog(Epaper.this, "No Internet Connection", "You don't have internet connection..Please Try Again Later. ", false);
                }
            }

            // value not in database
            else
            {
                showAlertDialog(Epaper.this, "No Internet Connection", "You don't have internet connection..Please Try Again Later. ", false);
            }
        }
	 	/*check
		 * http:///paper.enavamaratha.com/images/selecteddate/page no.
		 *
		 * is avaliable or not in server
		 * if avaliable then add count by 1
		 * else
		 * do nothing
		 */
	/*	for(int i=1;i<=15;i++)
	   {
			String URLName=url+selectdate+zero+i+exten.trim();
		    MyTask task = new MyTask();
	       task.execute(URLName);
		    try {
	        	 // get boolean value to check url is exist or on server
				Boolean value=task.get();

				if (value==true)
				{
					finalcount++;
					Log.i("Value Of Boolean count", ""+finalcount);
				}
				else
				{
					Log.i("No Value","No Urls Are False");
				}
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }



         db= openOrCreateDatabase("MyMb", MODE_PRIVATE, null);
	     db.execSQL("create table if not exists mytable1(id integer primary key autoincrement, time varchar , totalurl integer)");
		 Log.i(TAG,"Create Table");

	   db.execSQL("INSERT INTO mytable1 (time,totalurl)VALUES ( '"+selectdate+"', '"+finalcount+"')");

	   Log.i(TAG,"INSERT into DATABASE Values "+selectdate+finalcount); */

        // Add that urls in arraylist

		   /*  urls= new ArrayList<String>	();
		    for (int i = 1; i <= finalcount; i++)
	        {
		    String finalurl=url+selectdate+zero+i+exten.trim();
	      	urls.add(finalurl);
	        System.out.println(""+urls);
	       }

		    //Converting arraylist to array of string
            String [] images = new String[urls.size()];
            images = urls.toArray(images);
         	System.out.println(""+images);

			   ViewPager pager = (ViewPager) findViewById(R.id.archpager);
	      	   pager.setAdapter(new ImagePagerAdapter(images));
	    	  listener = new DetailOnPageChangeListener();
		       Log.i(TAG, "set Adapter");*/


    }



    // method for teble is exit or not
    public boolean isTableExists(String tableName, boolean openDb) {
        if (openDb)
        {
            if (db == null || !db.isOpen())
            {

            }

            if (!db.isReadOnly()) {


            }
        }

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



    // show dialouge
    private void showAlertDialog(final Context context, String title, String message, Boolean status) {

            AlertDialog.Builder builder = new AlertDialog.Builder(Epaper.this);
            builder.setTitle(title);
            builder.setMessage(message);

            String positiveText = getString(android.R.string.ok);
            builder.setPositiveButton(positiveText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // positive button logic
                            Intent i = new Intent(context, HomeActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.putExtra("home","home");
                            context.startActivity(i);
                        }
                    });

          builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
              @Override
              public void onCancel(DialogInterface dialog) {
                  dialog.dismiss();
                  Intent i = new Intent(context, HomeActivity.class);
                  i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                  i.putExtra("home","home");
                  context.startActivity(i);
              }
          });


            AlertDialog dialog = builder.create();
            // display dialog
            dialog.show();
        /*AlertDialog.Builder alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
        alertDialog.setIcon((status) ?R.drawable.ic_error_outline : R.drawable.ic_error_outline );

        // Setting OK Button

       *//* alertDialog.setButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(context, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("home","home");
                context.startActivity(i);
            }
        });*//*

        // Showing Alert Message
        alertDialog.show();*/
    }





    // for checking in background paper is avlaible or not in server
    public class MyTask extends AsyncTask<String, Integer, Boolean> {


      public OnTaskCompleted delegate;
   //  ProgressDialog  pDialog = new ProgressDialog(Epaper.this);
   ProgressDialog pDialog;
        private boolean socketTimedOut = false;

        public MyTask(OnTaskCompleted task)
        {
            this.delegate=task;
        }

        // before dobackground method
       @Override
       protected void onPreExecute()
       {
           super.onPreExecute();

           pDialog = new ProgressDialog(Epaper.this);
           this.pDialog.setMessage("Please Wait..Downloading ePaper");

           this.pDialog.setCancelable(false);
           this.pDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
           {
               @Override
               public void onCancel(DialogInterface dialog)
               {
                   MyTask.this.cancel(true);
                   finish();
                   pDialog.dismiss();
               }
           });
           this.pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener()
           {
               // Set a click listener for progress dialog cancel button
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   // dismiss the progress dialog
                   MyTask.this.cancel(true);
                   finish();
                  pDialog.dismiss();
                   // Tell the system about cancellation

               }
           });

           this.pDialog.show();
       }


        @Override
       protected Boolean doInBackground(String... params)
        {


           try {
               HttpURLConnection.setFollowRedirects(false);
               HttpURLConnection con = (HttpURLConnection) new URL(params[0]).openConnection();
               con.setRequestMethod("HEAD");
               con.setConnectTimeout(6000);
               con.connect();
              // System.out.println(con.getResponseCode());

               // if url in server then true
               if(con.getResponseCode() == HttpURLConnection.HTTP_OK)
               {
                   //System.out.println(con.getResponseCode());
                   return true;
               }
             //  return (con.getResponseCode() == HttpURLConnection.HTTP_OK);

               // else return false
               else
               {
                   //System.out.println(con.getResponseCode());
                  //  MyTask.this.cancel(true);
                   //finish();
                   pDialog.dismiss();
                  return false;
                  // return false;
               }

           }
           catch (java.net.SocketTimeoutException e)
           {
               return false;

           }
           catch (java.io.IOException e)
           {
               return false;
           }


       }
        // after do backgorund method

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);

            //isValid=result;
            delegate.onTaskCompleted(result);

            try
            {

                if ((this.pDialog != null) && this.pDialog.isShowing())
                {
                    this.pDialog.dismiss();
                }


            }
            catch (final IllegalArgumentException e)
            {
                // Handle or log or ignore
            }
            catch (final Exception e)
            {
                // Handle or log or ignore
            }
            finally
            {
                this.pDialog = null;
            }

          //  Log.d("Hi", "Done Downloading.");
        }

        }



    // epaper change / or swipe
    public class DetailOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener
    {

        private int currentPage;


        @Override
        public void onPageSelected(int position)
        {
              currentPage = position;
              int pageno=currentPage+1;
          //  textView.setText((arg0+1)+" of "+ images.size());
            // set page number to textview
             txtpagno.setText(pageno+" of "+urls.size());
             //  Toast.makeText(Epaper.this, "Page no:"+pageno,Toast.LENGTH_SHORT).show();

        }


        public int getCurrentPage()
        {
            return currentPage;
        }
    }


    // adapter for epaepr
   public class ImagePagerAdapter extends PagerAdapter
    {

        public View CurrentView;
        public String[] images;

        private LayoutInflater inflater;


       public ImagePagerAdapter( String[] images)
        {
            this.images = images;
            inflater = getLayoutInflater();
        }

        @Override
        public void destroyItem(View container, int position, Object object)
        {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public void finishUpdate(View container)
        {
        }

        @Override
        public int getCount()
        {
            return images.length;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            CurrentView = (View)object;
        }
        @Override
        public Object instantiateItem(View view, int position)
        {
            //final  TouchImageView imageView;
            final View imageLayout = inflater.inflate(R.layout.image, null);
            imageView=(TouchImageView)imageLayout.findViewById(R.id.imgDisplay);
            imageView.setMaxZoom(5f);

//            simpleZoomControls.show();
            // perform setOnZoomInClickListener event on ZoomControls

            //final ImageView imageView = (ImageView) imageLayout.findViewById(R.id.imageView1);
            final int  pos=position;

            /*
            OkHttpClient okHttpClient = new OkHttpClient();
            File customCacheDirectory = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/MyCache");
            okHttpClient.setCache(new Cache(customCacheDirectory, Integer.MAX_VALUE));
            OkHttpDownloader okHttpDownloader = new OkHttpDownloader(okHttpClient);
            Picasso picasso = new Picasso.Builder(mainActivity).downloader(okHttpDownloader).build();
            picasso.load(imageURL).into(viewHolder.image);*/



//            Picasso.setSingletonInstance(built);
            // using picasso library
         Picasso.with(context)
                    .load(images[position])
                    .placeholder(R.drawable.epaper_error)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                 .into(imageView, new Callback() {
                     @Override
                     public void onSuccess() {

                     }

                     @Override
                     public void onError() {
                         //Try again online if cache failed
                         Picasso.with(getApplicationContext())
                                 .load(images[pos])
                                 .error(R.drawable.downloderror)
                                 .placeholder(R.drawable.epaper_error)
                                 .into(imageView, new Callback() {
                                     @Override
                                     public void onSuccess() {

                                     }

                                     @Override
                                     public void onError() {


                                       //  Log.i("Picasso", "Could not fetch image");
                                         // Toast.makeText(Archieve.this, "Please Chcek Your Internet Connection or Try Again Later",Toast.LENGTH_SHORT).show();
                                     }
                                 });
                     }
                    });

           // Log.i(TAG, "" + images[position]);

               imageView.setTag(position);

            ((ViewPager) view).addView(imageLayout, 0);
         //   Log.i(TAG, "Add View");
            return imageLayout;
        }



        @Override
        public boolean isViewFromObject(View view, Object object)
        {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader)
        {
        }

        @Override
        public Parcelable saveState()
        {
            return null;
        }

        @Override
        public void startUpdate(View container)
        {
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_epaper, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {


            case R.id.menu_hommee:
                Intent intee = new Intent(Epaper.this,HomeActivity.class);
                intee.putExtra("home","home");
                startActivity(intee);
                return true;

            // save epaper
            case R.id.save:
                Bitmap bitmap;
                OutputStream output;
                int curr = pager.getCurrentItem();
            //  BitmapDrawable btmpDr = (BitmapDrawable)imageView.getDrawable();
             // bitma = btmpDr.getBitmap();


                ImageView Imgv = (ImageView)pager.findViewWithTag(pager.getCurrentItem());
                BitmapDrawable btmpDr = (BitmapDrawable)Imgv.getDrawable();
                bitmap = btmpDr.getBitmap();

               // Imgv.setDrawingCacheEnabled(true);

              //  Bitmap bitmap=Imgv.getDrawingCache();



                File filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                // create directory in downloads
                File dir = new File(filepath.getAbsolutePath() + "/NavaMaratha/");
                dir.mkdirs();

                Date d = new Date();

                CharSequence s = DateFormat.format("dd-MM-yy hh-mm", d.getTime());
                File file = new File(dir, "NavaMaratha"+s + ".jpeg");
                Toast.makeText(Epaper.this,"Image Saved to SD Card"+file,Toast.LENGTH_LONG).show();

                try {
                    output = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                    output.flush();
                    output.close();

                    /**Update image to gallery**/
                    MediaScannerConnection.scanFile(this, new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case android.R.id.home:
                finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }


}
