package uk.ac.ox.oxfish.biology.initializer.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.RockyLogisticInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates the Rocky Logistic Initializer
 * Created by carrknight on 9/29/15.
 */
public class RockyLogisticFactory implements AlgorithmFactory<RockyLogisticInitializer>
{




    private DoubleParameter sandyCarryingCapacity = new FixedDoubleParameter(2000);

    private DoubleParameter rockyCarryingCapacity = new FixedDoubleParameter(10000);



    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private DoubleParameter percentageLimitOnDailyMovement =new FixedDoubleParameter(0.01);

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private DoubleParameter differentialPercentageToMove =new FixedDoubleParameter(0.001);


    private int numberOfSpecies = 1;

    private AlgorithmFactory<? extends LogisticGrowerInitializer> grower = new SimpleLogisticGrowerFactory(0.6, 0.8);

    /**
     * Getter for property 'grower'.
     *
     * @return Value for property 'grower'.
     */
    public AlgorithmFactory<? extends LogisticGrowerInitializer> getGrower() {
        return grower;
    }

    /**
     * Setter for property 'grower'.
     *
     * @param grower Value to set for property 'grower'.
     */
    public void setGrower(
            AlgorithmFactory<? extends LogisticGrowerInitializer> grower) {
        this.grower = grower;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RockyLogisticInitializer apply(FishState state) {
        MersenneTwisterFast random = state.getRandom();
        return new RockyLogisticInitializer(rockyCarryingCapacity,
                                            sandyCarryingCapacity,
                                            percentageLimitOnDailyMovement.apply(random),
                                            differentialPercentageToMove.apply(random),
                                            numberOfSpecies,
                                            grower.apply(state));
    }


    public DoubleParameter getSandyCarryingCapacity() {
        return sandyCarryingCapacity;
    }

    public void setSandyCarryingCapacity(DoubleParameter sandyCarryingCapacity) {
        this.sandyCarryingCapacity = sandyCarryingCapacity;
    }

    public DoubleParameter getRockyCarryingCapacity() {
        return rockyCarryingCapacity;
    }

    public void setRockyCarryingCapacity(DoubleParameter rockyCarryingCapacity) {
        this.rockyCarryingCapacity = rockyCarryingCapacity;
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

    public int getNumberOfSpecies() {
        return numberOfSpecies;
    }

    public void setNumberOfSpecies(int numberOfSpecies) {
        this.numberOfSpecies = numberOfSpecies;
    }



}
