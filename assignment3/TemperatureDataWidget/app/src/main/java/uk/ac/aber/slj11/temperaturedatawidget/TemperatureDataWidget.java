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

        XYPlot plot = new XYPlot(context, "Temperature");
        plot.measure(10, 10);
        plot.layout(0, 0, 780, 250);
        plot.setDrawingCacheEnabled(true);

        // Create a couple arrays of y-values to plot:
        Number[] series1Numbers = {1, 8, 5, 2, 7, 4};

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "");                             // Set the display title of the series

        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(200, 200, 200),                   // line color
                Color.rgb(700, 700, 700),                   // point color
                Color.rgb(100, 100, 600),                   // point color
                null);                                  // fill color (none)

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);

        plot.getDomainLabelWidget().setHeight(1);

        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
        // To get rid of them call disableAllMarkup():
        //plot.disableAllMarkup();
        Bitmap bitmap = plot.getDrawingCache();

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.temperature_data_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setImageViewBitmap(R.id.temperatureView_graph, bitmap);

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

