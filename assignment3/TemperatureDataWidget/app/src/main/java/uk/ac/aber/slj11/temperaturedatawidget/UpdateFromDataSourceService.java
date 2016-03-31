package uk.ac.aber.slj11.temperaturedatawidget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import uk.ac.aber.slj11.temperaturedata.model.TemperatureData;
import uk.ac.aber.slj11.temperaturedatasourceparser.XMLDataSourceParser;

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

        Log.i("TESTING", "Running update from data source service.");
        // Gets data from the incoming Intent
        String urlString = intent.getDataString();
        String xmlData = getXmlFromUrl(URL);

        XMLDataSourceParser parser = new XMLDataSourceParser();
        InputStream stream = new ByteArrayInputStream(xmlData.getBytes());

        // parse the XML document returner from the URL
        Document doc = parser.getDocument(stream);
        TemperatureData data = parser.parseDataSource(doc);
        updateWidget(data);
    }

    private String formatTemperatureForDisplay(int prefixId, double temp) {
        DecimalFormat df = new DecimalFormat("#.00");
        return getString(prefixId) + " " + df.format(temp) + "C";
    }

    private void updateWidget(TemperatureData data) {
        // get connection to remote views
        Context context = this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.temperature_data_widget);
        ComponentName thisWidget = new ComponentName(context, TemperatureDataWidget.class);

        // pull data from model
        String currentTemp = formatTemperatureForDisplay(R.string.current_temp, data.getCurrentTemperature());
        String averageTemp = formatTemperatureForDisplay(R.string.average_temp, data.getAverageTemperatureForLastHour());
        String minTemp = formatTemperatureForDisplay(R.string.min_temp, data.getMinTemperatureForLastHour());
        String maxTemp = formatTemperatureForDisplay(R.string.max_temp, data.getMaxTemperatureForLastHour());

        // set text on interface
        remoteViews.setTextViewText(R.id.currentTime_text, data.getCurrentTimeFormatted());
        remoteViews.setTextViewText(R.id.currentTemperature_text, currentTemp);
        remoteViews.setTextViewText(R.id.averageTemp_text, averageTemp);
        remoteViews.setTextViewText(R.id.minTemp_text, minTemp);
        remoteViews.setTextViewText(R.id.maxTemp_text, maxTemp);



        XYPlot plot = new XYPlot(context, getString(R.string.graph_title));
        plot.measure(0, 0);
        plot.layout(0, 0, 780, 250);
        plot.setDrawingCacheEnabled(true);
        plot.getLegendWidget().setVisible(false);

        // Create a couple arrays of y-values to plot:
        ArrayList<Double> readings = data.getReadingsForLastHour();

        plot.getGraphWidget().getRangeTickLabelPaint().setTextSize(20);
        plot.getGraphWidget().getRangeOriginTickLabelPaint().setTextSize(20);
        plot.getGraphWidget().getDomainTickLabelPaint().setTextSize(20);
        plot.getGraphWidget().getDomainOriginTickLabelPaint().setTextSize(20);

        plot.getGraphWidget().setPaddingTop(10);
        plot.getGraphWidget().setPaddingRight(10);
        plot.getGraphWidget().refreshLayout();

        Log.i("TESTING", Integer.toString(readings.size()));
        if (readings.size() == 1) {
            // duplicate single data point
            // this prevents the graph from looking weird
            // on the x-axis
            readings.add(readings.get(0));
        }

        Number[] series1Numbers = new Double[readings.size()];
        readings.toArray(series1Numbers);

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

        plot.setDomainValueFormat(new DecimalFormat("#"));

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);

        plot.getDomainLabelWidget().setHeight(1);

        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
        // To get rid of them call disableAllMarkup():
        //plot.disableAllMarkup();
        Bitmap bitmap = plot.getDrawingCache();
        remoteViews.setImageViewBitmap(R.id.temperatureView_graph, bitmap);



        // update widget
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
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