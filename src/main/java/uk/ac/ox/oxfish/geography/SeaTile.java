package uk.ac.ox.oxfish.geography;

import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.weather.LocalWeather;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
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
     * a reference to a port if it is in the seatile
     */
    private Port portHere;


    /**
     * a description of the sea-bed and its properties
     */
    private TileHabitat habitat;

    /**
     * weather object, contains temperatures and such at this tile
     */
    private LocalWeather weather;


    public SeaTile(int gridX, int gridY, double altitude, TileHabitat habitat) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.altitude = altitude;
        this.habitat = habitat;
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
     * the biomass at this location for a single species.
     * @param species  the species you care about
     * @return the biomass of this species
     */
    public Double getBiomass(Species species) {
        return biology.getBiomass(species);
    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     * @param species the species fished
     * @param biomassFished the biomass fished
     */
    public void reactToThisAmountOfBiomassBeingFished(Species species, Double biomassFished) {
        biology.reactToThisAmountOfBiomassBeingFished(species, biomassFished);
    }


    @Override
    public String toString() {
        return "SeaTile "
                + gridX +
                "," + gridY +
                " altitude=" + altitude +
                " bio = " + biology;
    }


    public double getRockyPercentage() {
        return habitat.getHardPercentage();
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


    public LocalWeather grabLocalWeather() {
        return weather;
    }

    public void assignLocalWeather(LocalWeather weather) {
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

    public boolean isPortHere() {
        return portHere != null;
    }

    public void linkTileToPort(Port portHere) {
        this.portHere = portHere;
    }

    public TileHabitat getHabitat() {
        return habitat;
    }

    public void setHabitat(TileHabitat habitat) {
        this.habitat = habitat;
    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished these many fish (grouped by age) from this
     * location
     * @param species the species fished
     * @param maleCatches the biomass fished
     * @param femaleCatches
     */
    public void reactToThisAmountOfFishBeingCaught(Species species, int[] maleCatches, int[] femaleCatches) {
        biology.reactToThisAmountOfFishBeingCaught(species, maleCatches, femaleCatches);
    }

    /**
     * returns the number of male fish in this seatile belonging to this species, split into age cohorts
     * @param species the species examined
     * @return the male fish array.
     */
    public int[] getNumberOfMaleFishPerAge(Species species) {
        return biology.getNumberOfMaleFishPerAge(species);
    }

    /**
     * returns the number of female fish in this seatile belonging to this species, split into age cohorts
     * @param species the species examined
     * @return the female fish array.
     */
    public int[] getNumberOfFemaleFishPerAge(Species species) {
        return biology.getNumberOfFemaleFishPerAge(species);
    }

    public Port grabPortHere() {
        return portHere;
    }
}
