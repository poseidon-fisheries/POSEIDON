/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.experiments;

import sim.display.Console;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory.ExhaustiveAcquisitionFunctionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.CompleteNearestNeighborRegressionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.NearestNeighborTransductionFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.HeatmapDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PlanningHeatmapDestinationFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class PlanningVsSearching {


    public static void main(String[] args) throws IOException {

        DataColumn[] exploreExploit = runSimulation(new PerTripImitativeDestinationFactory());

        PlanningHeatmapDestinationFactory nextStrategy = new PlanningHeatmapDestinationFactory();
        nextStrategy.setAlmostPerfectKnowledge(true);
        DataColumn[] perfectPlanner = runSimulation(nextStrategy);

        HeatmapDestinationFactory kernel = new HeatmapDestinationFactory();
        CompleteNearestNeighborRegressionFactory regression = new CompleteNearestNeighborRegressionFactory();
        regression.setDistanceFromPortBandwidth(new UniformDoubleParameter(1, 1000));
        regression.setHabitatBandwidth(new UniformDoubleParameter(1, 1000));
        regression.setTimeBandwidth(new UniformDoubleParameter(1, 1000));
        regression.setxBandwidth(new UniformDoubleParameter(1, 1000));
        regression.setyBandwidth(new UniformDoubleParameter(1, 1000));
        regression.setNeighbors(new UniformDoubleParameter(1, 10));
        //regression.setForgettingFactor(new FixedDoubleParameter(.95d));
        kernel.setRegression(regression);
      //  DataColumn[] structuredSearch = runSimulation(kernel);

        PlanningHeatmapDestinationFactory strategy = new PlanningHeatmapDestinationFactory();
        ExhaustiveAcquisitionFunctionFactory acquisition = new ExhaustiveAcquisitionFunctionFactory();
        acquisition.setProportionSearched(new FixedDoubleParameter(.2));
        strategy.setAcquisition(acquisition);
        strategy.setAlmostPerfectKnowledge(false);
        strategy.setRegression(regression);
        DataColumn[] plannerLearner = runSimulation(strategy);

        FishStateUtilities.printCSVColumnsToFile(
                Paths.get("runs", "search_vs_plan", "distances.csv").toFile(),
                exploreExploit[0],
                perfectPlanner[0],
           //     structuredSearch[0],
                plannerLearner[0]
        );


    }

    private static DataColumn[] runSimulation(
            final AlgorithmFactory<? extends DestinationStrategy> destinationStrategy) throws IOException {
        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                FirstPaper.INPUT_FOLDER.resolve("oil_travel.yaml")));
        PrototypeScenario scenario = yaml.loadAs(scenarioYaml, PrototypeScenario.class);
        scenario.setFishers(25);
        scenario.setMapMakerDedicatedRandomSeed(0l);
        scenario.setGasPricePerLiter(new FixedDoubleParameter(0));
        scenario.setDestinationStrategy(destinationStrategy);
        FishState state = new FishState(0);
        state.setScenario(scenario);
        state.attachAdditionalGatherers();
        state.start();

        while (state.getYear() < 2)
            state.schedule.step(state);
        state.getPorts().iterator().next().setGasPricePerLiter(3d);
        while (state.getYear() < 4)
            state.schedule.step(state);

        return new DataColumn[]{
                state.getDailyDataSet().getColumn("Average Distance From Port"),
                state.getYearlyDataSet().getColumn("NET_CASH_FLOW")
        };
    }


    public static void gui(String[] args) throws IOException {

        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                FirstPaper.INPUT_FOLDER.resolve("oil_travel.yaml")));
        PrototypeScenario scenario = yaml.loadAs(scenarioYaml, PrototypeScenario.class);
        scenario.setFishers(100);
        scenario.setMapMakerDedicatedRandomSeed(0l);
        scenario.setGasPricePerLiter(new FixedDoubleParameter(0));

        FishState state = new FishState(0);
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                for (Fisher fisher : model.getFishers()) {
                    PlanningHeatmapDestinationFactory strategy = new PlanningHeatmapDestinationFactory();
                    strategy.setProbability(new FixedProbabilityFactory(0,1d));
                    ExhaustiveAcquisitionFunctionFactory acquisition = new ExhaustiveAcquisitionFunctionFactory();
                    acquisition.setProportionSearched(new FixedDoubleParameter(.2));
                    strategy.setAcquisition(acquisition);
                    strategy.setAlmostPerfectKnowledge(false);
                    strategy.setRegression(new NearestNeighborTransductionFactory());
                    if (fisher.getID() >= 50) {
                        fisher.getTags().add("ship");
                        fisher.getTags().add("orange");
                        fisher.getTags().add("planning");
                        fisher.setDestinationStrategy(strategy.apply(state));
                    }
                    else{
                        fisher.getTags().add("blue");
                     //   fisher.getTags().add("ship");
                    }

                }
            }

            @Override
            public void turnOff() {

            }
        });
        EquidegreeBuilder builder = (EquidegreeBuilder) scenario.getNetworkBuilder();
        //connect people that have the same destination strategy
        builder.addPredicate((from, to) -> {
            return (from.getID() <50 && to.getID() < 50) ||   (from.getID() >=50 && to.getID() >= 50);
        });
        scenario.setNetworkBuilder(builder);


        state.setScenario(scenario);
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                state.scheduleEveryXDay(new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        Port port = state.getPorts().iterator().next();
                        if(port.getGasPricePerLiter()>0.1)
                            port.setGasPricePerLiter(0);
                        else
                            port.setGasPricePerLiter(8d);

                    }
                }, StepOrder.POLICY_UPDATE,90);
            }

            @Override
            public void turnOff() {

            }
        });
        state.attachAdditionalGatherers();

        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);

    }
}
