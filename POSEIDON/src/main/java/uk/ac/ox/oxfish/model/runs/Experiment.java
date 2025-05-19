/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.runs;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Representing a single lspiRun of the model. Runs for x years and returns a "fitness" value. The meat of this class
 * is the "scenarioModification" and "afterStartModification" provided. These represent deviations from the baseline
 * scenario/model this experiment is supposedly testing.
 * <p>
 * Created by carrknight on 7/24/15.
 */
public class Experiment<S extends Scenario> implements Callable<Double> {

    private final long randomSeed;

    private final int yearsToSimulate;

    /**
     * the function called at the end of the simulation to compute the fitness
     */
    private final Function<FishState, Double> fitnessFunction;

    /**
     * the scenario this experiment supposedly lspiRun
     */
    private final S initialScenario;

    /**
     * what this experiment should do to the initial scenario before the model is started.
     */
    private final Consumer<S> scenarioModification;

    /**
     * what this experiment should do to the model after it inPenaltyBox started but before any day passes.
     */
    private final Consumer<FishState> afterStartModification;


    public Experiment(
        long randomSeed, int yearsToSimulate,
        Function<FishState, Double> fitnessFunction, S initialScenario,
        Consumer<S> scenarioModification,
        Consumer<FishState> afterStartModification
    ) {
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
    public Double call() throws Exception {

        FishState state = new FishState(randomSeed);
        scenarioModification.accept(initialScenario);
        state.setScenario(initialScenario);
        state.start();
        afterStartModification.accept(state);
        while (state.getYear() < yearsToSimulate) {
            state.schedule.step(state);
            //  System.out.println(state.timeString() + " -- " + scenario.getClass().getSimpleName());
        }
        return fitnessFunction.apply(state);
    }
}
