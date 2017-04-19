package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PeriodHabitExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PortDistanceExtractor;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The simplest possible regression we fit to WFS data
 * Created by carrknight on 4/18/17.
 */
public class BarebonesFloridaDestinationFactory implements
        AlgorithmFactory<LogitDestinationStrategy>
{


    //variables here are the handliner default

    /**
     * intercept of dummy variable (I have been here in the past 90 days)
     */
    private DoubleParameter habitIntercept =
            new FixedDoubleParameter(2.53163185);


    private DoubleParameter distanceInKm =
            new FixedDoubleParameter(-0.00759009);



    private AlgorithmFactory<? extends MapDiscretizer> discretizer =
            new CentroidMapFileFactory();
    {
        ((CentroidMapFileFactory) discretizer).setFilePath(Paths.get("temp_wfs", "areas.txt").toString());
        ((CentroidMapFileFactory) discretizer).setxColumnName("eastings");
        ((CentroidMapFileFactory) discretizer).setyColumnName("northings");
    }

    /**
     * everybody shares the parent same destination logit strategy
     */
    private final Locker<FishState,MapDiscretization> discretizationLocker = new Locker<>();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
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
        validAreas.add(0);


        //the same parameters for all the choices
        double[][] betas = new double[areas][2];
        betas[0][0] = habitIntercept.apply(state.getRandom());
        betas[0][1] = distanceInKm.apply(state.getRandom());
        for(int i=1; i<areas; i++)
        {
            betas[i][0] = betas[0][0];
            betas[i][1] = betas[0][1];
            validAreas.add(i);
        }

        //get the extractors
        ObservationExtractor[][] extractors = new ObservationExtractor[betas.length][];
        ObservationExtractor[] commonExtractor = new ObservationExtractor[]{
                new PeriodHabitExtractor(discretization,90),
                new PortDistanceExtractor()
        };
        for(int i=0; i<areas; i++)
            extractors[i] = commonExtractor;

        return new LogitDestinationStrategy(
                betas,
                extractors,
                validAreas,
                discretization,
                new FavoriteDestinationStrategy(state.getMap(), state.getRandom()),
                state.getRandom()
        );


    }


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
}
