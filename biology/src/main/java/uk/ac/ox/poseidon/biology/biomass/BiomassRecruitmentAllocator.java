/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.biology.biomass;

/**
 * Spreads a newly-recruited total biomass across a grid's habitable cells. Used by
 * {@link CommonBiomassGrower}, whose growth rule works on grid-wide totals and so needs a separate
 * step to turn that total back into per-cell biomass.
 */
public interface BiomassRecruitmentAllocator {
  /**
   * @param recruitedBiomassInKg the non-negative total biomass, in kilograms, to allocate
   * @param biomassGrid          the grid to add the allocated biomass into
   * @param carryingCapacityGrid the grid bounding how much each cell can receive
   */
  void allocate(
    double recruitedBiomassInKg,
    BiomassGrid biomassGrid,
    CarryingCapacityGrid carryingCapacityGrid
  );
}
