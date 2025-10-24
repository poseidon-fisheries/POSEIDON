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

package uk.ac.ox.poseidon.agents.vessels;

import lombok.*;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VesselScopeFactoriesByCode<C> extends VesselScopeFactory<C> {

    @Singular private Map<String, ? extends VesselScopeFactory<? extends C>> factories;
    private String code;

    @Override
    protected C newInstance(
        final Simulation simulation,
        final Vessel vessel
    ) {
        checkNotNull(
            code,
            "Cannot create new instance unless code is set."
        );
        return Optional
            .ofNullable(factories.get(code))
            .map(factory -> factory.get(simulation, vessel))
            .orElseThrow(() -> new IllegalArgumentException("No factory found for code: " + code));
    }
}
