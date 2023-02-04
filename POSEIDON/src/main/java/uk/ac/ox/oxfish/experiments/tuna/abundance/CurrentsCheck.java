package uk.ac.ox.oxfish.experiments.tuna.abundance;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadMapFactory;
import uk.ac.ox.oxfish.geography.fads.ExogenousFadMakerCSVFactory;
import uk.ac.ox.oxfish.geography.fads.WeibullLinearIntervalAttractorFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.scenario.FadsOnlyEpoAbundanceScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2016;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2017;

public class CurrentsCheck {



    public static void main(String[] args) throws IOException {
//        FileWriter writer = new FileWriter(Paths.get("docs","20220725 currents","poseidon_output.csv").toFile());
//        runModel("docs/20220725 currents/fad_deployments.csv",
//                writer, 30, Paths.get("inputs", "epo_inputs").resolve("currents").resolve("currents_2017.csv"), Paths.get("inputs", "epo_inputs").resolve("currents").resolve("currents_2018.csv"));

//        FileWriter writer = new FileWriter(Paths.get("docs","20220725 currents","poseidon_full.csv").toFile());
//        runModel("docs/20220725 currents/fad_deployments_full.csv",
//                writer, 5,
//                Paths.get("inputs", "epo_inputs").resolve("currents").resolve("currents_2017.csv"),
//                Paths.get("inputs", "epo_inputs").resolve("currents").resolve("currents_2018.csv"));

//        FileWriter writer = new FileWriter(Paths.get("docs","20220725 currents","poseidon_full_rf.csv").toFile());
//        runModel("docs/20220725 currents/fad_deployments_full.csv",
//                writer, 5,
//                Paths.get("docs").resolve("20220725 currents").resolve("rf_currents_2016.csv"),
//                Paths.get("docs").resolve("20220725 currents").resolve("rf_currents_2016.csv"),
//                false);


        FileWriter writer = new FileWriter(Paths.get("docs","20220725 currents","poseidon_output_rf.csv").toFile());
        runModel("docs/20220725 currents/fad_deployments.csv",
                writer, 30,
                Paths.get("docs").resolve("20220725 currents").resolve("rf_currents_2017.csv"),
                Paths.get("docs").resolve("20220725 currents").resolve("rf_currents_2018.csv"),
                false);
    }

    static private FishState runModel(String deploymentPath,
                                      FileWriter writer, final int frequency, Path currents2017, Path currents2018,
                                      boolean msInput) throws IOException {

        FadsOnlyEpoAbundanceScenario scenario = new FadsOnlyEpoAbundanceScenario();
        scenario.setFadSettingActive(false);
        WeibullLinearIntervalAttractorFactory initializer = new WeibullLinearIntervalAttractorFactory();
        initializer.setDaysInWaterBeforeAttraction(new FixedDoubleParameter(100000));
        initializer.setFadDudRate(new FixedDoubleParameter(1));
        LinkedHashMap<String, Double> fakeParameters = new LinkedHashMap<>();
        fakeParameters.put("Skipjack tuna",1d);
        fakeParameters.put("Bigeye tuna",1d);
        fakeParameters.put("Yellowfin tuna",1d);
        initializer.setCarryingCapacityScaleParameters(fakeParameters);
        initializer.setCarryingCapacityShapeParameters(fakeParameters);
        scenario.setFadInitializerFactory(initializer);


        ((ExogenousFadMakerCSVFactory) scenario.getFadMakerFactory()).setPathToFile(deploymentPath);

        scenario.setFadMapFactory(new AbundanceFadMapFactory(
                ImmutableMap.of(
                        Y2016, currents2017,
                        Y2017, currents2018
                )
        ));
        scenario.getFadMapFactory().setInputIsMetersPerSecond(msInput);

        writer.write("day,lat,lon,id" + "\n");
        writer.flush();

        final FishState fishState = new FishState();
        fishState.setScenario(scenario);
        fishState.start();
        fishState.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                model.scheduleOnceInXDays(new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        model.scheduleEveryXDay(new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                ((FishState) simState).getFadMap().allFads().
                                        forEach((Consumer<AbstractFad<? extends LocalBiology, ? extends AbstractFad<?, ?>>>) fad -> {
                                            StringBuilder fadCensus = new StringBuilder();
                                            fadCensus.append(((FishState) simState).getDay()).append(",");
                                            Coordinate coordinates = ((FishState) simState).getMap().getCoordinates(fad.getLocation());
                                            fadCensus.append(coordinates.y).append(",");
                                            fadCensus.append(coordinates.x).append(",");
                                            fadCensus.append(fad.getId()).append("\n");
                                            try {
                                                writer.write(fadCensus.toString());
                                                writer.flush();
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        });
                            }
                        }, StepOrder.DAILY_DATA_GATHERING, frequency);
                    }
                },StepOrder.DAWN,1);
            }
        });
        do {
            fishState.schedule.step(fishState);
            System.out.println("Step " + fishState.getStep());
            System.out.println(
                    fishState.getFadMap().getDriftingObjectsMap().getField().allObjects.numObjs
            );
        } while (fishState.getDay() <= 370);
        writer.flush();
        return fishState;
    }

}
