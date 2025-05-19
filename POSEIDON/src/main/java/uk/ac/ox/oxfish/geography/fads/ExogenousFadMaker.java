/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography.fads;

import com.vividsolutions.jts.geom.Coordinate;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.ReliableFishValueCalculator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * this object deploys and sets on FADs but without an associated boat or owner. In other words
 * it is useful to generate exogenous FAD setting other for microsimulation purposes, calibration or
 * demoes
 */
public class ExogenousFadMaker<B extends LocalBiology, F extends Fad> implements Startable, Steppable {


    /**
     * when in the model order are fads deployed.
     */
    public static final StepOrder EXOGENOUS_FAD_MAKER_STEPORDER = StepOrder.DAWN;
    private static final long serialVersionUID = -3662614779301930240L;
    /**
     * contains the rules on the nature of the fad we need to create
     */
    private final FadInitializer<B, F> fadInitializer;
    /**
     * an object that is given a reference to the fishState and called each step to
     * returns a list of seatiles to dump fads in at that step
     */
    private final Function<? super FishState, ? extends List<SeaTile>> generatorForFadDeploymentPositionsAtEachStep;
    /**
     * the object that keeps track of how many fads are in the water due to us
     */
    private FadManager fadManager;

    /**
     * generates an exogenous map maper that reads from a mapping day--->list of coordinates
     * where to deploy new fads each step
     *
     * @param fadInitializer      object that builds fads for you and place them in the water
     * @param dayToCoordinatesMap a mapping that returns for each simulated day, a list of coordinates to dump stuff in.
     */
    public ExogenousFadMaker(
        final FadInitializer<B, F> fadInitializer,
        final Map<Integer, ? extends Collection<Double2D>> dayToCoordinatesMap
    ) {
        this(fadInitializer, state -> {

            final List<SeaTile> toReturn = new LinkedList<>();
            final int simulatedDay = state.getDay();
            final Collection<Double2D> coordinatesToDumpNewFadsIn = dayToCoordinatesMap.get(simulatedDay);
            if (coordinatesToDumpNewFadsIn != null)
                for (final Double2D coordinate : coordinatesToDumpNewFadsIn) {
                    toReturn.add(state.getMap().getSeaTile(new Coordinate(coordinate.getX(), coordinate.getY())));
                }

            return toReturn;
        });
    }

    public ExogenousFadMaker(
        final FadInitializer<B, F> fadInitializer,
        final Function<? super FishState, ? extends List<SeaTile>> generatorForFadDeploymentPositionsAtEachStep
    ) {
        this.fadInitializer = fadInitializer;
        this.generatorForFadDeploymentPositionsAtEachStep = generatorForFadDeploymentPositionsAtEachStep;
    }

    @Override
    public void start(final FishState model) {

        //create the fad manager
        fadManager = new FadManager(
            model.getFadMap(),
            fadInitializer,
            null,
            new ReliableFishValueCalculator(model.getBiology())
        );

        //schedule yourself every day
        model.scheduleEveryDay(this, EXOGENOUS_FAD_MAKER_STEPORDER);

    }

    @Override
    public void step(final SimState simState) {

        final FishState model = (FishState) simState;
        final List<SeaTile> deployments = generatorForFadDeploymentPositionsAtEachStep.apply(model);
        if (deployments != null)
            for (final SeaTile tile : deployments) {
                if (tile.isWater()) {
                    fadManager.setNumFadsInStock(1);

                    fadManager.deployFad(
                        tile,
                        model.getRandom()
                    );

                }
            }
    }


}
