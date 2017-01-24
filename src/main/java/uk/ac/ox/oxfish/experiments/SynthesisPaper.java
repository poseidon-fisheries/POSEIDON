package uk.ac.ox.oxfish.experiments;


import org.jfree.util.Log;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TowHeatmapGatherer;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQStringFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class SynthesisPaper {


    public static void main(String[] args) throws IOException {
        avoidTheLine(100,
                     Paths.get("inputs","paper_synthesis"),
                     Paths.get("runs","paper_synthesis"));
    }

    /**
     * run multiple times a scenario with 2 species where one is only
     * loosely protected by an MPA in the first scenario and by both an MPA and an ITQ
     * in the second instance
     * @param runsPerScenario number of runs per regulation
     * @param inputFolder
     * @param outputFolder
     */
    public static void  avoidTheLine(
            int runsPerScenario, Path inputFolder,
            Path outputFolder) throws IOException {


        Log.info("DEMO-1");
        String scenarioYaml = String.join("\n", Files.readAllLines(inputFolder.resolve("avoid_the_line.yaml")));

        Path container = outputFolder.resolve("demo1");
        container.toFile().mkdirs();


        Supplier<AlgorithmFactory<? extends Regulation>> regulation = new Supplier<AlgorithmFactory<? extends Regulation>>() {
            @Override
            public AlgorithmFactory<? extends Regulation> get() {
                return new ProtectedAreasOnlyFactory();
            }
        };

        demo1Sweep(runsPerScenario, scenarioYaml, container, regulation,"mpa");


        regulation = new Supplier<AlgorithmFactory<? extends Regulation>>() {
            @Override
            public AlgorithmFactory<? extends Regulation> get() {
                return new AnarchyFactory();
            }
        };

        demo1Sweep(runsPerScenario, scenarioYaml, container, regulation,"anarchy");

        regulation = new Supplier<AlgorithmFactory<? extends Regulation>>() {
            @Override
            public AlgorithmFactory<? extends Regulation> get() {
                MultiITQStringFactory factory = new MultiITQStringFactory();
                factory.setYearlyQuotaMaps("1:500");
                return factory;
            }
        };

        demo1Sweep(runsPerScenario, scenarioYaml, container, regulation,"itq");


    }

    public static void demo1Sweep(
            int numberOfRuns, String readScenario, Path outputFolder,
            Supplier<AlgorithmFactory<? extends Regulation>> regulation, final String name) throws IOException {
        for(int run = 0; run< numberOfRuns; run++) {
            FishYAML reader = new FishYAML();
            PrototypeScenario scenario = reader.loadAs(readScenario, PrototypeScenario.class);
            Log.info("\tMPA CASE " + run);
            scenario.setRegulation(regulation.get());

            FishState state = new FishState(run);
            state.setScenario(scenario);

            //add tows on the line counter (the neighborhood is size 2 because that's the size of the border where
            //blue fish live and is not protected)
            DataColumn borders = state.getDailyDataSet().registerGatherer("Tows on the Line",
                                                                          (Gatherer<FishState>) state1 -> {

                                                                              double lineSum = 0;
                                                                              NauticalMap map = state1.getMap();
                                                                              for (SeaTile tile : map.getAllSeaTilesExcludingLandAsList()) {
                                                                                  int trawlsHere = map.getDailyTrawlsMap().get(
                                                                                          tile.getGridX(),
                                                                                          tile.getGridY());
                                                                                  if (!tile.isProtected() &&
                                                                                          map.getMooreNeighbors(tile,
                                                                                                                2).stream().anyMatch(
                                                                                                  o -> ((SeaTile) o).isProtected())) {
                                                                                      lineSum += trawlsHere;
                                                                                  }
                                                                              }

                                                                              return lineSum;

                                                                          }
                    , Double.NaN);
            //now just count all tows
            DataColumn totals = state.getDailyDataSet().registerGatherer("Tows",
                                                                       (Gatherer<FishState>) state1 -> {

                                                                           double lineSum = 0;
                                                                           NauticalMap map = state1.getMap();
                                                                           for (SeaTile tile : map.getAllSeaTilesExcludingLandAsList()) {
                                                                               int trawlsHere = map.getDailyTrawlsMap().get(
                                                                                       tile.getGridX(),
                                                                                       tile.getGridY());
                                                                               lineSum += trawlsHere;
                                                                           }
                                                                           return lineSum;
                                                                       }
                    , Double.NaN);


            //collect a picture of heatmap for the first run
            TowHeatmapGatherer mapper = null;
            if(run==0) {
                mapper = new TowHeatmapGatherer(0);
                state.registerStartable(mapper);
            }




            state.start();
            while(state.getYear()<20)
                state.schedule.step(state);


            if(run==0) {
                String grid = FishStateUtilities.gridToCSV(mapper.getTowHeatmap());
                Files.write(outputFolder.resolve("grid" + name + ".csv"), grid.getBytes());
            }

            FishStateUtilities.printCSVColumnsToFile(outputFolder.resolve(name+"_"+run+".csv").toFile(),
                                                     totals,
                                                     borders);


        }
    }
}
