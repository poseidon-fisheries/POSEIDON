package uk.ac.ox.oxfish.demoes;


import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.HalfBycatchFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.ITQMultiFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class TwoSpeciesITQSplit
{


    /**
     * 2 species ITQ, both are valuable but the quotas of the ones only available south are very few so that
     * it's better to fish north
     * @throws Exception
     */
    @Test
    public void itqAffectsGeography() throws Exception {



        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        ITQMultiFactory multiFactory = new ITQMultiFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));

        HalfBycatchFactory biologyFactory = new HalfBycatchFactory();
        biologyFactory.setCarryingCapacity(new FixedDoubleParameter(5000));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);

        scenario.setCoastalRoughness(0);
        scenario.forcePortPosition(new int[]{40,25});

        scenario.setUsePredictors(true);


        long towsNorth = 0;
        long towsSouth = 0;

        state.start();
        //first year, just run: there is no ITQ running anyway
        while(state.getYear()<1) {
            state.schedule.step(state);
        }

        while(state.getYear()<10) {
            state.schedule.step(state);
            for(int x =0; x<50; x++) {
                for (int y = 0; y < 25; y++) {
                    towsNorth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
                for (int y = 26; y < 50; y++) {
                    towsSouth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
            }
        }

        System.out.println("North vs South : " + towsNorth/((double)towsNorth+towsSouth));
        Assert.assertTrue(towsNorth/((double)towsNorth+towsSouth) > .7);

    }
}
