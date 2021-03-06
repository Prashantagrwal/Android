package com.example.dell.sunshine.app;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class ForcastAdapter extends CursorAdapter {
    String log_tag = ForcastAdapter.class.getSimpleName();
    Cursor cursor;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private boolean mUseTodayLayout = true;

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView cityNameView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            cityNameView=  (TextView) view.findViewById(R.id.list_item_city_textview);
        }
    }

    public ForcastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
                mUseTodayLayout = useTodayLayout;
            }
    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        if (viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forcast_today;
        }
        else if (viewType == VIEW_TYPE_FUTURE_DAY) {
            layoutId = R.layout.list_item_forcast;
        }
        // TODO: Determine layoutId from viewType
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.


        ViewHolder viewHolder = (ViewHolder) view.getTag();

                        int viewType = getItemViewType(cursor.getPosition());
                switch (viewType) {
                        case VIEW_TYPE_TODAY: {
                                // Get weather icon
                                        viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(
                                                        cursor.getInt(MainActivityFragment.COL_WEATHER_COND)));
                                break;
                            }
                        case VIEW_TYPE_FUTURE_DAY: {
                                // Get weather icon
                                        viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
                                                        cursor.getInt(MainActivityFragment.COL_WEATHER_COND)));
                                break;
                            }
                    }

                        // Read date from cursor
                                long dateInMillis = cursor.getLong(MainActivityFragment.COL_WEATHER_DATE);
                // Find TextView and set formatted date on it
                        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

                        // Read weather forecast from cursor
                               String description = cursor.getString(MainActivityFragment.COL_WEATHER_DSC);
                // Find TextView and set weather forecast on it
                        viewHolder.descriptionView.setText(description);
        viewHolder.iconView.setContentDescription(description);
                        // Read user preference for metric or imperial temperature units
                               boolean isMetric = Utility.isMetric(context);

                        // Read high temperature from cursor
                                double high = cursor.getDouble(MainActivityFragment.COL_WEATHER_MAX_TEMP);
                viewHolder.highTempView.setText(Utility.formatTemperature(context, high));

                       // Read low temperature from cursor
                                double low = cursor.getDouble(MainActivityFragment.COL_WEATHER_MIN_TEMP);
                viewHolder.lowTempView.setText(Utility.formatTemperature(context, low));
        String city=cursor.getString(MainActivityFragment.COL_LOCATION_CITY);
        Log.v(log_tag,city);
       viewHolder.cityNameView.setText(city);
    }
}
