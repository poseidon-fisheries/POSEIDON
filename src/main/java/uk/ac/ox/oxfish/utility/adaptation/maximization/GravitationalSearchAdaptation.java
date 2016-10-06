package uk.ac.ox.oxfish.utility.adaptation.maximization;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.MutablePair;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.adaptation.AbstractAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Created by carrknight on 10/4/16.
 */
public class GravitationalSearchAdaptation<T> extends AbstractAdaptation<T>
{

    /**
     * we work in a space of numbers but we might be controlling solutions that are
     * in fact not numbers but something else. This transforms back and forth
     */
    private CoordinateTransformer<T> transformer;


    /**
     * how the agent should judge himself and others, which in this algorithm is really the mass
     */
    private ObjectiveFunction<Fisher> mass;


    /**
     * the higher it is the faster you move towards higher mass
     */
    private double gravitationalConstant;


    /**
     *  you only copy this many people, and always the best ones
     */
    private int explorationMaximum;

    /**
     * you need this in memory rather than always calling the transformer because you might lose information in terms
     * of discretization
     */
    private double[] currentCoordinates;



    private double[] speed;

    private final DoubleParameter initialSpeed;
    /**
     * bounds up and down the coordinates
     */
    private Consumer<double[]> coordinatesBounder = new Consumer<double[]>() {
        @Override
        public void accept(double[] doubles) {
        }
    };


    public GravitationalSearchAdaptation(
            Sensor<T> sensor, Actuator<T> actuator,
            Predicate<Fisher> validator,
            CoordinateTransformer<T> transformer,
            ObjectiveFunction<Fisher> mass, double gravitationalConstant,
            int explorationMaximum,
            DoubleParameter initialSpeed,
            MersenneTwisterFast random) {
        super(sensor, actuator, validator);
        this.transformer = transformer;
        this.mass = mass;
        this.gravitationalConstant = gravitationalConstant;
        this.explorationMaximum = explorationMaximum;
        this.initialSpeed = initialSpeed;


    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    @Override
    protected void onStart(FishState model, Fisher fisher) {
        currentCoordinates = transformer.toCoordinates(getSensor().scan(fisher),
                                                       fisher,
                                                       model);

        speed = new double[currentCoordinates.length];
        for(int i=0; i<speed.length; i++)
            speed[i] =  initialSpeed.apply(model.getRandom());
    }

    @Override
    public T concreteAdaptation(Fisher toAdapt, FishState state, MersenneTwisterFast random) {

        double personalMass = mass.computeCurrentFitness(toAdapt);
        //don't bother if you don't have mass or coordinates
        Preconditions.checkNotNull(currentCoordinates);
        Preconditions.checkState(Double.isFinite(personalMass), Arrays.toString(currentCoordinates));


        //turn list of fishers into a list of coordinates--->utility
        List<MutablePair<double[], Double>> masses = state.getFishers().stream().map(
                fisher ->
                        new MutablePair<>(
                                transformer.toCoordinates(getSensor().scan(fisher), fisher, state),
                                mass.computeCurrentFitness(fisher)
                        ))
                //ignore those that have no coordinates or no utility
                .filter(pair -> pair.getFirst() != null &&
                        pair.getSecond() != null &&
                        Double.isFinite(pair.getSecond())).
                //collect them in a list
                        collect(Collectors.toList());

        assert masses.size() >=1; //we must be in at least
        if(masses.size()==1) //don't bother if there is no one else
            return
                    addSpeedAndReturn(new double[speed.length],random,toAdapt,state);

        //get best and worst
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        for(MutablePair<double[], Double> currentMass : masses )
        {
            if(max<currentMass.getSecond())
                max = currentMass.getSecond();
            if(min>currentMass.getSecond())
                min = currentMass.getSecond();
        }
        //if we are all the same, don't bother
        if(max == min)
            addSpeedAndReturn(new double[speed.length],random,toAdapt,state);

        assert max > min;
        double ratio = max-min;
        //reweigh everything (need to do this twice!)
        double sum = 0; //first we bound
        for (MutablePair<double[], Double> currentMass : masses)
        {
            double newValue = (currentMass.getSecond() - min) / ratio;
            currentMass.setSecond(newValue);
            sum += newValue;
        }
        //and then we normalize
        for (MutablePair<double[], Double> currentMass : masses)
        {
            currentMass.setSecond(currentMass.getSecond()/sum);
        }
        personalMass = Math.max(((personalMass - min)/ratio)/sum,.00001);


        //get top K
        List<MutablePair<double[], Double>> topMasses = masses.stream().sorted(
                (o1, o2) -> -Double.compare(o1.getSecond(), o2.getSecond())).
                limit(explorationMaximum).collect(Collectors.toList());


        //compute forces
        double[] force = new double[currentCoordinates.length];
        for(MutablePair<double[], Double> topMass : topMasses)
        {
            double distance = euclideanDistance(currentCoordinates,topMass.getFirst());
            if(distance > 0)
            {
                for (int d = 0; d < currentCoordinates.length; d++) {
                    force[d] += random.nextDouble() *
                            gravitationalConstant * (-currentCoordinates[d] + topMass.getFirst()[d]) *
                            (personalMass * topMass.getSecond()) / (distance + 0.00001);
                    assert Double.isFinite(force[d]);
                }
            }
        }
        //now add inertia and you are dooone!
        for (int d = 0; d < currentCoordinates.length; d++)
            force[d]=force[d]/personalMass;

        return  addSpeedAndReturn(force,random,toAdapt,state);

    }


    public T addSpeedAndReturn(double[] acceleration, MersenneTwisterFast random,
                               Fisher fisher,
                               FishState model)
    {
        assert acceleration.length == speed.length;
        assert currentCoordinates.length == speed.length;
        for(int i=0; i<acceleration.length; i++)
        {
            assert Double.isFinite(acceleration[i]);

            speed[i] = acceleration[i] + speed[i] *  random.nextDouble();
            currentCoordinates[i] = currentCoordinates[i] + speed[i];
            assert Double.isFinite(currentCoordinates[i]);
        }
        coordinatesBounder.accept(currentCoordinates);
        return transformer.fromCoordinates(currentCoordinates,fisher,model);
    }


    private double euclideanDistance(double[] coordinate1,double[] coordinate2)
    {
        double distance = 0;
        for(int i=0; i< coordinate1.length; i++)
            distance += Math.pow(coordinate1[i] - coordinate2[i],2);
        return distance;
    }

    public void setCoordinatesBounder(Consumer<double[]> coordinatesBounder) {
        this.coordinatesBounder = coordinatesBounder;
    }

}
