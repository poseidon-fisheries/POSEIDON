package uk.ac.ox.oxfish.biology.initializer.factory;


import uk.ac.ox.oxfish.biology.initializer.WellMixedBiologyInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

public class WellMixedBiologyFactory implements AlgorithmFactory<WellMixedBiologyInitializer>
{






    private DoubleParameter firstSpeciesCapacity = new FixedDoubleParameter(5000);

    /**
     * ratio of maxCapacitySecond/maxCapacityFirst
     */
    private DoubleParameter capacityRatioSecondToFirst = new FixedDoubleParameter(.2);

    private DoubleParameter steepness = new UniformDoubleParameter(0.6,0.8);


    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private DoubleParameter percentageLimitOnDailyMovement = new FixedDoubleParameter(0.01);

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private DoubleParameter differentialPercentageToMove = new FixedDoubleParameter(0.001);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public WellMixedBiologyInitializer apply(FishState state) {
        return new WellMixedBiologyInitializer(firstSpeciesCapacity,capacityRatioSecondToFirst,steepness,
                                               percentageLimitOnDailyMovement.apply(state.getRandom()),
                                               differentialPercentageToMove.apply(state.getRandom()));
    }

    public DoubleParameter getFirstSpeciesCapacity() {
        return firstSpeciesCapacity;
    }

    public void setFirstSpeciesCapacity(DoubleParameter firstSpeciesCapacity) {
        this.firstSpeciesCapacity = firstSpeciesCapacity;
    }

    public DoubleParameter getCapacityRatioSecondToFirst() {
        return capacityRatioSecondToFirst;
    }

    public DoubleParameter getSteepness() {
        return steepness;
    }

    public void setSteepness(DoubleParameter steepness) {
        this.steepness = steepness;
    }

    public DoubleParameter getPercentageLimitOnDailyMovement() {
        return percentageLimitOnDailyMovement;
    }

    public void setPercentageLimitOnDailyMovement(
            DoubleParameter percentageLimitOnDailyMovement) {
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
    }

    public DoubleParameter getDifferentialPercentageToMove() {
        return differentialPercentageToMove;
    }

    public void setDifferentialPercentageToMove(
            DoubleParameter differentialPercentageToMove) {
        this.differentialPercentageToMove = differentialPercentageToMove;
    }

    public void setCapacityRatioSecondToFirst(
            DoubleParameter capacityRatioSecondToFirst) {
        this.capacityRatioSecondToFirst = capacityRatioSecondToFirst;
    }
}
