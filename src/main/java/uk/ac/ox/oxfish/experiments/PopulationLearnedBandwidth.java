package uk.ac.ox.oxfish.experiments;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.DefaultKernelTransduction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.FiveParametersKernelTransductionFactory;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.strategies.destination.HeatmapDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.HeatmapDestinationFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.maximization.RandomStep;
import uk.ac.ox.oxfish.utility.adaptation.probability.FixedProbability;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.nio.file.Paths;
import java.util.function.Predicate;

/**
 * Created by carrknight on 7/8/16.
 */
public class PopulationLearnedBandwidth {


    private static final UniformDoubleParameter SPACE_BANDWIDTH = new UniformDoubleParameter(0.1, 100000);
    private static final UniformDoubleParameter HABITAT_BANDWIDTH = new UniformDoubleParameter(0.1, 100000);
    private static final UniformDoubleParameter PORT_BANDWIDTH = new UniformDoubleParameter(0.1, 100000);
    private static final UniformDoubleParameter RANDOM_BANDWDTH = new UniformDoubleParameter(0.1, 100000);
    private static final UniformDoubleParameter FORGETTING = new UniformDoubleParameter(0.7, 1);


    public static void main(String[] args) {

        FishState state = new FishState();
        PrototypeScenario scenario = new PrototypeScenario();
        HeatmapDestinationFactory destinationStrategy = new HeatmapDestinationFactory();
        scenario.setDestinationStrategy(destinationStrategy);
        FiveParametersKernelTransductionFactory factory = new FiveParametersKernelTransductionFactory();
        destinationStrategy.setRegression(factory);

        factory.setDistanceFromPortBandwidth(PORT_BANDWIDTH);
        factory.setForgettingFactor(FORGETTING);
        factory.setHabitatBandwidth(HABITAT_BANDWIDTH);
        factory.setSpaceBandwidth(SPACE_BANDWIDTH);
        factory.setRandomBandwidth(RANDOM_BANDWDTH);

        state.setScenario(scenario);

        Sensor<DefaultKernelTransduction> sensor = (Sensor<DefaultKernelTransduction>) fisher ->
                ((DefaultKernelTransduction) ((HeatmapDestinationStrategy) fisher.getDestinationStrategy()).getProfitRegression());

        //add adaptation
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                for (Fisher fisher : model.getFishers()) {


                    fisher.addYearlyAdaptation(new Adaptation<DefaultKernelTransduction>(
                            new Predicate<Fisher>() {
                                @Override
                                public boolean test(Fisher fisher) {
                                    return true;
                                }
                            },
                            new BeamHillClimbing<DefaultKernelTransduction>(
                                    new RandomStep<DefaultKernelTransduction>() {
                                        @Override
                                        public DefaultKernelTransduction randomStep(
                                                FishState state, MersenneTwisterFast random, Fisher fisher,
                                                DefaultKernelTransduction current) {
                                            return factory.apply(state);
                                        }
                                    }
                            ),
                            new Actuator<DefaultKernelTransduction>() {
                                @Override
                                public void apply(Fisher fisher, DefaultKernelTransduction change, FishState model) {
                                    DefaultKernelTransduction newish = new DefaultKernelTransduction
                                            (
                                                    model.getMap(),
                                                    change.getForgettingFactor(),
                                                    change.getSpace(),
                                                    change.getPort(),
                                                    change.getHabitat(),
                                                    change.getRandom()
                                            );
                                    ((HeatmapDestinationStrategy) fisher.getDestinationStrategy()).setProfitRegression(
                                            newish
                                    );
                                    //go through all your memory and retrain the model
                                    for (TripRecord record : fisher.getFinishedTrips()) {
                                        if (record.getMostFishedTileInTrip() != null)
                                            newish.addObservation(
                                                    new GeographicalObservation(record.getMostFishedTileInTrip(),
                                                                                0d,
                                                                                record.getProfitPerHour(true)),
                                                    fisher
                                            );
                                    }

                                }
                            },
                            sensor,
                            new CashFlowObjective(365),
                            new FixedProbability(.2, 1d)

                    ));


                }
            }

            @Override
            public void turnOff() {

            }
        });

        state.getYearlyDataSet().registerGatherer(
                "Space Bandwidth",
                (Gatherer<FishState>) ignored -> state.getFishers().stream().mapToDouble(
                        value -> sensor.scan(value).getSpace().getSpaceBandwidth()).sum() /
                        state.getFishers().size(), Double.NaN);

        state.getYearlyDataSet().registerGatherer(
                "Habitat Bandwidth",
                (Gatherer<FishState>) ignored -> state.getFishers().stream().mapToDouble(
                        value -> sensor.scan(value).getHabitat().getHabitatBandwidth()).sum() /
                        state.getFishers().size(), Double.NaN);


        state.getYearlyDataSet().registerGatherer(
                "Port Bandwidth",
                (Gatherer<FishState>) ignored -> state.getFishers().stream().mapToDouble(
                        value -> sensor.scan(value).getPort().getBandwidth()).sum() /
                        state.getFishers().size(), Double.NaN);


        state.getYearlyDataSet().registerGatherer(
                "Random Bandwidth",
                (Gatherer<FishState>) ignored -> state.getFishers().stream().mapToDouble(
                        value -> 1d/sensor.scan(value).getRandom().getMaxNoise()).sum() /
                        state.getFishers().size(), Double.NaN);



        state.getYearlyDataSet().registerGatherer(
                "Forgetting Factor",
                (Gatherer<FishState>) ignored -> state.getFishers().stream().mapToDouble(
                        value -> sensor.scan(value).getForgettingFactor()).sum() /
                        state.getFishers().size(), Double.NaN);


        state.start();
        while(state.getYear()<30) {
            state.schedule.step(state);
            if(state.getDayOfTheYear()==1)
                System.out.println("bing");
        }

        FishStateUtilities.printCSVColumnsToFile(Paths.get("runs","pop_learning.csv").toFile(),
                                                 state.getYearlyDataSet().getColumn("Space Bandwidth"),
                                                 state.getYearlyDataSet().getColumn("Habitat Bandwidth"),
                                                 state.getYearlyDataSet().getColumn("Port Bandwidth"),
                                                 state.getYearlyDataSet().getColumn("Random Bandwidth"),
                                                 state.getYearlyDataSet().getColumn("Forgetting Factor"));



        /*
        FishGUI gui = new FishGUI(state);
        Console c = new Console(gui);
        c.setVisible(true);
        */
    }
}
