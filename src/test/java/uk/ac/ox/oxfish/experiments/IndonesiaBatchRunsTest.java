package uk.ac.ox.oxfish.experiments;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.boxcars.AbundanceGathererBuilder;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.regs.factory.FishingSeasonFactory;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class IndonesiaBatchRunsTest {


    @Test
        public void policyMustHaveEffectOnAbundance() throws FileNotFoundException {

        FishYAML yaml = new FishYAML();
        FlexibleScenario scenario = yaml.loadAs(new FileReader(Paths.get("inputs","tests","fixed_recruits_indonesia.yaml").toFile()),
                FlexibleScenario.class);
        //no fishing!
        for (FisherDefinition fisherDefinition : scenario.getFisherDefinitions()) {
            fisherDefinition.setRegulation(new FishingSeasonFactory(30,true));
        }

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        for(int i=0; i<365*3+1; i++) {
            state.schedule.step(state);
        }

        Species species = state.getBiology().getSpecie("Lutjanus malabaricus");
        double[] correctWeights = new double[]{0.000151107784587565,0.00474259691462035,0.0235480929806492,0.0676642683063623,0.148848886613995,0.279341648474436,0.471766539146188,0.739069224130562,1.09447313227246,1.5514467878252,2.12367847874732,2.82505599261954,3.66965000869274,4.67170021933006,5.84560354594926,7.20590399922721,8.76728385500294,10.5445559002453,12.5526565616152,14.8066397709595,17.3216714527768,20.1130245416594,23.1960744551932,26.5862949612783,30.2992543893874};
        for(int i=0; i<25; i++)
            assertEquals(species.getWeight(0,i),correctWeights[i],.001);
        //biomass should be higher (since we have blocked fishing)
        DataColumn biomass = state.getYearlyDataSet().getColumn("Biomass Lutjanus malabaricus");
        assertTrue(biomass.getLatest()>biomass.get(0));
        assertTrue(biomass.getLatest()>biomass.get(1));
        double finalBiomass = biomass.getLatest();


        //check that it matches abundance weight!
        double weightFromAbundance = 0;
        for(int i=0; i<25; i++)
        {
            DataColumn column = state.getYearlyDataSet().getColumn("Lutjanus malabaricus Abundance 0." + i + " at day " + 365);
            assertEquals(column.size(),3);
            System.out.println(column.get(1) + "-----" + column.get(2) + " ------- " + (column.get(2)>column.get(1)));
            System.out.println();

            weightFromAbundance += column.get(2) * correctWeights[i];


         //   Assert.assertTrue();
        }
        System.out.println(weightFromAbundance);
        System.out.println(finalBiomass);
        assertEquals(finalBiomass,weightFromAbundance,.001);




    }
}