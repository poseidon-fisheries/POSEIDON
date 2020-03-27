/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.data.monitors;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.Accumulator;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public class PerSpeciesMonitor<O, V> extends GroupingMonitor<Species, O, V> {

    public PerSpeciesMonitor(
        IntervalPolicy resetInterval,
        String baseName,
        Collection<Species> allSpecies,
        Function<? super Species, Function<? super O, V>> valueExtractorBuilder,
        Supplier<Accumulator<V>> accumulatorSupplier
    ) {
        this(
            resetInterval,
            baseName,
            allSpecies,
            accumulatorSupplier,
            species -> new BasicMonitor<>(
                resetInterval,
                String.format("%s %s", species, baseName),
                accumulatorSupplier,
                valueExtractorBuilder.apply(species)
            )
        );
    }

    PerSpeciesMonitor(
        IntervalPolicy intervalPolicy,
        String baseName,
        Collection<Species> allSpecies,
        Supplier<Accumulator<V>> accumulatorSupplier,
        Function<? super Species, ? extends Monitor<O, V>> groupMonitorBuilder
    ) {
        super(
            intervalPolicy,
            baseName,
            accumulatorSupplier,
            __ -> allSpecies,
            allSpecies,
            groupMonitorBuilder
        );
    }

}