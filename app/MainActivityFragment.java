package com.example.dell.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.dell.sunshine.app.dataforforcast.ContractForForcast;
import com.example.dell.sunshine.app.sync.SunshineSyncAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
,SwipeRefreshLayout.OnRefreshListener {
    //this is an adapter of string
    Cursor cur;
    String log_tag = MainActivityFragment.class.getSimpleName();
    ForcastAdapter adapter;

    public MainActivityFragment() {
    }

    Bundle b;
    private SwipeRefreshLayout swipeView;
    boolean refreshToggle = true;
    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private static final int CURSOR_ID = 0;
    private ListView lv;
    private boolean mUseTodayLayout;
    private static final String[] FORECAST_COLUMNS = {
            ContractForForcast.WeatherEntry.TABLE_NAME + "." + ContractForForcast.WeatherEntry._ID,
            ContractForForcast.WeatherEntry.COLUMN_DATE,
            ContractForForcast.WeatherEntry.COLUMN_SHORT_DESC,
            ContractForForcast.WeatherEntry.COLUMN_MAX_TEMP,
            ContractForForcast.WeatherEntry.COLUMN_MIN_TEMP,
            ContractForForcast.WeatherEntry.COLUMN_WEATHER_ID,
            ContractForForcast.LocationEntry.COLUMN_SETTING,
            ContractForForcast.LocationEntry.COLUMN_LATITUDE,
            ContractForForcast.LocationEntry.COLUMN_LONGITUDE,
            ContractForForcast.LocationEntry.COLUMN_CITY_NAME
    };
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DSC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_COND = 5;
    static final int COL_LOCATION_SETTING = 6;
    static final int COL_LOCATION_LAT = 7;
    static final int COL_LOCATION_LON = 8;
    static final int COL_LOCATION_CITY = 9;

    public interface Callback {
        public void onItemSelected(Uri dateUri);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_main, container, false);
        final List<String> weekForCast = new ArrayList<String>();
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = ContractForForcast.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherfrLocationUri = ContractForForcast.WeatherEntry.
                buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
        cur = getActivity().getContentResolver().query(weatherfrLocationUri, null, null, null, sortOrder);
        adapter = new ForcastAdapter(getActivity(), null, 0);
        swipeView = (SwipeRefreshLayout) RootView.findViewById(R.id.swipe_refresh_layout);
        swipeView.setOnRefreshListener(this);
        swipeView.setColorSchemeColors(Color.GRAY, Color.GREEN, Color.BLUE,
                Color.RED, Color.CYAN);
        swipeView.setDistanceToTriggerSync(20);// in dips
        swipeView.setSize(SwipeRefreshLayout.DEFAULT);

        lv = (ListView) RootView.findViewById(R.id.listView_forcast);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //we will call our Main Activity
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(ContractForForcast.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                }
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        adapter.setUseTodayLayout(mUseTodayLayout);
        return RootView;
    }

    @Override

    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CURSOR_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String loactonDate = Utility.getPreferredLocation(getActivity());
        String sortOrder = ContractForForcast.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherforLocation = ContractForForcast.WeatherEntry.
                buildWeatherLocationWithStartDate(loactonDate, System.currentTimeMillis());
        return new CursorLoader(getActivity(),
                weatherforLocation,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            lv.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);//this is used to show weather you want to see the menu option or  not
    }

    //adding the menu option by inflating-
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragmentforcast, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
     /*   if (id == R.id.action_Refresh) {
            UpDateWeather();
            return true;
        }*/

        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void UpDateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    public void OnLocationChange() {
        UpDateWeather();
        getLoaderManager().restartLoader(CURSOR_ID, null, this);
    }

    @Override
    public void onRefresh() {

        UpDateWeather();
        swipeView.setRefreshing(false);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (adapter != null) {
            adapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if (null != adapter) {
            Cursor c = adapter.getCursor();
            if (null != c) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_LOCATION_LAT);
                String posLong = c.getString(COL_LOCATION_LON);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(log_tag, "Couldn't call " + geoLocation.toString()
                            + ", no receiving apps installed!");
                }
            }
        }
    }

}









