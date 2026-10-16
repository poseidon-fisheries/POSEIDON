/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

import static uk.ac.ox.poseidon.gui.palettes.PaletteColorMap.LAJOLLA;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.biology.biomass.BiomassGrid;
import uk.ac.ox.poseidon.biology.biomass.CarryingCapacityGrid;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
/**
 * A {@link SimulationScopeFactory} that portrays a single species'
 * {@link BiomassGrid} as a {@link NamedPortrayal}, named after the species and
 * coloured relative to {@link #carryingCapacityGrid}'s maximum.
 */
@SuperBuilder(toBuilder = true)
public class SpeciesBiomassFieldPortrayalFactory
  extends SimulationScopeFactory<NamedPortrayal>
{

  private final Factory<
    ? super SimulationScope,
    ? extends BiomassGrid
  > biomassGrid;
  private final Factory<
    ? super SimulationScope,
    ? extends CarryingCapacityGrid
  > carryingCapacityGrid;
  private final boolean visible;

  /**
   * @return a named portrayal of {@link #biomassGrid}, labelled with the
   *         species' name and life stage
   */
  @Override
  protected NamedPortrayal newInstance(final SimulationScope scope) {
    final Species species = biomassGrid.get(scope).getSpecies();
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
        this.biomassGrid,
        this.carryingCapacityGrid
      ).get(scope),
      visible
    );
  }
}
