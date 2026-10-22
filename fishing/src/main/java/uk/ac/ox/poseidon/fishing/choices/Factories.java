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

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.providers.random.Factories.randomNumberGenerator;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

/**
 * Factories for a vessel's learned {@link OptionValues}, its {@link Memory} of
 * observations and the rules updating it, and its destination-choice
 * strategies.
 */
public final class Factories {

  private Factories() {}

  /**
   * @param optionValues the component holding each vessel's own option values
   * @param friends      gives a vessel's current friends
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for
   *         a vessel's friends' best options
   * @see BestOptionsFromFriends
   */
  public static <O> BestOptionsFromFriendsFactory<O> bestOptionsFromFriends(
    final VesselComponent<
      ? extends Function<? super Vessel, ? extends OptionValues<O>>
    > optionValues,
    final ValueFactory<
      ? super VesselScope,
      ? super Vessel,
      ? extends Iterable<? extends Vessel>
    > friends
  ) {
    return new BestOptionsFromFriendsFactory<>(optionValues, friends);
  }

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
   * @param epsilon   gives the probability of exploring instead of exploiting,
   *                  in {@code [0, 1]}, read for the vessel on each choice
   * @param explorer  gives an exploratory destination for a vessel
   * @param exploiter gives the currently-best-known destination for a vessel
   * @return a
   *         {@link uk.ac.ox.poseidon.fishing.vessels.VesselComponentScopeFactory}
   *         for a vessel's destination chosen by an
   *         {@link EpsilonGreedyChooser}
   * @see EpsilonGreedyChooser
   */
  public static EpsilonGreedyDestinationFactory epsilonGreedyDestination(
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      Double
    > epsilon,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Cell
    > explorer,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Cell
    > exploiter
  ) {
    return new EpsilonGreedyDestinationFactory(
      epsilon,
      explorer,
      exploiter,
      randomNumberGenerator()
    );
  }

  /**
   * @param epsilon   the probability of exploring instead of exploiting, in
   *                  {@code [0, 1]}
   * @param explorer  gives an exploratory destination for a vessel
   * @param exploiter gives the currently-best-known destination for a vessel
   * @return a
   *         {@link uk.ac.ox.poseidon.fishing.vessels.VesselComponentScopeFactory}
   *         for a vessel's destination chosen by an
   *         {@link EpsilonGreedyChooser}, with a fixed epsilon
   * @see EpsilonGreedyChooser
   */
  public static EpsilonGreedyDestinationFactory epsilonGreedyDestination(
    final double epsilon,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Cell
    > explorer,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Cell
    > exploiter
  ) {
    return epsilonGreedyDestination(constant(epsilon), explorer, exploiter);
  }

  /**
   * @param alpha the weight of a new observation, between 0 and 1
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link Memory} update rule that is a species-by-species exponential
   *         moving average of {@link uk.ac.ox.poseidon.biology.buckets.Bucket}s
   * @see ExponentialMovingAverageOfBuckets
   */
  public static ExponentialMovingAverageOfBucketsFactory exponentialMovingAverageOfBuckets(
    final double alpha
  ) {
    return new ExponentialMovingAverageOfBucketsFactory(alpha);
  }

  /**
   * @param alpha the weight of a new observation, between 0 and 1
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link Memory} update rule that is an exponential moving average of
   *         {@code Double}s
   * @see ExponentialMovingAverageOfDoubles
   */
  public static ExponentialMovingAverageOfDoublesFactory exponentialMovingAverageOfDoubles(
    final double alpha
  ) {
    return new ExponentialMovingAverageOfDoublesFactory(alpha);
  }

  /**
   * @param alpha the smoothing factor, in {@code [0, 1]}
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for
   *         an {@link ExponentialMovingAverageOptionValues} of cells; it names
   *         its option type so that a scenario can hold it in a {@code var}
   * @see ExponentialMovingAverageOptionValues
   */
  public static ExponentialMovingAverageOptionValuesFactory<Cell> exponentialMovingAverageCellValues(
    final double alpha
  ) {
    return new ExponentialMovingAverageOptionValuesFactory<>(alpha);
  }

  /**
   * @param optionValues    gives a vessel's values of the options to pick from
   * @param optionPredicate which options can be picked for a vessel
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for a
   *         {@link GreedyPicker}
   * @see GreedyPicker
   */
  public static <O> GreedyPickerFactory<O> greedyPicker(
    final Factory<
      ? super VesselScope,
      ? extends Function<? super Vessel, ? extends OptionValues<O>>
    > optionValues,
    final MappingFactory<
      ? super VesselScope,
      ? super Vessel,
      ? super O,
      Boolean
    > optionPredicate
  ) {
    return new GreedyPickerFactory<>(
      optionValues,
      optionPredicate,
      randomNumberGenerator()
    );
  }

  /**
   * @param modelGrid      the grid giving the cell of a fishing event's
   *                       coordinate
   * @param memorySelector selects, from a fishing event, the memory to record
   *                       it in
   * @param updateRule     revises what is remembered of a cell with a new
   *                       observation of it
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link FishingEventRecorder}, which it does not register with
   *         anything
   * @see FishingEventRecorder
   */
  public static <S extends Scope> FishingEventRecorderFactory<S> fishingEventRecorder(
    final Factory<? super S, ? extends ModelGrid> modelGrid,
    final Factory<
      ? super S,
      ? extends Function<? super FishingEvent, ? extends Memory<Cell, Bucket>>
    > memorySelector,
    final Factory<? super S, ? extends BinaryOperator<Bucket>> updateRule
  ) {
    return new FishingEventRecorderFactory<>(
      modelGrid,
      memorySelector,
      updateRule
    );
  }

