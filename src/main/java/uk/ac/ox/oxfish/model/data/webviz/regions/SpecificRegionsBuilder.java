/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.data.webviz.regions;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.regs.SpecificProtectedArea;
import uk.ac.ox.oxfish.model.regs.TemporaryRegulation;
import uk.ac.ox.oxfish.model.regs.factory.SpecificProtectedAreaFactory;
import uk.ac.ox.oxfish.model.regs.factory.TemporaryRegulationFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.mapWithIndex;
import static java.lang.StrictMath.toIntExact;

@SuppressWarnings("UnstableApiUsage")
public final class SpecificRegionsBuilder implements JsonBuilder<Regions> {

    private final Function<FishState, List<AlgorithmFactory<?>>> specificRegionsFactoriesExtractor;

    SpecificRegionsBuilder(final Function<FishState, List<AlgorithmFactory<?>>> specificRegionsFactoriesExtractor) {
        this.specificRegionsFactoriesExtractor = specificRegionsFactoriesExtractor;
    }

    private Region makeRegion(int typeId, FishState fishState, AlgorithmFactory<?> regulationFactory) {
        return new Region(
            typeId,
            makePeriods(fishState, regulationFactory),
            extractCells(fishState, getSpecificProtectedArea(fishState, regulationFactory))
        );
    }

    private Collection<Period> makePeriods(FishState fishState, AlgorithmFactory<?> regulationFactory) {
        return regulationFactory instanceof TemporaryRegulationFactory
            ? makePeriods(fishState, ((TemporaryRegulationFactory) regulationFactory).apply(fishState))
            : ImmutableList.of(new Period(0, fishState.getDay()));
    }

    private Collection<Collection<Integer>> extractCells(
        FishState fishState,
        SpecificProtectedArea specificProtectedArea
    ) {
        return fishState.getMap()
            .getAllSeaTilesExcludingLandAsList()
            .stream()
            .filter(specificProtectedArea::isProtected)
            .map(this::seaTileToGridXY)
            .collect(toImmutableList());
    }

    private Collection<Period> makePeriods(FishState fishState, TemporaryRegulation temporaryRegulation) {
        ImmutableList.Builder<Period> periods = new ImmutableList.Builder<>();
        int start = -1;
        int end = -1;
        for (int step = 0; step <= fishState.getDay(); step++) {
            if (temporaryRegulation.isActive(fishState.getDayOfTheYear(step))) {
                if (start == -1) {
                    start = step;
                    end = step;
                } else if (step == end + 1) {
                    end = step;
                }
            } else if (start != -1) {
                periods.add(new Period(start, end));
                start = -1;
                end = -1;
            }
        }
        if (start != -1) periods.add(new Period(start, end));
        return periods.build();
    }

    private SpecificProtectedArea getSpecificProtectedArea(
        FishState fishState,
        AlgorithmFactory<?> regulationFactory
    ) {
        if (regulationFactory instanceof SpecificProtectedAreaFactory)
            return ((SpecificProtectedAreaFactory) regulationFactory).apply(fishState);
        else if (regulationFactory instanceof TemporaryRegulationFactory)
            return getSpecificProtectedArea(fishState, ((TemporaryRegulationFactory) regulationFactory).getDelegate());
        else
            throw new IllegalArgumentException("Can't get a SpecificProtectedArea from " + regulationFactory);
    }

    @Override public Regions buildJsonObject(
        final FishState fishState
    ) {
        final Stream<Region> regions = mapWithIndex(
            specificRegionsFactoriesExtractor.apply(fishState).stream(),
            (factory, index) -> makeRegion(toIntExact(index), fishState, factory)
        );
        return new Regions(regions::iterator);
    }

    private Collection<Integer> seaTileToGridXY(final SeaTile seaTile) {
        return ImmutableList.of(seaTile.getGridX(), seaTile.getGridY());
    }

}
