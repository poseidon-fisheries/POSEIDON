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

package uk.ac.ox.oxfish.biology.tuna;

import static java.util.stream.IntStream.range;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.complicated.StandardAgingProcess;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.model.FishState;

/**
 * This is just a wrapper around {@link StandardAgingProcess} that turns it into a {@link
 * BiologicalProcess}.
 */
public class AgingAndRecruitmentProcess implements BiologicalProcess<AbundanceLocalBiology> {

    private final Map<Species, ? extends RecruitmentProcess> recruitmentProcesses;

    private final StandardAgingProcess standardAgingProcess =
        new StandardAgingProcess(false);

    public AgingAndRecruitmentProcess(
        final Map<Species, ? extends RecruitmentProcess> recruitmentProcesses
    ) {
        this.recruitmentProcesses = ImmutableMap.copyOf(recruitmentProcesses);
    }

    @Override
    public Optional<AbundanceLocalBiology> process(
        final FishState fishState,
        final AbundanceLocalBiology aggregatedBiology
    ) {
        // Make a copy of the abundance that will be mutated by the standardAgingProcess
        final AbundanceLocalBiology biology = new AbundanceLocalBiology(aggregatedBiology);

        fishState.getSpecies().forEach(species -> {

            final StructuredAbundance abundance = biology.getAbundance(species);

            // Compute the new recruits
            final double recruits =
                recruitmentProcesses.get(species).recruit(
                    species,
                    species.getMeristics(),
                    abundance,
                    fishState.getDayOfTheYear(),
                    365 // this isn't true, but gives the "yearly" recruitment we need
                );

            // Let the aging process mutate the biology
            standardAgingProcess.ageLocally(
                biology,
                species,
                fishState,
                false,
                365 // needs to be 365 but isn't actually used by StandardAgingProcess
            );

            // Add the new recruits directly to the abundance matrix
            final int n = abundance.getSubdivisions();
            range(0, n).forEach(i ->
                abundance.asMatrix()[i][0] = recruits / n
            );

        });
        return Optional.of(biology);
    }
}
