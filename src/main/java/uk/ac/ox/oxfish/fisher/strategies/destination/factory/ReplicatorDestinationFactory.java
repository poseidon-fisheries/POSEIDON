package uk.ac.ox.oxfish.fisher.strategies.destination.factory;


import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.ReplicatorDrivenDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.StrategyReplicator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class ReplicatorDestinationFactory implements AlgorithmFactory<ReplicatorDrivenDestinationStrategy> {



    private List<AlgorithmFactory<? extends DestinationStrategy>> options = new LinkedList<>();
    {
        options.add(new BanditDestinationFactory());
        options.add(new GravitationalSearchDestinationFactory());
    }


    private Locker<FishState,StrategyReplicator> replicator = new Locker<>();

    private DoubleParameter inertia = new FixedDoubleParameter(.8);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ReplicatorDrivenDestinationStrategy apply(FishState state) {

        StrategyReplicator replicator =
                this.replicator.presentKey(state,
                                           new Supplier<StrategyReplicator>() {
                                               @Override

                                               public StrategyReplicator get() {

                                                   StrategyReplicator replicator = new StrategyReplicator(
                                                           options,
                                                           new CashFlowObjective(60),
                                                           inertia.apply(
                                                                   state.getRandom()));

                                                   state.registerStartable(replicator);


                                                   for(int strategy = 0; strategy<options.size(); strategy++)
                                                   {
                                                       int finalStrategy = strategy;
                                                       state.getDailyDataSet().
                                                               registerGatherer(
                                                                       "Fishers using strategy " + strategy,
                                                                                new Gatherer<FishState>() {
                                                                                    @Override
                                                                                    public Double apply(FishState state) {
                                                                                        double count = 0;
                                                                                        for(Fisher fisher : state.getFishers())
                                                                                        {
                                                                                            if(((ReplicatorDrivenDestinationStrategy) fisher.getDestinationStrategy()).getStrategyIndex() == finalStrategy)
                                                                                                count++;
                                                                                        }
                                                                                        return count;

                                                                                    }
                                                                                },Double.NaN);
                                                       int finalStrategy1 = strategy;
                                                       state.getDailyDataSet().
                                                               registerGatherer(
                                                                       "Fitness of strategy " + strategy,
                                                                       new Gatherer<FishState>() {
                                                                           @Override
                                                                           public Double apply(FishState state) {
                                                                               return replicator.getLastObservedFitnesses()[finalStrategy1];
                                                                           }
                                                                       },Double.NaN);
                                                   }
                                                   return replicator;
                                               }
                                           });

        int strategy = state.getRandom().nextInt(replicator.getOptions().size());
        return new ReplicatorDrivenDestinationStrategy(
                strategy,
                replicator.getOptions().get(strategy).apply(state)
        );

    }

    /**
     * Getter for property 'options'.
     *
     * @return Value for property 'options'.
     */
    public List<AlgorithmFactory<? extends DestinationStrategy>> getOptions() {
        return options;
    }

    /**
     * Setter for property 'options'.
     *
     * @param options Value to set for property 'options'.
     */
    public void setOptions(
            List<AlgorithmFactory<? extends DestinationStrategy>> options) {
        this.options = options;
    }


    /**
     * Getter for property 'inertia'.
     *
     * @return Value for property 'inertia'.
     */
    public DoubleParameter getInertia() {
        return inertia;
    }

    /**
     * Setter for property 'inertia'.
     *
     * @param inertia Value to set for property 'inertia'.
     */
    public void setInertia(DoubleParameter inertia) {
        this.inertia = inertia;
    }
}
