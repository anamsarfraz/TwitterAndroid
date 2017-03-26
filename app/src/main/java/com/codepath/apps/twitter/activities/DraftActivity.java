package com.codepath.apps.twitter.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.DraftsArrayAdapter;
import com.codepath.apps.twitter.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitter.databinding.ActivityDraftBinding;
import com.codepath.apps.twitter.models.Draft;
import com.codepath.apps.twitter.util.Connectivity;
import com.codepath.apps.twitter.util.EndlessRecyclerViewScrollListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.data;
import static com.codepath.apps.twitter.activities.TimelineActivity.DEBUG;

public class DraftActivity extends AppCompatActivity {

    private ActivityDraftBinding binding;
    DraftsArrayAdapter draftsArrayAdapter;
    List<Draft> drafts;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_draft);
        drafts = Draft.getDrafts();
        draftsArrayAdapter = new DraftsArrayAdapter(this, drafts);


        setSupportActionBar(binding.tbDrafts);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation((float)10.0);

        setUpRecycleView();
        setUpClickListener();
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

    private void setUpRecycleView() {
        binding.rvDrafts.setAdapter(draftsArrayAdapter);
        linearLayoutManager = new LinearLayoutManager(this);
        binding.rvDrafts.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dividerItemDecoration.setDrawable(getDrawable(R.drawable.line_divider));
        }

        binding.rvDrafts.addItemDecoration(dividerItemDecoration);
    }

    private void setUpClickListener() {

        draftsArrayAdapter.setOnItemClickListener(new DraftsArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {

                Intent intent = new Intent();
                // Pass relevant data back as a result

                intent.putExtra("draft", Parcels.wrap(drafts.get(position)));
                // Activity finished ok, return the data
                setResult(RESULT_OK, intent); // set result code and bundle data for response
                finish(); // closes the activity, pass data to
            }
        });
    }
}
