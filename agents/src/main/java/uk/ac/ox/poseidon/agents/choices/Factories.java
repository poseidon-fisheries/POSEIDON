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

package uk.ac.ox.poseidon.agents.choices;

import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.components.VesselComponentRegister;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Factories for a vessel's learned {@link OptionValues}, its {@link Memory} of observations and the
 * rules updating it, and its destination-choice strategies.
 */
public class Factories {

    private Factories() {}

    /**
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for an
     * {@link AverageOptionValues}
     * @see AverageOptionValues
     */
    public static <O> AverageOptionValuesFactory<O> averageOptionValues() {
        return new AverageOptionValuesFactory<>();
    }

    /**
     * @param optionValuesRegister looks up each other vessel's own option-values component
     * @param friendsSupplier      supplies the vessel's current friends
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a supplier of
     * friends' best options
     * @see BestOptionsFromFriends
     */
    public static <O> BestOptionsFromFriendsFactory<O> bestOptionsFromFriends(
        final Factory<
            ? super VesselScope,
            ? extends VesselComponentRegister<? extends OptionValues<O>>
            > optionValuesRegister,
        final Factory<? super VesselScope, ? extends Supplier<? extends Iterable<? extends Vessel>>>
            friendsSupplier
    ) {
        return new BestOptionsFromFriendsFactory<>(
            optionValuesRegister,
            friendsSupplier
        );
    }

    /**
     * @param optionValuesRegister looks up each other vessel's own option-values component
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a supplier of
     * every other active vessel's best options
     * @see BestOptions
     */
    public static <O> BestOptionsFactory<O> bestOptions(
        final Factory<
            ? super VesselScope,
            ? extends VesselComponentRegister<? extends OptionValues<O>>
            > optionValuesRegister
    ) {
        return new BestOptionsFactory<>(optionValuesRegister);
    }

    /**
     * @param modelGrid  the grid {@code coordinate} is resolved against
     * @param coordinate the fixed destination
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link ConstantDestinationSupplier}
     * @see ConstantDestinationSupplier
     */
    public static ConstantDestinationSupplierFactory constantDestination(
        final Factory<? super VesselScope, ? extends ModelGrid> modelGrid,
        final Factory<? super VesselScope, ? extends Coordinate> coordinate
    ) {
        return new ConstantDestinationSupplierFactory(modelGrid, coordinate);
    }

    /**
     * @param epsilon   probability of exploring instead of exploiting
     * @param explorer  supplies an exploratory destination
     * @param exploiter supplies the currently-best-known destination
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for an
     * {@link EpsilonGreedyChooser}-backed {@link DestinationSupplier}
     * @see EpsilonGreedyChooser
     */
    public static EpsilonGreedyDestinationSupplierFactory epsilonGreedyDestination(
        final double epsilon,
        final Factory<? super VesselScope, ? extends Supplier<Int2D>> explorer,
        final Factory<? super VesselScope, ? extends Supplier<Int2D>> exploiter
    ) {
        return new EpsilonGreedyDestinationSupplierFactory(
            epsilon, explorer, exploiter
        );
    }

    /**
     * @param alpha the weight of a new observation, between 0 and 1
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link Memory} update rule
     * that is a species-by-species exponential moving average of
     * {@link uk.ac.ox.poseidon.biology.buckets.Bucket}s
     * @see ExponentialMovingAverageOfBuckets
     */
    public static ExponentialMovingAverageOfBucketsFactory exponentialMovingAverageOfBuckets(
        final double alpha
    ) {
        return new ExponentialMovingAverageOfBucketsFactory(alpha);
    }

    /**
     * @param alpha the weight of a new observation, between 0 and 1
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link Memory} update rule
     * that is an exponential moving average of {@code Double}s
     * @see ExponentialMovingAverageOfDoubles
     */
    public static ExponentialMovingAverageOfDoublesFactory exponentialMovingAverageOfDoubles(
        final double alpha
    ) {
        return new ExponentialMovingAverageOfDoublesFactory(alpha);
    }

