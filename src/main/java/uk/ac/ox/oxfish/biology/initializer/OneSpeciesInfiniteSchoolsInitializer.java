package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import fr.ird.osmose.School;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * simple biology initializer where there are a bunch of schools that swim around
 * Created by carrknight on 11/17/16.
 */
public class OneSpeciesInfiniteSchoolsInitializer extends AbstractBiologyInitializer {


    private final int numberOfSchools;


    private final Pair<Integer,Integer>[] waypoints;

    private final DoubleParameter startingX;

    private final DoubleParameter startingY;

    private final DoubleParameter diameter;

    private final DoubleParameter speedInDays;

    private final DoubleParameter  biomassEach;

    private final List<InfiniteSchool> schools;


    public OneSpeciesInfiniteSchoolsInitializer(
            int numberOfSchools,
            Pair<Integer,Integer>[] waypoints, DoubleParameter startingX,
            DoubleParameter startingY, DoubleParameter diameter,
            DoubleParameter speedInDays, DoubleParameter biomassEach) {
        this.numberOfSchools = numberOfSchools;
        this.waypoints = waypoints;
        this.startingX = startingX;
        this.startingY = startingY;
        this.diameter = diameter;
        this.speedInDays = speedInDays;
        this.biomassEach = biomassEach;
        this.schools = new ArrayList<>(numberOfSchools);
    }


    /**
     * creates the global biology object for the model and build schools
     *
     * @param random                the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return a global biology object
     */
    @Override
    public GlobalBiology generateGlobal(MersenneTwisterFast random, FishState modelBeingInitialized) {
        GlobalBiology globalBiology = super.generateGlobal(random, modelBeingInitialized);

        for(int i =0 ; i< numberOfSchools ; i++) {
            InfiniteSchool school = new InfiniteSchool(
                    startingX.apply(random).intValue(),
                    startingY.apply(random).intValue(),
                    speedInDays.apply(random).intValue(),
                    diameter.apply(random),
                    biomassEach.apply(random),
                    globalBiology.getSpecie(0),
                    waypoints
            );
            schools.add(school);
        }


        return globalBiology;
    }

    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *
     * @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     */
    @Override
    public LocalBiology generateLocal(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells) {
        if(seaTile.getAltitude()>=0)
            return new EmptyLocalBiology();
        return new SchoolLocalBiology(schools,seaTile);
    }

    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random
     * @param model   the model: it is in the process of being initialized so it should be only used to schedule stuff rather
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model) {

        //start all schools
        for(InfiniteSchool school : schools)
            model.registerStartable(school);
    }

    /**
     * Get a list of the species with their names. The size of this array determines the size of the model array
     *
     * @return
     */
    @Override
    public String[] getSpeciesNames() {
        return new String[]{"Species 0"};
    }
}
