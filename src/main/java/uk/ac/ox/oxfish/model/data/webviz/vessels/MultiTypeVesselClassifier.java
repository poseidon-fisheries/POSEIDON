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

package uk.ac.ox.oxfish.model.data.webviz.vessels;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.Fisher;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.function.Function;

import static com.google.common.collect.Iterators.cycle;
import static java.util.stream.IntStream.iterate;
import static uk.ac.ox.oxfish.model.data.webviz.ColourSeries.SET1;

public class MultiTypeVesselClassifier<T> implements VesselClassifier {

    private final ImmutableList<String> typeNames;
    private final ImmutableList<Color> typeColors;
    private final ImmutableMap<T, Integer> typeIds;
    private final Function<Fisher, T> typeExtractor;

    public MultiTypeVesselClassifier(
        Map<T, String> typeNames,
        Function<Fisher, T> typeExtractor
    ) {
        this(typeNames, typeExtractor, SET1.getJavaColors());
    }

    @SuppressWarnings("WeakerAccess")
    public MultiTypeVesselClassifier(
        Map<T, String> typeNames,
        Function<Fisher, T> typeExtractor,
        Iterable<Color> typeColors
    ) {
        this.typeExtractor = typeExtractor;
        ImmutableList.Builder<String> typeNamesBuilder = new ImmutableList.Builder<>();
        ImmutableList.Builder<Color> typeColorsBuilder = new ImmutableList.Builder<>();
        ImmutableMap.Builder<T, Integer> typeIdsBuilder = new ImmutableMap.Builder<>();
        final Iterator<Color> colorCycle = cycle(typeColors);
        final PrimitiveIterator.OfInt typeIdIterator = iterate(1, i -> i + 1).iterator();
        typeNames.forEach((type, name) -> {
            typeNamesBuilder.add(name);
            typeColorsBuilder.add(colorCycle.next());
            typeIdsBuilder.put(type, typeIdIterator.next());
        });
        this.typeNames = typeNamesBuilder.build();
        this.typeColors = typeColorsBuilder.build();
        this.typeIds = typeIdsBuilder.build();
    }

    @Override public Color getColor(int typeId) { return typeColors.get(typeId); }

    @Override public String getLegend(int typeId) { return typeNames.get(typeId); }

    @Override public int applyAsInt(Fisher fisher) { return typeIds.get(typeExtractor.apply(fisher)); }

}
