package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.TwoSpeciesBoxInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * A more general two-species initializer, the "father" of well-mixed, split in half and half bycatch
 * Created by carrknight on 1/4/16.
 */
public class TwoSpeciesBoxFactory implements AlgorithmFactory<TwoSpeciesBoxInitializer> {


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public TwoSpeciesBoxInitializer apply(FishState fishState) {
        return new TwoSpeciesBoxInitializer(
                lowestX.apply(fishState.getRandom()).intValue(),
                lowestY.apply(fishState.getRandom()).intValue(),
                boxHeight.apply(fishState.getRandom()).intValue(),
                boxWidth.apply(fishState.getRandom()).intValue(),
                species0InsideTheBox,
                firstSpeciesCapacity,
                ratioFirstToSecondSpecies,
                percentageLimitOnDailyMovement.apply(fishState.getRandom()),
                differentialPercentageToMove.apply(fishState.getRandom()),
                grower.apply(fishState)
        );
    }



    /**
     * the smallest x that is inside the box
     */
    private DoubleParameter lowestX = new FixedDoubleParameter(0);

    /**
     * the smallest y that is inside the box
     */
    private DoubleParameter lowestY = new FixedDoubleParameter(0);

    /**
     * the height of the box
     */
    private DoubleParameter boxHeight = new FixedDoubleParameter(10);

    /**
     * the width of the box
     */
    private DoubleParameter boxWidth  = new FixedDoubleParameter(10d);

    /**
     * is the Species 0 also inside the box or not?
     */
    private boolean species0InsideTheBox = false;

    /**
     * max capacity for first species in each box
     */
    private DoubleParameter firstSpeciesCapacity = new FixedDoubleParameter(5000);

    /**
     * ratio of maxCapacitySecond/maxCapacityFirst
     */
    private DoubleParameter ratioFirstToSecondSpecies = new FixedDoubleParameter(1d);


    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private DoubleParameter percentageLimitOnDailyMovement = new FixedDoubleParameter(0.01);

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private DoubleParameter differentialPercentageToMove = new FixedDoubleParameter(0.001);


    public TwoSpeciesBoxFactory() {
    }


    public DoubleParameter getLowestX() {
        return lowestX;
    }

    public void setLowestX(DoubleParameter lowestX) {
        this.lowestX = lowestX;
    }

    public DoubleParameter getLowestY() {
        return lowestY;
    }

    public void setLowestY(DoubleParameter lowestY) {
        this.lowestY = lowestY;
    }

    public DoubleParameter getBoxHeight() {
        return boxHeight;
    }

    public void setBoxHeight(DoubleParameter boxHeight) {
        this.boxHeight = boxHeight;
    }

    public DoubleParameter getBoxWidth() {
        return boxWidth;
    }

    public void setBoxWidth(DoubleParameter boxWidth) {
        this.boxWidth = boxWidth;
    }

    public boolean isSpecies0InsideTheBox() {
        return species0InsideTheBox;
    }

    public void setSpecies0InsideTheBox(boolean species0InsideTheBox) {
        this.species0InsideTheBox = species0InsideTheBox;
    }

    public DoubleParameter getFirstSpeciesCapacity() {
        return firstSpeciesCapacity;
    }

    public void setFirstSpeciesCapacity(DoubleParameter firstSpeciesCapacity) {
        this.firstSpeciesCapacity = firstSpeciesCapacity;
    }

    public DoubleParameter getRatioFirstToSecondSpecies() {
        return ratioFirstToSecondSpecies;
    }

    public void setRatioFirstToSecondSpecies(DoubleParameter ratioFirstToSecondSpecies) {
        this.ratioFirstToSecondSpecies = ratioFirstToSecondSpecies;
    }

    private AlgorithmFactory<? extends LogisticGrowerInitializer> grower = new SimpleLogisticGrowerFactory(0.6,0.8);

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
}
