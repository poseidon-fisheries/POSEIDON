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

package uk.ac.ox.poseidon.fishing.choices;

import static uk.ac.ox.poseidon.core.providers.constant.Factories.always;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.providers.random.Factories.rng;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingActionEvent;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

/**
 * Factories for a vessel's learned {@link OptionValues}, its {@link MemoryEntries} of
 * observations and the rules updating it, and its destination-choice
 * strategies.
 */
public final class Factories {

  private Factories() {}

  /**
   * @param modelGrid the grid the coordinate is placed in
   * @param longitude the longitude of the destination, a value of the vessel
   * @param latitude  the latitude of the destination, a value of the vessel
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link DestinationAtCoordinate}
   * @see DestinationAtCoordinate
   */
  public static <S extends Scope, C> DestinationAtCoordinateFactory<
    S,
    C
  > destinationAt(
    final Factory<? super S, ? extends ModelGrid> modelGrid,
    final ValueFactory<? super S, ? super C, ? extends Double> longitude,
    final ValueFactory<? super S, ? super C, ? extends Double> latitude
  ) {
    return new DestinationAtCoordinateFactory<>(modelGrid, longitude, latitude);
  }

  /**
   * @param modelGrid the grid the coordinate is placed in
   * @param longitude the longitude of the destination
   * @param latitude  the latitude of the destination
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link DestinationAtCoordinate} at the same coordinate for every
   *         vessel
   * @see DestinationAtCoordinate
   */
  public static <S extends Scope> DestinationAtCoordinateFactory<
    S,
    Object
  > destinationAt(
    final Factory<? super S, ? extends ModelGrid> modelGrid,
    final double longitude,
    final double latitude
  ) {
    return new DestinationAtCoordinateFactory<>(
      modelGrid,
      constant(longitude),
      constant(latitude)
    );
  }

