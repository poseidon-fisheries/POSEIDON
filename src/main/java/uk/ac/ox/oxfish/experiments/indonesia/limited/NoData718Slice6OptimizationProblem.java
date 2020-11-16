package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.beust.jcommander.internal.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.opencsv.CSVReader;
import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.GAIndividualDoubleData;
import eva2.optimization.operator.selection.SelectTournament;
import eva2.optimization.population.Population;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.statistics.InterfaceTextListener;
import eva2.optimization.strategies.*;
import eva2.problems.SimpleProblemWrapper;
import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.IntervalTarget;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.FullSeasonalRetiredDataCollectorsFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

/**
 * this is an optimization problem that is hard to place as a genericOptimization because (1) I want to use Interval targets and pick the best year rather than a specific year
 * (2) I want to place a shock a specific year without bothering creating a YAML friendly startable
 */
public class NoData718Slice6OptimizationProblem extends SimpleProblemDouble implements Serializable {





    public static final int SEED = 0;
    public static final int POP_SIZE = 100;
    /**
     * does this simulation include a "price shock" event
     */
    private boolean priceShock = false;



    private List<OptimizationParameter> parameters = new LinkedList<>();
    {
        parameters.add(new SimpleOptimizationParameter(
                "fisherDefinitions$0.gear.delegate.delegate.gears~Lethrinus laticaudis.averageCatchability",
                1.0E-4,
                1.0E-7
        ));
    }


    private int minimumYear = 6;

    private int maximumYearsToRun = 40;

    private String logName = "total_log_hill_optimization";

    private List<IntervalTarget> targets  = new LinkedList<>();

    {
        //example
        targets.add(
                new IntervalTarget("Percentage Mature Catches Atrobucca brevis spr_agent3",
                        0.85,100,0)
        );
    }


    private String baselineScenario = Paths.get("docs","indonesia_hub/runs/718/slice6limited",
            "base.yaml").toString();


    @Override
    public double[] evaluate(double[] x) {


        //read in and modify parameters
        try {


            //without price shock run a fixed number of years
            int numberOfYearsToRun = maximumYearsToRun;
            int firstValidYear = minimumYear;

            Scenario scenario = getScenario(x, priceShock, baselineScenario, parameters);
            if(priceShock)
            {
                int yearOfPriceShock = computeYearOfPriceShock(x[x.length - 1], minimumYear, maximumYearsToRun);


                new Consumer<Scenario>() {
                    @Override
                    public void accept(Scenario scenario) {
                        ((FlexibleScenario) scenario).getPlugins().add(
                                new FullSeasonalRetiredDataCollectorsFactory()
                        );
                    }
                }.andThen(NoData718Slice4PriceIncrease.priceShockAndSeedingGenerator(0).
                        apply(yearOfPriceShock)).accept(scenario);

                //with price shock, run only 5 years until price shock
                //and don't bother accepting runs before the price shock occurs
                numberOfYearsToRun = yearOfPriceShock + 5;
                firstValidYear = yearOfPriceShock+1;
            }

            //run the model
            try {
                double error = computeErrorGivenScenario(scenario, numberOfYearsToRun, firstValidYear);



                FileWriter writer = new FileWriter(
                        Paths.get(baselineScenario).getParent().resolve(logName + ".log").toFile(),
                        true
                );
                synchronized(writer) {

                    writer.write(Arrays.toString(x) + " ---> " + error);
                    writer.write("\n");
                    writer.close();
                }
                System.out.println(Arrays.toString(x) + " ---> " + error);
                return new double[]{error};

            }
            catch (RuntimeException e){
                System.out.println(e);
                return new double[]{0};

            }


        } catch (Exception | OutOfMemoryError e) {
            System.out.println(e);
            return new double[]{0};
        }

    }

    public static int computeYearOfPriceShock(double x, int minimumYear, int maximumYearsToRun) {
        final double priceShockValue = x;
        int yearOfPriceShock = (int) SimpleOptimizationParameter.computeNumericValueFromEVABounds(
                priceShockValue, minimumYear, maximumYearsToRun, true
        );
        //bounds could be broken. Keep them reasonable
        if(yearOfPriceShock<2)
            yearOfPriceShock=2;
        if(yearOfPriceShock>50)
            yearOfPriceShock=50;
        return yearOfPriceShock;
    }

    public static Scenario getScenario(double[] x, boolean priceShock, String baselineScenarioPath, List<OptimizationParameter> parameters) throws FileNotFoundException {
        System.out.println(Arrays.toString(x));
        //last element is price shock year if price shock is active!
        double[] parameterArray = x;
        if(priceShock) {
            //remove from original, pass it to the problem with the last element missing
            parameterArray = Arrays.copyOf(x, x.length-1);
        }
        Scenario scenario = GenericOptimization.
                buildScenario(parameterArray, Paths.get(baselineScenarioPath).toFile(), parameters);
        return scenario;
    }




