package uk.ac.ox.oxfish.experiments;

import ec.util.MersenneTwisterFast;
import sim.display.Console;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.OsmoseBiologyFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.WellMixedBiologyFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomTrawlStringFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.selfanalysis.GearImitationAnalysis;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.OsmoseMapInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQStringFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * A scenario to test what happens if the gear switch is not in continuous space but an
 * either-or preposition. Useful to simulate market switches
 * Created by carrknight on 11/12/15.
 */
public class HardGearSwitch
{


    public static void main(String[] args) throws IOException {

        OsmoseBiologyFactory biologyFactory = new OsmoseBiologyFactory();
        OsmoseMapInitializerFactory mapInitializer = new OsmoseMapInitializerFactory();

        FishState model = HardGearSwitch.buildHardSwitchGearDemo(biologyFactory, mapInitializer, 3, 2, 400, 800);



        FishGUI gui = new FishGUI(model);
        Console c = new Console(gui);
        c.setVisible(true);



    }

    public static void guiPrototype(String[] args) throws IOException {


        WellMixedBiologyFactory biologyInitializer = new WellMixedBiologyFactory();
        biologyInitializer.setFirstSpeciesCapacity(new FixedDoubleParameter(5000));
        biologyInitializer.setCapacityRatioSecondToFirst(new FixedDoubleParameter(1d));
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();

        FishState model = buildHardSwitchGearDemo(biologyInitializer, mapInitializer, 0, 1, 500, 4500);


        FishGUI gui = new FishGUI(model);
        Console c = new Console(gui);
        c.setVisible(true);



    }

    public static FishState buildHardSwitchGearDemo(
            AlgorithmFactory<? extends BiologyInitializer> biologyInitializer,
            AlgorithmFactory<? extends MapInitializer> mapInitializer,
            final int firstSpecies, final int secondSpecies,
            final int firstQuota, final int secondQuota) {
        FishState model = new FishState(System.currentTimeMillis(), 1);

        PrototypeScenario scenario = new PrototypeScenario();

        scenario.setBiologyInitializer(biologyInitializer);
        scenario.setMapInitializer(mapInitializer);
        scenario.setFishers(100);
        RandomTrawlStringFactory gear = new RandomTrawlStringFactory();
        gear.setCatchabilityMap(firstSpecies+":.01");
        scenario.setGear(gear);

        //mpa rules
        MultiITQStringFactory itqs = new MultiITQStringFactory();
        itqs.setYearlyQuotaMaps(firstSpecies+": " +
                                        firstQuota +"," +
                                        secondSpecies +":"+
                                        secondQuota);
        scenario.setUsePredictors(true);
        scenario.setRegulation(itqs);


        RandomTrawlStringFactory option1 = new RandomTrawlStringFactory();
        option1.setCatchabilityMap(firstSpecies+":.01");
        RandomTrawlStringFactory option2= new RandomTrawlStringFactory();
        option2.setCatchabilityMap(secondSpecies+":.01");
        model.registerStartable(new Startable() {
                                    @Override
                                    public void start(FishState model) {

                                        for (Fisher fisher : model.getFishers()) {

                                            Adaptation<Gear> trawlAdaptation =
                                                    new Adaptation<>(
                                                            (Predicate<Fisher>) fisher1 -> true,
                                                            new BeamHillClimbing<Gear>() {
                                                                @Override
                                                                public Gear randomStep(
                                                                        FishState state, MersenneTwisterFast random,
                                                                        Fisher fisher,
                                                                        Gear current) {
                                                                    return state.random.nextBoolean() ?
                                                                            option1.apply(state) :
                                                                            option2.apply(state);
                                                                }
                                                            },
                                                            GearImitationAnalysis.DEFAULT_GEAR_ACTUATOR,
                                                            fisher1 -> ((RandomCatchabilityTrawl) fisher1.getGear()),
                                                            new CashFlowObjective(365),
                                                            .1, .8);

                                            //tell the fisher to use this once a year
                                            fisher.addYearlyAdaptation(trawlAdaptation);
                                        }
                                            model.getYearlyDataSet().registerGatherer(model.getSpecies().get(firstSpecies)+ " Catchers", state1 -> {
                                                double size = state1.getFishers().size();
                                                if (size == 0)
                                                    return Double.NaN;
                                                else {
                                                    double total = 0;
                                                    for (Fisher fisher1 : state1.getFishers())
                                                        total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[firstSpecies]
                                                                ;
                                                    return total / .01;
                                                }
                                            }, Double.NaN);


                                            model.getYearlyDataSet().registerGatherer(model.getSpecies().get(secondSpecies) + " Catchers", state1 -> {
                                                double size = state1.getFishers().size();
                                                if (size == 0)
                                                    return Double.NaN;
                                                else {
                                                    double total = 0;
                                                    for (Fisher fisher1 : state1.getFishers())
                                                        total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[secondSpecies]
                                                                ;
                                                    return total / .01;
                                                }
                                            }, Double.NaN);


                                    }

                                    /**
                                     * tell the startable to turnoff,
                                     */
                                    @Override
                                    public void turnOff() {

                                    }
                                }
            );



        //now work!
        model.setScenario(scenario);
        return model;
    }

}
