/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.complicated;

import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SingleSpeciesNaturalProcessesTest {


    @Test
    public void excelFile() throws Exception {


        Species species = MultipleSpeciesAbundanceInitializer.
                generateSpeciesFromFolder(Paths.get("inputs",
                                                    "california",
                                                    "biology",
                                                    "Sablefish"),"Sablefish");

        SingleSpeciesNaturalProcesses processes = new SingleSpeciesNaturalProcesses(
                new NaturalMortalityProcess(),
                new RecruitmentBySpawningBiomass(
                        species.getVirginRecruits(),
                        species.getSteepness(),
                        species.getCumulativePhi(),
                        species.isAddRelativeFecundityToSpawningBiomass()
                ),
                species,
                true, new StandardAgingProcess(false),
                new NoAbundanceDiffusion());

        GlobalBiology biology = new GlobalBiology(species);
        AbundanceBasedLocalBiology cell1 = new AbundanceBasedLocalBiology(biology);
        AbundanceBasedLocalBiology cell2 = new AbundanceBasedLocalBiology(biology);
        processes.add(cell1, mock(SeaTile.class));
        processes.add(cell2,mock(SeaTile.class) );
        for(int i=0; i<=species.getMaxAge(); i++)
        {
            cell1.getNumberOfFemaleFishPerAge(species)[i] = 5000;
            cell2.getNumberOfFemaleFishPerAge(species)[i] = 5000;
        }

        FishState model = mock(FishState.class);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        processes.step(model);
        Assert.assertEquals(cell1.getNumberOfFemaleFishPerAge(species)[3]+cell2.getNumberOfFemaleFishPerAge(species)[3],9231,2);
        Assert.assertEquals(cell1.getNumberOfFemaleFishPerAge(species)[0]+cell2.getNumberOfFemaleFishPerAge(species)[0] +
                                    cell1.getNumberOfMaleFishPerAge(species)[0]+cell2.getNumberOfMaleFishPerAge(species)[0] ,416140,4);
        Assert.assertEquals(cell1.getNumberOfFemaleFishPerAge(species)[0]+cell2.getNumberOfFemaleFishPerAge(species)[0],208070,2);

        processes.step(model);
        System.out.println(Arrays.toString(cell1.getNumberOfFemaleFishPerAge(species)));
        System.out.println(Arrays.toString(cell2.getNumberOfFemaleFishPerAge(species)));
        System.out.println(Arrays.toString(cell2.getNumberOfMaleFishPerAge(species)));
        System.out.println(Arrays.toString(cell2.getNumberOfMaleFishPerAge(species)));


        Assert.assertEquals(cell1.getNumberOfFemaleFishPerAge(species)[3]+cell2.getNumberOfFemaleFishPerAge(species)[3],8521,1);
        Assert.assertEquals(cell1.getNumberOfFemaleFishPerAge(species)[0]+cell2.getNumberOfFemaleFishPerAge(species)[0] +
                                    cell1.getNumberOfMaleFishPerAge(species)[0]+cell2.getNumberOfMaleFishPerAge(species)[0] ,384422,50);
        Assert.assertEquals(cell1.getNumberOfFemaleFishPerAge(species)[1]+cell2.getNumberOfFemaleFishPerAge(species)[1] ,192073,2);

    }

    @Test
    public void recruitsCorrectly() throws Exception
    {
        Log.info("Fixing the recruits they are allocated uniformly if the biomass is uniform");

        FishState model = MovingTest.generateSimple4x4Map();
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());

        //lifted from SingleSpeciesAbundanceInitializerTest
        //4x4 map with test
        Path testInput = Paths.get("inputs", "tests", "abundance", "fake");
        SingleSpeciesAbundanceInitializer initializer = new SingleSpeciesAbundanceInitializer(
                testInput, "fake", 2.0,model);
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        Species fakeSpecies = biology.getSpecie(0);
        NauticalMap map = model.getMap();
        RecruitmentProcess recruiter = mock(RecruitmentProcess.class);
        //recruit 3200 fish this year
        when(recruiter.recruit(any(),any(),any(),any())).thenReturn(3200d);
        //nothing dies
        NaturalMortalityProcess culler = mock(NaturalMortalityProcess.class);

        when(recruiter.recruit(any(),any(),any(),any())).thenReturn(3200d);
        SingleSpeciesNaturalProcesses processes =  new SingleSpeciesNaturalProcesses(
                culler,
                recruiter,
                fakeSpecies,
                true, new StandardAgingProcess(false), new NoAbundanceDiffusion());

        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            LocalBiology localBiology = initializer.generateLocal(biology,
                                                              element, new MersenneTwisterFast(), 4, 4,                                          mock(NauticalMap.class)
            );
            element.setBiology(localBiology); //put new biology in
            processes.add((AbundanceBasedLocalBiology) localBiology,element );
        }
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);

        //because the count is uniform I should see recruits distributed uniformly as well
        assertEquals(200,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[0],.001);
        assertEquals(200,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[0],.001);
        assertEquals(200,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[0],.001);
        assertEquals(250,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[0],.001);
        processes.step(model);
        //3200, half are female: 1600, that means 100 for each area
        assertEquals(100,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[0],.001);
        assertEquals(100,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[0],.001);
        assertEquals(100,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[0],.001);
        assertEquals(100,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[0],.001);
        //the others have aged
        assertEquals(200,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[1],.001);
        assertEquals(200,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[1],.001);
        assertEquals(200,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[1],.001);
        assertEquals(250,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[1],.001);



    }

    @Test
    public void recruitsWithFixedWeight() throws Exception
    {

        FishState model = MovingTest.generateSimple4x4Map();
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());


        //lifted from SingleSpeciesAbundanceInitializerTest
        //4x4 map with test
        Path testInput = Paths.get("inputs", "tests", "abundance", "fake");
        SingleSpeciesAbundanceInitializer initializer = new SingleSpeciesAbundanceInitializer(
                testInput, "fake", 2.0,model);
        MersenneTwisterFast random = new MersenneTwisterFast();
        GlobalBiology biology = initializer.generateGlobal(random, mock(FishState.class));
        Species fakeSpecies = biology.getSpecie(0);
        NauticalMap map = model.getMap();
        RecruitmentProcess recruiter = mock(RecruitmentProcess.class);
        //recruit 3200 fish this year
        when(recruiter.recruit(any(),any(),any(),any())).thenReturn(3200d);
        //nothing dies
        NaturalMortalityProcess culler = mock(NaturalMortalityProcess.class);

        when(recruiter.recruit(any(),any(),any(),any())).thenReturn(3200d);
        SingleSpeciesNaturalProcesses processes =  new SingleSpeciesNaturalProcesses(
                culler,
                recruiter,
                fakeSpecies,
                true, new StandardAgingProcess(false), new NoAbundanceDiffusion());

        HashMap<AbundanceBasedLocalBiology,Double> allocator = new HashMap<>();
        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            LocalBiology localBiology = initializer.generateLocal(biology,
                                                                  element, random, 4, 4, mock(NauticalMap.class)
            );
            element.setBiology(localBiology); //put new biology in
            processes.add((AbundanceBasedLocalBiology) localBiology, element);
            if(element.getGridX()==1 && element.getGridY()==1)
                allocator.put((AbundanceBasedLocalBiology) localBiology, 1d);
            else
                allocator.put((AbundanceBasedLocalBiology) localBiology, 0d);
        }
        when(model.getRandom()).thenReturn(random);

        initializer.processMap(biology, map, random, model);
        processes.setRecruitsAllocator(
                new BiomassAllocator() {
                    @Override
                    public double allocate(SeaTile tile, NauticalMap map, MersenneTwisterFast random) {
                        return allocator.get(((AbundanceBasedLocalBiology) tile.getBiology()));
                    }
                }
        );
        //because the count is uniform I should see recruits distributed uniformly as well
        assertEquals(200,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[0],.001);
        assertEquals(200,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[0],.001);
        assertEquals(200,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[0],.001);
        assertEquals(250,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[0],.001);
        processes.step(model);
        //3200, half are female: 1600
        assertEquals(0,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[0],.001);
        assertEquals(1600,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[0],.001);
        assertEquals(0,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[0],.001);
        assertEquals(0,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[0],.001);
        //the others have aged
        assertEquals(200,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[1],.001);
        assertEquals(200,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[1],.001);
        assertEquals(200,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[1],.001);
        assertEquals(250,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[1],.001);



    }


    @Test
    public void lastClassMortality() throws Exception {
        FishState model = mock(FishState.class);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());

        Log.info("if you set preserve old age to false, the last class has mortality of 100%");
        RecruitmentProcess recruitment = mock(RecruitmentProcess.class);
        when(recruitment.recruit(any(),any(),any(),any())).thenReturn(1000d); //always create a 1000 new fish

        //grab a fake species
        Path testInput = Paths.get("inputs", "tests", "abundance", "fake");
        SingleSpeciesAbundanceInitializer initializer = new SingleSpeciesAbundanceInitializer(
                testInput, "fake", 2.0,model);
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        Species species = biology.getSpecie(0);
        SingleSpeciesNaturalProcesses processes = new SingleSpeciesNaturalProcesses(
                new NaturalMortalityProcess(),
                recruitment,
                species,
                true, new StandardAgingProcess(false), new NoAbundanceDiffusion() );


        AbundanceBasedLocalBiology local = new AbundanceBasedLocalBiology(new GlobalBiology(species));
        //there are 500 male/female in each category oldest and 0  for second oldest
        local.getNumberOfFemaleFishPerAge(species)[species.getMaxAge()]=500;
        local.getNumberOfMaleFishPerAge(species)[species.getMaxAge()]=500;
        local.getNumberOfFemaleFishPerAge(species)[species.getMaxAge()-1]=0;
        local.getNumberOfMaleFishPerAge(species)[species.getMaxAge()-1]=0;
        processes.add(local, mock(SeaTile.class));

        //when false the oldest all die
        when(model.getSpecies()).thenReturn(Collections.singletonList(species));
        processes.step(model);
        assertEquals(0,local.getNumberOfFemaleFishPerAge(species)[species.getMaxAge()],.001);
        assertEquals(0,local.getNumberOfMaleFishPerAge(species)[species.getMaxAge()],.001);


        //but when I set it to true, they don't all die
        local.getNumberOfFemaleFishPerAge(species)[species.getMaxAge()]=500;
        local.getNumberOfMaleFishPerAge(species)[species.getMaxAge()]=500;
        local.getNumberOfFemaleFishPerAge(species)[species.getMaxAge()-1]=0;
        local.getNumberOfMaleFishPerAge(species)[species.getMaxAge()-1]=0;
        processes = new SingleSpeciesNaturalProcesses(
                new NaturalMortalityProcess(),
                recruitment,
                species,
                true, new StandardAgingProcess(true), new NoAbundanceDiffusion() );
        processes.add(local,mock(SeaTile.class) );
        processes.step(model);
        assertEquals(447,local.getNumberOfFemaleFishPerAge(species)[species.getMaxAge()],.001);
        assertEquals(447,local.getNumberOfMaleFishPerAge(species)[species.getMaxAge()],.001);


        //in fact they mingle with the new oldest fish
        local.getNumberOfFemaleFishPerAge(species)[species.getMaxAge()]=500;
        local.getNumberOfMaleFishPerAge(species)[species.getMaxAge()]=500;
        local.getNumberOfFemaleFishPerAge(species)[species.getMaxAge()-1]=500;
        local.getNumberOfMaleFishPerAge(species)[species.getMaxAge()-1]=500;
        processes.step(model);
        assertEquals(447+447,local.getNumberOfFemaleFishPerAge(species)[species.getMaxAge()],.001);
        assertEquals(447+447,local.getNumberOfMaleFishPerAge(species)[species.getMaxAge()],.001);


    }
}