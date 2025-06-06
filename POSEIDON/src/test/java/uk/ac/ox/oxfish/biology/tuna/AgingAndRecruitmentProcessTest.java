/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentBySpawningBiomass;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.complicated.TunaMeristics;
import uk.ac.ox.oxfish.model.FishState;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;

public class AgingAndRecruitmentProcessTest {

    @Test
    public void AgingAndRecruitmentProcessTester() {
        List<double[]> weights = new ArrayList<>();
        weights.add(new double[]{10, 15, 20, 25, 30}); //male
        weights.add(new double[]{15, 20, 25, 30, 35}); //female
        List<double[]> lengths = new ArrayList<>();
        lengths.add(new double[]{5, 6, 7, 8, 9});  //male
        lengths.add(new double[]{10, 11, 12, 13, 14}); //female

        TunaMeristics meristics = new TunaMeristics(
            weights,
            lengths,
            new double[]{.01, .05, .1, .5, .5},
            WeightGroups.SINGLE_GROUP
        );

        Species species1 = new Species("Piano Tuna", meristics);

        Map<String, String> sCodes = new HashMap<>();
        sCodes.put("SP1", species1.getName());
        SpeciesCodes speciesCodes = new SpeciesCodes(sCodes);

        final GlobalBiology globalBiology = new GlobalBiology(species1);
        HashMap<Species, double[][]> abundance = new HashMap<>();
        abundance.put(species1, new double[][]{{10000, 500, 400, 300, 100}, {10000, 500, 400, 300, 100}});

//        final NauticalMap nauticalMap = makeMap(1, 1);
//        nauticalMap.getAllSeaTilesAsList().forEach(seaTile ->
//                seaTile.setBiology(new AbundanceLocalBiology(abundance)
//                )
//        );

        //       List<SeaTile> allSeaTiles = nauticalMap.getAllSeaTilesAsList();

        Collection<AbundanceLocalBiology> localBiologies = new ArrayList<>();
        AbundanceLocalBiology localBio = new AbundanceLocalBiology(abundance);
        localBiologies.add(localBio);
        double dumbiomass = localBio.getBiomass(species1);
//        for (SeaTile allSeaTile : allSeaTiles) {
//            localBiologies.add((AbundanceLocalBiology) allSeaTile.getBiology());
//            double dumbiomass = allSeaTile.getBiology().getBiomass(species1);
//        }

        HashMap<Species, RecruitmentProcess> recruitmentProcesses = new HashMap<>();
        recruitmentProcesses.put(species1, new RecruitmentBySpawningBiomass(
            2000000,
            .95,
            .1,
            false,
            ((TunaMeristics) species1.getMeristics()).getMaturity().toArray(),
            null,
            FEMALE,
            false
        ));
        AgingAndRecruitmentProcess agingProcess = new AgingAndRecruitmentProcess(recruitmentProcesses);

        FishState fishState = mock(FishState.class);
        when(fishState.getSpecies()).thenReturn(globalBiology.getSpecies());
        when(fishState.getDayOfTheYear()).thenReturn(90);

        agingProcess.process(fishState, localBiologies);
//        for (SeaTile allSeaTile : allSeaTiles) {
//        dumbiomass = localBio.getBiomass(species1);
        //        }

        Assertions.assertEquals(localBio.getAbundance(species1).getAbundance(0, 0), 786573.430, 0.01);
        Assertions.assertEquals(localBio.getAbundance(species1).getAbundance(0, 1), 10000, 0.01);

    }
}
