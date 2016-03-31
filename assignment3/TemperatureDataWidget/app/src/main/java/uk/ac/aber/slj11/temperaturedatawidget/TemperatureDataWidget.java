package uk.ac.aber.slj11.temperaturedatawidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.Arrays;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TemperatureDataWidgetConfigureActivity TemperatureDataWidgetConfigureActivity}
 */
public class TemperatureDataWidget extends AppWidgetProvider {

    public static final String UPDATE_RESPONSE = "uk.ac.aber.slj11.temperaturedatawidget.UPDATE_RESPONSE";
    private static final String RELOAD_BUTTON = "uk.ac.aber.slj11.temperaturedatawidget.RELOAD_BUTTON";

    public String currentTemp;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            TemperatureDataWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
            TemperatureDataWidgetConfigureActivity.deleteDataSourcePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(intent.getAction().equals(RELOAD_BUTTON)) {
            // Build the intent to call the service
            Intent updateIntent = new Intent(context, UpdateFromDataSourceService.class);
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            context.startService(updateIntent);
        } else if (intent.getAction().equals(UPDATE_RESPONSE)) {
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

            currentTemp = intent.getStringExtra(UpdateFromDataSourceService.CURRENT_TEMP);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.temperature_data_widget);
            views.setTextViewText(R.id.currentTemperature_text, currentTemp);

            Toast.makeText(context, currentTemp,
                    Toast.LENGTH_LONG).show();
        }

    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        CharSequence widgetText = TemperatureDataWidgetConfigureActivity.loadTitlePref(context, appWidgetId);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.temperature_data_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);


        // setup click widget event handler intent
        Intent configIntent = new Intent(context, TemperatureDataWidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.mainGrid_layout, pendingIntent);

        //reload button
        views.setOnClickPendingIntent(R.id.reload_button, getPendingSelfIntent(context, RELOAD_BUTTON, appWidgetId));

        // Build the intent to call the service
        Intent updateIntent = new Intent(context, UpdateFromDataSourceService.class);
        context.startService(updateIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action, int appWidgetId) {
        Intent intent = new Intent(context, TemperatureDataWidget.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

}

