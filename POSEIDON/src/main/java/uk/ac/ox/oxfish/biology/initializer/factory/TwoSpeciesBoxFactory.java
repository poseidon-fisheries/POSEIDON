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

package uk.ac.ox.oxfish.biology.initializer.factory;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.GenericBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.BoundedAllocatorDecorator;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantBiomassAllocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * A more general two-species initializer, the "father" of well-mixed, split in half and half bycatch
 * Created by carrknight on 1/4/16.
 */
public class TwoSpeciesBoxFactory implements AlgorithmFactory<GenericBiomassInitializer> {


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
    private DoubleParameter boxWidth = new FixedDoubleParameter(10d);
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
    private AlgorithmFactory<? extends LogisticGrowerInitializer> grower = new SimpleLogisticGrowerFactory(0.6, 0.8);


    public TwoSpeciesBoxFactory() {
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public GenericBiomassInitializer apply(final FishState fishState) {


        final double x = lowestX.applyAsDouble(fishState.getRandom());
        final double y = lowestY.applyAsDouble(fishState.getRandom());
        final double width = getBoxWidth().applyAsDouble(fishState.getRandom());
        final double height = getBoxHeight().applyAsDouble(fishState.getRandom());


        return new GenericBiomassInitializer(


            Lists.newArrayList(
                firstSpeciesCapacity,
                //this was a very stupid way of doing ratios I am carrying over for backward compatibility
                new DoubleParameter() {
                    @Override
                    public DoubleParameter makeCopy() {
                        return this;
                    }

                    @Override
                    public double applyAsDouble(final MersenneTwisterFast mersenneTwisterFast) {
                        final double ratio = ratioFirstToSecondSpecies.applyAsDouble(mersenneTwisterFast);
                        final double firstSpecies = firstSpeciesCapacity.applyAsDouble(mersenneTwisterFast);
                        double secondSpeciesCapacity = 0;
                        if (ratio == 1)
                            secondSpeciesCapacity = firstSpecies;
                        else if (ratio > 1) {
                            secondSpeciesCapacity = ratio * firstSpecies;

                        } else
                            secondSpeciesCapacity = firstSpecies *
                                ratio / (1 - ratio);
                        return secondSpeciesCapacity;
                    }
                }
            ),
            new FixedDoubleParameter(0),
            new FixedDoubleParameter(1d),
            percentageLimitOnDailyMovement.applyAsDouble(fishState.getRandom()),
            differentialPercentageToMove.applyAsDouble(fishState.getRandom()),
            grower.apply(fishState),
            Lists.newArrayList(
                new BoundedAllocatorDecorator(x, y, x + width - 1, y + height - 1, false,
                    new ConstantBiomassAllocator(1)
                ),
                new BoundedAllocatorDecorator(x, y, x + width - 1, y + height - 1, true,
                    new ConstantBiomassAllocator(1)
                )
            )


        );
    }

    public DoubleParameter getBoxWidth() {
        return boxWidth;
    }

    public DoubleParameter getBoxHeight() {
        return boxHeight;
    }

    public void setBoxHeight(final DoubleParameter boxHeight) {
        this.boxHeight = boxHeight;
    }

    public void setBoxWidth(final DoubleParameter boxWidth) {
        this.boxWidth = boxWidth;
    }

    public DoubleParameter getLowestX() {
        return lowestX;
    }

    public void setLowestX(final DoubleParameter lowestX) {
        this.lowestX = lowestX;
    }

    public DoubleParameter getLowestY() {
        return lowestY;
    }

    public void setLowestY(final DoubleParameter lowestY) {
        this.lowestY = lowestY;
    }

    public boolean isSpecies0InsideTheBox() {
        return species0InsideTheBox;
    }

    public void setSpecies0InsideTheBox(final boolean species0InsideTheBox) {
        this.species0InsideTheBox = species0InsideTheBox;
    }

    public DoubleParameter getFirstSpeciesCapacity() {
        return firstSpeciesCapacity;
    }

    public void setFirstSpeciesCapacity(final DoubleParameter firstSpeciesCapacity) {
        this.firstSpeciesCapacity = firstSpeciesCapacity;
    }

    public DoubleParameter getRatioFirstToSecondSpecies() {
        return ratioFirstToSecondSpecies;
    }

    public void setRatioFirstToSecondSpecies(final DoubleParameter ratioFirstToSecondSpecies) {
        this.ratioFirstToSecondSpecies = ratioFirstToSecondSpecies;
    }

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
        final AlgorithmFactory<? extends LogisticGrowerInitializer> grower
    ) {
        this.grower = grower;
    }

    public DoubleParameter getPercentageLimitOnDailyMovement() {
        return percentageLimitOnDailyMovement;
    }

    public void setPercentageLimitOnDailyMovement(
        final DoubleParameter percentageLimitOnDailyMovement
    ) {
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
    }

    public DoubleParameter getDifferentialPercentageToMove() {
        return differentialPercentageToMove;
    }

    public void setDifferentialPercentageToMove(
        final DoubleParameter differentialPercentageToMove
    ) {
        this.differentialPercentageToMove = differentialPercentageToMove;
    }
}
