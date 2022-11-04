package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.AbundanceAggregator;
import uk.ac.ox.oxfish.biology.tuna.Aggregator;
import uk.ac.ox.oxfish.biology.tuna.BiomassAggregator;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.collectingAndThen;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;

public class TargetBiologiesGrabber<B extends LocalBiology, F extends AbstractFad<B, F>> {

    private static final Map<Class<? extends LocalBiology>, Aggregator<?>> AGGREGATORS =
        ImmutableMap.of(
            AbundanceLocalBiology.class, new AbundanceAggregator(),
            BiomassLocalBiology.class, new BiomassAggregator()
        );

    private final boolean canPoachFromFads;
    private final int rangeInSeaTiles;
    private final Aggregator<B> aggregator;
    private final Class<B> localBiologyClass;

    public TargetBiologiesGrabber(
        final boolean canPoachFromFads,
        final int rangeInSeaTiles,
        final Class<B> localBiologyClass
    ) {
        checkArgument(rangeInSeaTiles >= 0);
        this.canPoachFromFads = canPoachFromFads;
        this.rangeInSeaTiles = rangeInSeaTiles;
        this.localBiologyClass = localBiologyClass;
        //noinspection unchecked
        this.aggregator = (Aggregator<B>) AGGREGATORS.get(localBiologyClass);
    }

    /**
     * grabs all fad biologies in the area + the local biology
     */
    private Stream<B> getFadBiologiesAt(
        final SeaTile tile,
        final FadMap<B, F> fadMap
    ) {
        final Stream<AbstractFad<B, F>> fadsOnTile = bagToStream(fadMap.fadsAt(tile));
        return fadsOnTile.map(AbstractFad::getBiology);
    }

    private Stream<B> filterBiologies(final Stream<LocalBiology> biologies) {
        return biologies
            .filter(localBiologyClass::isInstance)
            .map(localBiologyClass::cast);
    }

    private Stream<B> getTileBiologiesInRange(
        final SeaTile tile,
        final NauticalMap nauticalMap
    ) {
        final Stream<B> tileBiology = filterBiologies(Stream.of(tile.getBiology()));
        return rangeInSeaTiles > 0
            ? Stream.concat(tileBiology, getNeighbourBiologies(tile, nauticalMap))
            : tileBiology;
    }

    @NotNull
    private Stream<B> getNeighbourBiologies(
        final SeaTile tile,
        final NauticalMap nauticalMap
    ) {
        final Stream<SeaTile> mooreNeighbors = bagToStream(nauticalMap.getMooreNeighbors(tile, rangeInSeaTiles));
        return filterBiologies(mooreNeighbors.map(SeaTile::getBiology));
    }

    public List<B> grabTargetBiologies(
        final SeaTile location,
        final Fisher fisher
    ) {
        final FishState fishState = fisher.grabState();
        final NauticalMap nauticalMap = fishState.getMap();
        final Stream<B> tileBiologiesInRange =
            getTileBiologiesInRange(location, nauticalMap);

        @SuppressWarnings("unchecked") final Stream<B> targetBiologies =
            canPoachFromFads
                ? Stream.concat(tileBiologiesInRange, getFadBiologiesAt(location, (FadMap<B, F>) fishState.getFadMap()))
                : tileBiologiesInRange;

        return targetBiologies.collect(collectingAndThen(
            Collectors.toCollection(ArrayList::new),
            list -> {
                Collections.shuffle(list, new Random(fisher.grabRandomizer().nextLong()));
                return list;
            }
        ));
    }

    /**
     * grabs local biology or local biologies of all the surrounding areas and aggregate them; this way it can
     * target larger stocks in a wider area
     */
    public Entry<B, List<B>> grabTargetBiologiesAndAggregateThem(
        final SeaTile location,
        final Fisher fisher
    ) {
        final List<B> targetBiologies = grabTargetBiologies(location, fisher);
        final B aggregatedBiology = aggregator.apply(fisher.grabState().getBiology(), targetBiologies);
        return entry(aggregatedBiology, targetBiologies);
    }

}
