/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class HeterogeneousLinearIntervalAttractorTest {

    ////
    ////
    //// same exact numbers as the constant version
    ////
    ////
    ////


    @Test
    public void computesDailyAttractionCorrectly() {

        Species species = new Species("Species 0",
                                      //age 0: 1kg; age 1:2kg
                                      new FromListMeristics(
                                              new double[]{1,2},
                                              1
                                      ));

        //fake selectivity: [.1,.5]
        HashMap<Species, NonMutatingArrayFilter> selectivityFilters = new HashMap<>();
        selectivityFilters.put(species,new NonMutatingArrayFilter(new double[]{.1,.5}));


        FishState state = mock(FishState.class,RETURNS_DEEP_STUBS);
        when(state.getSpecies()).thenReturn(ImmutableList.of(species));
        HeterogeneousLinearIntervalAttractor attractor = new HeterogeneousLinearIntervalAttractor(
                5, 10, 1, selectivityFilters,
                state,
                new DoubleParameter[]{new FixedDoubleParameter(800)}

        );


        //100 age 0, 2 age 1 fish
        double[][] totalAbundance = new double[1][2];
        totalAbundance[0][0]= 100;
        totalAbundance[0][1] = 2;
        when(state.getTotalAbundance(species)).thenReturn(totalAbundance);

        //each day you want to attract 80kg!
        attractor.step(state);

        AbundanceLocalBiology localBiology = mock(AbundanceLocalBiology.class,RETURNS_DEEP_STUBS);
        AbundanceLocalBiology fadBiology = mock(AbundanceLocalBiology.class, RETURNS_DEEP_STUBS);
        AbundanceFad fad = mock(AbundanceFad.class);
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(species,new StructuredAbundance(new double[]{999999,999999})));
        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{0});
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(species,new StructuredAbundance(new double[]{0,0})));
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(species,new StructuredAbundance(new double[]{999999,99999})));
        when(fad.getStepDeployed()).thenReturn(1);
        when(fad.getBiology()).thenReturn(fadBiology);
        when(fad.getTotalCarryingCapacity()).thenReturn(1d);
        when(state.getDay()).thenReturn(99999999);


        WeightedObject<AbundanceLocalBiology> attracted = attractor.attractImplementation(localBiology, fad);
        assertEquals(attracted.getTotalWeight(),
                     80.0,.0001);

        assertEquals(attracted.getObjectBeingWeighted().getAbundance(species).getAbundance(0,0),
                     80*10d/12d,.0001);
        assertEquals(attracted.getObjectBeingWeighted().getAbundance(species).getAbundance(0,1),
                     80d/12d,.0001);

    }


    @Test
    public void computesDailyAttractionCorrectlyThreeSpecies() {

        //three species, but the first won't be attracted;

        List<Species> allSpecies = ImmutableList.of(
                new Species("Species 0",
                            //age 0: 1kg; age 1:2kg
                            new FromListMeristics(
                                    new double[]{1,2},
                                    1
                            )),
                new Species("Species 1",
                            //age 0: 1kg; age 1:2kg
                            new FromListMeristics(
                                    new double[]{1,2},
                                    1
                            )),
                new Species("Species 2",
                            //age 0: 1kg; age 1:2kg
                            new FromListMeristics(
                                    new double[]{1,2},
                                    1
                            ))


        );
        allSpecies.get(1).resetIndexTo(1);
        allSpecies.get(2).resetIndexTo(2);

        //fake selectivity: [.1,.5]
        HashMap<Species, NonMutatingArrayFilter> selectivityFilters = new HashMap<>();
        selectivityFilters.put(allSpecies.get(0),new NonMutatingArrayFilter(new double[]{.1,.5}));
        selectivityFilters.put(allSpecies.get(1),new NonMutatingArrayFilter(new double[]{.1,.5}));
        selectivityFilters.put(allSpecies.get(2),new NonMutatingArrayFilter(new double[]{.1,.5}));


        FishState state = mock(FishState.class,RETURNS_DEEP_STUBS);
        when(state.getSpecies()).thenReturn(allSpecies);
        HeterogeneousLinearIntervalAttractor attractor = new HeterogeneousLinearIntervalAttractor(
                5, 10, 1, selectivityFilters,
                state,
                new DoubleParameter[]{
                        new FixedDoubleParameter(0),
                        new FixedDoubleParameter(800),
                        new FixedDoubleParameter(400)


                }

        );


        //100 age 0, 2 age 1 fish
        double[][] totalAbundance = new double[1][2];
        totalAbundance[0][0]= 100;
        totalAbundance[0][1] = 2;
        when(state.getTotalAbundance(allSpecies.get(0))).thenReturn(totalAbundance);
        when(state.getTotalAbundance(allSpecies.get(1))).thenReturn(totalAbundance);
        when(state.getTotalAbundance(allSpecies.get(2))).thenReturn(totalAbundance);

        //each day you want to attract 80kg!
        attractor.step(state);

        AbundanceLocalBiology localBiology = mock(AbundanceLocalBiology.class,RETURNS_DEEP_STUBS);
        AbundanceLocalBiology fadBiology = mock(AbundanceLocalBiology.class, RETURNS_DEEP_STUBS);
        AbundanceFad fad = mock(AbundanceFad.class);
        Map<Species,StructuredAbundance> fishHere = new HashMap<>();
        Map<Species,StructuredAbundance> fishFad = new HashMap<>();
        for (Species species : allSpecies) {
            fishHere.put(species,new StructuredAbundance(new double[]{999999,999999}));
            fishFad.put(species,new StructuredAbundance(new double[]{0,0}));
        }
        when(fadBiology.getStructuredAbundance()).thenReturn(fishFad);
        when(localBiology.getStructuredAbundance()).thenReturn(fishHere);

        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{0,0,0});

        when(fad.getStepDeployed()).thenReturn(1);
        when(fad.getBiology()).thenReturn(fadBiology);
        when(fad.getTotalCarryingCapacity()).thenReturn(1d);
        when(state.getDay()).thenReturn(99999999);


        WeightedObject<AbundanceLocalBiology> attracted = attractor.attractImplementation(localBiology, fad);
        assertEquals(attracted.getTotalWeight(),
                     120,.0001);

        assertEquals(attracted.getObjectBeingWeighted().getAbundance(allSpecies.get(0)).getAbundance(0,0),
                     0,.0001);
        assertEquals(attracted.getObjectBeingWeighted().getAbundance(allSpecies.get(0)).getAbundance(0,1),
                     0,.0001);
        assertEquals(attracted.getObjectBeingWeighted().getAbundance(allSpecies.get(1)).getAbundance(0,0),
                     80*10d/12d,.0001);
        assertEquals(attracted.getObjectBeingWeighted().getAbundance(allSpecies.get(1)).getAbundance(0,1),
                     80d/12d,.0001);
        assertEquals(attracted.getObjectBeingWeighted().getAbundance(allSpecies.get(2)).getAbundance(0,0),
                     40*10d/12d,.0001);
        assertEquals(attracted.getObjectBeingWeighted().getAbundance(allSpecies.get(2)).getAbundance(0,1),
                     40d/12d,.0001);

    }



    @Test
    public void onlyAttractsWhenNeeded() {

        Species species = new Species("Species 0",
                                      //age 0: 1kg; age 1:2kg
                                      new FromListMeristics(
                                              new double[]{1,2},
                                              1
                                      ));

        //fake selectivity: [.1,.5]
        HashMap<Species, NonMutatingArrayFilter> selectivityFilters = new HashMap<>();
        selectivityFilters.put(species,new NonMutatingArrayFilter(new double[]{.1,.5}));


        FishState state = mock(FishState.class,RETURNS_DEEP_STUBS);
        when(state.getSpecies()).thenReturn(ImmutableList.of(species));
        HeterogeneousLinearIntervalAttractor attractor = new HeterogeneousLinearIntervalAttractor(
                5, 10, 1, selectivityFilters,
                state,
                new DoubleParameter[]{new FixedDoubleParameter(800)}

        );


        //100 age 0, 2 age 1 fish
        double[][] totalAbundance = new double[1][2];
        totalAbundance[0][0]= 100;
        totalAbundance[0][1] = 2;
        when(state.getTotalAbundance(species)).thenReturn(totalAbundance);
        attractor.step(state);

        //too early!
        AbundanceFad fad = mock(AbundanceFad.class); when(fad.getTotalCarryingCapacity()).thenReturn(10000d);
        AbundanceLocalBiology fadBiology = mock(AbundanceLocalBiology.class, RETURNS_DEEP_STUBS);
        when(fad.getBiology()).thenReturn(fadBiology);
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(species, new StructuredAbundance(new double[]{0,0})));
        AbundanceLocalBiology localBiology = mock(AbundanceLocalBiology.class,RETURNS_DEEP_STUBS);
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(species,new StructuredAbundance(new double[]{999999,999999})));
        when(fad.getStepDeployed()).thenReturn(1);
        when(state.getDay()).thenReturn(3);

        assertNull(
                attractor.attractImplementation(localBiology,fad)
        );

        //too full
        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{999999});
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(species,new StructuredAbundance(new double[]{999999,999999})));
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(species,new StructuredAbundance(new double[]{999999,999999})));
        when(fad.getStepDeployed()).thenReturn(1);
        when(state.getDay()).thenReturn(99999999);

        assertNull(
                attractor.attractImplementation(localBiology,fad)
        );

        //empty local biology
        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{0});
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(species,new StructuredAbundance(new double[]{0,0})));
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(species,new StructuredAbundance(new double[]{1,1})));
        when(fad.getStepDeployed()).thenReturn(1);
        when(state.getDay()).thenReturn(99999999);

        assertNull(
                attractor.attractImplementation(localBiology,fad)
        );

        //valid
        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{0});
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(species,new StructuredAbundance(new double[]{0,0})));
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(species,new StructuredAbundance(new double[]{999999,99999})));
        when(fad.getStepDeployed()).thenReturn(1);
        when(state.getDay()).thenReturn(99999999);

        assertNotNull(
                attractor.attractImplementation(localBiology,fad)
        );
    }


}