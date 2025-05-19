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

package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

/**
 * This local biology has the same fixed biomass for each specie . It never gets ruined by fishing and is the same for
 * any specie. It doesn't represent realistically the number of fish present, rather it just divides the biomass by
 * weight of fish at age 0 and assumes there are that many fish at age 0 Created by carrknight on 4/11/15.
 */
public class ConstantLocalBiology extends AbstractBiomassBasedBiology {

    final private Double fixedBiomass;

    public ConstantLocalBiology(final double fixedBiomass) {
        this.fixedBiomass = fixedBiomass;
    }

    /**
     * returned the fixed biomass
     */
    @Override
    public double getBiomass(final Species species) {
        return fixedBiomass;
    }

    /**
     * Unsupported, since we can't infer total biomass without a list of species
     */
    @Override
    public double getTotalBiomass() {
        throw new UnsupportedOperationException();
    }

    /**
     * nothing happens
     *
     * @param caught
     * @param notDiscarded
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
        final Catch caught,
        final Catch notDiscarded,
        final GlobalBiology biology
    ) {

    }

    @Override
    public String toString() {
        return "fixed at " + fixedBiomass;
    }

    /**
     * ignored
     */
    @Override
    public void start(final FishState model) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }

}
