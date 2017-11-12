package com.enavamaratha.enavamaratha.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.service.ConnectionDetector;

public class Rate extends AppCompatActivity
{

    Button btnrate ,btnshare;
    Context context;
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btnrate=(Button)findViewById(R.id.btnrate);
        btnshare=(Button)findViewById(R.id.btnshare);
        context=getApplicationContext();


        btnshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent =
                        new Intent(android.content.Intent.ACTION_SEND);

                //set the type
                shareIntent.setType("text/plain");

                //add a subject
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        "Nava Maratha App");

                //build the body of the message to be shared
                String shareMessage = "Hey,I am using NavaMaratha eNewsPaper App.Please visit the below link to download. " +
                        "\n"+"https://play.google.com/store/apps/details?id=com.enavamaratha.enavamaratha";

                //add the message
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        shareMessage);

                //start the chooser for sharing
                startActivity(Intent.createChooser(shareIntent,
                        "Share Application Via"));
            }
        });


        btnrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(cd.isConnectingToInternet(context))
                {
                    launchmarket();
                }

                else
                {
                    showAlertDialog(Rate.this, "No Internet Connection", "You don't have internet connection..Please Try Again Later ", false);
                }
            }




        });



    }

    private void launchmarket()
    {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
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
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void showAlertDialog(final Context context, String title, String message, Boolean status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Rate.this);
        builder.setTitle(title);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

}
