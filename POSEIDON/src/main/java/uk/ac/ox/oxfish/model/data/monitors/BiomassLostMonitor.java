/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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
package uk.ac.ox.oxfish.model.data.monitors;

import tech.units.indriya.unit.Units;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassLostEvent;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.SummingAccumulator;

import javax.measure.quantity.Mass;
import java.util.Collection;
import java.util.function.UnaryOperator;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy.EVERY_YEAR;

public class BiomassLostMonitor
    extends GroupingMonitor<Species, BiomassLostEvent, Double, Mass> {
    private static final long serialVersionUID = -4843710999220455094L;

    public BiomassLostMonitor(
        final Collection<Species> allSpecies
    ) {
        super(
            "biomass lost",
            EVERY_YEAR,
            SummingAccumulator::new,
            Units.KILOGRAM,
            "Biomass",
            __ -> allSpecies,
            allSpecies.stream().collect(toImmutableMap(
                UnaryOperator.identity(),
                species -> new BasicMonitor<>(
                    String.format("%s %s", species, "biomass lost"),
                    EVERY_YEAR,
                    SummingAccumulator::new,
                    Units.KILOGRAM,
                    "Biomass",
                    event -> event.getBiomassLost().get(species)
                )
            ))
        );
    }
}
