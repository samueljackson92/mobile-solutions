package uk.ac.aber.slj11.temperaturedatawidget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import uk.ac.aber.slj11.temperaturedata.model.TemperatureData;
import uk.ac.aber.slj11.temperaturedata.parser.XMLDataSourceParser;

/**
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
        String urlString = intent.getStringExtra(TemperatureDataWidget.DATA_SOURCE);

        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        Log.i(LOG_ID, "Getting data from source: " + urlString);

        try {
            // load data from URL
            String xmlData = getXmlFromUrl(urlString);

            XMLDataSourceParser parser = new XMLDataSourceParser();
            InputStream stream = new ByteArrayInputStream(xmlData.getBytes());

            // parse the XML document returner from the URL
            Document doc = parser.getDocument(stream);
            TemperatureData data = parser.parseDataSource(doc);
            updateWidget(data, widgetId);

        } catch (IOException e) {
            showErrorMessage();
        } catch (SAXException e) {
            showErrorMessage();
        } catch (ParserConfigurationException e) {
            showErrorMessage();
        }
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
        // add the newly gathered data to the view
        updateRemoteViewsInterface(context, remoteViews, data);
        // bind new pending intents to remote views
        TemperatureDataWidget.buildPendingIntents(getApplicationContext(), remoteViews, widgetId);
        // update widget with new view
        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    private void updateRemoteViewsInterface(Context context, RemoteViews remoteViews, TemperatureData data) {
        // pull data from model
        String currentTemp = formatTemperatureForDisplay(R.string.current_temp, data.getCurrentTemperature());
        String averageTemp = formatTemperatureForDisplay(R.string.average_temp, data.getAverageTemperatureForLastHour());
        String minTemp = formatTemperatureForDisplay(R.string.min_temp, data.getMinTemperatureForDay());
        String maxTemp = formatTemperatureForDisplay(R.string.max_temp, data.getMaxTemperatureForDay());
        Bitmap bitmap = makeGraph(context, data);

        // set text on interface
        remoteViews.setTextViewText(R.id.currentTime_text, data.getCurrentTimeFormatted());
        remoteViews.setTextViewText(R.id.currentTemperature_text, currentTemp);
        remoteViews.setTextViewText(R.id.averageTemp_text, averageTemp);
        remoteViews.setTextViewText(R.id.minTemp_text, minTemp);
        remoteViews.setTextViewText(R.id.maxTemp_text, maxTemp);
        remoteViews.setImageViewBitmap(R.id.temperatureView_graph, bitmap);
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
        plot.layout(0, 0, 1200, 500);
        plot.setDrawingCacheEnabled(true);
        plot.getLegendWidget().setVisible(false);

        //set size of range & domain labels
        plot.getGraphWidget().getRangeTickLabelPaint().setTextSize(20);
        plot.getGraphWidget().getRangeOriginTickLabelPaint().setTextSize(20);
        plot.getGraphWidget().getDomainTickLabelPaint().setTextSize(20);
        plot.getGraphWidget().getDomainOriginTickLabelPaint().setTextSize(20);

        // add padding for display
        plot.getGraphWidget().setPaddingLeft(30);
        plot.getGraphWidget().setPaddingTop(10);
        plot.getGraphWidget().setPaddingRight(10);

        // reduce number of Y axis ticks
        plot.setTicksPerRangeLabel(3);

        // refresh to update display
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

        // set axis formatting
        plot.setDomainValueFormat(makeFormatForYAxis(minutesArray));
        plot.setRangeValueFormat(new DecimalFormat("#.##"));

        return series;
    }

    private NumberFormat makeFormatForYAxis(final Integer[] xValues) {
        return new NumberFormat() {
            @Override
            public StringBuffer format(double value, StringBuffer buffer, FieldPosition field) {
                return new StringBuffer(Integer.toString(xValues[(int) value]));
            }

            @Override
            public StringBuffer format(long value, StringBuffer buffer, FieldPosition field) {
                return null;
            }

            @Override
            public Number parse(String string, ParsePosition position) {
                return null;
            }
        };
    }

    private LineAndPointFormatter makeLineFormatter() {
        LineAndPointFormatter seriesFormatter = new LineAndPointFormatter(
                Color.rgb(200, 200, 200),                   // line color
                Color.rgb(700, 700, 700),                   // point color
                Color.rgb(100, 100, 600),                   // point color
                null);                                  // fill color (none)

        return seriesFormatter;
    }

    private String getXmlFromUrl(String urlString) throws IOException {
        StringBuffer output = new StringBuffer("");

        InputStream stream;
        URL url = new URL(urlString);
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
        } else {
            throw new IOException("Could not load data.");
        }
        return output.toString();
    }

    private void showErrorMessage() {
        new Handler(Looper.getMainLooper()).post(new DisplayToast());
    }

    private class DisplayToast implements Runnable{
        public void run(){
            String message = getString(R.string.data_source_error_message);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}

