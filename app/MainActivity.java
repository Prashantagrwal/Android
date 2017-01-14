package com.example.dell.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.dell.sunshine.app.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {

 String log_tag = MainActivity.class.getSimpleName();
private String DETAILACTIVITYFRAGMENT_TAG="HII BABY";

    private boolean mTwoPane;
    private String mlocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mlocation = Utility.getPreferredLocation(this);
        //In this we are calling a fragment in which we have add a xml file and a java file.
        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container,
                                new DetailActivityFragment(), DETAILACTIVITYFRAGMENT_TAG).commit();
            }

        } else {
            mTwoPane = false;
        }
        MainActivityFragment forecastFragment =  ((MainActivityFragment)getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_forecast));
                forecastFragment.setUseTodayLayout(!mTwoPane);
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //this is used to create action bar
        int id = item.getItemId();

        //this is for pening the setting
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

     public void openPreferenceForLocation()
     {
         String location=Utility.getPreferredLocation(this);
         Uri GeoLocation =Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",location).
                 build();
         Intent i=new Intent(Intent.ACTION_VIEW);
         i.setData(GeoLocation);
         if(i.resolveActivity(getPackageManager())!=null)
         {
             startActivity(i);
         }
         else
         Log.d(log_tag,"couldn't find"+ location);
     }

   /* private void openPreferredLocationInMap() {
        String location = Utility.getPreferredLocation(this);
        Log.d(log_tag, "Couldn't call " + location + ", no receiving apps installed!");
    }*/

        @Override
        protected void onResume() {
                super.onResume();
                String location = Utility.getPreferredLocation( this );
                // update the location in our second pane using the fragment manager
                        if (location != null && !location.equals(mlocation)) {
                        MainActivityFragment ff = (MainActivityFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_forecast);
                        if ( null != ff ) {
                                ff.OnLocationChange();
                            }
                            DetailActivityFragment df=(DetailActivityFragment) getSupportFragmentManager().
                                    findFragmentByTag(DETAILACTIVITYFRAGMENT_TAG);
                        mlocation = location;
                    }
            }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (mTwoPane) {
                        // In two-pane mode, show the detail view in this activity by
                                // adding or replacing the detail fragment using a
                                        // fragment transaction.
                                                Bundle args = new Bundle();
                        args.putParcelable(DetailActivityFragment.DETAIL_URI, dateUri);

            DetailActivityFragment fragment = new DetailActivityFragment();
                        fragment.setArguments(args);

                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.weather_detail_container, fragment, DETAILACTIVITYFRAGMENT_TAG)
                                        .commit();
                    } else {
                        Intent intent = new Intent(this, DetailActivity.class)
                                        .setData(dateUri);
                        startActivity(intent);
                    }
            }

}
