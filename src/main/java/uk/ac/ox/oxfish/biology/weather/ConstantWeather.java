package uk.ac.ox.oxfish.biology.weather;

/**
 * A simple "weather" object that always retuns the same numbers
 * Created by carrknight on 9/7/15.
 */
public class ConstantWeather implements LocalWeather {


    private double temperature;

    private double windSpeed;

    private double windDirection;


    public ConstantWeather(double temperature, double windSpeed, double windDirection) {
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
    }

    @Override
    public double getTemperatureInCelsius() {
        return temperature;
    }

    @Override
    public double getWindSpeedInKph() {
        return windSpeed;
    }

    @Override
    public double getWindDirection() {
        return windDirection;
    }


    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setWindDirection(double windDirection) {
        this.windDirection = windDirection;
    }
}
