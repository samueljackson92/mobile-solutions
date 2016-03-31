package uk.ac.aber.slj11.temperaturedatawidget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
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
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;

import uk.ac.aber.slj11.temperaturedata.model.TemperatureData;
import uk.ac.aber.slj11.temperaturedatasourceparser.XMLDataSourceParser;

/**
 * Created by samuel on 31/03/16.
 */
public class UpdateFromDataSourceService extends IntentService {

    public UpdateFromDataSourceService() {
        super("UpdateDataSourceService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i("TESTING", "Running update from data source service.");
        String urlString = intent.getStringExtra(TemperatureDataWidget.DATA_SOURCE);

        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        Log.i("TESTING", "Getting data from source: " + urlString);
        String xmlData = getXmlFromUrl(urlString);

        XMLDataSourceParser parser = new XMLDataSourceParser();
        InputStream stream = new ByteArrayInputStream(xmlData.getBytes());

        // parse the XML document returner from the URL
        Document doc = parser.getDocument(stream);
        TemperatureData data = parser.parseDataSource(doc);
        updateWidget(data, widgetId);
    }

    private String formatTemperatureForDisplay(int prefixId, double temp) {
        DecimalFormat df = new DecimalFormat("#.00");
        return getString(prefixId) + " " + df.format(temp) + "C";
    }

    private void updateWidget(TemperatureData data, int widgetId) {
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
        Bitmap bitmap = makeGraph(context, data);

        // set text on interface
        remoteViews.setTextViewText(R.id.currentTime_text, data.getCurrentTimeFormatted());
        remoteViews.setTextViewText(R.id.currentTemperature_text, currentTemp);
        remoteViews.setTextViewText(R.id.averageTemp_text, averageTemp);
        remoteViews.setTextViewText(R.id.minTemp_text, minTemp);
        remoteViews.setTextViewText(R.id.maxTemp_text, maxTemp);
        remoteViews.setImageViewBitmap(R.id.temperatureView_graph, bitmap);

        // update widget
        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    private Bitmap makeGraph(Context context, TemperatureData data) {
        XYPlot plot = makePlot(context);
        XYSeries series = makeSeriesData(plot, data);
        LineAndPointFormatter seriesFormatter = makeLineFormatter();

        plot.addSeries(series, seriesFormatter);
        return plot.getDrawingCache();
    }

    private XYPlot makePlot(Context context) {
        XYPlot plot = new XYPlot(context, getString(R.string.graph_title));
        plot.measure(0, 0);

        plot.layout(0, 0, 1000, 500); //780, 250
        plot.setDrawingCacheEnabled(true);
        plot.getLegendWidget().setVisible(false);

        plot.getGraphWidget().getRangeTickLabelPaint().setTextSize(20);
        plot.getGraphWidget().getRangeOriginTickLabelPaint().setTextSize(20);
        plot.getGraphWidget().getDomainTickLabelPaint().setTextSize(20);
        plot.getGraphWidget().getDomainOriginTickLabelPaint().setTextSize(20);

        plot.getGraphWidget().setPaddingLeft(30);
        plot.getGraphWidget().setPaddingTop(10);
        plot.getGraphWidget().setPaddingRight(10);

        plot.setTicksPerRangeLabel(3);

        plot.getGraphWidget().refreshLayout();
        return plot;
    }

    private XYSeries makeSeriesData(XYPlot plot, TemperatureData data) {
        // Create a couple arrays of y-values to plot:
        ArrayList<Double> readings = data.getReadingValuesForLastHour();
        ArrayList<Integer> minutes = data.getMinutesForLastHour();

        if (readings.size() == 1) {
            // duplicate single data point
            // this prevents the graph from looking weird
            // on the x-axis
            readings.add(readings.get(0));
        }

        Number[] readingsArray = new Double[readings.size()];
        readings.toArray(readingsArray);

        final Integer[] minutesArray = new Integer[minutes.size()];
        minutes.toArray(minutesArray);

        // Turn the above arrays into XYSeries':
        XYSeries series = new SimpleXYSeries(
                Arrays.asList(readingsArray),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "");

        plot.setDomainValueFormat(new NumberFormat() {
            @Override
            public StringBuffer format(double value, StringBuffer buffer, FieldPosition field) {
                return new StringBuffer(Integer.toString(minutesArray[(int) value]));
            }

            @Override
            public StringBuffer format(long value, StringBuffer buffer, FieldPosition field) {
                return null;
            }

            @Override
            public Number parse(String string, ParsePosition position) {
                return null;
            }
        });

        plot.setRangeValueFormat(new DecimalFormat("#.##"));

        return series;
    }

    private LineAndPointFormatter makeLineFormatter() {
        LineAndPointFormatter seriesFormatter = new LineAndPointFormatter(
                Color.rgb(200, 200, 200),                   // line color
                Color.rgb(700, 700, 700),                   // point color
                Color.rgb(100, 100, 600),                   // point color
                null);                                  // fill color (none)

        return seriesFormatter;
    }

    private String getXmlFromUrl(String urlString) {
        StringBuffer output = new StringBuffer("");

        InputStream stream;
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