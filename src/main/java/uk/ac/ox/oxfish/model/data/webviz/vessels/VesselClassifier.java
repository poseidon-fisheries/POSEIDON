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
import com.google.common.collect.ImmutableSortedSet;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.data.webviz.colours.ColourSeries;

import java.awt.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import static com.google.common.collect.Iterators.cycle;
import static java.util.Comparator.comparing;
import static java.util.stream.IntStream.iterate;

public class VesselClassifier<T> {

    private final ImmutableList<String> typeNames;
    private final ImmutableList<Color> typeColours;
    private final ImmutableMap<T, Integer> typeIds;
    private final Function<Fisher, T> typeExtractor;
    private final SortedMap<Fisher, Integer> vesselTypes = new TreeMap<>(comparing(Fisher::getID));

    public VesselClassifier(
        Map<T, String> typeNames,
        Function<Fisher, T> typeExtractor,
        ColourSeries typeColours
    ) {
        this.typeExtractor = typeExtractor;
        ImmutableList.Builder<String> typeNamesBuilder = new ImmutableList.Builder<>();
        ImmutableList.Builder<Color> typeColorsBuilder = new ImmutableList.Builder<>();
        ImmutableMap.Builder<T, Integer> typeIdsBuilder = new ImmutableMap.Builder<>();
        final Iterator<Color> colorCycle = cycle(typeColours.getJavaColors());
        final PrimitiveIterator.OfInt typeIdIterator = iterate(0, i -> i + 1).iterator();
        typeNames.forEach((type, name) -> {
            typeNamesBuilder.add(name);
            typeColorsBuilder.add(colorCycle.next());
            typeIdsBuilder.put(type, typeIdIterator.next());
        });
        this.typeNames = typeNamesBuilder.build();
        this.typeColours = typeColorsBuilder.build();
        this.typeIds = typeIdsBuilder.build();
    }

    public static VesselClassifier<Integer> singleTypeClassifier(String typeName, Color javaColor) {
        return new VesselClassifier<>(
            ImmutableMap.of(0, typeName),
            __ -> 0,
            new ColourSeries(javaColor)
        );
    }

    Map<Fisher, Integer> getVesselTypes() { return vesselTypes; }

    Set<Integer> getTypeIds() { return ImmutableSortedSet.copyOf(vesselTypes.values()); }

    void classify(Fisher fisher) {
        vesselTypes.computeIfAbsent(fisher, f ->
            typeIds.get(typeExtractor.apply(f))
        );
    }

    Color getJavaColor(int typeId) { return typeColours.get(typeId); }

    String getLegend(int typeId) { return typeNames.get(typeId); }

    public Collection<T> getTypes() { return typeIds.keySet(); }

    public Color getJavaColor(T type) { return typeColours.get(typeIds.get(type)); }

    public String getLegend(T type) { return typeNames.get(typeIds.get(type)); }

}
