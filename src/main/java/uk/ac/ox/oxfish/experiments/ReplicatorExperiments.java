package uk.ac.ox.oxfish.experiments;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.ReplicatorDestinationFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.network.NetworkBuilder;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.ITQMonoFactory;
import uk.ac.ox.oxfish.model.regs.factory.TACMonoFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;

/**
 * Created by carrknight on 2/28/17.
 */
public class ReplicatorExperiments {



    //private final static Path INPUT_FILE = Paths.get("runs", "replicator", "fixed_dynamic.yaml");
    private final static Path INPUT_FILE = Paths.get("runs", "replicator", "chaser.yaml");
    private static final Path OUTPUT_FILE = Paths.get("docs", "20170301 replicator", "results.csv");
    private static final Path DYNAMIC_FILE = Paths.get("docs", "20170301 replicator", "dynamics.csv");

    private static final Path EFFICIENCY_FILE = Paths.get("docs", "20170301 replicator", "efficiency.csv");

    public static final int NUMBER_OF_RUNS = 100;
    public static final int YEARS_TO_RUN = 15;
    public static final int NUMBER_OF_FISHERS = 100;

    public static void main(String[] args) throws IOException {


        File outputFile = OUTPUT_FILE.toFile();
        if (!outputFile.exists()) {
            Files.write(OUTPUT_FILE, "scenario,fishers,name,profits,strategy1,strategy2\n".getBytes());
        }

        File covarianceFile = DYNAMIC_FILE.toFile();
        if (!covarianceFile.exists()) {
            Files.write(DYNAMIC_FILE,
                        "scenario,id,time,strategy1,strategy2,fitness1,fitness2,name\n".getBytes());
        }

        File efficencyFile = EFFICIENCY_FILE.toFile();
        if (!efficencyFile.exists()) {
            Files.write(EFFICIENCY_FILE,
                        "scenario,fishers,name,landings,effort\n".getBytes());
        }
        System.out.println(INPUT_FILE);


    //    efficiencyRuns();


         runs();




    }

    private static void runs() throws IOException {
        run("anarchy",
            YEARS_TO_RUN,
            INPUT_FILE,
            new AnarchyFactory(),
            NUMBER_OF_RUNS,
            OUTPUT_FILE, DYNAMIC_FILE);


        ITQMonoFactory itq = new ITQMonoFactory();
        itq.setIndividualQuota(new FixedDoubleParameter(4000));

        run("itq",
            YEARS_TO_RUN,
            INPUT_FILE,
            itq,
            NUMBER_OF_RUNS,
            OUTPUT_FILE, DYNAMIC_FILE);


        itq.setIndividualQuota(new FixedDoubleParameter(2000));

        run("lowitq",
            YEARS_TO_RUN,
            INPUT_FILE,
            itq,
            NUMBER_OF_RUNS,
            OUTPUT_FILE, DYNAMIC_FILE);


        itq.setIndividualQuota(new FixedDoubleParameter(8000));

        run("highitq",
            YEARS_TO_RUN,
            INPUT_FILE,
            itq,
            NUMBER_OF_RUNS,
            OUTPUT_FILE, DYNAMIC_FILE);


        TACMonoFactory tac = new TACMonoFactory();
        tac.setQuota(new FixedDoubleParameter(4000* NUMBER_OF_FISHERS));

        run("tac",
            YEARS_TO_RUN,
            INPUT_FILE,
            tac,
            NUMBER_OF_RUNS,
            OUTPUT_FILE, DYNAMIC_FILE);
    }

    private static void efficiencyRuns() throws IOException {
        efficiency("anarchy",
                   YEARS_TO_RUN,
                   INPUT_FILE,
                   new AnarchyFactory(),
                   NUMBER_OF_RUNS,
                   EFFICIENCY_FILE);
        TACMonoFactory tac = new TACMonoFactory();
        tac.setQuota(new FixedDoubleParameter(4000* NUMBER_OF_FISHERS));

        efficiency("tac",
                   YEARS_TO_RUN,
                   INPUT_FILE,
                   tac,
                   NUMBER_OF_RUNS,
                   EFFICIENCY_FILE);
    }


