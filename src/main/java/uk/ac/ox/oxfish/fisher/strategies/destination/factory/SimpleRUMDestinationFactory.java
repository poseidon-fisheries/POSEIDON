package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PeriodHabitContinuousExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PortDistanceExtractor;
import uk.ac.ox.oxfish.fisher.log.TripLaggedExtractor;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitWithLaggedExtractorsDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * creates a simple RUM a la Abbot and Wilen (2011) and cognates; with revenue, distance, habit and CPUE as covariates
 * of a simple logit
 */
public class SimpleRUMDestinationFactory implements AlgorithmFactory<LogitWithLaggedExtractorsDestinationStrategy> {

    /**
     * everybody shares the same discretization
     */
    private final Locker<FishState, MapDiscretization> discretizationLocker = new Locker<>();

    /**
     *
     */
    private Locker<FishState,List<TripLaggedExtractor>> fleetWideLocker = new Locker<>();

    protected AlgorithmFactory<? extends MapDiscretizer> discretizer =
            new SquaresMapDiscretizerFactory();


    private DoubleParameter betaDistance = new FixedDoubleParameter(-1);

    private DoubleParameter betaHabit = new FixedDoubleParameter(1);

    private DoubleParameter betaRevenue = new FixedDoubleParameter(1);

    /**
     * map linking species we want to study their CPUE of and the beta parameter associated with them
     */
    private LinkedHashMap<String,String>  betaCPUE = new LinkedHashMap();
    {
        betaCPUE.put("Species 0", "1");
    }



    private boolean automaticallyAvoidMPA = true;

    private boolean automaticallyAvoidWastelands = true;

    private boolean fleetWide = true;


    public AlgorithmFactory<? extends MapDiscretizer> getDiscretizer() {
        return discretizer;
    }

    public void setDiscretizer(AlgorithmFactory<? extends MapDiscretizer> discretizer) {
        this.discretizer = discretizer;
    }


    @Override
    public LogitWithLaggedExtractorsDestinationStrategy apply(FishState state) {

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

        //assign coefficients (they are the same for every option)
        double[][] betas = buildBetas(state, areas);

        //create observers
        Pair<ObservationExtractor[][], List<TripLaggedExtractor>> extractorPair =
                buildExtractors(state, discretization, areas, betas);
        ObservationExtractor[][] extractors = extractorPair.getFirst();

        return new LogitWithLaggedExtractorsDestinationStrategy(
                betas,extractors,
                validAreas,
                discretization,
                new FavoriteDestinationStrategy(state.getMap(), state.getRandom()),
                state.getRandom(),
                automaticallyAvoidMPA,
                automaticallyAvoidWastelands,
                fleetWide,
                extractorPair.getSecond()

        );

    }


    protected double[][] buildBetas(FishState state, int areas) {
        //the same parameters for all the choices
        double[][] betas = new double[areas][3 + betaCPUE.size()];
        betas[0][0] = betaDistance.apply(state.getRandom());
        betas[0][1] = betaHabit.apply(state.getRandom());
        betas[0][2] = betaRevenue.apply(state.getRandom());
        int i=3;
        for (Map.Entry<String, String> cpues : betaCPUE.entrySet()) {
            betas[0][i] = DoubleParameter.parseDoubleParameter(cpues.getValue()).apply(state.getRandom());
            i++;
        }


        for(i=1; i<areas; i++)
        {
            for(int j=0; j<betas[0].length; j++)
                betas[i][j] = betas[0][j];
        }
        return betas;
    }



    @NotNull
    protected Pair<ObservationExtractor[][],List<TripLaggedExtractor>> buildExtractors(
            FishState state, MapDiscretization discretization, int areas, double[][] betas) {
        //get the extractors
        ObservationExtractor[][] extractors = new ObservationExtractor[betas.length][];
        ObservationExtractor[] commonExtractor = new ObservationExtractor[3 + betaCPUE.size()];
        commonExtractor[0] = new PortDistanceExtractor();
        commonExtractor[1] = new PeriodHabitContinuousExtractor(discretization,
                365);
        List<TripLaggedExtractor> otherExtractors = getTripLaggedExtractors(discretization,state);
        int i=2;
        for (TripLaggedExtractor laggedExtractor : otherExtractors) {
            commonExtractor[i] = laggedExtractor;
            i++;
        }

        assert i==commonExtractor.length;

        for(i=0; i<areas; i++)
            extractors[i] = commonExtractor;
        return new Pair<>(extractors,otherExtractors);
    }


    private List<TripLaggedExtractor> getTripLaggedExtractors(MapDiscretization discretization,
                                                              FishState state){
        if(!fleetWide)
            return buildTripLaggedExtractors(discretization,state);
        else
            return fleetWideLocker.presentKey(state,
                    new Supplier<List<TripLaggedExtractor>>() {
                        @Override
                        public List<TripLaggedExtractor> get() {
                            return buildTripLaggedExtractors(discretization,state);
                        }
                    });

    }

    private List<TripLaggedExtractor> buildTripLaggedExtractors(MapDiscretization discretization,
                                                                FishState state) {
        //the first is always the revenue
        List<TripLaggedExtractor> extractors = new LinkedList<>();
        //earnings/hr
        extractors.add(
                new TripLaggedExtractor(
                        new Function<TripRecord, Double>() {
                            @Override
                            public Double apply(TripRecord tripRecord) {
                                return tripRecord.getEarnings()/tripRecord.getDurationInHours();
                            }
                        }
                        ,
                        discretization
                )
        );
        for (Map.Entry<String, String> cpueBeta : betaCPUE.entrySet()) {
            extractors.add(
                    new TripLaggedExtractor(
                            new Function<TripRecord, Double>() {
                                @Override
                                public Double apply(TripRecord tripRecord) {
                                    return tripRecord.getTotalCPUE()[
                                            state.getBiology().getSpecie(cpueBeta.getKey()).getIndex()];
                                }
                            }
                            ,
                            discretization
                    )
            );
        }

        return extractors;


    }


    public DoubleParameter getBetaDistance() {
        return betaDistance;
    }

    public void setBetaDistance(DoubleParameter betaDistance) {
        this.betaDistance = betaDistance;
    }

    public DoubleParameter getBetaHabit() {
        return betaHabit;
    }

    public void setBetaHabit(DoubleParameter betaHabit) {
        this.betaHabit = betaHabit;
    }

    public DoubleParameter getBetaRevenue() {
        return betaRevenue;
    }

    public void setBetaRevenue(DoubleParameter betaRevenue) {
        this.betaRevenue = betaRevenue;
    }

    public LinkedHashMap<String, String> getBetaCPUE() {
        return betaCPUE;
    }

    public void setBetaCPUE(LinkedHashMap<String, String> betaCPUE) {
        this.betaCPUE = betaCPUE;
    }

    public boolean isAutomaticallyAvoidMPA() {
        return automaticallyAvoidMPA;
    }

    public void setAutomaticallyAvoidMPA(boolean automaticallyAvoidMPA) {
        this.automaticallyAvoidMPA = automaticallyAvoidMPA;
    }

    public boolean isAutomaticallyAvoidWastelands() {
        return automaticallyAvoidWastelands;
    }

    public void setAutomaticallyAvoidWastelands(boolean automaticallyAvoidWastelands) {
        this.automaticallyAvoidWastelands = automaticallyAvoidWastelands;
    }

    public boolean isFleetWide() {
        return fleetWide;
    }

    public void setFleetWide(boolean fleetWide) {
        this.fleetWide = fleetWide;
    }
}
