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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.fisher.equipment.gear.GarbageGearDecorator;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates a gear where one species is not modelled directly in the biology but this gear still catches a bunch
 * of it in a fixed proportion to the rest of the world
 * Created by carrknight on 3/22/17.
 */
public class GarbageGearFactory implements AlgorithmFactory<GarbageGearDecorator> {


    private boolean rounding = true;
    private String garbageSpeciesName = MultipleSpeciesAbundanceInitializer.FAKE_SPECIES_NAME;

    private DoubleParameter proportionSimulatedToGarbage = new FixedDoubleParameter(0.3);

    private AlgorithmFactory<? extends Gear> delegate = new FixedProportionGearFactory();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public GarbageGearDecorator apply(FishState state) {

        Species garbageSpecies = state.getBiology().getSpecie(garbageSpeciesName);
        Preconditions.checkArgument(garbageSpecies != null && garbageSpecies.isImaginary(),
                                    "The garbage species must be exist and be'imaginary'");
        Gear delegate = this.delegate.apply(state);

        return new GarbageGearDecorator(garbageSpecies,
                                        proportionSimulatedToGarbage.apply(state.getRandom()),
                                        delegate,
                                        rounding);


    }

    /**
     * Getter for property 'garbageSpeciesName'.
     *
     * @return Value for property 'garbageSpeciesName'.
     */
    public String getGarbageSpeciesName() {
        return garbageSpeciesName;
    }

    /**
     * Setter for property 'garbageSpeciesName'.
     *
     * @param garbageSpeciesName Value to set for property 'garbageSpeciesName'.
     */
    public void setGarbageSpeciesName(String garbageSpeciesName) {
        this.garbageSpeciesName = garbageSpeciesName;
    }

    /**
     * Getter for property 'proportionSimulatedToGarbage'.
     *
     * @return Value for property 'proportionSimulatedToGarbage'.
     */
    public DoubleParameter getProportionSimulatedToGarbage() {
        return proportionSimulatedToGarbage;
    }

    /**
     * Setter for property 'proportionSimulatedToGarbage'.
     *
     * @param proportionSimulatedToGarbage Value to set for property 'proportionSimulatedToGarbage'.
     */
    public void setProportionSimulatedToGarbage(DoubleParameter proportionSimulatedToGarbage) {
        this.proportionSimulatedToGarbage = proportionSimulatedToGarbage;
    }

    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public AlgorithmFactory<? extends Gear> getDelegate() {
        return delegate;
    }

    /**
     * Setter for property 'delegate'.
     *
     * @param delegate Value to set for property 'delegate'.
     */
    public void setDelegate(
            AlgorithmFactory<? extends Gear> delegate) {
        this.delegate = delegate;
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
    public void setRounding(boolean rounding) {
        this.rounding = rounding;
    }
}
