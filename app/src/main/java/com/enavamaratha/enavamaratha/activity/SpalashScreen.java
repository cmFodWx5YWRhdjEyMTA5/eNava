package com.enavamaratha.enavamaratha.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.enavamaratha.enavamaratha.R;

public class SpalashScreen extends AppCompatActivity
{
    ImageView image;
    private static int SPLASH_TIME_OUT = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalash_screen);

        new Handler().postDelayed(new Runnable()
        {

            @Override
            public void run()
            {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(getApplicationContext(),Registration.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    @Override public void onBackPressed()
    { this.finish();
        super.onBackPressed();
    }
}
