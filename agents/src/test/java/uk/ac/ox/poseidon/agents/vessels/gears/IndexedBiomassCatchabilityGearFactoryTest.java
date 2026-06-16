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

package uk.ac.ox.poseidon.agents.vessels.gears;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IndexedBiomassCatchabilityGearFactoryTest {

    @Test
    void appliesFunctionPerSpecies() {
        final Factory<Scope, Supplier<Duration>> durationSupplier =
            scope -> () -> Duration.ofHours(1);
        final Factory<Scope, Collection<? extends Species>> species =
            scope -> List.of(new Species("COD", "adult", "Cod"));
        final var factory = Factories.indexedBiomassCatchabilityGear(
            "G1",
            durationSupplier,
            species,
            scope -> s -> 0.5
        );
        final var gear = factory.get(Scope.GLOBAL_SCOPE);
        assertThat(gear.getCode()).isEqualTo("G1");
    }

    @Test
    void rejectsOutOfRangeProportions() {
        final Factory<Scope, Supplier<Duration>> durationSupplier =
            scope -> () -> Duration.ofHours(1);
        final Factory<Scope, Collection<? extends Species>> species =
            scope -> List.of(new Species("COD", "adult", "Cod"));
        final var factory = Factories.indexedBiomassCatchabilityGear(
            "G1",
            durationSupplier,
            species,
            scope -> s -> 1.5
        );
        assertThatThrownBy(() -> factory.get(Scope.GLOBAL_SCOPE))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
