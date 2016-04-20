package uk.ac.ox.oxfish.biology.weather;

/**
 * The weather in the current sea-tile
 * Created by carrknight on 9/7/15.
 */
public interface LocalWeather {


    double getTemperatureInCelsius();


    double getWindSpeedInKph();


    double getWindDirection();



}
