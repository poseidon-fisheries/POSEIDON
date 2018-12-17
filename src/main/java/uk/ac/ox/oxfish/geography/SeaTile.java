/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.geography;

import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.biology.weather.LocalWeather;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * This is the "cell", the tile of the sea grid. The plan is for this to have information about whether it is protected or not
 * a link to larger fishing-tiles if needed and more in general as a place where to store geographical information we don't
 * want to re-compute over and over again.
 *
 * Created by carrknight on 4/2/15.
 */
public class SeaTile implements Startable, LocalBiology{


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

       this.setBiology(biology,true);
    }


    public void setBiology(LocalBiology biology,boolean turnOffPreviousOne) {

        if(turnOffPreviousOne && this.biology != null)
            this.biology.turnOff();
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


    private String coords;

    @Override
    public String toString() {
        if(coords==null)
        return "SeaTile "
                + gridX +
                "," + gridY +
                " altitude=" + altitude +
                " bio = " + biology;
        else
            return "SeaTile "
                    + gridX +
                    "," + gridY +
                    " altitude=" + altitude +
                    " bio = " + biology +
                    " coords = " + coords;
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
        coords = model.getMap().getCoordinates(this).toString();

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
     * Tells the local biology that a fisher (or something anyway) fished from this location
     * @param caught fish taken from the sea
     * @param notDiscarded fish put in hold
     * @param globalBiology biology object
     */
    public void reactToThisAmountOfBiomassBeingFished(
            Catch caught, Catch notDiscarded, GlobalBiology globalBiology){
        //don't bother cascading if it's nothing
        if(caught.totalCatchWeight() >= FishStateUtilities.EPSILON)
            biology.reactToThisAmountOfBiomassBeingFished(caught, notDiscarded, globalBiology);
    }


    /**
     * checks if the tile is valid for fishing, which means that it's on sea and not land AND that there is a biology in it
     * that is not always empty; <b>THIS IS NOT A REGULATION CHECK</b>
     * @return true if this tile can in theory contain fish
     */
    public boolean isFishingEvenPossibleHere()
    {
        if(altitude > 0 || getBiology() instanceof EmptyLocalBiology)
            return false;
        else
            return true;

    }


    public StructuredAbundance getAbundance(Species species) {
        return biology.getAbundance(species);
    }



    public Port grabPortHere() {
        return portHere;
    }
}
