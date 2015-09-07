package uk.ac.ox.oxfish.biology.weather;

/**
 * The weather in the current sea-tile
 * Created by carrknight on 9/7/15.
 */
public interface LocalWeather {


    public double getTemperatureInCelsius();


    public double getWindSpeedInKph();


    public double getWindDirection();



}
