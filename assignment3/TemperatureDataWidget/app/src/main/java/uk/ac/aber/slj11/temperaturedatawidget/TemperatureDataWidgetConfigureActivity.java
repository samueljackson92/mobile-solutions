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
 * The configuration screen for the {@link TemperatureDataWidgetProvider TemperatureDataWidget} AppWidget.
 */
public class TemperatureDataWidgetConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Spinner dataSourceSpinner;

    private static final String PREFS_NAME = "uk.ac.aber.slj11.temperaturedatawidget.TemperatureDataWidgetProvider";
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dataSourceNames);
        int option = loadDataSourceIndexPref(TemperatureDataWidgetConfigureActivity.this, mAppWidgetId);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // setup the spinner
        dataSourceSpinner = (Spinner) findViewById(R.id.dataSource_spinner);
        dataSourceSpinner.setAdapter(adapter);
        dataSourceSpinner.setSelection(option);
    }

    /** Click event handler for the button on the configuration screen
     *
     * This setup the interface for the first time, add save the users selected preferences, and
     * send an intent to update the widget immediately.
     *
     */
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = TemperatureDataWidgetConfigureActivity.this;

            // Store data source to use
            int option = dataSourceSpinner.getSelectedItemPosition();
            saveDataSourcePref(context, mAppWidgetId, option);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            TemperatureDataWidgetBuilder viewBuilder = new TemperatureDataWidgetBuilder();
            viewBuilder.setContext(context);
            viewBuilder.setWidgetId(mAppWidgetId);

            RemoteViews view = viewBuilder.buildWidget();
            appWidgetManager.updateAppWidget(mAppWidgetId, view);

            // send intent to update widget immediately
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, context, TemperatureDataWidgetProvider.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {mAppWidgetId});
            sendBroadcast(intent);

            // exit the config screen
            finish();
        }
    };

    /** Save the users preferred choice of data source for this widget to the shared preferences
     *
     * @param context the current context
     * @param appWidgetId the widget id this preference relates to
     * @param source the string representing the data source to use
     */
    static void saveDataSourcePref(Context context, int appWidgetId, int source) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId + PREF_DATA_SOURCE_POSTFIX, source);
        prefs.commit();
    }

    /** Load the users preferred choice of data source for this widget from shared preferences
     *
     * @param context the current context
     * @param appWidgetId the widget id this preference relates to
     * @return a string URL representing the data source to load from
     */
    static int loadDataSourceIndexPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int sourceValue = prefs.getInt(PREF_PREFIX_KEY + appWidgetId + PREF_DATA_SOURCE_POSTFIX, 0);
        return sourceValue;
    }

    static String loadDataSourcePref(Context context, int appWidgetId) {
        String[] dataSourceValues = context.getResources().getStringArray(R.array.data_source_values);
        int sourceValue = loadDataSourceIndexPref(context, appWidgetId);
        return dataSourceValues[sourceValue];
    }

    /** Delete the users choice of data source for a particular widget from the shared preferences
     *
     * @param context the current context
     * @param appWidgetId the widget id this preference relates to
     */
    static void deleteDataSourcePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + PREF_DATA_SOURCE_POSTFIX);
        prefs.commit();
    }

}

