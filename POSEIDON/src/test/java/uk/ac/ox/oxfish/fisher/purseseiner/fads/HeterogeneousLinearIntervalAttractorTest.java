/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2022-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class HeterogeneousLinearIntervalAttractorTest {

    ////
    ////
    //// same exact numbers as the constant version
    ////
    ////
    ////


    @Test
    public void computesDailyAttractionCorrectly() {

        final Species species = new Species(
            "Species 0",
            //age 0: 1kg; age 1:2kg
            new FromListMeristics(
                new double[]{1, 2},
                1
            )
        );

        //fake selectivity: [.1,.5]
        final HashMap<Species, NonMutatingArrayFilter> selectivityFilters = new HashMap<>();
        selectivityFilters.put(species, new NonMutatingArrayFilter(new double[]{.1, .5}));


        final FishState state = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(state.getSpecies()).thenReturn(ImmutableList.of(species));
        final HeterogeneousLinearIntervalAttractor attractor =
            new HeterogeneousLinearIntervalAttractor(
                5,
                10,
                1,
                selectivityFilters,
                state
            );


        //100 age 0, 2 age 1 fish
        final double[][] totalAbundance = new double[1][2];
        totalAbundance[0][0] = 100;
        totalAbundance[0][1] = 2;
        when(state.getTotalAbundance(species)).thenReturn(totalAbundance);

        //each day you want to attract 80kg!
        attractor.step(state);

        final AbundanceLocalBiology localBiology = mock(AbundanceLocalBiology.class, RETURNS_DEEP_STUBS);
        final AbundanceLocalBiology fadBiology = mock(AbundanceLocalBiology.class, RETURNS_DEEP_STUBS);
        final AbundanceAggregatingFad fad = mock(AbundanceAggregatingFad.class);
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{999999, 999999})
        ));
        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{0});
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{0, 0})
        ));
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{999999, 99999})
        ));
        when(fad.getStepDeployed()).thenReturn(1);
        when(fad.getBiology()).thenReturn(fadBiology);
        when(fad.getCarryingCapacity()).thenReturn(new PerSpeciesCarryingCapacity(new double[]{800}));
        when(fad.isActive()).thenReturn(true);
        when(state.getDay()).thenReturn(99999999);


        final WeightedObject<AbundanceLocalBiology> attracted = attractor.attractImplementation(localBiology, fad);
        Assertions.assertEquals(attracted.getTotalWeight(), 80.0, .0001);

        Assertions.assertEquals(attracted.getObjectBeingWeighted().getAbundance(species).getAbundance(0, 0),
            80 * 10d / 12d,
            .0001);
        Assertions.assertEquals(attracted.getObjectBeingWeighted().getAbundance(species).getAbundance(0, 1),
            80d / 12d,
            .0001);

    }


    @Test
    public void computesDailyAttractionCorrectlyThreeSpecies() {

        //three species, but the first won't be attracted;

        final List<Species> allSpecies = ImmutableList.of(
            new Species(
                "Species 0",
                //age 0: 1kg; age 1:2kg
                new FromListMeristics(
                    new double[]{1, 2},
                    1
                )
            ),
            new Species(
                "Species 1",
                //age 0: 1kg; age 1:2kg
                new FromListMeristics(
                    new double[]{1, 2},
                    1
                )
            ),
            new Species(
                "Species 2",
                //age 0: 1kg; age 1:2kg
                new FromListMeristics(
                    new double[]{1, 2},
                    1
                )
            )


        );
        allSpecies.get(1).resetIndexTo(1);
        allSpecies.get(2).resetIndexTo(2);

        //fake selectivity: [.1,.5]
        final HashMap<Species, NonMutatingArrayFilter> selectivityFilters = new HashMap<>();
        selectivityFilters.put(allSpecies.get(0), new NonMutatingArrayFilter(new double[]{.1, .5}));
        selectivityFilters.put(allSpecies.get(1), new NonMutatingArrayFilter(new double[]{.1, .5}));
        selectivityFilters.put(allSpecies.get(2), new NonMutatingArrayFilter(new double[]{.1, .5}));


        final FishState state = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(state.getSpecies()).thenReturn(allSpecies);
        final HeterogeneousLinearIntervalAttractor attractor =
            new HeterogeneousLinearIntervalAttractor(
                5,
                10,
                1,
                selectivityFilters,
                state
            );


        //100 age 0, 2 age 1 fish
        final double[][] totalAbundance = new double[1][2];
        totalAbundance[0][0] = 100;
        totalAbundance[0][1] = 2;
        when(state.getTotalAbundance(allSpecies.get(0))).thenReturn(totalAbundance);
        when(state.getTotalAbundance(allSpecies.get(1))).thenReturn(totalAbundance);
        when(state.getTotalAbundance(allSpecies.get(2))).thenReturn(totalAbundance);

        //each day you want to attract 80kg!
        attractor.step(state);

        final AbundanceLocalBiology localBiology = mock(AbundanceLocalBiology.class, RETURNS_DEEP_STUBS);
        final AbundanceLocalBiology fadBiology = mock(AbundanceLocalBiology.class, RETURNS_DEEP_STUBS);
        final AbundanceAggregatingFad fad = mock(AbundanceAggregatingFad.class);
        when(fad.getCarryingCapacity()).thenReturn(new PerSpeciesCarryingCapacity(
            new double[]{0, 800, 400}
        ));
        final Map<Species, StructuredAbundance> fishHere = new HashMap<>();
        final Map<Species, StructuredAbundance> fishFad = new HashMap<>();
        for (final Species species : allSpecies) {
            fishHere.put(species, new StructuredAbundance(new double[]{999999, 999999}));
            fishFad.put(species, new StructuredAbundance(new double[]{0, 0}));
        }
        when(fadBiology.getStructuredAbundance()).thenReturn(fishFad);
        when(localBiology.getStructuredAbundance()).thenReturn(fishHere);

        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{0, 0, 0});

        when(fad.getStepDeployed()).thenReturn(1);
        when(fad.getBiology()).thenReturn(fadBiology);
        when(fad.isActive()).thenReturn(true);
        when(state.getDay()).thenReturn(99999999);


        final WeightedObject<AbundanceLocalBiology> attracted = attractor.attractImplementation(localBiology, fad);
        Assertions.assertEquals(attracted.getTotalWeight(), 120, .0001);

        Assertions.assertEquals(
            attracted.getObjectBeingWeighted().getAbundance(allSpecies.get(0)).getAbundance(0, 0),
            0,
            .0001
        );
        Assertions.assertEquals(
            attracted.getObjectBeingWeighted().getAbundance(allSpecies.get(0)).getAbundance(0, 1),
            0,
            .0001
        );
        Assertions.assertEquals(attracted.getObjectBeingWeighted().getAbundance(allSpecies.get(1)).getAbundance(0, 0),
            80 * 10d / 12d,
            .0001);
        Assertions.assertEquals(attracted.getObjectBeingWeighted().getAbundance(allSpecies.get(1)).getAbundance(0, 1),
            80d / 12d,
            .0001);
        Assertions.assertEquals(attracted.getObjectBeingWeighted().getAbundance(allSpecies.get(2)).getAbundance(0, 0),
            40 * 10d / 12d,
            .0001);
        Assertions.assertEquals(attracted.getObjectBeingWeighted().getAbundance(allSpecies.get(2)).getAbundance(0, 1),
            40d / 12d,
            .0001);

    }


    @Test
    public void onlyAttractsWhenNeeded() {

        final Species species = new Species(
            "Species 0",
            //age 0: 1kg; age 1:2kg
            new FromListMeristics(
                new double[]{1, 2},
                1
            )
        );

        //fake selectivity: [.1,.5]
        final HashMap<Species, NonMutatingArrayFilter> selectivityFilters = new HashMap<>();
        selectivityFilters.put(species, new NonMutatingArrayFilter(new double[]{.1, .5}));


        final FishState state = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(state.getSpecies()).thenReturn(ImmutableList.of(species));
        final HeterogeneousLinearIntervalAttractor attractor = new HeterogeneousLinearIntervalAttractor(
            5,
            10,
            1,
            selectivityFilters,
            state
        );


        //100 age 0, 2 age 1 fish
        final double[][] totalAbundance = new double[1][2];
        totalAbundance[0][0] = 100;
        totalAbundance[0][1] = 2;
        when(state.getTotalAbundance(species)).thenReturn(totalAbundance);
        attractor.step(state);

        //too early!
        final AbundanceAggregatingFad fad = mock(AbundanceAggregatingFad.class);
        when(fad.getCarryingCapacity()).thenReturn(new PerSpeciesCarryingCapacity(
            new double[]{800}
        ));
        final AbundanceLocalBiology fadBiology = mock(AbundanceLocalBiology.class, RETURNS_DEEP_STUBS);
        when(fad.getBiology()).thenReturn(fadBiology);
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{0, 0})
        ));
        final AbundanceLocalBiology localBiology = mock(AbundanceLocalBiology.class, RETURNS_DEEP_STUBS);
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{999999, 999999})
        ));
        when(fad.getStepDeployed()).thenReturn(1);
        when(state.getDay()).thenReturn(3);
        when(fad.isActive()).thenReturn(true);

        Assertions.assertNull(attractor.attractImplementation(localBiology, fad));

        //too full
        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{999999});
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{999999, 999999})
        ));
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{999999, 999999})
        ));
        when(fad.getStepDeployed()).thenReturn(1);
        when(state.getDay()).thenReturn(99999999);

        Assertions.assertNull(attractor.attractImplementation(localBiology, fad));

        //empty local biology
        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{0});
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{0, 0})
        ));
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{1, 1})
        ));
        when(fad.getStepDeployed()).thenReturn(1);
        when(state.getDay()).thenReturn(99999999);

        Assertions.assertNull(attractor.attractImplementation(localBiology, fad));

        //valid
        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{0});
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{0, 0})
        ));
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{999999, 99999})
        ));
        when(fad.getStepDeployed()).thenReturn(1);
        when(state.getDay()).thenReturn(99999999);

        Assertions.assertNotNull(attractor.attractImplementation(localBiology, fad));
    }


}
