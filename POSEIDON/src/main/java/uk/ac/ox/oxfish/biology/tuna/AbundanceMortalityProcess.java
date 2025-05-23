/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.primitives.ImmutableDoubleArray;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.Observer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;

/**
 * A proportional mortality process. Note that "proportional mortality" here means mortality as a direct percentage as
 * opposed to {@link uk.ac.ox.oxfish.biology.complicated.ProportionalMortalityProcess} that does exponentiation.
 */
public class AbundanceMortalityProcess implements BiologicalProcess<AbundanceLocalBiology> {

    private final List<Observer<DeathEvent>> deathEventsObservers = new LinkedList<>();
    private final Map<Species, Map<String, List<ImmutableDoubleArray>>> mortalitySources;

    public AbundanceMortalityProcess(
        final Map<Species, Map<String, List<List<Double>>>> mortalitySources
    ) {
        // deep copy everything to immutable structures
        this.mortalitySources =
            mortalitySources.entrySet().stream().collect(toImmutableMap(
                Entry::getKey,
                entry1 -> entry1.getValue().entrySet().stream().collect(toImmutableMap(
                    Entry::getKey,
                    entry2 -> entry2.getValue()
                        .stream()
                        .map(ImmutableDoubleArray::copyOf)
                        .collect(toImmutableList())
                ))
            ));
    }

    public List<Observer<DeathEvent>> getDeathEventsObservers() {
        return deathEventsObservers;
    }

    @Override
    public Collection<AbundanceLocalBiology> process(
        final FishState fishState,
        final Collection<AbundanceLocalBiology> biologies
    ) {
        return biologies.stream().map(biology ->
            new AbundanceLocalBiology(
                biology.getAbundance().entrySet().stream().collect(toImmutableMap(
                    Entry::getKey,
                    entry -> applyMortality(entry.getKey(), entry.getValue())
                ))
            )
        ).collect(toImmutableList());
    }

    private double[][] applyMortality(
        final Species species,
        final double[][] abundance
    ) {
        final int subs = species.getNumberOfSubdivisions();
        final int bins = species.getNumberOfBins();
        final double[][] newAbundance =
            stream(abundance)
                .map(double[]::clone)
                .toArray(double[][]::new);
        mortalitySources
            .get(species)
            .values()
            .stream()
            .map(mortality ->
                range(0, subs).mapToObj(sub ->
                    range(0, bins).mapToDouble(bin ->
                        abundance[sub][bin] * mortality.get(sub).get(bin)
                    ).toArray()
                ).toArray(double[][]::new)
            )
            .forEach(deaths ->
                range(0, subs).forEach(sub ->
                    range(0, bins).forEach(bin -> {
                        deathEventsObservers.forEach(observer ->
                            observer.observe(new DeathEvent(species, sub, bin, deaths[sub][bin]))
                        );
                        newAbundance[sub][bin] -= deaths[sub][bin];
                    })
                )
            );
        return newAbundance;
    }

    public static class DeathEvent {
        public final Species species;
        public final int sub;
        public final int bin;
        public final double deaths;

        public DeathEvent(
            final Species species,
            final int sub,
            final int bin,
            final double deaths
        ) {
            this.species = species;
            this.sub = sub;
            this.bin = bin;
            this.deaths = deaths;
        }
    }
}
