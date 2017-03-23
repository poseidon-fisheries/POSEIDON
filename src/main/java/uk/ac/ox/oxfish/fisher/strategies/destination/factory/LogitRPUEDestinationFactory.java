package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.SimulatedHourlyCostExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.SimulatedHourlyProfitExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.SimulatedHourlyRevenueExtractor;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.IdentityDiscretizerFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedList;
import java.util.function.Supplier;

/**
 * Logit destination strategy using RPUE - travel costs with full knowledge
 * Created by carrknight on 2/6/17.
 */
public class LogitRPUEDestinationFactory implements AlgorithmFactory<LogitDestinationStrategy>
{



    /**
     * everybody shares the parent same destination logit strategy
     */
    private Locker<FishState,MapDiscretization> discretizationLocker = new Locker<>();


    private AlgorithmFactory<? extends MapDiscretizer> discretizer = new IdentityDiscretizerFactory();


    private DoubleParameter profitBeta = new FixedDoubleParameter(1d);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public LogitDestinationStrategy apply(FishState state) {

        MapDiscretization discretization = discretizationLocker.
                presentKey(
                        state,
                        new Supplier<MapDiscretization>()
                        {
                            @Override
                            public MapDiscretization get() {

                                MapDiscretizer mapDiscretizer = discretizer.apply(state);
                                MapDiscretization toReturn = new MapDiscretization(mapDiscretizer);
                                toReturn.discretize(state.getMap());
                                return toReturn;
                            }
                        }
                );


        //betas are just +1 for revenue and -1 for gas costs
        int numberOfGroups = discretization.getNumberOfGroups();
        double[][] betas = new double[numberOfGroups][1];

        for(int i=0; i<numberOfGroups; i++)
        {
            betas[i][0] = profitBeta.apply(state.getRandom());;

        }

        //use trip simulator (poorly) to simulate trips so you can figure out what revenues and costs are
        //0: revenue
        //1: gas costs
        ObservationExtractor[][] extractors = buildRPUEExtractors(numberOfGroups);


        //"names" are one to one
        LinkedList<Integer> rowNames = new LinkedList<>();
        for(int i=0; i<numberOfGroups; i++)
            rowNames.add(i);

        return
                new LogitDestinationStrategy(
                        betas,
                        extractors,
                        rowNames,
                        discretization,
                        new FavoriteDestinationStrategy(state.getMap(), state.getRandom()),
                        state.getRandom());


    }

    private ObservationExtractor[][] buildRPUEExtractors(int numberOfGroups) {
        LameTripSimulator simulator = new LameTripSimulator();
        ObservationExtractor[] commonExtractor = new ObservationExtractor[1];
        commonExtractor[0] = new SimulatedHourlyProfitExtractor(5*24d);
        ObservationExtractor[][] extractors = new ObservationExtractor[numberOfGroups][];
        for(int i=0; i<numberOfGroups; i++)
            extractors[i] = commonExtractor;
        return extractors;
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
     * Getter for property 'profitBeta'.
     *
     * @return Value for property 'profitBeta'.
     */
    public DoubleParameter getProfitBeta() {
        return profitBeta;
    }

    /**
     * Setter for property 'profitBeta'.
     *
     * @param profitBeta Value to set for property 'profitBeta'.
     */
    public void setProfitBeta(DoubleParameter profitBeta) {
        this.profitBeta = profitBeta;
    }
}
