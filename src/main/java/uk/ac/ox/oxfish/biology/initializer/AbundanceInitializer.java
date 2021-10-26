/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.initializer;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Arrays.setAll;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.MALE;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import java.util.List;
import java.util.Map;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.TunaMeristics;
import uk.ac.ox.oxfish.biology.initializer.allocator.AbundanceReallocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public class AbundanceInitializer implements BiologyInitializer {

    private final Map<String, List<Bin>> binsPerSpecies;
    private final AbundanceReallocator abundanceReallocator;

    AbundanceInitializer(
        final SpeciesCodes speciesCodes,
        final Map<String, List<Bin>> binsPerSpecies,
        final AbundanceReallocator abundanceReallocator
    ) {
        this.binsPerSpecies = binsPerSpecies.entrySet().stream()
            .collect(toImmutableMap(
                entry -> speciesCodes.getSpeciesName(entry.getKey()),
                entry -> ImmutableList.copyOf(entry.getValue())
            ));
        this.abundanceReallocator = abundanceReallocator;
    }

    public AbundanceReallocator getAbundanceReallocator() {
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
        final Map<Species, double[][]> aggregatedAbundances =
            binsPerSpecies.entrySet().stream()
                .collect(toImmutableMap(
                    entry -> globalBiology.getSpecie(entry.getKey()),
                    entry -> binsToAbundance(entry.getValue())
                ));
        abundanceReallocator.reallocateAtStep(
            0,
            aggregatedAbundances,
            nauticalMap
        );
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
                    ImmutableList.of(
                        bins.stream().mapToDouble(bin -> bin.maleWeight).toArray(),
                        bins.stream().mapToDouble(bin -> bin.femaleWeight).toArray()
                    ),
                    ImmutableList.of(
                        bins.stream().mapToDouble(bin -> bin.maleLength).toArray(),
                        bins.stream().mapToDouble(bin -> bin.femaleLength).toArray()
                    )
                );
                return new Species(speciesName, tunaMeristics);
            })
            .toArray(Species[]::new);
        return new GlobalBiology(species);
    }

    private static double[][] binsToAbundance(final List<? extends Bin> bins) {
        final int numSubdivisions = 2; // MALE and FEMALE
        final double[][] abundance = new double[numSubdivisions][bins.size()];
        setAll(abundance[MALE], i -> bins.get(i).numberOfMales);
        setAll(abundance[FEMALE], i -> bins.get(i).numberOfFemales);
        return abundance;
    }

    static class Bin {

        final double numberOfFemales;
        final double numberOfMales;
        final double femaleWeight;
        final double maleWeight;
        final double femaleLength;
        final double maleLength;
        final double femaleNaturalMortality;
        final double maleNaturalMortality;
        final double maturity;

        Bin(
            final double numberOfFemales,
            final double numberOfMales,
            final double femaleWeight,
            final double maleWeight,
            final double femaleLength,
            final double maleLength,
            final double maturity,
            final double femaleNaturalMortality,
            final double maleNaturalMortality
        ) {
            this.numberOfFemales = numberOfFemales;
            this.numberOfMales = numberOfMales;
            this.femaleWeight = femaleWeight;
            this.maleWeight = maleWeight;
            this.femaleLength = femaleLength;
            this.maleLength = maleLength;
            this.maturity = maturity;
            this.femaleNaturalMortality = femaleNaturalMortality;
            this.maleNaturalMortality = maleNaturalMortality;
        }
    }
}
