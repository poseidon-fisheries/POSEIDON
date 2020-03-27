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
import uk.ac.ox.oxfish.model.data.monitors.regions.Locatable;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision.Region;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public class PerSpeciesPerRegionMonitor<O extends Locatable, V>
    extends PerSpeciesMonitor<O, V> {

    public PerSpeciesPerRegionMonitor(
        IntervalPolicy resetInterval,
        String baseName,
        Function<? super Species, Function<Region, Function<? super O, V>>> valueExtractorBuilder,
        Collection<Species> allSpecies,
        RegionalDivision regionalDivision,
        Supplier<Accumulator<V>> accumulatorSupplier
    ) {
        super(
            resetInterval,
            baseName,
            allSpecies,
            accumulatorSupplier,
            species -> new PerRegionMonitor<>(
                resetInterval,
                String.format("%s %s", species, baseName),
                regionalDivision,
                valueExtractorBuilder.apply(species),
                accumulatorSupplier
            )
        );
    }

}
