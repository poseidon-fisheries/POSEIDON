package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesAbundanceFromDirectoryFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.FishingSeasonFactory;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbstractScenario;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbundanceScenario;

import java.util.HashMap;

public class CaliforniaBiomassDynamics {


    public static void main(String[] args)
    {

        CaliforniaAbundanceScenario scenario = new CaliforniaAbundanceScenario();
        scenario.setResetBiologyAtYear1(false);
        scenario.setRegulationPreReset(new FishingSeasonFactory(0,true));
        scenario.setUsePremadeInput(false);
        scenario.setSablefishDiffusingRate(0);
        scenario.setCountFileName("count_2011.csv");
        scenario.setMortalityAt100PercentForOldestFish(false);
        scenario.setExogenousCatches(new HashMap<>());
        scenario.setSablefishDiffusingRate(0);
        scenario.setPortFileName("no_ports.csv");

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        Species sablefish = state.getBiology().getSpecie("Sablefish");
        Species rockfish = state.getBiology().getSpecie("Yelloweye Rockfish");
        Species shortThornyhead = state.getBiology().getSpecie("Shortspine Thornyhead");
        Species longspineThornyhead = state.getBiology().getSpecie("Longspine Thornyhead");
        Species doverSole = state.getBiology().getSpecie("Dover Sole");

        while(state.getYear()<500)
        {
            state.schedule.step(state);
            if(state.getDayOfTheYear()==1) {
                System.out.println(state.getYear() + "," +
                                           state.getTotalBiomass(sablefish) / 1000 + ","
                                           +
                                           state.getTotalBiomass(rockfish) / 1000 +
                                           ","
                                           +
                                           state.getTotalBiomass(shortThornyhead) / 1000 +
                                           ","
                                           +
                                           state.getTotalBiomass(longspineThornyhead) / 1000 + ","
                                           +
                                           state.getTotalBiomass(doverSole) / 1000

                );
            }
        }



    }
}
