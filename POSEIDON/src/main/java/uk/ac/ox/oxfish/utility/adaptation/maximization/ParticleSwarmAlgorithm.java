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

package uk.ac.ox.oxfish.utility.adaptation.maximization;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * A somewhat generic PSO. Requires a lot of inputs because PSO is a somewhat weird optimization.
 * The idea of PSO is to pretend variables are point in space that attract one another towards a common maximum.
 * This requires to turn whatever T is into coordinates in space and then back into T
 * Created by carrknight on 8/7/15.
 */
public class ParticleSwarmAlgorithm<T> implements AdaptationAlgorithm<T> {

    /**
     * function used to extract the best memory out of the fisher.
     * Necessary for the PSO to work
     */
    private final Function<Fisher, T> getBestMemory;


    private final CoordinateTransformer<T> transformers;
    private final int dimensions;
    private final double[] velocities;
    private final double[] velocityShocks;
    private final double memoryWeight;
    private final double socialWeight;
    /**
     * optional function to stop coordinates from going out of bounds
     */
    private Consumer<double[]> coordinatesBounder = doubles -> {
    };
    private double[] currentCoordinates;
    private double inertia;
    private FishState model;


    public ParticleSwarmAlgorithm(
        final double inertia, final double memoryWeight, final double socialWeight, final int dimensions,
        final Function<Fisher, T> getBestMemory,
        final CoordinateTransformer<T> transformers,
        final double[] velocityShocks,
        final MersenneTwisterFast random,
        final DoubleParameter[] initialVelocities
    ) {
        this.inertia = inertia;
        this.memoryWeight = memoryWeight;
        this.socialWeight = socialWeight;
        this.dimensions = dimensions;
        this.getBestMemory = getBestMemory;
        this.transformers = transformers;
        this.velocityShocks = velocityShocks;

        currentCoordinates = new double[dimensions];
        velocities = new double[dimensions];
        for (int i = 0; i < dimensions; i++) {
            velocities[i] = initialVelocities[i].applyAsDouble(random);
        }
    }

    /**
     * premade PSO algorithm for seatile optimization
     */
    public static ParticleSwarmAlgorithm<SeaTile> defaultSeatileParticleSwarm(
        final double inertia, final double memoryWeight, final double socialWeight,
        final double velocityShock, final DoubleParameter[] initialVelocity,
        final MersenneTwisterFast random, final int mapWidth, final int mapHeight
    ) {
        final ParticleSwarmAlgorithm<SeaTile> seaTileParticleSwarmAlgorithm =
            new ParticleSwarmAlgorithm<SeaTile>(
                inertia,
                memoryWeight,
                socialWeight,
                2,
                fisher -> fisher.getBestSpotForTripsRemembered(
                    (o1, o2) -> Double.compare(
                        o1.getInformation().getProfitPerHour(true),
                        o2.getInformation().getProfitPerHour(true)
                    )
                ),
                new CoordinateTransformer<SeaTile>() {
                    @Override
                    public double[] toCoordinates(
                        final SeaTile variable,
                        final Fisher fisher,
                        final FishState model
                    ) {
                        return variable == null ? null :
                            new double[]{
                                variable.getGridX(),
                                variable.getGridY()};
                    }

                    @Override
                    public SeaTile fromCoordinates(
                        final double[] variable,
                        final Fisher fisher,
                        final FishState model
                    ) {
                        return model.getMap().getSeaTile(
                            (int) variable[0],
                            (int) variable[1]
                        );
                    }
                },
                new double[]{velocityShock, velocityShock},
                random,
                initialVelocity
            );
        //add bounds
        seaTileParticleSwarmAlgorithm.setCoordinatesBounder(coordinates -> {
            coordinates[0] = Math.min(Math.max(0, coordinates[0]), mapWidth - 1);
            coordinates[1] = Math.min(Math.max(0, coordinates[1]), mapHeight - 1);
        });
        return seaTileParticleSwarmAlgorithm;

    }

    @Override
    public void start(final FishState model, final Fisher agent, final T initial) {
        this.model = model;
        currentCoordinates = transformers.toCoordinates(initial, agent, model);
    }