  /**
   * @param alpha the weight of a new observation, between 0 and 1
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link UpdateRule} that is a species-by-species exponential
   *         moving average of {@link uk.ac.ox.poseidon.biology.buckets.Bucket}s
   * @see ExponentialMovingAverageOfBuckets
   */
  public static ExponentialMovingAverageOfBucketsFactory exponentialMovingAverageOfBuckets(
    final double alpha
  ) {
    return new ExponentialMovingAverageOfBucketsFactory(alpha);
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for a
   *         plain memory of numbers by cell
   * @see MemoryFactory
   */
  public static MemoryFactory<Cell, Double> numbersByCell() {
    return new MemoryFactory<>();
  }

  /**
   * @param partitionKey selects the partition from an event or vessel
   * @param <T> the input type
   * @param <P> the partition key type
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for
   *         separate catch entries by cell for each partition
   * @see PartitionedMemory
   */
  public static <T, P> PartitionedMemoryFactory<
    T,
    P,
    Cell,
    Bucket
  > catchesByCellPartitionedBy(
    final ValueFactory<? super VesselScope, ? super T, ? extends P> partitionKey
  ) {
    return new PartitionedMemoryFactory<>(partitionKey);
  }

  /**
   * @param <T> the observed and remembered type
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for the
   *         keep-last-received rule
   * @see KeepLastReceived
   */
  public static <T> KeepLastReceivedFactory<T> keepLastReceived() {
    return new KeepLastReceivedFactory<>();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for the
   *         {@link FishingCell}
   * @see FishingCell
   */
  public static FishingCellFactory fishingCell() {
    return new FishingCellFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for the
   *         {@link RetainedCatch}
   * @see RetainedCatch
   */
  public static RetainedCatchFactory retainedCatch() {
    return new RetainedCatchFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for the
   *         {@link DestinationCell}
   * @see DestinationCell
   */
  public static DestinationCellFactory destinationCell() {
    return new DestinationCellFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for the
   *         {@link ProfitPerHour}
   * @see ProfitPerHour
   */
  public static ProfitPerHourFactory profitPerHour() {
    return new ProfitPerHourFactory();
  }

  /**
   * @param key extracts the key, or no key to skip an event
   * @param observation extracts the observation
   * @param memory selects the entries from the event
   * @param updateRule folds the observation into the entries
   * @param <S> the scope type
   * @param <K> the entry key type
   * @param <T> the observation type
   * @param <M> the remembered type
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a recorder
   * @see EventRecorder
   */
  public static <S extends Scope, K, T, M> EventRecorderFactory<
    S,
    FishingActionEvent,
    K,
    T,
    M
  > fishingActionRecorder(
    final ValueFactory<
      ? super S,
      ? super FishingActionEvent,
      ? extends Optional<K>
    > key,
    final ValueFactory<
      ? super S,
      ? super FishingActionEvent,
      ? extends T
    > observation,
    final Factory<
      ? super S,
      ? extends Function<
        ? super FishingActionEvent,
        ? extends MutableMemoryEntries<K, M>
      >
    > memory,
    final Factory<? super S, ? extends UpdateRule<? super T, M>> updateRule
  ) {
    return new EventRecorderFactory<>(
      FishingActionEvent.class.getName(),
      key,
      observation,
      memory,
      updateRule
    );
  }

  /**
   * @param key extracts the key, or no key to skip an event
   * @param observation extracts the observation
   * @param memory selects the entries from the event
   * @param updateRule folds the observation into the entries
   * @param <S> the scope type
   * @param <K> the entry key type
   * @param <T> the observation type
   * @param <M> the remembered type
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a recorder
   * @see EventRecorder
   */
  public static <S extends Scope, K, T, M> EventRecorderFactory<
    S,
    FishingTripEndEvent,
    K,
    T,
    M
  > fishingTripEndRecorder(
    final ValueFactory<
      ? super S,
      ? super FishingTripEndEvent,
      ? extends Optional<K>
    > key,
    final ValueFactory<
      ? super S,
      ? super FishingTripEndEvent,
      ? extends T
    > observation,
    final Factory<
      ? super S,
      ? extends Function<
        ? super FishingTripEndEvent,
        ? extends MutableMemoryEntries<K, M>
      >
    > memory,
    final Factory<? super S, ? extends UpdateRule<? super T, M>> updateRule
  ) {
    return new EventRecorderFactory<>(
      FishingTripEndEvent.class.getName(),
      key,
      observation,
      memory,
      updateRule
    );
  }

  /**
   * @param optionValues the values to compare for a vessel
   * @param optionEligibilityCriteria which options may be selected
   * @return a vessel-level factory for a greedy picker
   * @see GreedyPicker
   */
  public static <O> GreedyPickerFactory<O> greedyPicker(
    final Factory<? super VesselScope, ? extends OptionValues<O>> optionValues,
    final MappingFactory<
      ? super VesselScope,
      ? super Vessel,
      ? super O,
      Boolean
    > optionEligibilityCriteria
  ) {
    return new GreedyPickerFactory<>(
      optionValues,
      optionEligibilityCriteria,
      rng()
    );
  }

  /**
   * @param optionValues the values to compare without an eligibility filter
   * @return a vessel-level factory for an unfiltered greedy picker
   * @see GreedyPicker
   */
  public static <O> GreedyPickerFactory<O> greedyPicker(
    final Factory<? super VesselScope, ? extends OptionValues<O>> optionValues
  ) {
    return greedyPicker(optionValues, always(true));
  }

  /**
   * @param memorySelector selects the vessel's remembered outcomes
   * @param valuation values an outcome in the receiver's circumstances
   * @return a relative-scope factory for memory-based option values
   * @see MemoryBasedOptionValues
   */
  public static <S extends Scope, O, M> MemoryBasedOptionValuesFactory<
    S,
    O,
    M
  > memoryBasedOptionValues(
    final Factory<
      ? super S,
      ? extends Function<? super Vessel, ? extends MemoryEntries<O, M>>
    > memorySelector,
    final Factory<? super S, ? extends Valuation<? super M>> valuation
  ) {
    return new MemoryBasedOptionValuesFactory<>(memorySelector, valuation);
  }

  /**
   * Reads remembered numbers that already express the preference and unit
   * being compared; no identity valuation is exposed to scenario code.
   * @param memorySelector selects the vessel's remembered returns
   * @return a relative-scope factory for direct reading of remembered worth
   * @see MemoryBasedOptionValues
   */
  public static <S extends Scope, O> MemoryBasedOptionValuesFactory<
    S,
    O,
    Double
  > memoryBasedOptionValues(
    final Factory<
      ? super S,
      ? extends Function<? super Vessel, ? extends MemoryEntries<O, Double>>
    > memorySelector
  ) {
    return new MemoryBasedOptionValuesFactory<>(memorySelector, null);
  }

  /**
   * @param optionValues the values before cost subtraction
   * @param cost the cost in the same unit, subtracted once per visited option
   * @return a relative-scope factory for net option values
   * @see NetOptionValues
   */
  public static <S extends Scope, O> NetOptionValuesFactory<
    S,
    O
  > netOptionValues(
    final Factory<? super S, ? extends OptionValues<O>> optionValues,
    final Factory<? super S, ? extends Cost<? super O>> cost
  ) {
    return new NetOptionValuesFactory<>(optionValues, cost);
  }

  /**
   * @param candidates the stable, distinct candidates, read once per pick
   * @param optionEligibilityCriteria which options may be selected
   * @return a vessel-level factory for a random picker
   * @see RandomPicker
   */
  public static <O> RandomPickerFactory<O> randomPicker(
    final ValueFactory<
      ? super VesselScope,
      ? super Vessel,
      ? extends List<? extends O>
    > candidates,
    final MappingFactory<
      ? super VesselScope,
      ? super Vessel,
      ? super O,
      Boolean
    > optionEligibilityCriteria
  ) {
    return new RandomPickerFactory<>(
      candidates,
      optionEligibilityCriteria,
      rng()
    );
  }

  /**
   * @param candidates the stable, distinct candidates, read once per pick
   * @return a vessel-level factory for an unfiltered random picker
   * @see RandomPicker
   */
  public static <O> RandomPickerFactory<O> randomPicker(
    final ValueFactory<
      ? super VesselScope,
      ? super Vessel,
      ? extends List<? extends O>
    > candidates
  ) {
    return randomPicker(candidates, always(true));
  }

  /**
   * @param pathFinder supplies accessible water neighbours
   * @param cellEligibilityCriteria which cells may be selected
   * @param searchRadius the live radius in cells, read once per pick
   * @param maximumWidening the maximum additional radius in cells
   * @param searchCentre gives the centre once; absence means no exploration
   * @return a component-level factory for nearby-cell exploration
   * @see NearbyCellPicker
   */
  public static NearbyCellPickerFactory nearbyCellPicker(
    final Factory<
      ? super VesselComponentScope,
      ? extends GridPathFinder
    > pathFinder,
    final MappingFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? super Cell,
      Boolean
    > cellEligibilityCriteria,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      Integer
    > searchRadius,
    final int maximumWidening,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Optional<Cell>
    > searchCentre
  ) {
    return new NearbyCellPickerFactory(
      pathFinder,
      cellEligibilityCriteria,
      searchRadius,
      maximumWidening,
      searchCentre,
      rng()
    );
  }

  /**
   * @param epsilon the live exploration probability, in [0, 1]
   * @param explorer the exploration picker
   * @param exploiter the exploitation picker
   * @return a component-level factory for epsilon-greedy picking
   * @see EpsilonGreedyPicker
   */
  public static <O> EpsilonGreedyPickerFactory<O> epsilonGreedyPicker(
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      Double
    > epsilon,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Optional<O>
    > explorer,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Optional<O>
    > exploiter
  ) {
    return new EpsilonGreedyPickerFactory<>(
      epsilon,
      explorer,
      exploiter,
      rng()
    );
  }

  /**
   * @param epsilon the exploration probability, in [0, 1]
   * @param explorer the exploration picker
   * @param exploiter the exploitation picker
   * @return a component-level factory for epsilon-greedy picking
   * @see EpsilonGreedyPicker
   */
  public static <O> EpsilonGreedyPickerFactory<O> epsilonGreedyPicker(
    final double epsilon,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Optional<O>
    > explorer,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Optional<O>
    > exploiter
  ) {
    return epsilonGreedyPicker(constant(epsilon), explorer, exploiter);
  }

  /**
   * @param pickers the pickers to try in order
   * @return a relative-scope factory for picking with explicit fallback
   * @see FallbackPicker
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public static <S extends Scope, O> FallbackPickerFactory<S, O> fallbackPicker(
    final ValueFactory<
      ? super S,
      ? super Vessel,
      ? extends Optional<O>
    >... pickers
  ) {
    return new FallbackPickerFactory<S, O>(ImmutableList.copyOf(pickers));
  }
}