    /**
     * @param alpha the smoothing factor, in {@code [0, 1]}
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for an
     * {@link ExponentialMovingAverageOptionValues}
     * @see ExponentialMovingAverageOptionValues
     */
    public static <O> ExponentialMovingAverageOptionValuesFactory<O>
    exponentialMovingAverageOptionValues(final double alpha) {
        return new ExponentialMovingAverageOptionValuesFactory<>(alpha);
    }

    /**
     * @param portGrid locates the vessel's home port
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link HomePortDestinationSupplier}
     * @see HomePortDestinationSupplier
     */
    public static HomePortDestinationSupplierFactory homePortDestination(
        final Factory<? super VesselScope, ? extends PortGrid> portGrid
    ) {
        return new HomePortDestinationSupplierFactory(portGrid);
    }

    /**
     * @param optionValues          the vessel's own learned option values
     * @param optionPredicate       filters which candidate options are eligible
     * @param optionValuesSupplier  supplies the candidate options to imitate
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for an
     * {@link ImitatingPicker}
     * @see ImitatingPicker
     */
    public static <O> ImitatingPickerFactory<O> imitatingPicker(
        final Factory<? super VesselScope, ? extends OptionValues<O>> optionValues,
        final Factory<? super VesselScope, ? extends Predicate<? super O>> optionPredicate,
        final Factory<? super VesselScope, ? extends Supplier<OptionValues<O>>>
            optionValuesSupplier
    ) {
        return new ImitatingPickerFactory<>(
            optionValues, optionPredicate, optionValuesSupplier
        );
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for an empty
     * {@link KeyedMemory}
     * @see KeyedMemory
     */
    public static <K, O, M> KeyedMemoryFactory<K, O, M> keyedMemory() {
        return new KeyedMemoryFactory<>();
    }

    /**
     * @param keyedMemory  the keyed memory to select from
     * @param keyExtractor extracts the key from the selector's input
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link KeyedMemorySelector}
     * @see KeyedMemorySelector
     */
    public static <S extends Scope, T, K, O, M>
    KeyedMemorySelectorFactory<S, T, K, O, M> keyedMemorySelector(
        final Factory<? super S, ? extends KeyedMemory<K, O, M>> keyedMemory,
        final Factory<? super S, ? extends Function<? super T, ? extends K>> keyExtractor
    ) {
        return new KeyedMemorySelectorFactory<>(keyedMemory, keyExtractor);
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for an empty
     * {@link Memory}
     * @see Memory
     */
    public static <O, M> MemoryFactory<O, M> memory() {
        return new MemoryFactory<>();
    }

    /**
     * @param optionValues              the vessel's own learned option values, for the search's
     *                                  starting cell
     * @param pathFinder                finds accessible water neighbours
     * @param cellPredicate             filters which candidate cells are eligible
     * @param neighbourhoodSizeSupplier the initial search radius
     * @param fallbackCellPicker        supplies a starting cell if there's no best-known option
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link NeighbourhoodCellPicker}
     * @see NeighbourhoodCellPicker
     */
    public static NeighbourhoodGridExplorerFactory neighbourhoodGridExplorer(
        final Factory<? super VesselScope, ? extends OptionValues<Int2D>> optionValues,
        final Factory<? super VesselScope, ? extends GridPathFinder> pathFinder,
        final Factory<? super VesselScope, ? extends Predicate<? super Int2D>> cellPredicate,
        final Factory<? super VesselScope, ? extends IntSupplier> neighbourhoodSizeSupplier,
        final Factory<? super VesselScope, ? extends Supplier<Int2D>> fallbackCellPicker
    ) {
        return new NeighbourhoodGridExplorerFactory(
            optionValues, pathFinder, cellPredicate, neighbourhoodSizeSupplier, fallbackCellPicker
        );
    }

    /**
     * @param cellsSupplier the candidate cells to pick from
     * @param cellPredicate filters which candidate cells are eligible
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link RandomPicker}
     * @see RandomPicker
     */
    public static RandomGridExplorerFactory randomGridExplorer(
        final Factory<? super VesselScope, ? extends Supplier<? extends List<? extends Int2D>>> cellsSupplier,
        final Factory<? super VesselScope, ? extends Predicate<? super Int2D>> cellPredicate
    ) {
        return new RandomGridExplorerFactory(cellsSupplier, cellPredicate);
    }
}
