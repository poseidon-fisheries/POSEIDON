/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

import java.util.Map.Entry;

import static java.lang.Math.min;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class BiomassCatchMaker implements CatchMaker<BiomassLocalBiology> {

    private final GlobalBiology globalBiology;

    public BiomassCatchMaker(final GlobalBiology globalBiology) {
        this.globalBiology = globalBiology;
    }

    @Override
    public Entry<Catch, BiomassLocalBiology> apply(
        final BiomassLocalBiology availableBiology,
        final BiomassLocalBiology desiredBiology
    ) {
        final double[] caught = new double[globalBiology.getSize()];
        final double[] uncaught = new double[globalBiology.getSize()];
        globalBiology.getSpecies().forEach(species -> {
            final int i = species.getIndex();
            final double desired = desiredBiology.getBiomass(species);
            caught[i] = min(availableBiology.getBiomass(species), desired);
            uncaught[i] = desired - caught[i];
        });
        return entry(new Catch(caught), new BiomassLocalBiology(uncaught));
    }
}
