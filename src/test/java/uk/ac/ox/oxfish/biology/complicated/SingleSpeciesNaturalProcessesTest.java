package uk.ac.ox.oxfish.biology.complicated;

import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SingleSpeciesNaturalProcessesTest {


    @Test
    public void recruitsCorrectly() throws Exception
    {
        Log.info("Fixing the recruits they are allocated uniformly if the biomass is uniform");



        //lifted from SingleSpeciesAbundanceInitializerTest
        //4x4 map with test
        Path testInput = Paths.get("inputs", "tests", "abundance", "fake");
        SingleSpeciesAbundanceInitializer initializer = new SingleSpeciesAbundanceInitializer(
                testInput, "fake", 2.0);
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        Species fakeSpecies = biology.getSpecie(0);
        FishState model = MovingTest.generateSimple4x4Map();
        NauticalMap map = model.getMap();
        RecruitmentProcess recruiter = mock(RecruitmentProcess.class);
        //recruit 3200 fish this year
        when(recruiter.recruit(any(),any(),any(),any())).thenReturn(3200);
        //nothing dies
        NaturalMortalityProcess culler = mock(NaturalMortalityProcess.class);

        when(recruiter.recruit(any(),any(),any(),any())).thenReturn(3200);
        SingleSpeciesNaturalProcesses processes =  new SingleSpeciesNaturalProcesses(
                culler,
                recruiter,
                fakeSpecies
        );

        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            LocalBiology localBiology = initializer.generateLocal(biology,
                                                              element, new MersenneTwisterFast(), 4, 4);
            element.setBiology(localBiology); //put new biology in
            processes.add((AbundanceBasedLocalBiology) localBiology);
        }
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);

        //because the count is uniform I should see recruits distributed uniformly as well
        assertEquals(200,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(200,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(200,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(250,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        processes.step(model);
        //3200, half are female: 1600, that means 100 for each area
        assertEquals(100,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(100,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(100,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(100,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        //the others have aged
        assertEquals(200,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[1]);
        assertEquals(200,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[1]);
        assertEquals(200,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[1]);
        assertEquals(250,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[1]);



    }

    @Test
    public void recruitsWithFixedWeight() throws Exception
    {




        //lifted from SingleSpeciesAbundanceInitializerTest
        //4x4 map with test
        Path testInput = Paths.get("inputs", "tests", "abundance", "fake");
        SingleSpeciesAbundanceInitializer initializer = new SingleSpeciesAbundanceInitializer(
                testInput, "fake", 2.0);
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        Species fakeSpecies = biology.getSpecie(0);
        FishState model = MovingTest.generateSimple4x4Map();
        NauticalMap map = model.getMap();
        RecruitmentProcess recruiter = mock(RecruitmentProcess.class);
        //recruit 3200 fish this year
        when(recruiter.recruit(any(),any(),any(),any())).thenReturn(3200);
        //nothing dies
        NaturalMortalityProcess culler = mock(NaturalMortalityProcess.class);

        when(recruiter.recruit(any(),any(),any(),any())).thenReturn(3200);
        SingleSpeciesNaturalProcesses processes =  new SingleSpeciesNaturalProcesses(
                culler,
                recruiter,
                fakeSpecies
        );

        HashMap<AbundanceBasedLocalBiology,Double> allocator = new HashMap<>();
        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            LocalBiology localBiology = initializer.generateLocal(biology,
                                                                  element, new MersenneTwisterFast(), 4, 4);
            element.setBiology(localBiology); //put new biology in
            processes.add((AbundanceBasedLocalBiology) localBiology);
            if(element.getGridX()==1 && element.getGridY()==1)
                allocator.put((AbundanceBasedLocalBiology) localBiology, 1d);
            else
                allocator.put((AbundanceBasedLocalBiology) localBiology, 0d);
        }
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);
        processes.setFixedRecruitmentWeight(allocator);
        //because the count is uniform I should see recruits distributed uniformly as well
        assertEquals(200,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(200,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(200,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(250,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        processes.step(model);
        //3200, half are female: 1600
        assertEquals(0,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(1600,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(0,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(0,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        //the others have aged
        assertEquals(200,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[1]);
        assertEquals(200,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[1]);
        assertEquals(200,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[1]);
        assertEquals(250,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[1]);



    }
}