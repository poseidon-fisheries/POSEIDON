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

package uk.ac.ox.poseidon.biology.species;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link GlobalScopeFactory} counterpart of {@link Species}. Note the field order here
 * ({@code code}, {@code name}, {@code lifeStage}) doesn't match {@link Species}'s own constructor
 * order ({@code code}, {@code lifeStage}, {@code name}) — {@link #newInstance} maps between them
 * by name, but take care when constructing either directly. Built via
 * {@link Factories#species()}/{@link Factories#species(String)}/
 * {@link Factories#species(String, String, String)}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpeciesFactory extends GlobalScopeFactory<Species> {

    private static final String PREFIX = "Species";

    private String code;
    private String name;
    private String lifeStage;

    /** @param code the species' code; {@code name} defaults to {@code "Species <code>"} */
    public SpeciesFactory(final String code) {
        this(code, PREFIX + " " + code, null);
    }

    @Override
    protected Species newInstance(final Scope scope) {
        return new Species(
            checkNotNull(code),
            lifeStage != null ? lifeStage : null,
            name != null ? name : PREFIX + " " + code
        );
    }
}
