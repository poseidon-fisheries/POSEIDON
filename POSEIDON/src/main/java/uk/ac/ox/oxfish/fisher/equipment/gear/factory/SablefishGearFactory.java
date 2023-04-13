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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.ArrayFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.RetentionAbundanceFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;

/**
 * Logistic retention but customized sablefish selectivity
 * Created by carrknight on 3/21/17.
 */
public class SablefishGearFactory implements HomogeneousGearFactory {


    private static final double[] FEMALE =
        new double[]{0.197813, 0.879397, 0.868362, 0.851041, 0.944782, 1, 0.983772, 0.909702, 0.79965, 0.674605, 0.547736,
            0.428506, 0.32337, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663,
            0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663,
            0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663,
            0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663, 0.235663,
            0.235663, 0.235663, 0.235663, 0.235663, 0.235663};


    private static final double[] MALE =
        new double[]{
            0.197813, 0.876199, 0.862056, 0.841788, 0.931111, 0.98431, 0.967137, 0.893213, 0.784184, 0.660738, 0.535813,
            0.41866, 0.315549, 0.229678, 0.229394, 0.22911, 0.228826, 0.228543, 0.22826, 0.227977, 0.227695, 0.227413,
            0.227132, 0.226851, 0.22657, 0.226289, 0.226009, 0.22573, 0.22545, 0.225171, 0.224892, 0.224614, 0.224336,
            0.224058, 0.223781, 0.223504, 0.223227, 0.222951, 0.222675, 0.222399, 0.222124, 0.221849, 0.221574, 0.2213,
            0.221026, 0.220753, 0.220479, 0.220207, 0.219934, 0.219662, 0.21939, 0.21939, 0.21939, 0.21939, 0.21939,
            0.21939, 0.21939, 0.21939, 0.21939, 0.21939
        };
    private final Locker<String, ArrayFilter> selectivity = new Locker<>();
    /**
     * retention inflection parameter
     */
    private DoubleParameter retentionInflection = new FixedDoubleParameter(45.5128);
    /**
     * retention slope parameter
     */
    private DoubleParameter retentionSlope = new FixedDoubleParameter(3.12457);
    /**
     * retention slope parameter: if null retention is ignored
     */
    private DoubleParameter retentionAsymptote = new FixedDoubleParameter(0.910947);
    private DoubleParameter litersOfGasConsumedPerHour = new FixedDoubleParameter(0);
    private DoubleParameter averageCatchability = new FixedDoubleParameter(0);


    private boolean rounding = true;

    public SablefishGearFactory() {
    }


    public SablefishGearFactory(
        final double catchability,
        final double retentionInflection,
        final double retentionSlope,
        final double retentionAsymptote,
        final double litersOfGasConsumedPerHour
    ) {
        this.averageCatchability = new FixedDoubleParameter(catchability);
        this.retentionInflection = new FixedDoubleParameter(retentionInflection);
        this.retentionSlope = new FixedDoubleParameter(retentionSlope);
        this.retentionAsymptote = new FixedDoubleParameter(retentionAsymptote);
        this.litersOfGasConsumedPerHour = new FixedDoubleParameter(litersOfGasConsumedPerHour);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HomogeneousAbundanceGear apply(final FishState state) {
        final MersenneTwisterFast random = state.getRandom();
        return new HomogeneousAbundanceGear(
            litersOfGasConsumedPerHour.applyAsDouble(random),
            new FixedProportionFilter(averageCatchability.applyAsDouble(random), rounding),
            selectivity.presentKey(
                state.getUniqueID(),
                () -> new ArrayFilter(
                    true, Arrays.copyOf(MALE, MALE.length),
                    Arrays.copyOf(FEMALE, FEMALE.length)
                )
            ),
            new RetentionAbundanceFilter(true,
                retentionInflection.applyAsDouble(random),
                retentionSlope.applyAsDouble(random),
                retentionAsymptote.applyAsDouble(random), rounding
            )

        );
    }


    /**
     * Getter for property 'retentionInflection'.
     *
     * @return Value for property 'retentionInflection'.
     */
    public DoubleParameter getRetentionInflection() {
        return retentionInflection;
    }

    /**
     * Setter for property 'retentionInflection'.
     *
     * @param retentionInflection Value to set for property 'retentionInflection'.
     */
    public void setRetentionInflection(final DoubleParameter retentionInflection) {
        this.retentionInflection = retentionInflection;
    }

    /**
     * Getter for property 'retentionSlope'.
     *
     * @return Value for property 'retentionSlope'.
     */
    public DoubleParameter getRetentionSlope() {
        return retentionSlope;
    }

    /**
     * Setter for property 'retentionSlope'.
     *
     * @param retentionSlope Value to set for property 'retentionSlope'.
     */
    public void setRetentionSlope(final DoubleParameter retentionSlope) {
        this.retentionSlope = retentionSlope;
    }

    /**
     * Getter for property 'retentionAsymptote'.
     *
     * @return Value for property 'retentionAsymptote'.
     */
    public DoubleParameter getRetentionAsymptote() {
        return retentionAsymptote;
    }

    /**
     * Setter for property 'retentionAsymptote'.
     *
     * @param retentionAsymptote Value to set for property 'retentionAsymptote'.
     */
    public void setRetentionAsymptote(final DoubleParameter retentionAsymptote) {
        this.retentionAsymptote = retentionAsymptote;
    }

    /**
     * Getter for property 'litersOfGasConsumedPerHour'.
     *
     * @return Value for property 'litersOfGasConsumedPerHour'.
     */
    public DoubleParameter getLitersOfGasConsumedPerHour() {
        return litersOfGasConsumedPerHour;
    }

    /**
     * Setter for property 'litersOfGasConsumedPerHour'.
     *
     * @param litersOfGasConsumedPerHour Value to set for property 'litersOfGasConsumedPerHour'.
     */
    public void setLitersOfGasConsumedPerHour(final DoubleParameter litersOfGasConsumedPerHour) {
        this.litersOfGasConsumedPerHour = litersOfGasConsumedPerHour;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleParameter getAverageCatchability() {
        return averageCatchability;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAverageCatchability(final DoubleParameter averageCatchability) {
        this.averageCatchability = averageCatchability;
    }

    /**
     * Getter for property 'rounding'.
     *
     * @return Value for property 'rounding'.
     */
    public boolean isRounding() {
        return rounding;
    }

    /**
     * Setter for property 'rounding'.
     *
     * @param rounding Value to set for property 'rounding'.
     */
    public void setRounding(final boolean rounding) {
        this.rounding = rounding;
    }
}
