/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HoldLimitingDecoratorGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 6/1/17.
 */
public class HoldLimitingDecoratorFactory implements AlgorithmFactory<HoldLimitingDecoratorGear> {


    private AlgorithmFactory<? extends Gear> delegate = new FixedProportionGearFactory();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HoldLimitingDecoratorGear apply(FishState state) {
        return new HoldLimitingDecoratorGear(delegate.apply(state));
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
        AlgorithmFactory<? extends Gear> delegate
    ) {
        this.delegate = delegate;
    }
}
