package uk.ac.aber.slj11.temperaturedata.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/** Temperature Data
 *
 * This class holds information loaded from an external data source. This provides several
 * convenience functions for calculating statistics from a data set of readings.
 *
 * Created by samuel on 31/03/16.
 */
public class TemperatureData {

    /** The Temperature Reading objects loaded from the data source */
    private ArrayList<TemperatureReading> readings;
    /** The current time loaded from the data source */
    private Date currentTime;


    /** Create a new temperature data object
     *
     * This takes an array list of reading objects and the current time as a Date
     *
     * @param currentTime the current time loaded from the data source
     * @param readings an array list of readings
     */
    public TemperatureData(Date currentTime, ArrayList<TemperatureReading> readings) {
        setCurrentTime(currentTime);
        setReadings(readings);
    }

    public ArrayList<TemperatureReading> getReadings() {
        return readings;
    }

    public void setReadings(ArrayList<TemperatureReading> readings) {
        this.readings = readings;
    }

    public Date getCurrentTime() {
        return currentTime;
    }

    /** Get the current time formatted as HH:mm
     *
     * @return a string with the formatted date
     */
    public String getCurrentTimeFormatted() {
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(currentTime);
    }

    /** Get the most recent temperature value
     *
     * @return the most recent temperature value loaded
     */
    public double getCurrentTemperature() {
        return readings.get(readings.size()-1).getTemperature();
    }

    /** Get the maximum temperature value for the current day
     *
     * @return the maximum temperature reading value for the day
     */
    public double getMaxTemperatureForDay() {
        ArrayList<Double> allReadings = getReadingValues();
        return Collections.max(allReadings);
    }

    /** Get the minimum temperature value for the current day
     *
     * @return the minimum temperature reading value for the day
     */
    public double getMinTemperatureForDay() {
        ArrayList<Double> allReadings = getReadingValues();
        return Collections.min(allReadings);
    }

    /** Get the average temperature for the last hour
     *
     * @return the average temperature for the last hour
     */
    public double getAverageTemperatureForLastHour() {
        ArrayList<Double> lastHourReadings = getReadingValuesForLastHour();
        return averageArray(lastHourReadings);
    }

    public ArrayList<Double> getReadingValuesForLastHour() {
        ArrayList<TemperatureReading> readingsForLastHour = getReadingsForLastHour();
        ArrayList<Double> valuesForLastHour = new ArrayList<>();
        for(TemperatureReading r : readingsForLastHour) {
            valuesForLastHour.add(r.getTemperature());
        }
        return valuesForLastHour;
    }

    /** Get all of the temperature readings for the past hour
     *
     * Items are in order from oldest to newest
     *
     * @return an ArrayList of TemperatureReading objects for the last hour
     */
    public ArrayList<TemperatureReading> getReadingsForLastHour() {
        TemperatureReading mostRecent = readings.get(readings.size()-1);
        int earliestHour = mostRecent.getHour()-1;
        int earliestMin = mostRecent.getMinute();
        // convert to seconds for easy comparison
        long seconds = convertToSeconds(earliestHour, earliestMin);
        return getReadingsSince(seconds);
    }

    /** Get all of the minutes for the past hour
     *
     * Items are in order from oldest to newest
     *
     * @return an ordered ArrayList of ints representing the minutes for the last hour
     */
    public ArrayList<Integer> getMinutesForLastHour() {
        ArrayList<TemperatureReading> readingsForLastHour = getReadingsForLastHour();
        ArrayList<Integer> valuesForLastHour = new ArrayList<>();
        for(TemperatureReading r : readingsForLastHour) {
            valuesForLastHour.add(r.getMinute());
        }
        return valuesForLastHour;
    }

    /** Get all of the readings since the given time in seconds
     *
     * This is useful to find all of the temperature readings after a given time. i.e. all of
     * the temperature readings for the last hour.
     *
     * Items are in order from oldest to newest
     *
     * @param seconds the time in seconds from which to keep readings since
     * @return an ordered ArrayList of readings since the seconds
     */
    private ArrayList<TemperatureReading> getReadingsSince(long seconds) {
        ArrayList<TemperatureReading> filteredReadings = new ArrayList<>();
        for(TemperatureReading r : readings) {
            // check if the current reading was more recent than "seconds"
            if (convertToSeconds(r.getHour(), r.getMinute()) >= seconds) {
                filteredReadings.add(r);
            }
        }
        return filteredReadings;
    }

    /** Get the values of all of the TemperatureReading objects
     *
     * Items are in order from oldest to newest
     *
     * @return a ordered ArrayList of temperature reading values
     */
    private ArrayList<Double> getReadingValues() {
        ArrayList<Double> filteredReadings = new ArrayList<>();
        for(TemperatureReading r : readings) {
                filteredReadings.add(r.getTemperature());
        }
        return filteredReadings;
    }

    /** Helper method to convert hours and minutes into seconds
     *
     * @param hour hours for the time to convert
     * @param min minutes for the time to convert
     * @return the time represented in seconds
     */
    private long convertToSeconds(int hour, int min) {
        final int HOUR_IN_SEC = 3600;
        final int MINS_IN_SEC = 60;
        return hour*HOUR_IN_SEC + min*MINS_IN_SEC;
    }

    /** Helper method to calculate the average of an ArrayList<Double>
     *
     * @param array the array to average
     * @return the mean over all values in the array
     */
    private double averageArray(ArrayList<Double> array) {
        int n = array.size();
        // check if we have elements
        // this prevents possible division by zero
        if(n == 0) {
            return 0;
        }

        // sum over all items to get the total
        double total = 0;
        for (double reading : array) {
            total += reading;
        }

        return total / n;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }
}
