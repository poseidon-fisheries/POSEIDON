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

import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Factories {
    private Factories() {}

    public static <S extends Scope> IndexedDiscardRatesFactory<S> discardRates(
        final Factory<? super S, ? extends Collection<? extends Species>> species,
        final Factory<? super S, ? extends Function<? super Species, Double>> rateFunction
    ) {
        return new IndexedDiscardRatesFactory<S>(species, rateFunction);
    }

    public static <S extends Scope> IndexedDiscardMortalityFactory<S> indexedDiscardMortality(
        final Factory<? super S, ? extends Collection<? extends Species>> species,
        final Factory<? super S, ? extends Function<? super Species, Double>> rateFunction
    ) {
        return new IndexedDiscardMortalityFactory<S>(species, rateFunction);
    }

    @SafeVarargs
    public static <S extends Scope> CompositeDispositionProcessFactory<S> compositeDispositionProcess(
        final Factory<? super S, ? extends DispositionProcess>... dispositionStrategies
    ) {
        return new CompositeDispositionProcessFactory<>(dispositionStrategies);
    }

    public static <S extends Scope> SelectedSpeciesRetentionFactory<S> selectedSpeciesRetention(
        final Factory<? super S, ? extends Collection<? extends Species>> selectedSpecies
    ) {
        return new SelectedSpeciesRetentionFactory<>(selectedSpecies);
    }

    public static <S extends Scope> DiscardMortalityFactory<S> discardMortality(
        final Factory<? super S, ? extends Function<? super Species, Double>> mortalityRate
    ) {
        return new DiscardMortalityFactory<>(mortalityRate);
    }

    public static ProportionallyLimitingBiomassToHoldFactory proportionallyLimitingBiomassToHold() {
        return new ProportionallyLimitingBiomassToHoldFactory();
    }
}
