package uk.ac.aber.slj11.temperaturedata.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by samuel on 31/03/16.
 */
public class TemperatureData {
    private ArrayList<TemperatureReading> readings;
    private Date currentTime;


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

    public String getCurrentTimeFormatted() {
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(currentTime);
    }

    public double getCurrentTemperature() {
        return readings.get(readings.size()-1).getTemperature();
    }

    public double getMaxTemperatureForDay() {
        ArrayList<Double> allReadings = getReadingValues();
        return Collections.max(allReadings);
    }

    public double getMinTemperatureForDay() {
        ArrayList<Double> allReadings = getReadingValues();
        return Collections.min(allReadings);
    }

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

    public ArrayList<TemperatureReading> getReadingsForLastHour() {
        TemperatureReading mostRecent = readings.get(readings.size()-1);
        int earliestHour = mostRecent.getHour()-1;
        int earliestMin = mostRecent.getMinute();
        // convert to seconds for easy comparison
        long seconds = convertToSeconds(earliestHour, earliestMin);
        return getReadingsSince(seconds);
    }

    public ArrayList<Integer> getMinutesForLastHour() {
        ArrayList<TemperatureReading> readingsForLastHour = getReadingsForLastHour();
        ArrayList<Integer> valuesForLastHour = new ArrayList<>();
        for(TemperatureReading r : readingsForLastHour) {
            valuesForLastHour.add(r.getMinute());
        }
        return valuesForLastHour;
    }

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

    private ArrayList<Double> getReadingValues() {
        ArrayList<Double> filteredReadings = new ArrayList<>();
        for(TemperatureReading r : readings) {
                filteredReadings.add(r.getTemperature());
        }
        return filteredReadings;
    }

    private long convertToSeconds(int hour, int min) {
        final int HOUR_IN_SEC = 3600;
        final int MINS_IN_SEC = 60;
        return hour*HOUR_IN_SEC + min*MINS_IN_SEC;
    }

    private double averageArray(ArrayList<Double> array) {
        int n = array.size();
        // check if we have elements
        // this prevents divison by zero
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
