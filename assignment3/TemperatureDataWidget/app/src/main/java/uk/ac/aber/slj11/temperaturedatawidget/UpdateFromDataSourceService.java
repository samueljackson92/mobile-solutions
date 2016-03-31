package uk.ac.aber.slj11.temperaturedatawidget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by samuel on 31/03/16.
 */
public class UpdateFromDataSourceService extends IntentService {

    public static final String CURRENT_TEMP = "uk.ac.aber.slj11.temperaturedatawidget.CurrentTemperature";

    static final String URL = "http://users.aber.ac.uk/aos/CSM22/temp1data.php";

    public UpdateFromDataSourceService() {
        super("UpdateDataSourceService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        // Gets data from the incoming Intent
//        String urlString = intent.getDataString();
//        String xmlData = getXmlFromUrl(urlString);
//
//        XMLDataSourceParser parser = new XMLDataSourceParser();
//        InputStream stream = new ByteArrayInputStream(xmlData.getBytes());
//        Document doc = parser.getDocument(stream);
//
//        RemoteViews views = new RemoteViews(getPackageName(), R.layout.temperature_data_widget);
//        views.setTextViewText(R.id.averageTemp_text, "Hello!");

        Log.i("TESTING", "HEEEEEEEEEEEEEEEEEELLLLLLLLLOOOOOOOOOO");
//
////
//        final Context context = UpdateFromDataSourceService.this;
//        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

//        RemoteViews views = new RemoteViews(this.getApplicationContext().getPackageName(),
//                R.layout.temperature_data_widget);
//        views.setTextViewText(R.id.currentTime_text, "TEst");

        Context context = this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.temperature_data_widget);
        ComponentName thisWidget = new ComponentName(context, TemperatureDataWidget.class);
        remoteViews.setTextViewText(R.id.currentTemperature_text, "myText" + System.currentTimeMillis());
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);

//        TemperatureDataWidget.updateAppWidget(context, appWidgetManager, widgetId);


//        AppWidgetManager appWidgetMan = AppWidgetManager.getInstance(this);
//        RemoteViews views = new RemoteViews(this.getPackageName(),R.layout.temperature_data_widget);
//        views.setTextViewText(R.id.currentTemperature_text, "Test");
//        appWidgetMan.updateAppWidget(widgetId, views);
//
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction(TemperatureDataWidget.UPDATE_RESPONSE);
//        broadcastIntent.putExtra(CURRENT_TEMP, "Test");
//        broadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
//        sendBroadcast(broadcastIntent);
    }

    private String getXmlFromUrl(String urlString) {
        StringBuffer output = new StringBuffer("");

        InputStream stream = null;
        URL url;
        try {
            url = new URL(urlString);
            URLConnection connection = url.openConnection();

            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(stream));
                String s = "";
                while ((s = buffer.readLine()) != null)
                    output.append(s);
            }
        } catch (MalformedURLException e) {
            Log.e("Error", "Unable to parse URL", e);
        } catch (IOException e) {
            Log.e("Error", "IO Exception", e);
        }

        return output.toString();
    }
}