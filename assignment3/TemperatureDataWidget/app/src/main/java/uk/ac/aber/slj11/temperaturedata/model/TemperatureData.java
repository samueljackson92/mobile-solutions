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

    public double getMaxTemperatureForLastHour() {
        ArrayList<Double> lastHourReadings = getReadingValuesForLastHour();
        return Collections.max(lastHourReadings);
    }

    public double getMinTemperatureForLastHour() {
        ArrayList<Double> lastHourReadings = getReadingValuesForLastHour();
        return Collections.min(lastHourReadings);
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
            if (convertToSeconds(r.getHour(), r.getMinute()) >= seconds) {
                filteredReadings.add(r);
            }
        }
        return filteredReadings;
    }

    private long convertToSeconds(int hour, int min) {
        return hour*3600 + min*60;
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
