package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.strategies.departing.MonthlyDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Creates the monthly departing Strategy
 * Created by carrknight on 1/6/16.
 */
public class MonthlyDepartingFactory implements AlgorithmFactory<MonthlyDepartingStrategy>{


    private DoubleParameter probabilityEachMonthToGoOutFishing = new FixedDoubleParameter(.3);


    private boolean addYearlyAdapatation = false;

    private DoubleParameter mutationRate = new FixedDoubleParameter(.05);

    private DoubleParameter explorationRate = new FixedDoubleParameter(.2);

    private LinkedList<FishState> registeredStates = new LinkedList<>();

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MonthlyDepartingStrategy apply(FishState fishState) {
        boolean[] months = new boolean[12];
        for(int i=0;i<12;i++)
            months[i] = fishState.random.nextBoolean(probabilityEachMonthToGoOutFishing.apply(fishState.getRandom()));

        if(!addYearlyAdapatation)
            return new MonthlyDepartingStrategy(months);
        else
        {
            double mutationRate = this.mutationRate.apply(fishState.getRandom());
            double explorationRate = this.explorationRate.apply(fishState.getRandom());
            //if it's the first you are building
            if(!registeredStates.contains(fishState))
            {
                //create data counters
                fishState.getYearlyDataSet().
                        registerGatherer("Yearly Effort In Months",
                                         new Function<FishState, Double>() {
                                             @Override
                                             public Double apply(FishState fishState) {
                                                 double sum=0;
                                                 for(Fisher fisher : fishState.getFishers())
                                                 for(Boolean fishing:
                                                     ((MonthlyDepartingStrategy) fisher.getDepartingStrategy()).getAllowedAtSea())
                                                     if(fishing)
                                                         sum++;

                                                 return sum;
                                             }
                                         },
                                         Double.NaN
                        );

                //do it for every month too
                for(int month=0; month<12; month++)
                {
                    final int currentMonth = month;
                    fishState.getYearlyDataSet().
                            registerGatherer("Yearly Efforts In Month " + month,
                                             new Function<FishState, Double>() {
                                                 @Override
                                                 public Double apply(FishState fishState) {
                                                     double sum=0;
                                                     for(Fisher fisher : fishState.getFishers())
                                                             if(((MonthlyDepartingStrategy) fisher.getDepartingStrategy()).getAllowedAtSea()[currentMonth])
                                                                 sum++;

                                                     return sum;
                                                 }
                                             }
                                    , Double.NaN);
                }


                //do this so that we know we have created the counters and we don't do so again
                registeredStates.add(fishState);



                //set the adaptation up (when the model starts)
                fishState.registerStartable(new Startable() {
                    @Override
                    public void start(FishState model) {
                        for(Fisher fisher : model.getFishers()) {
                            fisher.addYearlyAdaptation(new Adaptation(
                                    (Predicate<Fisher>) fisher1 -> true,
                                    //beam hill-climber with random mutation chance for each month
                                    new BeamHillClimbing<MonthlyDepartingStrategy>() {
                                        @Override
                                        public MonthlyDepartingStrategy randomStep(
                                                FishState state, MersenneTwisterFast random, Fisher fisher,
                                                MonthlyDepartingStrategy current) {
                                            boolean[] months = Arrays.copyOf(current.getAllowedAtSea(), 12);
                                            for (int i = 0; i < 12; i++)
                                                if (random.nextBoolean(mutationRate))
                                                    months[i] = !current.getAllowedAtSea()[i];

                                            return new MonthlyDepartingStrategy(months);
                                        }
                                    },
                                    (Actuator<MonthlyDepartingStrategy>) (fisher1, change, model1) -> {
                                        fisher1.setDepartingStrategy(change);
                                    },
                                    new Sensor() {
                                        @Override
                                        public Object scan(Fisher fisher) {
                                            return fisher.getDepartingStrategy();
                                        }
                                    },
                                    new CashFlowObjective(365),
                                    explorationRate,
                                    1
                            ));
                        }
                    }

                    @Override
                    public void turnOff() {

                    }
                });
            }
            return new MonthlyDepartingStrategy(months);
        }
    }


    public DoubleParameter getProbabilityEachMonthToGoOutFishing() {
        return probabilityEachMonthToGoOutFishing;
    }

    public void setProbabilityEachMonthToGoOutFishing(
            DoubleParameter probabilityEachMonthToGoOutFishing) {
        this.probabilityEachMonthToGoOutFishing = probabilityEachMonthToGoOutFishing;
    }

    public boolean isAddYearlyAdapatation() {
        return addYearlyAdapatation;
    }

    public void setAddYearlyAdapatation(boolean addYearlyAdapatation) {
        this.addYearlyAdapatation = addYearlyAdapatation;
    }

    public DoubleParameter getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(DoubleParameter mutationRate) {
        this.mutationRate = mutationRate;
    }

    public DoubleParameter getExplorationRate() {
        return explorationRate;
    }

    public void setExplorationRate(DoubleParameter explorationRate) {
        this.explorationRate = explorationRate;
    }
}
