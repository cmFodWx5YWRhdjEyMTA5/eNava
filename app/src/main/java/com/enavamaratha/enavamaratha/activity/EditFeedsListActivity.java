
package  com.enavamaratha.enavamaratha.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import  com.enavamaratha.enavamaratha.R;
import  com.enavamaratha.enavamaratha.utils.UiUtils;

public class EditFeedsListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_feeds);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }
}
