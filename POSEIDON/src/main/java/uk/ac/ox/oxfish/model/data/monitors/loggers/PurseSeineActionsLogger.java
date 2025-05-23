/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.data.monitors.loggers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.biology.complicated.TunaMeristics;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.observers.PurseSeinerActionObserver;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.cartesianProduct;
import static java.lang.String.join;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class PurseSeineActionsLogger implements AdditionalStartable, RowProvider {

    private static final Set<String> SPECIES_CODES =
        ImmutableSortedSet.of("bet", "skj", "yft");

    private static final List<String> HEADERS =
        ImmutableList.<String>builder().add(
            "ves_no",
            "action_type",
            "lon",
            "lat",
            "date_time",
            "fad_id",
            "trip_id"
        ).addAll(
            SPECIES_CODES
        ).addAll(
            cartesianProduct(ImmutableList.of(
                SPECIES_CODES,
                ImmutableSet.of("small", "medium", "large")
            )).stream().map(xs -> join("_", xs)).collect(toList())
        ).build();

    private final Collection<ActionObserver<? extends PurseSeinerAction>> observers =
        ImmutableList.of(
            new ActionObserver<>(FadDeploymentAction.class),
            new ActionObserver<>(FadSetAction.class),
            new ActionObserver<>(OpportunisticFadSetAction.class),
            new ActionObserver<>(NonAssociatedSetAction.class),
            new ActionObserver<>(DolphinSetAction.class)
        );
    private final ImmutableList.Builder<ActionRecord> actionRecords = new ImmutableList.Builder<>();
    private final FishState fishState;

    public PurseSeineActionsLogger(final FishState fishState) {
        this.fishState = fishState;
    }

    @Override
    public List<String> getHeaders() {
        return HEADERS;
    }

    @Override
    public Iterable<? extends List<?>> getRows() {
        return actionRecords.build().stream().map(ActionRecord::asRow).collect(toImmutableList());
    }

    @Override
    public void start(final FishState fishState) {
        assert this.fishState == fishState;
        observers.forEach(observer -> observer.start(fishState));
    }

    private Map<String, Double> getCatchPerSize(
        final Species species,
        final StructuredAbundance abundance
    ) {
        final TunaMeristics meristics = (TunaMeristics) species.getMeristics();
        final List<Map<String, List<Integer>>> weightBins = meristics.getWeightBins();
        return range(0, species.getNumberOfSubdivisions()).boxed()
            .flatMap(sub ->
                weightBins.get(sub)
                    .entrySet()
                    .stream()
                    .map(entry -> entry(
                            entry.getKey(),
                            entry.getValue()
                                .stream()
                                .mapToDouble(bin ->
                                    abundance.getAbundance(sub, bin) * meristics.getWeight(sub, bin)
                                )
                                .sum()
                        )
                    )
            )
            .collect(groupingBy(Entry::getKey, summingDouble(Entry::getValue)));
    }

    private class ActionRecord {

        private final String boatId;
        private final String actionType;
        private final double lon;
        private final double lat;
        private final LocalDateTime dateTime;
        private final String fadId;
        private final long tripId;
        private Double bet;
        private Double skj;
        private Double yft;
        private Map<String, Double> betCatchPerSize;
        private Map<String, Double> skjCatchPerSize;
        private Map<String, Double> yftCatchPerSize;

        private ActionRecord(final PurseSeinerAction action) {
            this.boatId = action.getFisher().getTagsList().get(0);
            this.actionType = ActionClass.classMap.get(action.getClass()).toString();
            final Coordinate coordinates = fishState.getMap().getCoordinates(action.getLocation());
            this.lon = coordinates.x;
            this.lat = coordinates.y;
            this.dateTime = action.getTime()
                .map(action.getDate()::atTime)
                .orElseThrow(() -> new IllegalStateException("Time not set for action: " + action));
            this.fadId = Optional.of(action)
                .filter(FadRelatedAction.class::isInstance)
                .map(a -> ((FadRelatedAction) a).getFad())
                .map(Fad::getId)
                .map(Object::toString)
                .orElse("NA");
            final TripRecord currentTrip = action.getFisher().getCurrentTrip();
            this.tripId = currentTrip.getTripId();
            if (action instanceof AbstractSetAction) {
                ((AbstractSetAction) action).getCatchesKept().ifPresent(catchesKept -> {
                    setCatches(catchesKept, "Bigeye tuna", x -> this.bet = x, x -> this.betCatchPerSize = x);
                    setCatches(catchesKept, "Skipjack tuna", x -> this.skj = x, x -> this.skjCatchPerSize = x);
                    setCatches(catchesKept, "Yellowfin tuna", x -> this.yft = x, x -> this.yftCatchPerSize = x);
                });
            }
        }

        private void setCatches(
            final Catch catchesKept,
            final String speciesName,
            final Consumer<? super Double> weightCaughtSetter,
            final Consumer<? super Map<String, Double>> catchPerSizeSetter
        ) {
            final Species species = fishState.getSpecies(speciesName);
            weightCaughtSetter.accept(catchesKept.getWeightCaught(species));
            if (catchesKept.hasAbundanceInformation()) {
                final StructuredAbundance abundance = requireNonNull(catchesKept.getAbundance(species));
                catchPerSizeSetter.accept(getCatchPerSize(species, abundance));
            }
        }

        private List<?> asRow() {
            return unmodifiableList(newArrayList(
                boatId,
                actionType,
                lon,
                lat,
                dateTime,
                fadId,
                tripId,
                bet,
                skj,
                yft,
                betCatchPerSize == null ? null : betCatchPerSize.get("small"),
                betCatchPerSize == null ? null : betCatchPerSize.get("medium"),
                betCatchPerSize == null ? null : betCatchPerSize.get("large"),
                skjCatchPerSize == null ? null : skjCatchPerSize.get("small"),
                skjCatchPerSize == null ? null : skjCatchPerSize.get("medium"),
                skjCatchPerSize == null ? null : skjCatchPerSize.get("large"),
                yftCatchPerSize == null ? null : yftCatchPerSize.get("small"),
                yftCatchPerSize == null ? null : yftCatchPerSize.get("medium"),
                yftCatchPerSize == null ? null : yftCatchPerSize.get("large")
            ));
        }

    }

    private class ActionObserver<A extends PurseSeinerAction> extends PurseSeinerActionObserver<A> {

        ActionObserver(final Class<A> observedClass) {
            super(observedClass);
        }

        @Override
        public void observe(final A action) {
            actionRecords.add(new ActionRecord(action));
        }

    }

}