  /**
   * @param optionValues         gives a vessel's own learned option values
   * @param optionPredicate      filters which candidate options are eligible
   *                             for a vessel
   * @param imitatedOptionValues gives the option values a vessel imitates
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for
   *         an {@link ImitatingPicker}
   * @see ImitatingPicker
   */
  public static <O> ImitatingPickerFactory<O> imitatingPicker(
    final Factory<
      ? super VesselScope,
      ? extends Function<? super Vessel, ? extends OptionValues<O>>
    > optionValues,
    final MappingFactory<
      ? super VesselScope,
      ? super Vessel,
      ? super O,
      Boolean
    > optionPredicate,
    final Factory<
      ? super VesselScope,
      ? extends Function<? super Vessel, ? extends OptionValues<O>>
    > imitatedOptionValues
  ) {
    return new ImitatingPickerFactory<>(
      optionValues,
      optionPredicate,
      imitatedOptionValues,
      randomNumberGenerator()
    );
  }

  /**
   * @param key the key, such as the code of a gear, read from what is
   *            remembered (a {@link FishingEvent}) and from what reads (a
   *            {@link Vessel}); each key gets its own memory
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for
   *         an empty memory of the catches made in each cell, one per vessel;
   *         give everything that remembers in or reads a vessel's memory the
   *         same factory object (two make two memories)
   * @see KeyedMemory
   */
  public static <T, K> KeyedMemorySelectorFactory<
    T,
    K,
    Cell,
    Bucket
  > catchMemoryBy(
    final ValueFactory<? super VesselScope, ? super T, ? extends K> key
  ) {
    return new KeyedMemorySelectorFactory<>(key);
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for
   *         an empty {@link Memory}, one per vessel
   * @see Memory
   */
  public static <O, M> MemoryFactory<O, M> memory() {
    return new MemoryFactory<>();
  }

  /**
   * @param memorySelector selects the memory to read for a vessel
   * @param valuation      values an option for a vessel, given what is
   *                       remembered about it
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for a
   *         vessel's option values read from its memory
   * @see MemoryBasedOptionValues
   */
  public static <O, M> MemoryBasedOptionValuesFactory<
    O,
    M
  > memoryBasedOptionValues(
    final Factory<
      ? super VesselScope,
      ? extends Function<? super Vessel, ? extends Memory<O, M>>
    > memorySelector,
    final Factory<
      ? super VesselScope,
      ? extends Valuation<? super O, ? super M>
    > valuation
  ) {
    return new MemoryBasedOptionValuesFactory<>(memorySelector, valuation);
  }

  /**
   * @param pathFinder                finds accessible water neighbours
   * @param cellPredicate             filters which candidate cells are eligible
   * @param neighbourhoodSize the initial search radius
   * @param maximumWidening           how many cells the radius may grow beyond
   *                                  the initial one while no cell satisfies
   *                                  {@code cellPredicate}
   * @param searchCentre              gives the cell to search around for a
   *                                  vessel, asked for anew on each pick
   * @return a
   *         {@link uk.ac.ox.poseidon.fishing.vessels.VesselComponentScopeFactory}
   *         for a {@link NeighbourhoodCellPicker}
   * @see NeighbourhoodCellPicker
   */
  public static NeighbourhoodGridExplorerFactory neighbourhoodGridExplorer(
    final Factory<
      ? super VesselComponentScope,
      ? extends GridPathFinder
    > pathFinder,
    final MappingFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? super Cell,
      Boolean
    > cellPredicate,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      Integer
    > neighbourhoodSize,
    final int maximumWidening,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Cell
    > searchCentre
  ) {
    return new NeighbourhoodGridExplorerFactory(
      pathFinder,
      cellPredicate,
      neighbourhoodSize,
      maximumWidening,
      searchCentre,
      randomNumberGenerator()
    );
  }

  /**
   * @param optionValues gives a vessel's values of the options
   * @param fallback     gives an option for a vessel when none is known
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for a
   *         {@link BestKnownOption}
   * @see BestKnownOption
   */
  public static <O> BestKnownOptionFactory<O> bestKnownOption(
    final Factory<
      ? super VesselScope,
      ? extends Function<? super Vessel, ? extends OptionValues<O>>
    > optionValues,
    final ValueFactory<
      ? super VesselScope,
      ? super Vessel,
      ? extends O
    > fallback
  ) {
    return new BestKnownOptionFactory<>(
      optionValues,
      fallback,
      randomNumberGenerator()
    );
  }

  /**
   * @param revenueValuation values an option for a vessel, given what is
   *                         remembered about it
   * @param costValuation    values the cost of an option for a vessel, given
   *                         what is remembered about it
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link NetValuation}
   * @see NetValuation
   */
  public static <S extends Scope, O, M> NetValuationFactory<
    S,
    O,
    M
  > netValuation(
    final Factory<
      ? super S,
      ? extends Valuation<? super O, ? super M>
    > revenueValuation,
    final Factory<
      ? super S,
      ? extends Valuation<? super O, ? super M>
    > costValuation
  ) {
    return new NetValuationFactory<>(revenueValuation, costValuation);
  }

  /**
   * @param cells         the candidate cells to pick from, read for the vessel
   *                      picked for
   * @param cellPredicate filters which candidate cells are eligible for a
   *                      vessel
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for a
   *         {@link RandomPicker}
   * @see RandomPicker
   */
  public static RandomGridExplorerFactory randomGridExplorer(
    final ValueFactory<
      ? super VesselScope,
      ? super Vessel,
      ? extends List<? extends Cell>
    > cells,
    final MappingFactory<
      ? super VesselScope,
      ? super Vessel,
      ? super Cell,
      Boolean
    > cellPredicate
  ) {
    return new RandomGridExplorerFactory(
      cells,
      cellPredicate,
      randomNumberGenerator()
    );
  }
}
