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

package uk.ac.ox.poseidon.agents.behaviours.disposition;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;

import java.util.function.DoubleSupplier;

import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

@RequiredArgsConstructor(access = PACKAGE)
public class GeneralDiscardMortality implements DispositionProcess<Biomass> {

    private final DoubleSupplier mortalityRateSupplier;

    @Override
    public Disposition<Biomass> partition(
        final Disposition<Biomass> currentDisposition,
        final double availableCapacityInKg
    ) {
        final Bucket<Biomass> newlyDead =
            currentDisposition
                .getDiscardedAlive()
                .mapContent(biomass ->
                    biomass.multiply(
                        checkUnitRange(
                            mortalityRateSupplier.getAsDouble(),
                            "Mortality"
                        )
                    )
                );
        return new Disposition<>(
            currentDisposition.getRetained(),
            currentDisposition.getDiscardedAlive().subtract(newlyDead),
            currentDisposition.getDiscardedDead().add(newlyDead)
        );
    }
}
