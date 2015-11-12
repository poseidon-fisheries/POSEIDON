package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import sim.display.Console;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.initializer.factory.WellMixedBiologyFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomTrawlStringFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.selfanalysis.GearImitationAnalysis;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQStringFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * A scenario to test what happens if the gear switch is not in continuous space but an
 * either-or preposition. Useful to simulate market switches
 * Created by carrknight on 11/12/15.
 */
public class HardGearSwitch
{


    public static void main(String[] args) throws IOException {
        FishState model = new FishState(-1, 1);
        Log.setLogger(new FishStateLogger(model, Paths.get("log.txt")));
        Log.set(Log.LEVEL_TRACE);

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setDestinationStrategy(new PerTripImitativeDestinationFactory()); // do not imitate!
        scenario.setMapMakerDedicatedRandomSeed(0l);
        WellMixedBiologyFactory biologyInitializer = new WellMixedBiologyFactory();
        biologyInitializer.setFirstSpeciesCapacity(new FixedDoubleParameter(5000));
        biologyInitializer.setCapacityRatioSecondToFirst(new FixedDoubleParameter(1d));
        scenario.setBiologyInitializer(biologyInitializer);
        // scenario.setMapInitializer(new OsmoseMapInitializerFactory());
        scenario.setFishers(100);
        RandomTrawlStringFactory gear = new RandomTrawlStringFactory();
        gear.setCatchabilityMap("0:.01");
        scenario.setGear(gear);

        //mpa rules
        MultiITQStringFactory itqs = new MultiITQStringFactory();
        itqs.setYearlyQuotaMaps("0:500,1:4500");
        scenario.setUsePredictors(true);
        scenario.setRegulation(itqs);

        RandomTrawlStringFactory option1 = new RandomTrawlStringFactory();
        option1.setCatchabilityMap("0:.01");
        RandomTrawlStringFactory option2= new RandomTrawlStringFactory();
        option1.setCatchabilityMap("1:.01");
        model.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                GearImitationAnalysis.attachGearAnalysisToEachFisher(model.getFishers(), model,
                                                                     Arrays.asList(option1.apply(model), option2.apply(model)),
                                                                     new CashFlowObjective(60));
            }

            @Override
            public void turnOff() {

            }
        });

        //sanity check: you either catch 2 or 3
        model.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                model.scheduleEveryDay(new Steppable() {
                    @Override
                    public void step(SimState simState)
                    {

                        for(Fisher fisher : model.getFishers())
                        {

            /*                if(!( Double.isNaN(fisher.predictDailyCatches(0)) ^
                                    ( fisher.predictDailyCatches(0) < FishStateUtilities.EPSILON && fisher.predictDailyCatches(1) > FishStateUtilities.EPSILON) ^
                                    ( fisher.predictDailyCatches(0) < FishStateUtilities.EPSILON && fisher.predictDailyCatches(1) < FishStateUtilities.EPSILON) ^
                                    ( fisher.predictDailyCatches(0) >FishStateUtilities.EPSILON && fisher.predictDailyCatches(1) < FishStateUtilities.EPSILON))) {
                                Preconditions.checkArgument(
                                        Double.isNaN(fisher.predictDailyCatches(0)) ^
                                                (fisher.predictDailyCatches(
                                                        0) < FishStateUtilities.EPSILON && fisher.predictDailyCatches(
                                                        1) > FishStateUtilities.EPSILON) ^
                                                (fisher.predictDailyCatches(
                                                        0) < FishStateUtilities.EPSILON && fisher.predictDailyCatches(
                                                        1) < FishStateUtilities.EPSILON) ^
                                                (fisher.predictDailyCatches(
                                                        0) > FishStateUtilities.EPSILON && fisher.predictDailyCatches(
                                                        1) < FishStateUtilities.EPSILON)
                                        , fisher.predictDailyCatches(0) + " ---- " + fisher.predictDailyCatches(
                                                1) + " ---- " +
                                                ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0] + " , " +
                                                ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[1] + " <--- " +
                                                fisher.getID() + "\n"


                                );

                            }
                             */
                        }
                    }
                }, StepOrder.AFTER_DATA);
            }

            @Override
            public void turnOff() {

            }
        });



        //now work!
        model.setScenario(scenario);
        FishGUI gui = new FishGUI(model);
        Console c = new Console(gui);
        c.setVisible(true);



    }

}
