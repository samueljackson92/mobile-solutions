package uk.ac.aber.slj11.temperaturedatawidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TemperatureDataWidgetConfigureActivity TemperatureDataWidgetConfigureActivity}
 */
public class TemperatureDataWidget extends AppWidgetProvider {
    public static final String DATA_SOURCE = "uk.ac.aber.slj11.temperaturedatawidget.DATA_SOURCE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            refreshWidgetContents(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            TemperatureDataWidgetConfigureActivity.deleteDataSourcePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    /** Starts a service to gather data from the data source
     *
     * @param context context of the update
     * @param appWidgetId ID of the widget requesting an update
     */
    public void refreshWidgetContents(Context context, int appWidgetId) {
        // Call reload intent to update interface
        // A new remote view object is created in the service and the view is updated there.
        Intent reloadIntent = createIntent(context, TemperatureDataSourceService.class, "reload", appWidgetId);
        context.startService(reloadIntent);
    }

    /** Helper method to create intents
     *
     * Makes sure the widget id and URI path are included
     *
     * @param context context of the intent
     * @param cls class to target the intent at
     * @param action string action to be encoded in the URI
     * @param appWidgetId the id of the widget sending the intent
     * @return an intent with the appropriate application data attached
     */
    private Intent createIntent(Context context, Class cls, String action, int appWidgetId) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(DATA_SOURCE, TemperatureDataWidgetConfigureActivity.loadDataSourcePref(context, appWidgetId));
        Uri data = Uri.withAppendedPath(Uri.parse("widget://widget/id/#"+action+appWidgetId), String.valueOf(appWidgetId));
        intent.setData(data);
        return intent;
    }
}

