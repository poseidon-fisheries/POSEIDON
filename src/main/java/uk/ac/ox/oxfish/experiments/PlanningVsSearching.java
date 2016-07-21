package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.NearestNeighborTransductionFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.HeatmapDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PlanningHeatmapDestinationFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class PlanningVsSearching
{


    public static void main(String[] args) throws IOException {

        DataColumn[] exploreExploit = runSimulation(new PerTripImitativeDestinationFactory());

        PlanningHeatmapDestinationFactory nextStrategy = new PlanningHeatmapDestinationFactory();
        nextStrategy.setAlmostPerfectKnowledge(true);
        DataColumn[] perfectPlanner = runSimulation(nextStrategy);

        HeatmapDestinationFactory kernel = new HeatmapDestinationFactory();
        NearestNeighborTransductionFactory regression = new NearestNeighborTransductionFactory();
        //regression.setForgettingFactor(new FixedDoubleParameter(.95d));
        kernel.setRegression(regression);
        DataColumn[] structuredSearch = runSimulation(kernel);

        PlanningHeatmapDestinationFactory strategy = new PlanningHeatmapDestinationFactory();
        strategy.setAlmostPerfectKnowledge(false);
        strategy.setRegression(regression);
        DataColumn[] plannerLearner = runSimulation(strategy);

        FishStateUtilities.printCSVColumnsToFile(
                Paths.get("runs","search_vs_plan","distances.csv").toFile(),
                exploreExploit[0],
                perfectPlanner[0],
                structuredSearch[0],
                plannerLearner[0]
        );



    }

    private static DataColumn[] runSimulation(final AlgorithmFactory<? extends DestinationStrategy> destinationStrategy) throws IOException {
        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                FirstPaper.INPUT_FOLDER.resolve("oil_travel.yaml")));
        PrototypeScenario scenario =  yaml.loadAs(scenarioYaml, PrototypeScenario.class);
        scenario.setFishers(15);
        scenario.setMapMakerDedicatedRandomSeed(0l);
        scenario.setGasPricePerLiter(new FixedDoubleParameter(0));
        scenario.setDestinationStrategy(destinationStrategy);
        FishState state = new FishState(0);
        state.setScenario(scenario);
        state.attachAdditionalGatherers();
        state.start();

        while(state.getYear()<2)
            state.schedule.step(state);
        state.getPorts().iterator().next().setGasPricePerLiter(3d);
        while(state.getYear()<4)
            state.schedule.step(state);

        return new DataColumn[]{
                state.getDailyDataSet().getColumn("Average Distance From Port"),
                state.getYearlyDataSet().getColumn("NET_CASH_FLOW")
        };
    }


}
