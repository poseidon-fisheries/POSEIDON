package uk.ac.ox.oxfish.biology.initializer.factory;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.GenericBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.BoundedConstantAllocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * A more general two-species initializer, the "father" of well-mixed, split in half and half bycatch
 * Created by carrknight on 1/4/16.
 */
public class TwoSpeciesBoxFactory implements AlgorithmFactory<GenericBiomassInitializer> {


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public GenericBiomassInitializer apply(FishState fishState) {


        double x = lowestX.apply(fishState.getRandom());
        double y = lowestY.apply(fishState.getRandom());
        double width = getBoxWidth().apply(fishState.getRandom());
        double height = getBoxHeight().apply(fishState.getRandom());


        return  new GenericBiomassInitializer(


                Lists.newArrayList(
                        firstSpeciesCapacity,
                        //this was a very stupid way of doing ratios I am carrying over for backward compatibility
                        new DoubleParameter() {
                            @Override
                            public DoubleParameter makeCopy() {
                                return this;
                            }

                            @Override
                            public Double apply(MersenneTwisterFast mersenneTwisterFast) {
                                Double ratio = ratioFirstToSecondSpecies.apply(mersenneTwisterFast);
                                double firstSpecies = firstSpeciesCapacity.apply(mersenneTwisterFast);
                                double secondSpeciesCapacity = 0;
                                if(ratio == 1)
                                    secondSpeciesCapacity = firstSpecies;
                                else if(ratio>1)
                                {
                                    secondSpeciesCapacity = ratio* firstSpecies;

                                }
                                else
                                    secondSpeciesCapacity = firstSpecies *
                                            ratio/(1-ratio);
                                return  secondSpeciesCapacity;
                            }
                        }
                ),
                new FixedDoubleParameter(0),
                new FixedDoubleParameter(1d),
                percentageLimitOnDailyMovement.apply(fishState.getRandom()),
                differentialPercentageToMove.apply(fishState.getRandom()),
                grower.apply(fishState),
                Lists.newArrayList(
                        new BoundedConstantAllocator(x, y, x+width-1, y+height-1, false),
                        new BoundedConstantAllocator(x, y, x+width-1, y+height-1,true)
                )


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
