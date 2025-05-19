/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

/**
 * intercepts and doesn't pass along "reactTo" calls
 */
public class ConstantBiomassDecorator implements VariableBiomassBasedBiology {


    private final VariableBiomassBasedBiology delegate;

    public ConstantBiomassDecorator(VariableBiomassBasedBiology delegate) {
        this.delegate = delegate;
    }


    @Override
    public double getBiomass(Species species) {
        return delegate.getBiomass(species);
    }

    @Override
    public void reactToThisAmountOfBiomassBeingFished(Catch caught, Catch notDiscarded, GlobalBiology biology) {
        //neutralized!
    }

    @Override
    public StructuredAbundance getAbundance(Species species) {
        return delegate.getAbundance(species);
    }

    @Override
    public void start(FishState model) {
        delegate.start(model);
    }

    @Override
    public void turnOff() {
        delegate.turnOff();
    }


    @Override
    public double getCarryingCapacity(Species species) {
        return delegate.getCarryingCapacity(species);
    }

    @Override
    public double getCarryingCapacity(int index) {
        return delegate.getCarryingCapacity(index);
    }

    @Override
    public void setCarryingCapacity(Species s, double newCarryingCapacity) {
        delegate.setCarryingCapacity(s, newCarryingCapacity);
    }

    @Override
    public void setCurrentBiomass(Species s, double newCurrentBiomass) {
        delegate.setCurrentBiomass(s, newCurrentBiomass);
    }

    @Override
    public double[] getCurrentBiomass() {
        return delegate.getCurrentBiomass();
    }
}
