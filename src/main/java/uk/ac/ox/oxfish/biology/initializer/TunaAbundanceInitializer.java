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

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.TunaMeristics;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public class TunaAbundanceInitializer implements BiologyInitializer {

    private final SpeciesCodes speciesCodes;
    private final Map<String, List<Bin>> binsPerSpecies;

    public TunaAbundanceInitializer(
        final SpeciesCodes speciesCodes,
        final Map<String, List<Bin>> binsPerSpecies
    ) {
        this.speciesCodes = speciesCodes;
        this.binsPerSpecies = binsPerSpecies.entrySet().stream()
            .collect(toImmutableMap(
                Entry::getKey,
                entry -> ImmutableList.copyOf(entry.getValue())
            ));
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
        final GlobalBiology biology,
        final NauticalMap nauticalMap,
        final MersenneTwisterFast rng,
        final FishState fishState
    ) {

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
                final String speciesName = speciesCodes.getSpeciesName(entry.getKey());
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
