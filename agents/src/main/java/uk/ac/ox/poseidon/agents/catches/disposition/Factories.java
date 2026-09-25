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

/** Factories for the steps of a catch's {@link DispositionProcess} (retention/discarding). */
public class Factories {
    private Factories() {}

    /**
     * @param species      the species to index rates over
     * @param rateFunction the proportion of retained catch to discard, per species
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an {@link IndexedDiscardRates}
     * @see IndexedDiscardRates
     */
    public static <S extends Scope> IndexedDiscardRatesFactory<S> discardRates(
        final Factory<? super S, ? extends Collection<? extends Species>> species,
        final Factory<? super S, ? extends Function<? super Species, Double>> rateFunction
    ) {
        return new IndexedDiscardRatesFactory<S>(species, rateFunction);
    }

    /**
     * @param species      the species to index rates over
     * @param rateFunction the discard mortality rate, per species
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an {@link IndexedDiscardMortality}
     * @see IndexedDiscardMortality
     */
    public static <S extends Scope> IndexedDiscardMortalityFactory<S> indexedDiscardMortality(
        final Factory<? super S, ? extends Collection<? extends Species>> species,
        final Factory<? super S, ? extends Function<? super Species, Double>> rateFunction
    ) {
        return new IndexedDiscardMortalityFactory<S>(species, rateFunction);
    }

    /**
     * @param dispositionStrategies the steps to chain, in order
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link CompositeDispositionProcess}
     * @see CompositeDispositionProcess
     */
    @SafeVarargs
    public static <S extends Scope> CompositeDispositionProcessFactory<S> compositeDispositionProcess(
        final Factory<? super S, ? extends DispositionProcess>... dispositionStrategies
    ) {
        return new CompositeDispositionProcessFactory<>(dispositionStrategies);
    }

    /**
     * @param selectedSpecies the species allowed to stay retained
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link SelectedSpeciesRetention}
     * @see SelectedSpeciesRetention
     */
    public static <S extends Scope> SelectedSpeciesRetentionFactory<S> selectedSpeciesRetention(
        final Factory<? super S, ? extends Collection<? extends Species>> selectedSpecies
    ) {
        return new SelectedSpeciesRetentionFactory<>(selectedSpecies);
    }

    /**
     * @param mortalityRate the discard mortality rate, per species
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link DiscardMortality}
     * @see DiscardMortality
     */
    public static <S extends Scope> DiscardMortalityFactory<S> discardMortality(
        final Factory<? super S, ? extends Function<? super Species, Double>> mortalityRate
    ) {
        return new DiscardMortalityFactory<>(mortalityRate);
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
     * {@link ProportionallyLimitingBiomassToHold}
     * @see ProportionallyLimitingBiomassToHold
     */
    public static ProportionallyLimitingBiomassToHoldFactory proportionallyLimitingBiomassToHold() {
        return new ProportionallyLimitingBiomassToHoldFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
     * {@link FullDiscardMortality}
     * @see FullDiscardMortality
     */
    public static FullDiscardMortalityFactory fullDiscardMortality() {
        return new FullDiscardMortalityFactory();
    }
}
