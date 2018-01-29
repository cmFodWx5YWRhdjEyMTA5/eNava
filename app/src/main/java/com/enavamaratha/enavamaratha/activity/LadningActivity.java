package com.enavamaratha.enavamaratha.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.enavamaratha.enavamaratha.R;

public class LadningActivity extends AppCompatActivity implements View.OnClickListener {


    private Button mButtonePaper, mButtonHeadline, mButtonEconomics, mButtonHealth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ladning);


        mButtonePaper = (Button) findViewById(R.id.btn_epaper);
        mButtonHeadline = (Button) findViewById(R.id.btn_latestnews);
        mButtonHealth = (Button) findViewById(R.id.btn_healthnews);
        mButtonEconomics = (Button) findViewById(R.id.btn_econonews);


        mButtonePaper.setOnClickListener(this);
        mButtonHeadline.setOnClickListener(this);
        mButtonEconomics.setOnClickListener(this);
        mButtonHealth.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        int id = v.getId();


        if (id == R.id.btn_epaper) {
            openNewsActivity("0");
        }

        if (id == R.id.btn_latestnews) {
            openNewsActivity("3");
        } else if (id == R.id.btn_econonews) {
            openNewsActivity("4");
        } else if (id == R.id.btn_healthnews) {
            openNewsActivity("5");
        }


    }


    private void openNewsActivity(String newstype) {
        // Log.e("Landing Click ", "openNews: ----------- "+newstype);
        Intent home = new Intent(LadningActivity.this, HomeActivity.class);
        home.putExtra("land", newstype);
        startActivity(home);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //   Log.e("Landing Back ", "onBackPressed: ");
    }
}
