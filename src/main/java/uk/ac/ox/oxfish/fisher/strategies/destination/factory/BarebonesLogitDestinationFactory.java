package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PortDistanceExtractor;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by carrknight on 7/17/17.
 */
public abstract class  BarebonesLogitDestinationFactory implements
        AlgorithmFactory<LogitDestinationStrategy>{
    /**
     * everybody shares the parent same destination logit strategy
     */
    private final Locker<FishState,MapDiscretization> discretizationLocker = new Locker<>();
    protected AlgorithmFactory<? extends MapDiscretizer> discretizer =
            new CentroidMapFileFactory();
    /**
     * intercept of dummy variable (I have been here in the past 90 days)
     */
    private DoubleParameter habitIntercept =
            new FixedDoubleParameter(2.53163185);
    private DoubleParameter distanceInKm =
            new FixedDoubleParameter(-0.00759009);
    private DoubleParameter habitPeriodInDays = new FixedDoubleParameter(90);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    public LogitDestinationStrategy apply(FishState state) {
        //create the discretization
        MapDiscretization discretization = discretizationLocker.presentKey(
                state, new Supplier<MapDiscretization>() {
                    @Override
                    public MapDiscretization get() {
                        MapDiscretizer mapDiscretizer = discretizer.apply(state);
                        MapDiscretization toReturn = new MapDiscretization(mapDiscretizer);
                        toReturn.discretize(state.getMap());
                        return toReturn;

                    }
                }
        );

        //every area is valid
        int areas = discretization.getNumberOfGroups();
        List<Integer> validAreas = new LinkedList<>();
        for(int i=0; i<areas; i++)
            validAreas.add(i);


        double[][] betas = buildBetas(state, areas, validAreas);

        ObservationExtractor[][] extractors = buildExtractors(state, discretization, areas, betas);


        return new LogitDestinationStrategy(
                betas,
                extractors,
                validAreas,
                discretization,
                new FavoriteDestinationStrategy(state.getMap(), state.getRandom()),
                state.getRandom()
        );


    }

    @NotNull
    protected ObservationExtractor[][] buildExtractors(
            FishState state, MapDiscretization discretization, int areas, double[][] betas) {
        //get the extractors
        ObservationExtractor[][] extractors = new ObservationExtractor[betas.length][];
        ObservationExtractor[] commonExtractor = new ObservationExtractor[]{
                buildHabitExtractor(discretization,
                                    getHabitPeriodInDays().apply(state.getRandom()).intValue()),
                new PortDistanceExtractor()
        };
        for(int i=0; i<areas; i++)
            extractors[i] = commonExtractor;
        return extractors;
    }

    protected double[][] buildBetas(FishState state, int areas, List<Integer> validAreas) {
        //the same parameters for all the choices
        double[][] betas = new double[areas][2];
        betas[0][0] = habitIntercept.apply(state.getRandom());
        betas[0][1] = distanceInKm.apply(state.getRandom());
        for(int i=1; i<areas; i++)
        {
            betas[i][0] = betas[0][0];
            betas[i][1] = betas[0][1];
        }
        return betas;
    }

    @NotNull
    public abstract ObservationExtractor buildHabitExtractor(MapDiscretization discretization, int period);

    /**
     * Getter for property 'habitIntercept'.
     *
     * @return Value for property 'habitIntercept'.
     */
    public DoubleParameter getHabitIntercept() {
        return habitIntercept;
    }

    /**
     * Setter for property 'habitIntercept'.
     *
     * @param habitIntercept Value to set for property 'habitIntercept'.
     */
    public void setHabitIntercept(DoubleParameter habitIntercept) {
        this.habitIntercept = habitIntercept;
    }

    /**
     * Getter for property 'distanceInKm'.
     *
     * @return Value for property 'distanceInKm'.
     */
    public DoubleParameter getDistanceInKm() {
        return distanceInKm;
    }

    /**
     * Setter for property 'distanceInKm'.
     *
     * @param distanceInKm Value to set for property 'distanceInKm'.
     */
    public void setDistanceInKm(DoubleParameter distanceInKm) {
        this.distanceInKm = distanceInKm;
    }

    /**
     * Getter for property 'discretizer'.
     *
     * @return Value for property 'discretizer'.
     */
    public AlgorithmFactory<? extends MapDiscretizer> getDiscretizer() {
        return discretizer;
    }

    /**
     * Setter for property 'discretizer'.
     *
     * @param discretizer Value to set for property 'discretizer'.
     */
    public void setDiscretizer(
            AlgorithmFactory<? extends MapDiscretizer> discretizer) {
        this.discretizer = discretizer;
    }

    /**
     * Getter for property 'habitPeriodInDays'.
     *
     * @return Value for property 'habitPeriodInDays'.
     */
    public DoubleParameter getHabitPeriodInDays() {
        return habitPeriodInDays;
    }

    /**
     * Setter for property 'habitPeriodInDays'.
     *
     * @param habitPeriodInDays Value to set for property 'habitPeriodInDays'.
     */
    public void setHabitPeriodInDays(DoubleParameter habitPeriodInDays) {
        this.habitPeriodInDays = habitPeriodInDays;
    }
}
