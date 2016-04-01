package uk.ac.aber.slj11.temperaturedatawidget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Spinner;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;

import uk.ac.aber.slj11.temperaturedata.model.TemperatureData;

/**
 * The configuration screen for the {@link TemperatureDataWidget TemperatureDataWidget} AppWidget.
 */
public class TemperatureDataWidgetConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Spinner dataSourceSpinner;

    private static final String PREFS_NAME = "uk.ac.aber.slj11.temperaturedatawidget.TemperatureDataWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private static final String PREF_DATA_SOURCE_POSTFIX = "_data_source";

    public TemperatureDataWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);
        setContentView(R.layout.temperature_data_widget_configure);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);


        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // get data source option names & values
        String[] dataSourceNames = getResources().getStringArray(R.array.data_source_names);
        String[] dataSourceValues = getResources().getStringArray(R.array.data_source_values);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dataSourceNames);
        String source = loadDataSourcePref(TemperatureDataWidgetConfigureActivity.this, mAppWidgetId);

        // find current index of option
        int selectedOption = 0;
        for(int i =0; i < adapter.getCount(); ++i) {
            if(source.equals(dataSourceValues[i])) {
                selectedOption = i;
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // setup the spinner
        dataSourceSpinner = (Spinner) findViewById(R.id.dataSource_spinner);
        dataSourceSpinner.setAdapter(adapter);
        dataSourceSpinner.setSelection(selectedOption);
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = TemperatureDataWidgetConfigureActivity.this;

            // Store data source to use
            int option = dataSourceSpinner.getSelectedItemPosition();
            String[] dataSourceValues = getResources().getStringArray(R.array.data_source_values);
            String source = dataSourceValues[option];
            saveDataSourcePref(context, mAppWidgetId, source);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);

            // It is the responsibility of the configuration activity to update the app widget
            RemoteViews views =  new RemoteViews(context.getPackageName(), R.layout.temperature_data_widget);
            TemperatureDataWidget.buildPendingIntents(context, views, mAppWidgetId);

            // send intent to update widget immediately
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, context, TemperatureDataWidget.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {mAppWidgetId});
            sendBroadcast(intent);

            finish();
        }
    };

    static void saveDataSourcePref(Context context, int appWidgetId, String source) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + PREF_DATA_SOURCE_POSTFIX, source);
        prefs.commit();
    }

    static String loadDataSourcePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String sourceDefault = context.getResources().getString(R.string.data_source_default);
        String sourceValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId + PREF_DATA_SOURCE_POSTFIX, sourceDefault);
        return sourceValue;
    }

    static void deleteDataSourcePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + PREF_DATA_SOURCE_POSTFIX);
        prefs.commit();
    }

}

