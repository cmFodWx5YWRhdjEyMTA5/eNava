package com.enavamaratha.enavamaratha.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.service.ConnectionDetector;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;
import com.shockwave.pdfium.PdfDocument;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class EpaperPdfActivity extends AppCompatActivity implements OnPageChangeListener,OnLoadCompleteListener{

    private static final String TAG = EpaperPdfActivity.class.getSimpleName();

    // Pdf Name will be : DD_MM_YYYY.pdf
    // Store this file in sd card folder under NavaMaratha folder
    private PDFView pdfView;
    private RelativeLayout RelDeniedLayout;
    private Button btnSettings;

    private String pdfName,mSelectedDate,mSelectedDatePdfName,mPdfUrl,mUrlSlash,mePaperPdfUrl;

    public static final String SAMPLE_FILE = "2017-11-04.pdf";

    // Progress Dialog
    private ProgressDialog pDialog,progressDialog;
    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;
    Integer pageNumber = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epaper_pdf);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //  Single Runtime Permission for Above Marshmallow
        // External Storage Permission required for read and write pdf file in sd card
        // Uses Library
        // ---- https://github.com/Karumi/Dexter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {


            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {


                Dexter.withActivity(this)
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {

                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {

                                // we need to initialisation view for snackbar
                               // initView();


                                if (response.isPermanentlyDenied()) {

                                    StoragePermissionDeniedView();
                                   /* Snackbar snackbar = Snackbar.make(pdfView, "Storage access is needed for ePaper", Snackbar.LENGTH_LONG)
                                            .setAction("Settings", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    openSettings();
                                                }
                                            });

                                    snackbar.show();*/
                                } else {

                                    StoragePermissionDeniedView();
                                    /*Snackbar snackbar = Snackbar.make(pdfView, "Storage access is needed for ePaper", Snackbar.LENGTH_LONG)
                                            .setAction("Settings", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    openSettings();
                                                }
                                            });

                                    snackbar.show();*/
                                }


                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();


            }

            // If Permission is granted then download paper and view
            else
            {

                initView();

                ReadEPaper();

            }



        }


        // For below than marshmallow versions
        else {

            initView();

            // Download Epaper and view ePaper
            ReadEPaper();

        }









    }



    // We Create methods  because we need for above marshmallow and below that
    private void initView()
    {


        // Pdf View
        pdfView = (PDFView)findViewById(R.id.pdfView);

        /// ---------- Permission Denied Layout

        RelDeniedLayout = (RelativeLayout)findViewById(R.id.RelDenied);
        btnSettings = (Button)findViewById(R.id.btnSettings);

        //-----------

        progressDialog = new ProgressDialog(EpaperPdfActivity.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        //get the selected date from datepiceker --- Url path for access pdf
        // Date will be yyyy/MM/dd
        mSelectedDate = getIntent().getStringExtra("date");

        // get Value from Home.Activity after date picker will be Click
        // Date format will be dd_mm_yyyy
        // and we required pdf name is: DD_MM_YYYY.pdf
        mSelectedDatePdfName = getIntent().getStringExtra("pdf");
        mSelectedDatePdfName += ".pdf";


        // Pdf name to show on toolbar ePaper dd_mm_yyyy
        pdfName = getIntent().getStringExtra("pdf");
        pdfName = "ePaper\t"+pdfName;


        // Url of ePaper Pdf download

        mPdfUrl = "http://paper.enavamaratha.com/images/";
        mUrlSlash = "/";


    }


    private void StoragePermissionDeniedView()
    {
        initView();
        pdfView.setVisibility(View.GONE);
        RelDeniedLayout.setVisibility(View.VISIBLE);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
    }





    private void ReadEPaper()
    {


        // Check internet Connection
        if (ConnectionDetector.isConnectingToInternet(getApplicationContext()))
        {

            // Check file is Exists or not
            // If Exists then show from sd card
            if (getPdfFilePath().exists())
            {

                ReadPdfFromFile(getPdfFilePath());

            }

            // Else Check Url is Exists or Not and Download that file
            else
            {

                // Url is Exists or not on server : http://paper.enavamaratha.com/images/yyyy/mm/dd/dd_mm_yyyy.pdf.

                mePaperPdfUrl = mPdfUrl+mSelectedDate+mUrlSlash+mSelectedDatePdfName.trim();

                new  CheckUrlIsExists().execute(mePaperPdfUrl);


            }


        }

        // No Internet Connection
        else
        {
            // Check file is Exists or not
            // If Exists then show from sd card

            if (getPdfFilePath().exists())
            {

                ReadPdfFromFile(getPdfFilePath());


            }

            // Else show no internet connection
            else
            {

                showAlertDialog(EpaperPdfActivity.this, "No Internet Connection", "You don't have internet connection..Please Try Again Later. ", false);
            }


        }

    }




    /*
    *  Pdf Viewer Implemented functions
    *
    * */


    @Override
    public void onPageChanged(int page, int pageCount) {

        pageNumber = page;
        // Set title for Toolbar as Pdf Name (ePaper Date Page of 1/10)
        setTitle(String.format("%s %s %s / %s", pdfName,"\t\t\t\t",page + 1, pageCount));
        //getSupportActionBar().setSubtitle(String.format("%s / %s",page+1,pageCount));

    }

    @Override
    public void loadComplete(int nbPages) {

        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }



    // For Permissions go to Settings---
    private  void openSettings(){
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getApplicationContext().getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myAppSettings);
        finish();
    }


    /**
     * Background Async Task to download Selected Date ePaper Pdf file
     */
    private class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {


                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                String path = Environment.getExternalStorageDirectory() + "/" + "NavaMaratha/";
                File fo = new File(path);
                if (!fo.exists()) {
                    fo.mkdirs();
                }
                // Output stream

                File newFile = new File(path, mSelectedDatePdfName);

                OutputStream output = new FileOutputStream(newFile);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();


            } catch (Exception e) {

            }

            return null;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);


            ReadPdfFromFile(getPdfFilePath());

        }


    }



    /// Function for Read pdf file from
    private void ReadPdfFromFile(File pdfFilePath)
    {


        pdfView.fromFile(pdfFilePath)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .enableDoubletap(true)
                .swipeHorizontal(false)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();


    }


    /// SAMPLE function for pdf test
    // Read pdf file from Asset
    private void ReadPdfFromAsset(String Filename)
    {

        Filename = Filename.substring(0,Filename.indexOf("."));
        pdfName = "ePaper\t"+Filename;


        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .enableDoubletap(true)
                .swipeHorizontal(false)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }



    private File getPdfFilePath()
    {
        // Path for saving ePaper Pdf in SD Card
        String path = Environment.getExternalStorageDirectory() + "/" + "NavaMaratha/";
        File fo = new File(path);
        if (!fo.exists()) {
            fo.mkdirs();
        }

        File newFile = new File(path, mSelectedDatePdfName);

        return  newFile;
    }





    private class CheckUrlIsExists extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {

            progressDialog.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection con =  (HttpURLConnection) new URL(params[0]).openConnection();
                con.setRequestMethod("HEAD");
                System.out.println(con.getResponseCode());
                return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            boolean bResponse = result;
            progressDialog.dismiss();
            if (bResponse)
            {
                new DownloadFileFromURL().execute(mePaperPdfUrl);
            }
            else
            {
                showAlertDialog(EpaperPdfActivity.this, "ePaper Not Available", "ePaper Not Available Yet..Please Try Again Later ", false);
            }
        }
    }


    /**
     * Showing Dialog
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading ePaper. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(false);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }


// show dialouge
    private void showAlertDialog(final Context context, String title, String message, Boolean status) {

        AlertDialog.Builder builder = new AlertDialog.Builder(EpaperPdfActivity.this);
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






}
