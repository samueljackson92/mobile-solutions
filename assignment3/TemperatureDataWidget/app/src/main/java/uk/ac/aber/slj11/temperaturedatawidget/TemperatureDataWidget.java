package uk.ac.aber.slj11.temperaturedatawidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TemperatureDataWidgetConfigureActivity TemperatureDataWidgetConfigureActivity}
 */
public class TemperatureDataWidget extends AppWidgetProvider {
    public static final String DATA_SOURCE = "uk.ac.aber.slj11.temperaturedatawidget.DATA_SOURCE";

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
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.temperature_data_widget);

        // setup click widget event handler intent
        Intent configIntent = new Intent(context, TemperatureDataWidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        configIntent.putExtra(DATA_SOURCE, TemperatureDataWidgetConfigureActivity.loadDataSourcePref(context, appWidgetId));
        Uri data = Uri.withAppendedPath(Uri.parse("widget://widget/id/#click"+appWidgetId), String.valueOf(appWidgetId));
        configIntent.setData(data);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.mainGrid_layout, pendingIntent);

        //reload button
        Intent reloadIntent = new Intent(context, UpdateFromDataSourceService.class);
        reloadIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        reloadIntent.putExtra(DATA_SOURCE, TemperatureDataWidgetConfigureActivity.loadDataSourcePref(context, appWidgetId));
        data = Uri.withAppendedPath(Uri.parse("widget://widget/id/#reload"+appWidgetId), String.valueOf(appWidgetId));
        reloadIntent.setData(data);
        pendingIntent = PendingIntent.getService(context, 0, reloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.reload_button, pendingIntent);

        // Build the intent to call the service
        Intent updateIntent = new Intent(context, UpdateFromDataSourceService.class);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        updateIntent.putExtra(DATA_SOURCE, TemperatureDataWidgetConfigureActivity.loadDataSourcePref(context, appWidgetId));
        data = Uri.withAppendedPath(Uri.parse("widget://widget/id/#config"+appWidgetId), String.valueOf(appWidgetId));
        updateIntent.setData(data);
        context.startService(updateIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

