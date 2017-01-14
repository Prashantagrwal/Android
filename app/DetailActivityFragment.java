package com.example.dell.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dell.sunshine.app.dataforforcast.ContractForForcast;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{


    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final String ForCast_Data = "#SunShineApp";
    private ShareActionProvider mShareActionProvider;
    private String mForecast;
    private Uri mUri;
    static final String DETAIL_URI = "URI";
    private static final int CURSOR_ID=0;

    private static final String[] FORECAST_COLUMNS={
            ContractForForcast.WeatherEntry.TABLE_NAME + "." + ContractForForcast.WeatherEntry._ID,
            ContractForForcast.WeatherEntry.COLUMN_DATE,
            ContractForForcast.WeatherEntry.COLUMN_SHORT_DESC,
            ContractForForcast.WeatherEntry.COLUMN_MAX_TEMP,
            ContractForForcast.WeatherEntry.COLUMN_MIN_TEMP,
            ContractForForcast.WeatherEntry.COLUMN_HUMIDITY,
            ContractForForcast.WeatherEntry.COLUMN_WIND_SPEED,
            ContractForForcast.WeatherEntry.COLUMN_PRESSURE,
            ContractForForcast.WeatherEntry.COLUMN_DEGREES,
            ContractForForcast.WeatherEntry.COLUMN_WEATHER_ID,
    ContractForForcast.LocationEntry.COLUMN_SETTING};

    static final int COL_WEATHER_ID=0;
    static final int COL_WEATHER_DATE=1;
    static final int COL_WEATHER_DSC=2;
    static final int COL_WEATHER_MAX_TEMP=3;
    static final int COL_WEATHER_MIN_TEMP=4;
    static final int COL_WEATHER_HUMIDITY=5;
    static final int COL_WEATHER_WIND_SPEED=6;
    static final int COL_WEATHER_PRESSURE=7;
    static final int COL_WEATHER_DEGREES=8;
    static final int COL_WEATHER_COND=9;



    private  ImageView IconView;
    private  TextView DateView;
    private  TextView MonthView;
    private  TextView DescriptionView;
    private  TextView HighTempView;
    private  TextView LowTempView;
    private TextView  WindView;
    private TextView HumidityView;
    private TextView PressureView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
                if (arguments != null) {
                        mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
                    }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        IconView = (ImageView) rootView.findViewById(R.id.detail_icon);
                DateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
                MonthView = (TextView) rootView.findViewById(R.id.detail_day_textview);
                DescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
                HighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
                LowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
                HumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
                WindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
                PressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragmentshare, menu);
        MenuItem item = menu.findItem(R.id.action_Shared);
         mShareActionProvider = (ShareActionProvider) MenuItemCompat.
                getActionProvider(item);
      //  if(mForecast!=null)
       // mShareActionProvider.setShareIntent(createShareForecastIntent());
        }

        private Intent createShareForecastIntent() {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + ForCast_Data);
                return shareIntent;
            }

        void onLocationChanged( String newLocation ) {
                // replace the uri, since the location has changed
                        Uri uri = mUri;
                if (null != uri) {
                        long date = ContractForForcast.WeatherEntry.getDateFromUri(uri);
                        Uri updatedUri = ContractForForcast.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
                        mUri = updatedUri;
                        getLoaderManager().restartLoader(CURSOR_ID, null, this);
                    }
            }
    @Override
            public void onActivityCreated(Bundle savedInstanceState) {
                    getLoaderManager().initLoader(CURSOR_ID, null, this);
                    super.onActivityCreated(savedInstanceState);
                }

                    @Override
           public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    Log.v(LOG_TAG, "In onCreateLoader");
                        if ( null != mUri ) {
                                        // Now create and return a CursorLoader that will take care of
                                                // creating a Cursor for the data being displayed.
                                                        return new CursorLoader(
                                    getActivity(),
                                                        mUri,
                                                        FORECAST_COLUMNS,
                                                        null,
                                                        null,
                                                        null
                                                        );
                                    }
                                return null; }

                    @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                        Log.v(LOG_TAG, "In onLoadFinished");
                                    if (!data.moveToFirst()) { return; }

                        int weatherId = data.getInt(COL_WEATHER_COND);

                                            // Use weather art image
                                                   IconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

                                            // Read date from cursor and update views for day of week and date
                                                    long date = data.getLong(COL_WEATHER_DATE);
                                    String friendlyDateText = Utility.getDayName(getActivity(), date);
                                    String dateText = Utility.getFormattedMonthDay(getActivity(), date);
                                    DateView.setText(friendlyDateText);
                                    MonthView.setText(dateText);

                                            // Read description from cursor and update view
                                                    String description = data.getString(COL_WEATHER_DSC);
                                    DescriptionView.setText(description);
                        IconView.setContentDescription(description);

                                            // Read high temperature from cursor and update view
                                                    boolean isMetric = Utility.isMetric(getActivity());

                                            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
                                    String highString = Utility.formatTemperature(getActivity(), high);
                                    HighTempView.setText(highString);

                                            // Read low temperature from cursor and update view
                                                    double low = data.getDouble(COL_WEATHER_MIN_TEMP);
                                    String lowString = Utility.formatTemperature(getActivity(), low);
                                    LowTempView.setText(lowString);

                                            // Read humidity from cursor and update view
                                                    float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
                                    HumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

                                            // Read wind speed and direction from cursor and update view
                                                    float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
                                    float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
                                    WindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

                                            // Read pressure from cursor and update view
                                                    float pressure = data.getFloat(COL_WEATHER_PRESSURE);
                                    PressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

                                            // We still need this for the share intent
                                                   mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);

                                            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
                                                    if (mShareActionProvider != null) {
                                            mShareActionProvider.setShareIntent(createShareForecastIntent());
                                        }
                                }
                            @Override
            public void onLoaderReset(Loader<Cursor> loader) { }
}

