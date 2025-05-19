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

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

import java.util.Map.Entry;

import static java.lang.Math.min;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class AbundanceCatchMaker implements CatchMaker<AbundanceLocalBiology> {

    private final GlobalBiology globalBiology;

    public AbundanceCatchMaker(final GlobalBiology globalBiology) {
        this.globalBiology = globalBiology;
    }

    @Override
    public Entry<Catch, AbundanceLocalBiology> apply(
        final AbundanceLocalBiology availableBiology,
        final AbundanceLocalBiology desiredBiology
    ) {
        final AbundanceLocalBiology caughtBiology = new AbundanceLocalBiology(globalBiology);
        final AbundanceLocalBiology uncaughtBiology = new AbundanceLocalBiology(globalBiology);
        globalBiology.getSpecies().forEach(species -> {
            final double[][] available = availableBiology.getAbundance().get(species);
            final double[][] desired = desiredBiology.getAbundance().get(species);
            final double[][] caught = caughtBiology.getAbundance().get(species);
            final double[][] uncaught = uncaughtBiology.getAbundance().get(species);
            range(0, species.getNumberOfSubdivisions()).forEach(sub ->
                range(0, species.getNumberOfBins()).forEach(bin -> {
                    caught[sub][bin] = min(desired[sub][bin], available[sub][bin]);
                    uncaught[sub][bin] = desired[sub][bin] - caught[sub][bin];
                })
            );
        });
        return entry(new Catch(globalBiology, caughtBiology), uncaughtBiology);
    }
}
