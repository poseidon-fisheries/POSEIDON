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

package uk.ac.ox.poseidon.fishing.catches.disposition;

import static uk.ac.ox.poseidon.core.providers.constant.Factories.always;

import java.util.Collection;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * Factories for catch sorting and discard mortality.
 */
@SuppressWarnings({ "varargs", "overloads" })
public final class Factories {

  private Factories() {}

  /**
   * @param species      the species to index rates over
   * @param rateFunction the proportion of retained catch to discard, per
   *                     species; evaluated once when built, so later input
   *                     changes are not seen
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an
   *         {@link IndexedDiscardRates}
   * @see IndexedDiscardRates
   */
  public static <S extends Scope> IndexedDiscardRatesFactory<S> discardRates(
    final Factory<? super S, ? extends Collection<? extends Species>> species,
    final MappingFactory<
      ? super S,
      ? super Void,
      ? super Species,
      Double
    > rateFunction
  ) {
    return new IndexedDiscardRatesFactory<S>(species, rateFunction);
  }

  /**
   * @param species the species to index rates over
   * @param rate    the rate, the same for every species
   * @return the same as {@link #discardRates(Factory, MappingFactory)}, with a
   *         constant {@code rate}
   */
  public static <S extends Scope> IndexedDiscardRatesFactory<S> discardRates(
    final Factory<? super S, ? extends Collection<? extends Species>> species,
    final double rate
  ) {
    return discardRates(species, always(rate));
  }

  /**
   * @param species      the species to index rates over
   * @param rateFunction the discard mortality rate, per species; evaluated
   *                     once when built, so later input changes are not seen
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an
   *         {@link IndexedDiscardMortality}
   * @see IndexedDiscardMortality
   */
  public static <S extends Scope> IndexedDiscardMortalityFactory<S> indexedDiscardMortality(
    final Factory<? super S, ? extends Collection<? extends Species>> species,
    final MappingFactory<
      ? super S,
      ? super Void,
      ? super Species,
      Double
    > rateFunction
  ) {
    return new IndexedDiscardMortalityFactory<S>(species, rateFunction);
  }

  /**
   * @param steps the steps to chain, in order
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link CompositeCatchSorting}
   * @see CompositeCatchSorting
   */
  @SafeVarargs
  public static <S extends Scope> CompositeCatchSortingFactory<S> compositeCatchSorting(
    final Factory<? super S, ? extends CatchSorting>... steps
  ) {
    return new CompositeCatchSortingFactory<>(steps);
  }

  /**
   * @param selectedSpecies the species allowed to stay retained
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link SelectedSpeciesRetention}
   * @see SelectedSpeciesRetention
   */
  public static <S extends Scope> SelectedSpeciesRetentionFactory<S> selectedSpeciesRetention(
    final Factory<
      ? super S,
      ? extends Collection<? extends Species>
    > selectedSpecies
  ) {
    return new SelectedSpeciesRetentionFactory<>(selectedSpecies);
  }

  /**
   * @param mortalityRate the fraction of discards that dies, in [0, 1]
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link UniformDiscardMortality}
   * @see UniformDiscardMortality
   */
  public static UniformDiscardMortalityFactory uniformDiscardMortality(
    final double mortalityRate
  ) {
    return UniformDiscardMortalityFactory.builder()
      .mortalityRate(mortalityRate)
      .build();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link ProportionallyLimitingBiomassToHold}
   * @see ProportionallyLimitingBiomassToHold
   */
  public static ProportionallyLimitingBiomassToHoldFactory proportionallyLimitingBiomassToHold() {
    return new ProportionallyLimitingBiomassToHoldFactory();
  }
}
