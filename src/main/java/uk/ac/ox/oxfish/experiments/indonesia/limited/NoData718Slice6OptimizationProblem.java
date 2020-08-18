package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.io.ByteStreams;
import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.IntervalTarget;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * this is an optimization problem that is hard to place as a genericOptimization because (1) I want to use Interval targets and pick the best year rather than a specific year
 * (2) I want to place a shock a specific year without bothering creating a YAML friendly startable
 */
public class NoData718Slice6OptimizationProblem extends SimpleProblemDouble implements Serializable {


    public static final int SEED = 0;
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

    private String logName = "total_log_optimization";

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


        //last element is price shock year if price shock is active!
        double[] parameterArray = x;
        if(priceShock) {
            //remove from original, pass it to the problem with the last element missing
            parameterArray = Arrays.copyOf(x,x.length-1);
        }


        //read in and modify parameters
        try {
            Scenario scenario = GenericOptimization.
                    buildScenario(parameterArray, Paths.get(baselineScenario).toFile(), parameters);

            //without price shock run a fixed number of years
            int numberOfYearsToRun = maximumYearsToRun;
            int firstValidYear = minimumYear;

            if(priceShock)
            {
                final double priceShockValue = x[x.length - 1];
                int yearOfPriceShock = (int) SimpleOptimizationParameter.computeNumericValueFromEVABounds(
                        priceShockValue, minimumYear, maximumYearsToRun, true
                );
                //bounds could be broken. Keep them reasonable
                if(yearOfPriceShock<2)
                    yearOfPriceShock=2;
                if(yearOfPriceShock>50)
                    yearOfPriceShock=50;

                NoData718Slice4PriceIncrease.priceShockAndSeedingGenerator(0).
                        apply(yearOfPriceShock).accept(scenario);

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



    public double computeErrorGivenScenario(Scenario scenario,
                                            int yearsToRun, int firstValidYear) {
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

        for(int year=minimumYear; year<=yearsToRun; year++) {
            int successesThisYear = 0;
            for (IntervalTarget target : targets) {
                if(successes.get(target)[year])
                    successesThisYear++;
            }
            if(successesThisYear>bestValue)
                bestValue=successesThisYear;
        }
        //minimization!
        System.out.println("best year passed " + bestValue + " filters");
        return -bestValue;
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





    //    public static void main(String[] args) throws IOException {
//
//        NoData718Slice6OptimizationProblem problem = new NoData718Slice6OptimizationProblem();
//        FishYAML yaml = new FishYAML();
//        yaml.dump(problem,new FileWriter(
//                Paths.get("docs","indonesia_hub/runs/718/slice6limited",
//                        "optimization.yaml").toFile()        )
//        );
//
//
//    }
}
