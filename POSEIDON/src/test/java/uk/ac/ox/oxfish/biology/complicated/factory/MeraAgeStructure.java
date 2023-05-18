package uk.ac.ox.oxfish.biology.complicated.factory;

import com.beust.jcommander.internal.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.FishingSeasonFactory;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import static org.junit.Assert.assertEquals;

public class MeraAgeStructure {

    @Test
    public void meraNumbersInMeraNumbersOut() {

        //start the population depleted, run a simulation with no fishers, should return to virgin levels
        //all the numbers are from MERA;
        // butterfly meme: is this replication?
        FlexibleScenario scenario = new FlexibleScenario();
        final SingleSpeciesAbundanceFactory biology = new SingleSpeciesAbundanceFactory();
        scenario.setBiologyInitializer(biology);

        final ExponentialMortalityFactory exponential = new ExponentialMortalityFactory();
        exponential.setMortalityPerSubdivision(new double[]{0.178735599880122});
        biology.setMortalityProcess(exponential);
        final StandardAgingFactory aging = new StandardAgingFactory();
        aging.setPreserveLastAge(true);
        biology.setAging(aging);
        biology.setDaily(false);
        final InitialAbundanceFromListFactory initialAbundanceFactory = new InitialAbundanceFromListFactory();
        initialAbundanceFactory.setFishPerBinPerSex(
            Lists.newArrayList(14330404.5976275,
                12199595.2467544, 10242081.3263143,
                8769139.19435211, 7481267.18544559,
                6193095.94775102, 5013545.52495044,
                3620632.9065258, 2336396.12202206, 1385779.35582218,
                714148.609581004, 326110.903153423, 227962.041789235
            )
        );
        biology.setInitialAbundanceFactory(initialAbundanceFactory);

        final SimpleListMeristicFactory meristics = new SimpleListMeristicFactory();
        meristics.setWeights(
            Lists.newArrayList(
                1.55277354207604e-05,
                0.0229963164063278,
                0.140813655733514,
                0.386903254008029,
                0.757135744908356,
                1.22880707379054,
                1.77278031293784,
                2.36026187769683,
                2.96610564787652,
                3.57004687316694,
                4.15679030554477,
                4.71552669330297,
                5.23921686731728
            )
        );
        meristics.setLengths(
            Lists.newArrayList(
                1.21040984832636,
                12.4058757968452,
                22.105470270532,
                30.5090627057734,
                37.789817175378,
                44.0977606001883,
                49.5628741975115,
                54.2977718686245,
                58.4000207160944,
                61.9541515073967,
                65.0334005123599,
                67.701218606666,
                70.0125787379303
            )
        );
        biology.setMeristics(meristics);

        final SimplifiedBevertonHoltRecruitmentFactory recruitment = new SimplifiedBevertonHoltRecruitmentFactory();
        biology.setRecruitment(recruitment);
        recruitment.setMaturity(
            Lists.newArrayList(
                0d,
                0.00205900630324256,
                0.00846612148354748,
                0.0341287439785158,
                0.127571851434192,
                0.376997842548468,
                0.714630239932825,
                0.911997301689871,
                0.977214005277808,
                0.994397085088203,
                0.998640312350846,
                0.999671100607516,
                0.99992050363287
            )
        );

        recruitment.setSteepness(new FixedDoubleParameter(0.612042489938904));
        recruitment.setSpawningStockBiomass(new FixedDoubleParameter(189776614));
        recruitment.setVirginRecruits(new FixedDoubleParameter(24324645));

        final SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(5));
        mapInitializer.setWidth(new FixedDoubleParameter(5));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(mapInitializer);
        for (FisherDefinition fisherDefinition : scenario.getFisherDefinitions()) {
            fisherDefinition.setRegulation(new FishingSeasonFactory(0, true));
        }
        FishYAML yaml = new FishYAML();

        System.out.println(yaml.dump(biology));

        biology.setSpeciesName("MERA Test");
        biology.setRounding(false);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        while (state.getYear() < 200) {
            state.schedule.step(state);
            if (state.getDayOfTheYear() == 2)
                System.out.println(
                    state.getTotalBiomass(state.getSpecies("MERA Test"))
                );
        }

        //carrying capacity ought to be about equal
        assertEquals(
            state.getTotalBiomass(state.getSpecies("MERA Test")) / 1000d,
            219599420 / 1000d,
            5 //off by about 5 tons, because even 200 years is not enough to reach the asymptote
        );
    }
}