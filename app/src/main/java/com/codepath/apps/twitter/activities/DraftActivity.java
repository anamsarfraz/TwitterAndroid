package com.codepath.apps.twitter.activities;

import android.databinding.DataBindingUtil;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.databinding.ActivityDraftBinding;

public class DraftActivity extends AppCompatActivity {

    private ActivityDraftBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_draft);

        setSupportActionBar(binding.tbDrafts);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation((float)10.0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
