package uk.ac.aber.slj11.temperaturedata.model;

/**
 * Created by samuel on 31/03/16.
 */
public class TemperatureReading {
    private int hour;
    private int minute;
    private double temperature;

    public TemperatureReading(int hour, int minute, double temperature) {
        this.setHour(hour);
        this.setMinute(minute);
        this.setTemperature(temperature);
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
