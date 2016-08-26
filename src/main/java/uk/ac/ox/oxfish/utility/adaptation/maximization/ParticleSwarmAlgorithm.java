package uk.ac.ox.oxfish.utility.adaptation.maximization;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A somewhat generic PSO. Requires a lot of inputs because PSO is a somewhat weird optimization.
 * The idea of PSO is to pretend variables are point in space that attract one another towards a common maximum.
 * This requires to turn whatever T is into coordinates in space and then back into T
 * Created by carrknight on 8/7/15.
 */
public class ParticleSwarmAlgorithm<T> implements AdaptationAlgorithm<T>
{

    /**
     * function used to extract the best memory out of the fisher.
     * Necessary for the PSO to work
     */
    private final Function<Fisher,T> getBestMemory;



    private final PSOCoordinateTransformer<T> transformers;
    /**
     * optional function to stop coordinates from going out of bounds
     */
    private Consumer<double[]> coordinatesBounder = doubles -> {};

    private final int  dimensions;

    private double[] currentCoordinates;

    private final double[] velocities;

    private double inertia;

    private final double[] velocityShocks;

    private final double memoryWeight;

    private final double socialWeight;

    private FishState model;


    @Override
    public void start(FishState model, Fisher agent, T initial) {
        this.model = model;
        currentCoordinates = transformers.toCoordinates(initial,agent,model);
    }

    public ParticleSwarmAlgorithm(
            double inertia, double memoryWeight, double socialWeight, int dimensions,
            Function<Fisher, T> getBestMemory,
            PSOCoordinateTransformer<T> transformers,
            double[] velocityShocks,
            MersenneTwisterFast random,
            DoubleParameter[] initialVelocities) {
        this.inertia = inertia;
        this.memoryWeight = memoryWeight;
        this.socialWeight = socialWeight;
        this.dimensions = dimensions;
        this.getBestMemory = getBestMemory;
        this.transformers = transformers;
        this.velocityShocks = velocityShocks;

        currentCoordinates = new double[dimensions];
        velocities = new double[dimensions];
        for(int i=0; i<dimensions; i++)
        {
            velocities[i] =  initialVelocities[i].apply(random);
        }
    }

    /**
     * premade PSO algorithm for seatile optimization
     */
    public static ParticleSwarmAlgorithm<SeaTile> defaultSeatileParticleSwarm(
            double inertia, double memoryWeight, double socialWeight,
            double velocityShock, DoubleParameter[] initialVelocity,
            MersenneTwisterFast random, int mapWidth, int mapHeight
    )
    {
        ParticleSwarmAlgorithm<SeaTile> seaTileParticleSwarmAlgorithm =
                new ParticleSwarmAlgorithm<SeaTile>(inertia,
                                                    memoryWeight,
                                                    socialWeight,
                                                    2,
                                                    new Function<Fisher, SeaTile>() {
                                                        @Override
                                                        public SeaTile apply(
                                                                Fisher fisher) {
                                                            return fisher.getBestSpotForTripsRemembered(
                                                                    (o1, o2) -> Double.compare(
                                                                            o1.getInformation().getProfitPerHour(true),
                                                                            o2.getInformation().getProfitPerHour(true))
                                                            );
                                                        }
                                                    },
                                                    new PSOCoordinateTransformer<SeaTile>() {
                                                        @Override
                                                        public double[] toCoordinates(
                                                                SeaTile variable,
                                                                Fisher fisher,
                                                                FishState model) {
                                                            return variable == null ? null :
                                                                    new double[]{variable.getGridX(),
                                                                    variable.getGridY()};
                                                        }

                                                        @Override
                                                        public SeaTile fromCoordinates(
                                                                double[] variable,
                                                                Fisher fisher,
                                                                FishState model) {
                                                            return model.getMap().getSeaTile(
                                                                    (int)variable[0],
                                                                    (int)variable[1]);
                                                        }
                                                    },
                                                    new double[]{velocityShock, velocityShock},
                                                    random,
                                                    initialVelocity);
        //add bounds
        seaTileParticleSwarmAlgorithm.setCoordinatesBounder(new Consumer<double[]>() {
            @Override
            public void accept(double[] coordinates) {
                coordinates[0] = Math.min(Math.max(0,coordinates[0]),mapWidth-1);
                coordinates[1] = Math.min(Math.max(0,coordinates[1]),mapHeight-1);
            }
        });
        return seaTileParticleSwarmAlgorithm;

    }