    public static void optimize(Path previousSuccesses) throws IOException {


        FishYAML yaml = new FishYAML();
        final NoData718Slice6OptimizationProblem optiProblem = yaml.loadAs(new FileReader(
                        Paths.get("docs", "indonesia_hub/runs/718/slice6limited",
                                "optimization_with_price_shock_hardedge_lag3.yaml").toFile()
                ),
                NoData718Slice6OptimizationProblem.class);

        //  int type = Integer.parseInt();
        int parallelThreads = 1;


        CSVReader acceptanceReader = new CSVReader(
                new FileReader(previousSuccesses.toFile())
        );
        final List<String[]> initialGuesses = acceptanceReader.readAll();



        //anonymous class to make sure we initialize the model well
        SimpleProblemWrapper problem = new SimpleProblemWrapper(); /* {

            @Override
            public void initializePopulation(Population population) {


                super.initializePopulation(population);
                for (int scenarioNumber = 0; scenarioNumber < initialGuesses.size(); scenarioNumber++) {
                    String[] line = initialGuesses.get(scenarioNumber);
                    double[] convertedLine = new double[line.length];
                    for (int i = 0; i < line.length; i++) {
                        convertedLine[i] = Double.parseDouble(line[i]);
                    }
                    final ESIndividualDoubleData individual = new ESIndividualDoubleData((ESIndividualDoubleData) population.get(0));
                    individual.setDoubleGenotype(convertedLine);
                    individual.setDoublePhenotype(convertedLine);

                    population.replaceIndividualAt(scenarioNumber,
                            individual);
                }
            }


        };
        */
        problem.setSimpleProblem(optiProblem);
        problem.setParallelThreads(parallelThreads);
        problem.setDefaultRange(18);


        OptimizationParameters params;
        GeneticAlgorithm opt = new GeneticAlgorithm();
        opt.setParentSelection(new SelectTournament(12));

        params = OptimizerFactory.makeParams(

                opt,
                POP_SIZE,
                problem
        );




        OptimizerRunnable runnable = new OptimizerRunnable(params,
                "eva"); //ignored, we are outputting to window
        runnable.setOutputFullStatsToText(true);
        runnable.setVerbosityLevel(InterfaceStatisticsParameters.OutputVerbosity.ALL);
        runnable.setOutputTo(InterfaceStatisticsParameters.OutputTo.WINDOW);

        String name =
                Files.getNameWithoutExtension("tournament.yaml");

        FileWriter writer = new FileWriter(Paths.get("docs", "indonesia_hub/runs/718/slice6limited").
                resolve("goodstart_hill_log_"+ name+".log").toFile());

        runnable.setTextListener(new InterfaceTextListener() {
            @Override
            public void print(String str) {
                System.out.println(str);
                try {
                    writer.write(str);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void println(String str) {
                System.out.println(str);
                try {
                    writer.write(str);
                    writer.write("\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        runnable.run();

    }


    public static void main(String[] args) throws IOException {


        FishYAML yaml = new FishYAML();
        final NoData718Slice6OptimizationProblem optiProblem = yaml.loadAs(new FileReader(
                        Paths.get("docs", "indonesia_hub/runs/718/slice6limited", "optimization_with_price_shock_hardedge_lag3.yaml").toFile()
                ),
                NoData718Slice6OptimizationProblem.class);

        prepareScenarios(
                optiProblem,
                Paths.get("docs", "indonesia_hub/runs/718/slice6limited",
                        "ga_total_scenarios"),
                Paths.get("docs", "indonesia_hub/runs/718/slice6limited",
                        "ga_total_arrays.csv"),
                4
        );



//        optimize(
//                Paths.get("docs", "indonesia_hub/runs/718/slice6limited",
//                        "ga_18_arrays.csv")
//        );
    }

    public static void prepareScenarios(NoData718Slice6OptimizationProblem problem,
                                        Path directory,
                                        Path csvWithAcceptances,
                                        int priceShockLag) throws IOException {

        CSVReader acceptanceReader = new CSVReader(
                new FileReader(csvWithAcceptances.toFile())
        );
        final List<String[]> allSuccesses = acceptanceReader.readAll();

        directory.toFile().mkdirs();


        final FileWriter listFile = new FileWriter(directory.getParent().resolve("successes_total_ga.csv").toFile());
        listFile.write("scenario,price_shock_year,new_policy_year");
        listFile.write("\n");
        listFile.flush();

        for (int scenarioNumber = 0; scenarioNumber < allSuccesses.size(); scenarioNumber++) {
            String[] line = allSuccesses.get(scenarioNumber);
            double[] convertedLine = new double[line.length];
            for (int i = 0; i < line.length; i++) {
                convertedLine[i] = Double.parseDouble(line[i]);
            }

            final Path scenarioPath = directory.resolve("scenario_" + scenarioNumber + ".yaml");


            /***
             * you need to run the scenario once to figure out what year to input policy
             */
            //without price shock run a fixed number of years
            int numberOfYearsToRun;
            int firstValidYear;

            Scenario scenario = getScenario(convertedLine,true,
                    problem.getBaselineScenario(),
                    problem.getParameters());
            int yearOfPriceShock = computeYearOfPriceShock(convertedLine[convertedLine.length - 1], 6, 40);


            new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {
                    ((FlexibleScenario) scenario).getPlugins().add(
                            new FullSeasonalRetiredDataCollectorsFactory()
                    );
                }
            }.andThen(NoData718Slice4PriceIncrease.priceShockAndSeedingGenerator(0).
                    apply(yearOfPriceShock)).accept(scenario);

            //with price shock, run only 5 years until price shock
            //and don't bother accepting runs before the price shock occurs
            numberOfYearsToRun = yearOfPriceShock + 5;
            firstValidYear = yearOfPriceShock+1;

            final Pair<Integer, Integer> runResult = problem.runScenarioAndReturnFilterPassedAndBestYearPair(scenario,
                    numberOfYearsToRun,
                    firstValidYear);

            ///////////////////////////////////////////



            scenario = getScenario(convertedLine,
                    true,
                    problem.getBaselineScenario(),
                    problem.getParameters()
            );
            final FishYAML yaml = new FishYAML();
            yaml.dump(scenario,
                    new FileWriter(scenarioPath.toFile()));


            int yearOfShock = computeYearOfPriceShock(convertedLine[convertedLine.length-1],
                    problem.getMinimumYear(),
                    problem.getMaximumYearsToRun());

            listFile.write(scenarioPath.toString() + "," +
                    yearOfShock + "," + runResult.getSecond() +"\n");
            listFile.flush();
        }
        listFile.close();




    }



    public Pair<Integer,Integer> runScenarioAndReturnFilterPassedAndBestYearPair(
            Scenario scenario,
            int yearsToRun,
            int firstValidYear
    ){
        FishState model = new FishState(SEED);

        model.setScenario(scenario);
        model.start();
        System.out.println("starting run");
        while (model.getYear() <= yearsToRun) {
            model.schedule.step(model);

        }
        model.schedule.step(model);

        //collect error
        HashMap<IntervalTarget,boolean[]> successes = new HashMap<>();
        for (IntervalTarget target : targets) {
            successes.put(target,target.test(model));
        }

        int bestValue = 0;
        int bestYear = -1;

        for(int year=firstValidYear; year<=yearsToRun; year++) {
            int successesThisYear = 0;
            for (IntervalTarget target : targets) {
                if(successes.get(target)[year])
                    successesThisYear++;
            }
            if(successesThisYear>bestValue) {
                bestValue = successesThisYear;
                bestYear = year;
            }

        }
        return new Pair<>(bestValue,bestYear);
    }

    public double computeErrorGivenScenario(Scenario scenario,
                                            int yearsToRun, int firstValidYear) {

        final Pair<Integer, Integer> receipt = runScenarioAndReturnFilterPassedAndBestYearPair(scenario, yearsToRun, firstValidYear);
        //minimization!
        System.out.println("best year passed " + receipt.getFirst() + " filters");
        System.out.println("best year is " + receipt.getSecond());
        return -receipt.getFirst();
    }


    @Override
    public int getProblemDimension() {
        return priceShock ? parameters.size() : parameters.size()+1;
    }

    public NoData718Slice6OptimizationProblem() {


    }

    public List<IntervalTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<IntervalTarget> targets) {
        this.targets = targets;
    }


    public List<OptimizationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<OptimizationParameter> parameters) {
        this.parameters = parameters;
    }


    public int getMinimumYear() {
        return minimumYear;
    }

    public void setMinimumYear(int minimumYear) {
        this.minimumYear = minimumYear;
    }


    public String getBaselineScenario() {
        return baselineScenario;
    }

    public void setBaselineScenario(String baselineScenario) {
        this.baselineScenario = baselineScenario;
    }

    public int getMaximumYearsToRun() {
        return maximumYearsToRun;
    }

    public void setMaximumYearsToRun(int maximumYearsToRun) {
        this.maximumYearsToRun = maximumYearsToRun;
    }

    public boolean isPriceShock() {
        return priceShock;
    }

    public void setPriceShock(boolean priceShock) {
        this.priceShock = priceShock;
    }

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }




}
