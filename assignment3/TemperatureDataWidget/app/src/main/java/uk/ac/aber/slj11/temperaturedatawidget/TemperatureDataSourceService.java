package uk.ac.aber.slj11.temperaturedatawidget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import uk.ac.aber.slj11.temperaturedata.model.TemperatureData;
import uk.ac.aber.slj11.temperaturedata.parser.XMLDataSourceParser;

/**
 * Temperature Data Service
 *
 * This class implements an IntentService that loads data from a URL. The service then updates
 * the widget interface with the data loaded from the external data source.
 *
 * Created by samuel on 31/03/16.
 */
public class TemperatureDataSourceService extends IntentService {
    static private final String LOG_ID = "TemperatureDataWidget:";
    public TemperatureDataSourceService() {
        super("UpdateDataSourceService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(LOG_ID, "Running update from data source service.");
        String urlString = intent.getStringExtra(TemperatureDataWidgetProvider.DATA_SOURCE);

        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        Log.i(LOG_ID, "Getting data from source: " + urlString);

        TemperatureData data = getTemperatureData(urlString);
        if (data != null) {
            updateWidget(data, widgetId);
        }
    }

    /** Get a temperature data object from a remote XML data source
     *
     * This uses a XML data source parser object to get the data and parse the response.
     * Exceptions are handled as toast popups.
     *
     * @param urlString the URL to parse the XML response from
     * @return a temperature data object with the loaded response
     */
    private TemperatureData getTemperatureData(String urlString) {
        try {
            XMLDataSourceParser parser = new XMLDataSourceParser();
            String xmlData = parser.getXmlFromUrl(urlString);
            Document doc = parser.getDocument(xmlData);
            return parser.parseDataSource(doc);
        } catch (IOException e) {
            showErrorMessage();
        } catch (SAXException e) {
            showErrorMessage();
        } catch (ParserConfigurationException e) {
            showErrorMessage();
        }
        return null;
    }

    /** Update the interface of the widget.
     *
     * This uses the data from the loaded response object to create a new remote view
     * and update the our widget
     *
     * @param data the data loaded from the data source
     * @param widgetId the id of the widget to create a new view for
     */
    private void updateWidget(TemperatureData data, int widgetId) {
        // get connection to remote views
        Context context = this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        // build new remote view
        TemperatureDataWidgetBuilder viewBuilder = new TemperatureDataWidgetBuilder();
        viewBuilder.setContext(context);
        viewBuilder.setWidgetId(widgetId);
        viewBuilder.setTemperatureData(data);

        // update widget with new view
        RemoteViews view = viewBuilder.buildWidget();
        appWidgetManager.updateAppWidget(widgetId, view);
    }

    /** Display an error message by showing a toast
     */
    private void showErrorMessage() {
        new Handler(Looper.getMainLooper()).post(new DisplayToast());
    }

    /** Class to display a toast from within an intent service.
     *
     * Based on code from: http://stackoverflow.com/questions/3955410/
     */
    private class DisplayToast implements Runnable{
        public void run(){
            String message = getString(R.string.data_source_error_message);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}

