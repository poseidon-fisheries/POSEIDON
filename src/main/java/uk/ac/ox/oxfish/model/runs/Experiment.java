package uk.ac.ox.oxfish.model.runs;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Representing a single run of the model. Runs for x years and returns a "fitness" value. The meat of this class
 * is the "scenarioModification" and "afterStartModification" provided. These represent deviations from the baseline
 * scenario/model this experiment is supposedly testing.
 *
 * Created by carrknight on 7/24/15.
 */
public class Experiment<S extends Scenario> implements Callable<Double>
{

    private final long randomSeed;

    private final int yearsToSimulate;

    /**
     * the function called at the end of the simulation to compute the fitness
     */
    private final Function<FishState,Double> fitnessFunction;

    /**
     * the scenario this experiment supposedly run
     */
    private final S initialScenario;

    /**
     * what this experiment should do to the initial scenario before the model is started.
     */
    private final Consumer<S>  scenarioModification;

    /**
     * what this experiment should do to the model after it inPenaltyBox started but before any day passes.
     */
    private final Consumer<FishState>  afterStartModification;


    public Experiment(
            long randomSeed, int yearsToSimulate,
            Function<FishState, Double> fitnessFunction, S initialScenario,
            Consumer<S> scenarioModification,
            Consumer<FishState> afterStartModification) {
        this.randomSeed = randomSeed;
        this.yearsToSimulate = yearsToSimulate;
        this.fitnessFunction = fitnessFunction;
        this.initialScenario = initialScenario;
        this.scenarioModification = scenarioModification;
        this.afterStartModification = afterStartModification;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Double call() throws Exception
    {

        FishState state = new FishState(randomSeed);
        scenarioModification.accept(initialScenario);
        state.setScenario(initialScenario);
        state.start();
        afterStartModification.accept(state);
        while(state.getYear() <yearsToSimulate)
        {
            state.schedule.step(state);
            //  System.out.println(state.timeString() + " -- " + scenario.getClass().getSimpleName());
        }
        return fitnessFunction.apply(state);
    }
}
