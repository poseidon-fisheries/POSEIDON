package uk.ac.ox.oxfish.demoes;


import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.HalfBycatchFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.regs.factory.ITQMultiFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class TwoSpeciesITQSplit
{


    /**
     * 2 species ITQ, both are valuable but the quotas of the ones only available south are very few so that
     * it's better to fish north. The results are muffled by the fact that over time north gets consumed and it becomes better
     * to fish south instead anyway.
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
        scenario.forcePortPosition(new int[]{40, 25});

        scenario.setUsePredictors(true);


        long towsNorth = 0;
        long towsSouth = 0;

        state.start();
        //first year, just run: there is no ITQ running anyway
        while (state.getYear() < 1) {
            state.schedule.step(state);
        }
        state.schedule.step(state);


        while (state.getYear() < 5) {
            state.schedule.step(state);
            for (int x = 0; x < 50; x++) {
                for (int y = 0; y <= 25; y++) {
                    towsNorth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
                for (int y = 26; y < 50; y++) {
                    towsSouth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
            }
        }

        System.out.println("North vs South : " + towsNorth / ((double) towsNorth + towsSouth));
        Assert.assertTrue(towsNorth / ((double) towsNorth + towsSouth) > .6);



    }


    /**
     * we make fish so mobile that the depletion of reds isn't a problem: we are going to see geography choices
     * being as effective as switching gear
     * @throws Exception
     */
    @Test
    public void TwoSpeciesITQSplitUnmuffled() throws Exception
    {



        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        ITQMultiFactory multiFactory = new ITQMultiFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));

        HalfBycatchFactory biologyFactory = new HalfBycatchFactory();
        biologyFactory.setCarryingCapacity(new FixedDoubleParameter(5000));
        biologyFactory.setSteepness(new FixedDoubleParameter(.9));
        biologyFactory.setDifferentialPercentageToMove(new FixedDoubleParameter(.2));
        biologyFactory.setPercentageLimitOnDailyMovement(new FixedDoubleParameter(.2));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);

        scenario.setCoastalRoughness(0);
        scenario.forcePortPosition(new int[]{40, 25});

        scenario.setUsePredictors(true);


        long towsNorth = 0;
        long towsSouth = 0;

        state.start();
        //first year, just run: there is no ITQ running anyway
        while (state.getYear() < 1) {
            state.schedule.step(state);
        }
        state.schedule.step(state);

        state.schedule.step(state);
        double earlyRedLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(0) + " " +
                                                                                        AbstractMarket.LANDINGS_COLUMN_NAME);
        double earlyBlueLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(1) + " " +
                                                                                         AbstractMarket.LANDINGS_COLUMN_NAME);

        System.out.println("Early Landings: " + earlyRedLandings + " --- " + earlyBlueLandings);
        //blue start as a choke species
        double totalBlueQuotas = 500 * 100;
        Assert.assertTrue(earlyBlueLandings > .8 * totalBlueQuotas);
        //red is underutilized
        double totalRedQuotas = 4500 * 100;
        Assert.assertTrue(earlyRedLandings < .6 * totalRedQuotas);


        while (state.getYear() < 5) {
            state.schedule.step(state);
            for (int x = 0; x < 50; x++) {
                for (int y = 0; y <= 25; y++) {
                    towsNorth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
                for (int y = 26; y < 50; y++) {
                    towsSouth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
            }
        }

        System.out.println("North vs South : " + towsNorth / ((double) towsNorth + towsSouth));
        Assert.assertTrue(towsNorth / ((double) towsNorth + towsSouth) > .6);

        //by year 10 the quotas are very well used!
        double lateRedLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(0) + " " +
                                                                                       AbstractMarket.LANDINGS_COLUMN_NAME);
        double lateBlueLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(1) + " " +
                                                                                        AbstractMarket.LANDINGS_COLUMN_NAME);
        System.out.println("Late Landings: " + lateRedLandings + " --- " + lateBlueLandings);
        System.out.println(
                "Late Quota Efficiency: " + lateRedLandings / totalRedQuotas + " --- " + lateBlueLandings / totalBlueQuotas);

        //geographical choice with "fixed" biology works very strongly
        Assert.assertTrue(lateRedLandings > .9 * totalRedQuotas);

    }


}
