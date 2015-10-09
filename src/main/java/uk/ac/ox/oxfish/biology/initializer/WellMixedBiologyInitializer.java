package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * A well mixed multiple-species diffusing logistic biology initializer
 * Created by carrknight on 10/8/15.
 */
public class WellMixedBiologyInitializer implements BiologyInitializer
{

    /**
     * max capacity for first species in each box
     */
    private final DoubleParameter firstSpeciesCapacity;

    /**
     * ratio of maxCapacitySecond/maxCapacityFirst
     */
    private final DoubleParameter ratioFirstToSecondSpecies;


    private final DoubleParameter steepness;


    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private final double percentageLimitOnDailyMovement;

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private final double differentialPercentageToMove;


    public WellMixedBiologyInitializer(
            DoubleParameter firstSpeciesCapacity,
            DoubleParameter ratioFirstToSecondSpecies, DoubleParameter steepness, double percentageLimitOnDailyMovement,
            double differentialPercentageToMove) {
        this.firstSpeciesCapacity = firstSpeciesCapacity;
        this.ratioFirstToSecondSpecies = ratioFirstToSecondSpecies;
        this.steepness = steepness;
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
        this.differentialPercentageToMove = differentialPercentageToMove;
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
    public LocalBiology generate(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells) {

        if(seaTile.getAltitude() > 0)
            return new EmptyLocalBiology();

        double firstSpeciesCapacity = this.firstSpeciesCapacity.apply(random);
        double secondSpeciesCapacity = 0;
        double secondSpeciesRatio = ratioFirstToSecondSpecies.apply(random);
        Preconditions.checkArgument(firstSpeciesCapacity > 0);
        Preconditions.checkArgument(secondSpeciesRatio>=0);
        Preconditions.checkArgument(secondSpeciesRatio<=1);
        if(secondSpeciesRatio == 1)
            secondSpeciesCapacity = firstSpeciesCapacity;
        else
            secondSpeciesCapacity = firstSpeciesCapacity * secondSpeciesRatio/(1-secondSpeciesRatio);


        return new LogisticLocalBiology(
                new Double[]{random.nextDouble() * firstSpeciesCapacity,random.nextDouble() * secondSpeciesCapacity},
                new Double[]{firstSpeciesCapacity,secondSpeciesCapacity},
                new Double[]{steepness.apply(random),steepness.apply(random)}
        );
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

        BiomassDiffuser diffuser = new BiomassDiffuser(map,random,biology,differentialPercentageToMove,percentageLimitOnDailyMovement);
        model.scheduleEveryDay(diffuser, StepOrder.BIOLOGY_PHASE);

    }

    @Override
    public int getNumberOfSpecies() {
        return 2;
    }
}
