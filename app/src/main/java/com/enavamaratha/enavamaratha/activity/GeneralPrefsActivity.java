

package  com.enavamaratha.enavamaratha.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import  com.enavamaratha.enavamaratha.R;
import  com.enavamaratha.enavamaratha.utils.UiUtils;

public class GeneralPrefsActivity extends BaseActivity
{

    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_general_prefs);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



           }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.menu_homee:
                Intent intee = new Intent(GeneralPrefsActivity.this,HomeActivity.class);
                intee.putExtra("home","home");
                startActivity(intee);
                return true;

            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }



}
