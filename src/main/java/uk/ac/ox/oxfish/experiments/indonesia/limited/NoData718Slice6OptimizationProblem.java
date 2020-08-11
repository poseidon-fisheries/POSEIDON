package uk.ac.ox.oxfish.experiments.indonesia.limited;

import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.DataTarget;
import uk.ac.ox.oxfish.maximization.generic.IntervalTarget;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * this is an optimization problem that is hard to place as a genericOptimization because (1) I want to use Interval targets and pick the best year rather than a specific year
 * (2) I want to place a shock a specific year without bothering creating a YAML friendly startable
 */
public class NoData718Slice6OptimizationProblem extends SimpleProblemDouble implements Serializable {




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
            Scenario scenario = GenericOptimization.
                    buildScenario(x, Paths.get(baselineScenario).toFile(), parameters);

            //run the model
            double error = computeErrorGivenScenario(scenario, maximumYearsToRun);

            return new double[]{error};


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }



    public double computeErrorGivenScenario(Scenario scenario,
                                            int simulatedYears) {
        FishState model = new FishState(System.currentTimeMillis());

        double error = 0;
        model.setScenario(scenario);
        model.start();
        System.out.println("starting run");
        while (model.getYear() < simulatedYears) {
            model.schedule.step(model);
        }
        model.schedule.step(model);

        //collect error
        HashMap<IntervalTarget,boolean[]> successes = new HashMap<>();
        for (IntervalTarget target : targets) {
            successes.put(target,target.test(model));
        }

        int bestValue = 0;

        for(int year=minimumYear; year<maximumYearsToRun; year++) {
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
        return parameters.size();
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
