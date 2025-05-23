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

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.AbundanceAggregator;
import uk.ac.ox.oxfish.biology.tuna.Aggregator;
import uk.ac.ox.oxfish.biology.tuna.BiomassAggregator;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Suppliers.memoize;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.shufflingCollector;
import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;

public class TargetBiologiesGrabber<B extends LocalBiology> {

    private static final Map<Class<? extends LocalBiology>, Aggregator<?>> AGGREGATORS =
        ImmutableMap.of(
            AbundanceLocalBiology.class, new AbundanceAggregator(),
            BiomassLocalBiology.class, new BiomassAggregator()
        );

    private final boolean canPoachFromFads;
    private final int rangeInSeaTiles;
    private final Aggregator<B> aggregator;
    private final Class<? extends B> localBiologyClass;

    @SuppressWarnings("unchecked")
    public TargetBiologiesGrabber(
        final boolean canPoachFromFads,
        final int rangeInSeaTiles,
        final Class<? extends B> localBiologyClass
    ) {
        checkArgument(rangeInSeaTiles >= 0);
        this.canPoachFromFads = canPoachFromFads;
        this.rangeInSeaTiles = rangeInSeaTiles;
        this.localBiologyClass = localBiologyClass;
        this.aggregator = (Aggregator<B>) AGGREGATORS.get(localBiologyClass);
    }

    /**
     * In order to allow school sets to meet their empirical targets, we need to give them access to more biologies,
     * so we provide a list of biologies that can include neighbouring cell biologies and FAD biologies. We also
     * provide a way to aggregate all those biologies into a single one, but since that's an expensive operation
     * that we don't need all the time, we wrap this bit in a supplier.
     */
    public Entry<List<B>, Supplier<B>> grabTargetBiologiesAndAggregator(
        final SeaTile location,
        final Fisher fisher
    ) {
        final List<B> targetBiologies =
            grabTargetBiologies(location, fisher);
        final Supplier<B> aggregatedBiologySupplier =
            memoize(() -> aggregator.apply(fisher.grabState().getBiology(), targetBiologies));
        return entry(targetBiologies, aggregatedBiologySupplier);
    }

    private List<B> grabTargetBiologies(
        final SeaTile location,
        final Fisher fisher
    ) {
        final FishState fishState = fisher.grabState();
        final MersenneTwisterFast rng = fisher.grabRandomizer();
        final NauticalMap nauticalMap = fishState.getMap();
        final Stream<B> tileBiologiesInRange = getTileBiologiesInRange(location, nauticalMap, rng);

        @SuppressWarnings("unchecked") final Stream<B> targetBiologies =
            canPoachFromFads
                ? Stream.concat(
                tileBiologiesInRange,
                getFadBiologiesAt(location, fishState.getFadMap(), rng)
            )
                : tileBiologiesInRange;

        return targetBiologies.collect(toImmutableList());
    }

    private Stream<B> getTileBiologiesInRange(
        final SeaTile tile,
        final NauticalMap nauticalMap,
        final MersenneTwisterFast rng
    ) {
        final Stream<B> tileBiology = filterBiologies(Stream.of(tile.getBiology()));
        return rangeInSeaTiles > 0
            ? Stream.concat(tileBiology, getNeighbourBiologies(tile, nauticalMap, rng).stream())
            : tileBiology;
    }

    /**
     * grabs all fad biologies in the area + the local biology
     */
    private Stream<B> getFadBiologiesAt(
        final SeaTile tile,
        final FadMap fadMap,
        final MersenneTwisterFast rng
    ) {
        final Stream<Fad> fadsOnTile = bagToStream(fadMap.fadsAt(tile));
        return filterBiologies(fadsOnTile.map(Fad::getBiology))
            .collect(shufflingCollector(rng))
            .stream();
    }

    private Stream<B> filterBiologies(final Stream<LocalBiology> biologies) {
        return biologies
            .filter(localBiologyClass::isInstance)
            .map(localBiologyClass::cast);
    }

    private List<B> getNeighbourBiologies(
        final SeaTile tile,
        final NauticalMap nauticalMap,
        final MersenneTwisterFast rng
    ) {
        final Stream<SeaTile> mooreNeighbors = bagToStream(nauticalMap.getMooreNeighbors(tile, rangeInSeaTiles));
        return filterBiologies(mooreNeighbors.map(SeaTile::getBiology)).collect(shufflingCollector(rng));
    }

}