    /**
     * shocks all velocities; use sparingly
     */
    @Override
    public T randomize(MersenneTwisterFast random, Fisher agent, double currentFitness, T current) {
        assert velocities.length == dimensions;
        assert currentCoordinates.length == dimensions;
        for(int i=0; i<dimensions; i++)
            velocities[i] += random.nextDouble()*2*velocityShocks[i] - velocityShocks[i];

        return move(agent);

    }


    /**
     * returns null
     */
    @Override
    public T judgeRandomization(
            MersenneTwisterFast random, Fisher agent, double previousFitness,
            double currentFitness, T previous, T current) {
        return current;
    }

    /**
     * add speed to each parameter
     */
    private T move(Fisher agent)
    {
        for(int i=0; i<dimensions; i++)
            currentCoordinates[i] += velocities[i];
        coordinatesBounder.accept(currentCoordinates);
        return transformers.fromCoordinates(currentCoordinates,agent,model);
    }


    /**
     * the meat of the PSO and what should be called the most: change velocities so that it drifts towards
     * best memory and best friend
     */
    @Override
    public Pair<T,Fisher> imitate(
            MersenneTwisterFast random, Fisher agent, double fitness, T current, Collection<Fisher> friends,
            ObjectiveFunction<Fisher> objectiveFunction, Sensor<T> sensor)
    {

        //get best memory
        T bestMemory = getBestMemory.apply(agent);
        double[] memoryCoordinates = bestMemory == null ? null : transformers.toCoordinates(bestMemory,
                                                                                           agent,
                                                                                           model);
        //now get your best friend
        Optional<Fisher> bestFriend = friends.stream().max(new Comparator<Fisher>() {
            @Override
            public int compare(Fisher o1, Fisher o2) {
                return Double.compare(objectiveFunction.computeCurrentFitness(o1),
                                      objectiveFunction.computeCurrentFitness(o2));
            }
        });


        double[] socialCoordinates = bestFriend.isPresent() && objectiveFunction.computeCurrentFitness(agent)<
                objectiveFunction.computeCurrentFitness(bestFriend.get())?
                transformers.toCoordinates(getBestMemory.apply(bestFriend.get()),
                                           agent,model) :
                null;

        for(int i=0; i<dimensions; i++)
        {
            velocities[i] = velocities[i] * inertia;
            if(memoryCoordinates != null)
                velocities[i]+= memoryWeight * (memoryCoordinates[i]-currentCoordinates[i]);
            if(socialCoordinates != null)
                velocities[i]+= socialWeight * (socialCoordinates[i]-currentCoordinates[i]);
        }

        return new Pair<>(move(agent),bestFriend.orElse(null));

    }

    /**
     * just drift slowing down through inertia. Useful if you are without friends
     */
    @Override
    public T exploit(MersenneTwisterFast random, Fisher agent, double currentFitness, T current)
    {
        for(int i=0; i<dimensions; i++)
        {
            velocities[i] = velocities[i] * inertia;
        }
        return move(agent);
    }


    /**
     * returns null
     */
    @Override
    public T judgeImitation(
            MersenneTwisterFast random, Fisher agent, Fisher friendImitated, double fitnessBeforeImitating,
            double fitnessAfterImitating, T previous, T current) {
        return null;
    }

    /**
     * object to convert from  PSO made up coordinates to actual variables and viceversa
     * @param <T>
     */
    public  interface PSOCoordinateTransformer<T>
    {
        /**
         * turn a T into PSO coordinates
         */
        double[] toCoordinates(T variable, Fisher fisher, FishState model);

        /**
         * turn coordinates into the variable we want to maximize
         */
        T fromCoordinates(double[] variable, Fisher fisher, FishState model);
    }

    public PSOCoordinateTransformer<T> getTransformers() {
        return transformers;
    }

    public Consumer<double[]> getCoordinatesBounder() {
        return coordinatesBounder;
    }

    public void setCoordinatesBounder(Consumer<double[]> coordinatesBounder) {
        this.coordinatesBounder = coordinatesBounder;
    }

    public int getDimensions() {
        return dimensions;
    }

    public double[] getCurrentCoordinates() {
        return currentCoordinates;
    }

    public void setCurrentCoordinates(double[] currentCoordinates) {
        this.currentCoordinates = currentCoordinates;
    }

    public double[] getVelocities() {
        return velocities;
    }

    public double getInertia() {
        return inertia;
    }

    public void setInertia(double inertia) {
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

    public void setModel(FishState model) {
        this.model = model;
    }
}
