package uk.ac.aber.slj11.temperaturedatawidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.widget.RemoteViews;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;

import uk.ac.aber.slj11.temperaturedata.model.TemperatureData;

/** Temperature Data Widget Remote View
 *
 * This extends a remote views object to encapsulate the custom operations we need to preform on
 * the temperature data widget. This is useful to ensure that the click event handlers are always
 * correctly setup and separates the view setup code from the rest of the application.
 *
 * Created by samuel on 10/04/16.
 */
public class TemperatureDataWidgetRemoteView extends RemoteViews {

    private final int widgetId;
    private final Context context;

    public TemperatureDataWidgetRemoteView(Context context, int layoutId, int widgetId) {
        super(context.getPackageName(), widgetId);

        this.widgetId = widgetId;
        this.context = context;

        setupEventHandlers();
    }

    /** Setup the event handlers for the interface
     *
     * This includes the reload button and the click to open then interface.
     */
    private void setupEventHandlers() {
        // setup click widget event handler intent
        Intent configIntent = createIntent(context, TemperatureDataWidgetConfigureActivity.class, "click", widgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.setOnClickPendingIntent(R.id.mainGrid_layout, pendingIntent);

        // setup reload button pending intent
        Intent reloadIntent = createIntent(context, TemperatureDataSourceService.class, "reload", widgetId);
        pendingIntent = PendingIntent.getService(context, widgetId, reloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.setOnClickPendingIntent(R.id.reload_button, pendingIntent);
    }

    /** Update the interface with the given temperature data object
     *
     * @param data the data object to update the interface with
     */
    public void updateInterface(TemperatureData data) {
        // pull data from model
        String currentTemp = formatTemperatureForDisplay(R.string.current_temp, data.getCurrentTemperature());
        String averageTemp = formatTemperatureForDisplay(R.string.average_temp, data.getAverageTemperatureForLastHour());
        String minTemp = formatTemperatureForDisplay(R.string.min_temp, data.getMinTemperatureForDay());
        String maxTemp = formatTemperatureForDisplay(R.string.max_temp, data.getMaxTemperatureForDay());
        Bitmap bitmap = makeGraph(context, data);

        // set text on interface
        this.setTextViewText(R.id.currentTime_text, data.getCurrentTimeFormatted());
        this.setTextViewText(R.id.currentTemperature_text, currentTemp);
        this.setTextViewText(R.id.averageTemp_text, averageTemp);
        this.setTextViewText(R.id.minTemp_text, minTemp);
        this.setTextViewText(R.id.maxTemp_text, maxTemp);
        this.setImageViewBitmap(R.id.temperatureView_graph, bitmap);
    }

    /** Helper method to convert a temperature reading value to a nicely formatted string
     *
     * @param prefixId the prefix to show before the value
     * @param temp the temperature reading value
     * @return a formatted string representing the reading value.
     */
    private String formatTemperatureForDisplay(int prefixId, double temp) {
        DecimalFormat df = new DecimalFormat("#.00");
        return context.getString(prefixId) + " " + df.format(temp) + "C";
    }

    /** Create a new graph from the temperature data
     *
     * This generates the graph from the temperature data loaded from the a remote data source.
     * Only a limited number of components are supported on widget interfaces. Custom components are
     * not allowed. Instead we dynamically generate an XYPlot then convert it to a bitmap image.
     * This image can then be shown via a standard ImageView.
     *
     * @param context the current context
     * @param data the data loaded from the data source
     * @return a bitmap representing the data for a XYPlot
     */
    private Bitmap makeGraph(Context context, TemperatureData data) {
        XYPlot plot = makePlot(context);
        XYSeries series = makeSeriesData(plot, data);
        LineAndPointFormatter seriesFormatter = makeLineFormatter();
        plot.addSeries(series, seriesFormatter);
        return plot.getDrawingCache();
    }

    /** Create a new plot object
     *
     * This cannot be done directly on the interface because Android only supports a limited number
     * of types of components. Instead we generate the graph dynamically in this method. It can then
     * be converted to a bitmap an shown via an image view later.
     *
     * @param context the current context
     * @return a stylised XYPlot object
     */
    private XYPlot makePlot(Context context) {
        XYPlot plot = new XYPlot(context, context.getString(R.string.graph_title));

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

    /** Generate the series data objects from the temperature data
     *
     * Temperature readings are shown on the Y axis and minutes on the X axis
     *
     * @param plot the plot to add format the axes for. This uses data for the series to format it
     * @param data the data to generate the series from
     * @return a XYSeries object for the last hour
     */
    private XYSeries makeSeriesData(XYPlot plot, TemperatureData data) {
        // Create a couple arrays of y-values to plot:
        ArrayList<Double> readings = data.getReadingValuesForLastHour();
        ArrayList<Integer> minutes = data.getMinutesForLastHour();

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

    /** Helper method to generate an X axis with custom ticks
     *
     * @param xValues the values to use on the x axis
     * @return a NumberFormat that can be used to format the X axis of a graph
     */
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

    /** Helper method to generate a line formatting object with the desired colour formatting
     *
     * @return LineAndPointFormatter with desired colours
     */
    private LineAndPointFormatter makeLineFormatter() {
        LineAndPointFormatter seriesFormatter = new LineAndPointFormatter(
                Color.rgb(200, 200, 200),                   // line color
                Color.rgb(700, 700, 700),                   // point color
                Color.rgb(100, 100, 600),                   // point color
                null);                                  // fill color (none)

        return seriesFormatter;
    }

    /** Helper method to create intents
     *
     * Makes sure the widget id and URI path are included
     *
     * @param context context of the intent
     * @param cls class to target the intent at
     * @param action string action to be encoded in the URI
     * @param appWidgetId the id of the widget sending the intent
     * @return
     */
    static private Intent createIntent(Context context, Class cls, String action, int appWidgetId) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(TemperatureDataWidget.DATA_SOURCE, TemperatureDataWidgetConfigureActivity.loadDataSourcePref(context, appWidgetId));
        Uri data = Uri.withAppendedPath(Uri.parse("widget://widget/id/#"+action+appWidgetId), String.valueOf(appWidgetId));
        intent.setData(data);
        return intent;
    }
}
