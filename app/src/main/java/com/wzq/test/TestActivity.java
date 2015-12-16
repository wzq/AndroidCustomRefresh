package com.wzq.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.wzq.customrefresh.CustomRefreshLayout;


public class TestActivity extends Activity {

    private CustomRefreshLayout mRefreshLayout;
    private ListView mList;
    private Button mStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mRefreshLayout = (CustomRefreshLayout) findViewById(R.id.refresh_layout);
        mList = (ListView) findViewById(R.id.list);
        mStop = (Button) findViewById(R.id.stop_refresh);

        String[] strs = {
            "The",
            "Canvas",
            "class",
            "holds",
            "the",
            "draw",
            "calls",
            ".",
            "To",
            "draw",
            "something,",
            "you",
            "need",
            "4 basic",
            "components",
            "Bitmap",
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strs);
        mList.setAdapter(adapter);

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRefreshLayout.finishRefreshing();
            }
        });

        mRefreshLayout.setOnRefreshListener(
                new CustomRefreshLayout.OnCircleRefreshListener() {
            @Override
            public void refreshing() {
                // do something when refresh starts
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.finishRefreshing();
                    }
                }, 3000);
            }

            @Override
            public void completeRefresh() {
                // do something when refresh complete
            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
