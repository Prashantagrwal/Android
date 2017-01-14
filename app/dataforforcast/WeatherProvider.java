/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.dell.sunshine.app.dataforforcast;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class WeatherProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private weatherdbhelper mOpenHelper;
    private final String LOG_TAG = WeatherProvider.class.getSimpleName();

    public static final int WEATHER = 100;
    public static final int WEATHER_WITH_LOCATION = 101;
    public static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    public static final int LOCATION = 300;

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static{
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sWeatherByLocationSettingQueryBuilder.setTables(
                ContractForForcast.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        ContractForForcast.LocationEntry.TABLE_NAME +
                        " ON " + ContractForForcast.WeatherEntry.TABLE_NAME +
                        "." + ContractForForcast.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + ContractForForcast.LocationEntry.TABLE_NAME +
                        "." + ContractForForcast.LocationEntry._ID);
    }
    //location.location_setting = ?
    private static final String sLocationSettingSelection =
            ContractForForcast.LocationEntry.TABLE_NAME+
                    "." + ContractForForcast.LocationEntry.COLUMN_SETTING + " = ? ";

    //location.location_setting = ? AND date >= ?
    private static final String sLocationSettingWithStartDateSelection =
            ContractForForcast.LocationEntry.TABLE_NAME+
                    "." + ContractForForcast.LocationEntry.COLUMN_SETTING + " = ? AND " +
                    ContractForForcast.WeatherEntry.COLUMN_DATE + " >= ? ";

    //location.location_setting = ? AND date = ?
    private static final String sLocationSettingAndDaySelection =
            ContractForForcast.LocationEntry.TABLE_NAME +
                    "." + ContractForForcast.LocationEntry.COLUMN_SETTING + " = ? AND " +
                    ContractForForcast.WeatherEntry.COLUMN_DATE + " = ? ";

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = ContractForForcast.WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = ContractForForcast.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
            selection = sLocationSettingWithStartDateSelection;
        }

        return  sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeatherByLocationSettingAndDate(Uri uri, String[] projection, String sortOrder)
    {
        String locationSetting = ContractForForcast.WeatherEntry.getLocationSettingFromUri(uri);
        long date = ContractForForcast.WeatherEntry.getDateFromUri(uri);

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

    public static UriMatcher buildUriMatcher() {
        UriMatcher matcher=new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(ContractForForcast.CONTENT_AUTHORITY,ContractForForcast.PATH_WEATHER+"/",
                WEATHER);
        matcher.addURI(ContractForForcast.CONTENT_AUTHORITY,ContractForForcast.PATH_WEATHER+"/*",
                WEATHER_WITH_LOCATION);
        matcher.addURI(ContractForForcast.CONTENT_AUTHORITY,ContractForForcast.PATH_WEATHER+
                "/*/#",WEATHER_WITH_LOCATION_AND_DATE);
        matcher.addURI(ContractForForcast.CONTENT_AUTHORITY,ContractForForcast.PATH_LOCATION+"/",
                LOCATION);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new weatherdbhelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
             case WEATHER_WITH_LOCATION_AND_DATE:
                 return ContractForForcast.WeatherEntry.CONTENT_ITEM_TYPE;
          case WEATHER_WITH_LOCATION:
              return ContractForForcast.WeatherEntry.CONTENT_TYPE;
            case WEATHER:
                return ContractForForcast.WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return ContractForForcast.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case WEATHER_WITH_LOCATION_AND_DATE:
            {
                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case WEATHER_WITH_LOCATION: {
                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case WEATHER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ContractForForcast.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,null,sortOrder
                        );
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ContractForForcast.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,null,sortOrder
                );

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case WEATHER: {
                normalizeDate(values);
                long _id = db.insert(ContractForForcast.WeatherEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ContractForForcast.WeatherEntry.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION:{
                long _id = db.insert(ContractForForcast.LocationEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ContractForForcast.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int RowDelete;
        final int match = sUriMatcher.match(uri);
        if(null==selection) selection="1";
        switch (match) {
            case WEATHER: {
           RowDelete=db.delete(ContractForForcast.WeatherEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }
        case LOCATION: {
            RowDelete=db.delete(ContractForForcast.LocationEntry.TABLE_NAME,selection,selectionArgs);
            break;
        }
        default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
        // return the actual rows deleted
        if(RowDelete!=0)
        {
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return  RowDelete;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(ContractForForcast.WeatherEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(ContractForForcast.WeatherEntry.COLUMN_DATE);
            values.put(ContractForForcast.WeatherEntry.COLUMN_DATE, ContractForForcast.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int RowUpdate;
        final int match = sUriMatcher.match(uri);
        if(null==selection){selection="1";}
        switch (match) {
            case WEATHER: {
RowUpdate=db.update(ContractForForcast.WeatherEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            }
            case LOCATION:{
 RowUpdate=db.update(ContractForForcast.LocationEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            }
            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(RowUpdate!=0)
        {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return RowUpdate;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(ContractForForcast.WeatherEntry.TABLE_NAME,null,value);
                        Log.v(LOG_TAG, "FetchWeatherTask values:" + value +"\n");
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}