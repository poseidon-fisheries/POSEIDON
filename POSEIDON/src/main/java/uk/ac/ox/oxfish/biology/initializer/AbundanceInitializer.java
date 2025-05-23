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

package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.TunaMeristics;
import uk.ac.ox.oxfish.biology.tuna.Reallocator;
import uk.ac.ox.oxfish.biology.tuna.WeightGroups;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Arrays.setAll;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.MALE;

public class AbundanceInitializer implements BiologyInitializer {

    private final Map<String, List<Bin>> binsPerSpecies;

    private final SpeciesCodes speciesCodes;
    private final Reallocator<AbundanceLocalBiology> abundanceReallocator;
    private final Map<String, WeightGroups> weightGroupsPerSpecies;

    AbundanceInitializer(
        final SpeciesCodes speciesCodes,
        final Map<String, List<Bin>> binsPerSpecies,
        final Map<String, WeightGroups> weightGroupsPerSpecies,
        final Reallocator<AbundanceLocalBiology> abundanceReallocator
    ) {
        this.speciesCodes = speciesCodes;
        this.binsPerSpecies = binsPerSpecies.entrySet().stream()
            .collect(toImmutableMap(
                entry -> speciesCodes.getSpeciesName(entry.getKey()),
                entry -> ImmutableList.copyOf(entry.getValue())
            ));
        this.weightGroupsPerSpecies = ImmutableMap.copyOf(weightGroupsPerSpecies);
        this.abundanceReallocator = abundanceReallocator;
    }

    @SuppressWarnings("unused")
    public Reallocator<AbundanceLocalBiology> getAbundanceReallocator() {
        return abundanceReallocator;
    }

    @Override
    public LocalBiology generateLocal(
        final GlobalBiology globalBiology,
        final SeaTile seaTile,
        final MersenneTwisterFast rng,
        final int mapHeightInCells,
        final int mapWidthInCells,
        final NauticalMap nauticalMap
    ) {
        return new AbundanceLocalBiology(globalBiology);
    }

    @Override
    public void processMap(
        final GlobalBiology globalBiology,
        final NauticalMap nauticalMap,
        final MersenneTwisterFast rng,
        final FishState fishState
    ) {
        final AbundanceLocalBiology aggregatedAbundance =
            new AbundanceLocalBiology(
                binsPerSpecies.entrySet()
                    .stream()
                    .collect(toImmutableMap(
                        entry -> globalBiology.getSpeciesByCaseInsensitiveName(entry.getKey()),
                        entry -> binsToAbundanceMatrix(entry.getValue())
                    ))
            );
        abundanceReallocator.reallocate(
            0,
            globalBiology,
            nauticalMap.getAllSeaTilesExcludingLandAsList(),
            aggregatedAbundance
        );
    }

    private static double[][] binsToAbundanceMatrix(final List<? extends Bin> bins) {
        final int numSubdivisions = 2; // MALE and FEMALE
        final double[][] abundance = new double[numSubdivisions][bins.size()];
        setAll(abundance[MALE], i -> bins.get(i).numberOfMales);
        setAll(abundance[FEMALE], i -> bins.get(i).numberOfFemales);
        return abundance;
    }

    @Override
    public GlobalBiology generateGlobal(
        final MersenneTwisterFast rng,
        final FishState fishState
    ) {
        final Species[] species = binsPerSpecies
            .entrySet()
            .stream()
            .map(entry -> {
                final String speciesName = entry.getKey();
                final List<Bin> bins = entry.getValue();
                final TunaMeristics tunaMeristics = new TunaMeristics(
                    makeList(bins, bin -> bin.maleWeight, bin -> bin.femaleWeight),
                    makeList(bins, bin -> bin.maleLength, bin -> bin.femaleLength),
                    bins.stream().mapToDouble(bin -> bin.maturity).toArray(),
                    weightGroupsPerSpecies.get(speciesName)
                );
                return new Species(
                    speciesName,
                    speciesCodes.getSpeciesCode(speciesName),
                    tunaMeristics,
                    false
                );
            })
            .toArray(Species[]::new);
        return new GlobalBiology(species);
    }

    private static ImmutableList<double[]> makeList(
        final Collection<Bin> bins,
        final ToDoubleFunction<Bin> getMaleValue,
        final ToDoubleFunction<Bin> getFemaleValue
    ) {
        return ImmutableList.of(
            bins.stream().mapToDouble(getMaleValue).toArray(),
            bins.stream().mapToDouble(getFemaleValue).toArray()
        );
    }

    static class Bin {

        final double numberOfFemales;
        final double numberOfMales;
        final double femaleWeight;
        final double maleWeight;
        final double femaleLength;
        final double maleLength;
        final double maturity;

        Bin(
            final double numberOfFemales,
            final double numberOfMales,
            final double femaleWeight,
            final double maleWeight,
            final double femaleLength,
            final double maleLength,
            final double maturity
        ) {
            this.numberOfFemales = numberOfFemales;
            this.numberOfMales = numberOfMales;
            this.femaleWeight = femaleWeight;
            this.maleWeight = maleWeight;
            this.femaleLength = femaleLength;
            this.maleLength = maleLength;
            this.maturity = maturity;
        }
    }
}
