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

package uk.ac.ox.poseidon.agents.catches.disposition;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.buckets.BiomassBucket;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;
import uk.ac.ox.poseidon.biology.species.SpeciesIndexedDoubleArray;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.utils.ObjectFactory;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.agents.catches.disposition.Factories.discardRates;

class SpeciesSpecificDiscardRatesTest {

    @Test
    void discardsPerSpeciesRatesFromRetained() {
        final Species a = new Species("A", null, null);
        final Species b = new Species("B", null, null);
        final SpeciesIndex index = SpeciesIndex.of(a, b);
        final Bucket retained = BiomassBucket.of(new double[]{10.0, 20.0}, index);
        final SpeciesIndexedDoubleArray rates =
            SpeciesIndexedDoubleArray.of(new double[]{0.5, 0.0}, index);
        final Disposition disposition =
            new Disposition(retained, Bucket.empty(), Bucket.empty());

        final SpeciesSpecificDiscardRates process = new SpeciesSpecificDiscardRates(rates);
        final Disposition updated = process.partition(disposition, 0.0);

        assertThat(updated.getRetained().getKg(a)).isEqualTo(5.0);
        assertThat(updated.getRetained().getKg(b)).isEqualTo(20.0);
        assertThat(updated.getDiscardedAlive().getKg(a)).isEqualTo(5.0);
        assertThat(updated.getDiscardedAlive().getKg(b)).isEqualTo(0.0);
    }

    @Test
    void appliesMortalityRatesToDiscardedAlive() {
        final Species a = new Species("A", null, null);
        final Species b = new Species("B", null, null);
        final SpeciesIndex index = SpeciesIndex.of(a, b);
        final Bucket discardedAlive = BiomassBucket.of(new double[]{10.0, 20.0}, index);
        final SpeciesIndexedDoubleArray rates =
            SpeciesIndexedDoubleArray.of(new double[]{0.1, 0.0}, index);
        final Disposition disposition =
            new Disposition(Bucket.empty(), discardedAlive, Bucket.empty());

        final SpeciesSpecificDiscardMortalityRates process =
            new SpeciesSpecificDiscardMortalityRates(rates);
        final Disposition updated = process.partition(disposition, 0.0);

        assertThat(updated.getDiscardedAlive().getKg(a)).isEqualTo(9.0);
        assertThat(updated.getDiscardedAlive().getKg(b)).isEqualTo(20.0);
        assertThat(updated.getDiscardedDead().getKg(a)).isEqualTo(1.0);
        assertThat(updated.getDiscardedDead().getKg(b)).isEqualTo(0.0);
    }

    @Test
    void factoryAppliesRatesBySpeciesKey() {
        final Species adult = new Species("A", "adult", null);
        final Species juvenile = new Species("A", "juvenile", null);
        final Set<Species> species = Set.of(adult, juvenile);

        final SpeciesSpecificDiscardRatesFactory<Scope> factory =
            discardRates(
                new ObjectFactory<>(species),
                scope -> Map.of(
                    adult.getKey(), 0.25,
                    juvenile.getKey(), 0.25
                )
            );

        final SpeciesSpecificDiscardRates process = factory.get(Scope.GLOBAL_SCOPE);
        final SpeciesIndex index = SpeciesIndex.of(species);
        final Bucket retained = BiomassBucket.of(new double[]{8.0, 4.0}, index);
        final Disposition disposition =
            new Disposition(retained, Bucket.empty(), Bucket.empty());

        final Disposition updated = process.partition(disposition, 0.0);

        assertThat(updated.getDiscardedAlive().getKg(adult)).isEqualTo(2.0);
        assertThat(updated.getDiscardedAlive().getKg(juvenile)).isEqualTo(1.0);
    }
}