    /**
     * shocks all velocities; use sparingly
     */
    @Override
    public T randomize(
        final MersenneTwisterFast random,
        final Fisher agent,
        final double currentFitness,
        final T current
    ) {
        assert velocities.length == dimensions;
        assert currentCoordinates.length == dimensions;
        for (int i = 0; i < dimensions; i++)
            velocities[i] += random.nextDouble() * 2 * velocityShocks[i] - velocityShocks[i];

        return move(agent);

    }

    /**
     * add speed to each parameter
     */
    private T move(final Fisher agent) {
        for (int i = 0; i < dimensions; i++)
            currentCoordinates[i] += velocities[i];
        coordinatesBounder.accept(currentCoordinates);
        return transformers.fromCoordinates(currentCoordinates, agent, model);
    }

    /**
     * returns null
     */
    @Override
    public T judgeRandomization(
        final MersenneTwisterFast random, final Fisher agent, final double previousFitness,
        final double currentFitness, final T previous, final T current
    ) {
        return current;
    }

    /**
     * the meat of the PSO and what should be called the most: change velocities so that it drifts towards
     * best memory and best friend
     */
    @Override
    public Map.Entry<T, Fisher> imitate(
        final MersenneTwisterFast random,
        final Fisher agent,
        final double fitness,
        final T current,
        final Collection<Fisher> friends,
        final ObjectiveFunction<Fisher> objectiveFunction,
        final Sensor<Fisher, T> sensor
    ) {

        //get best memory
        final T bestMemory = getBestMemory.apply(agent);
        final double[] memoryCoordinates = bestMemory == null ? null : transformers.toCoordinates(
            bestMemory,
            agent,
            model
        );
        //now get your best friend
        final Optional<Fisher> bestFriend = friends.stream().max((o1, o2) -> Double.compare(
            objectiveFunction.computeCurrentFitness(o1, o1),
            objectiveFunction.computeCurrentFitness(o1, o2)
        ));


        final double[] socialCoordinates = bestFriend.isPresent() && objectiveFunction.computeCurrentFitness(
            agent,
            agent
        ) <
            objectiveFunction.computeCurrentFitness(agent, bestFriend.get()) ?
            transformers.toCoordinates(getBestMemory.apply(bestFriend.get()),
                agent, model
            ) :
            null;

        for (int i = 0; i < dimensions; i++) {
            velocities[i] = velocities[i] * inertia;
            if (memoryCoordinates != null)
                velocities[i] += memoryWeight * (memoryCoordinates[i] - currentCoordinates[i]);
            if (socialCoordinates != null)
                velocities[i] += socialWeight * (socialCoordinates[i] - currentCoordinates[i]);
        }

        return entry(move(agent), bestFriend.orElse(null));

    }

    /**
     * just drift slowing down through inertia. Useful if you are without friends
     */
    @Override
    public T exploit(
        final MersenneTwisterFast random,
        final Fisher agent,
        final double currentFitness,
        final T current
    ) {
        for (int i = 0; i < dimensions; i++) {
            velocities[i] = velocities[i] * inertia;
        }
        return move(agent);
    }


    /**
     * returns null
     */
    @Override
    public T judgeImitation(
        final MersenneTwisterFast random,
        final Fisher agent,
        final Fisher friendImitated,
        final double fitnessBeforeImitating,
        final double fitnessAfterImitating,
        final T previous,
        final T current
    ) {
        return null;
    }

    public CoordinateTransformer<T> getTransformers() {
        return transformers;
    }

    public Consumer<double[]> getCoordinatesBounder() {
        return coordinatesBounder;
    }

    public void setCoordinatesBounder(final Consumer<double[]> coordinatesBounder) {
        this.coordinatesBounder = coordinatesBounder;
    }

    public int getDimensions() {
        return dimensions;
    }

    public double[] getCurrentCoordinates() {
        return currentCoordinates;
    }

    public void setCurrentCoordinates(final double[] currentCoordinates) {
        this.currentCoordinates = currentCoordinates;
    }

    public double[] getVelocities() {
        return velocities;
    }

    public double getInertia() {
        return inertia;
    }

    public void setInertia(final double inertia) {
        this.inertia = inertia;
    }

    public double[] getVelocityShocks() {
        return velocityShocks;
    }

    public double getMemoryWeight() {
        return memoryWeight;
    }

    public double getSocialWeight() {
        return socialWeight;
    }

    public FishState getModel() {
        return model;
    }

    public void setModel(final FishState model) {
        this.model = model;
    }
}