    public static void run(String name,
                    int yearsToRun,
                    Path inputYamlPath,
                    AlgorithmFactory<? extends Regulation> regulation,
                    int numberOfRuns,
                    Path outputPath,
                    Path dynamicPath) throws IOException {
        for(int run = 0; run<numberOfRuns; run++) {
            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario = yaml.loadAs(
                    new FileReader(inputYamlPath.toFile()),
                    PrototypeScenario.class);

            Preconditions.checkState(scenario.getDestinationStrategy() instanceof ReplicatorDestinationFactory);
            Preconditions.checkState(((ReplicatorDestinationFactory) scenario.getDestinationStrategy()).getOptions().size()==2);

            scenario.setRegulation(regulation);
            scenario.setFishers(NUMBER_OF_FISHERS);
            FishState state = new FishState(System.currentTimeMillis());
            state.setScenario(scenario);
            state.start();
            while (state.getYear() < yearsToRun)
                state.schedule.step(state);

            double profits = 0d;
            for (Double cash : state.getYearlyDataSet().getColumn("Average Cash-Flow")) {
                profits+=cash;
            }
            int strategy0 = state.getDailyDataSet().getLatestObservation("Fishers using strategy " + 0).intValue();
            int strategy1 = state.getDailyDataSet().getLatestObservation("Fishers using strategy " + 1).intValue();

            //"scenario,fishers,name,profits,strategy1,strategy2\n"
            String output =
                    inputYamlPath.getFileName().toString() + "," +
                            NUMBER_OF_FISHERS + "," +
                            name + "," +
                            profits + "," +
                            strategy0 + "," +
                            strategy1 + "\n";

            Files.write(outputPath, output.getBytes(), StandardOpenOption.APPEND);

            DataColumn column0 = state.getDailyDataSet().getColumn("Fishers using strategy " + 0);
            DataColumn column1 = state.getDailyDataSet().getColumn("Fishers using strategy " + 1);
            DataColumn fitness0 = state.getDailyDataSet().getColumn("Fitness of strategy " + 0);
            DataColumn fitness1 = state.getDailyDataSet().getColumn("Fitness of strategy " + 1);
            final  long id = System.currentTimeMillis();
            //second output is more complicated
            for(int t=0; t<column0.size(); t+=60)
            {
                //"scenario,id,time,strategy1,strategy2,fitness1,fitness2,name\n"
                output =
                        inputYamlPath.getFileName().toString() + "," +
                                id + "," +
                                t + "," +
                                column0.get(t) + "," +
                                column1.get(t) + "," +
                                fitness0.get(t) + "," +
                                fitness1.get(t) + "," +
                                name + "\n";

                Files.write(dynamicPath, output.getBytes(), StandardOpenOption.APPEND);

            }

        }
    }


    public static void efficiency(String name,
                           int yearsToRun,
                           Path inputYamlPath,
                           AlgorithmFactory<? extends Regulation> regulation,
                           int numberOfRuns,
                           Path efficiencyPath) throws IOException
    {
        for(int run = 0; run<numberOfRuns; run++) {

            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario = yaml.loadAs(
                    new FileReader(inputYamlPath.toFile()),
                    PrototypeScenario.class);

            Preconditions.checkState(scenario.getDestinationStrategy() instanceof ReplicatorDestinationFactory);
            Preconditions.checkState(
                    ((ReplicatorDestinationFactory) scenario.getDestinationStrategy()).getOptions().size() == 2);

            scenario.setRegulation(regulation);
            scenario.setFishers(NUMBER_OF_FISHERS);
            FishState state = new FishState(System.currentTimeMillis());
            state.setScenario(scenario);
            state.start();
            while (state.getYear() < yearsToRun)
                state.schedule.step(state);

            double profits = 0d;
            double efforts = 0d;
            for (Double cash : state.getYearlyDataSet().getColumn("Species 0 Landings")) {
                profits += cash;
            }
            for (Double effort : state.getYearlyDataSet().getColumn("Total Effort")) {
                efforts += effort;
            }

            String output =
                    inputYamlPath.getFileName().toString() + "," +
                            NUMBER_OF_FISHERS + "," +
                            name + "," +
                            profits + "," +
                            efforts + "\n";

            Files.write(efficiencyPath, output.getBytes(), StandardOpenOption.APPEND);
        }
    }
}
