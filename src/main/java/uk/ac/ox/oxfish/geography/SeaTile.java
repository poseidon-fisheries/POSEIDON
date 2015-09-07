package uk.ac.ox.oxfish.geography;

import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.biology.weather.LocalWeather;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;

/**
 * This is the "cell", the tile of the sea grid. The plan is for this to have information about whether it is protected or not
 * a link to larger fishing-tiles if needed and more in general as a place where to store geographical information we don't
 * want to re-compute over and over again.
 *
 * Created by carrknight on 4/2/15.
 */
public class SeaTile implements Startable{


    private final int gridX;
    private final int gridY;

    /**
     * How high is this tile. Negative means underwater
     */
    private final double altitude;

    /**
     * the mpa this tile belongs to
     */
    private MasonGeometry mpa;

    /**
     * the local-biology object, used to check biomass
     */
    private LocalBiology biology;


    /**
     * weather object, contains temperatures and such at this tile
     */
    private LocalWeather weather;


    public SeaTile(int gridX, int gridY, double altitude) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.altitude = altitude;
    }


    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public double getAltitude() {
        return altitude;
    }

    /**
     *
     * @return true if it belongs to a MPA
     */
    public boolean isProtected(){
        return mpa != null;
    }

    public MasonGeometry grabMPA() {
        return mpa;
    }

    public void assignMpa(MasonGeometry mpa) {
        this.mpa = mpa;
    }


    public LocalBiology getBiology() {
        return biology;
    }

    /**
     * set the biology object. Without there is no biomass!
     * @param biology the local biology
     */
    public void setBiology(LocalBiology biology) {
        this.biology = biology;
    }

    /**
     * the biomass at this location for a single specie.
     * @param specie  the specie you care about
     * @return the biomass of this specie
     */
    public Double getBiomass(Specie specie) {
        return biology.getBiomass(specie);
    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     * @param specie the specie fished
     * @param biomassFished the biomass fished
     */
    public void reactToThisAmountOfBiomassBeingFished(Specie specie, Double biomassFished) {
        biology.reactToThisAmountOfBiomassBeingFished(specie, biomassFished);
    }


    @Override
    public String toString() {
        return "SeaTile "
                + gridX +
                "," + gridY +
                " altitude=" + altitude +
                " bio = " + biology;
    }


    /**
     * starts local biology
     */
    @Override
    public void start(FishState model) {
        biology.start(model);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        biology.turnOff();
    }


    public LocalWeather getWeather() {
        return weather;
    }

    public void setWeather(LocalWeather weather) {
        this.weather = weather;
    }

    public double getTemperatureInCelsius() {
        return weather.getTemperatureInCelsius();
    }

    public double getWindSpeedInKph() {
        return weather.getWindSpeedInKph();
    }

    public double getWindDirection() {
        return weather.getWindDirection();
    }
}
