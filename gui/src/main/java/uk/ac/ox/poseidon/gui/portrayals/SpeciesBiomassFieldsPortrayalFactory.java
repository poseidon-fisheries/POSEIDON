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

package uk.ac.ox.poseidon.gui.portrayals;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.biology.biomass.BiomassGrid;
import uk.ac.ox.poseidon.biology.biomass.CarryingCapacityGrid;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.gui.palettes.PaletteColorMap.LAJOLLA;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpeciesBiomassFieldsPortrayalFactory
    extends SimulationScopeFactory<List<NamedPortrayal>> {

    private Factory<? super SimulationScope, List<? extends BiomassGrid>> biomassGrids;
    private Factory<? super SimulationScope, List<? extends CarryingCapacityGrid>>
        carryingCapacityGrids;
    private boolean visible;

    @Override
    protected List<NamedPortrayal> newInstance(final SimulationScope scope) {

        final List<? extends BiomassGrid> biomassGrids =
            this.biomassGrids.get(scope);

        final List<? extends CarryingCapacityGrid> carryingCapacityGrids =
            this.carryingCapacityGrids.get(scope);

        final int n = Math.max(biomassGrids.size(), carryingCapacityGrids.size());

        checkState(Set.of(1, n).contains(biomassGrids.size()));
        checkState(Set.of(1, n).contains(carryingCapacityGrids.size()));

        return IntStream
            .range(0, n)
            .mapToObj(i -> {
                final BiomassGrid biomassGrid =
                    biomassGrids.get(biomassGrids.size() == 1 ? 0 : i);
                final CarryingCapacityGrid carryingCapacityGrid =
                    carryingCapacityGrids.get(carryingCapacityGrids.size() == 1 ? 0 : i);

                final Species species = biomassGrid.getSpecies();
                final String name =
                    species.getName() +
                        (species.getLifeStage() != null ? " " + species.getLifeStage() : "") +
                        " biomass";
                return new NamedPortrayal(
                    name,
                    new NumberGridWithCapacityPortrayalFactory(
                        LAJOLLA,
                        name,
                        false,
                        object(biomassGrid),
                        object(carryingCapacityGrid)
                    ).get(scope),
                    visible
                );

            })
            .toList();

    }
}
