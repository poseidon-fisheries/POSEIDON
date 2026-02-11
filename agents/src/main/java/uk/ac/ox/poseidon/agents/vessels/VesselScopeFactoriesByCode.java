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
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VesselScopeFactoriesByCode<C> extends VesselScopeFactory<C> {

    /* FIXME: Ultimately, I think that VesselScopeFactoriesByCode might broken because changing
        the code associated with the "default factory" changes the key of the main factory.
        Maybe that's fine because that code will always be the same as the code of the main
        factory, but I'm still uneasy about it. Also, this is the main place where having
        side effects (like putting things on the schedule) in factory (in this case the
        default factory) can cause problems.
    */

    @Singular private Map<String, ? extends Factory<? super VesselScope, ? extends C>> factories;
    private Factory<? super VesselScope, ? extends C> defaultFactory;
    private String code;

    @Override
    protected C newInstance(final VesselScope scope) {
        checkNotNull(
            code,
            "Cannot create new instance unless code is set."
        );
        return Optional
            .ofNullable(factories.get(code))
            .map(factory -> (C) factory.get(scope))
            .or(() ->
                Optional
                    .ofNullable(defaultFactory)
                    .map(factory -> factory.get(scope))
            )
            .orElseThrow(() -> new IllegalArgumentException(
                "No factory found for code %s and no default factory provided.".formatted(code)
            ));
    }
}
