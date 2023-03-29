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


    private final Locker<String, StrategyReplicator> replicator = new Locker<>();
    private List<AlgorithmFactory<? extends DestinationStrategy>> options = new LinkedList<>();
    private DoubleParameter inertia = new FixedDoubleParameter(.8);

    {
        options.add(new BanditDestinationFactory());
        options.add(new GravitationalSearchDestinationFactory());
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ReplicatorDrivenDestinationStrategy apply(final FishState state) {

        final StrategyReplicator replicator =
            this.replicator.presentKey(
                state.getHopefullyUniqueID(),
                new Supplier<StrategyReplicator>() {
                    @Override

                    public StrategyReplicator get() {

                        final StrategyReplicator replicator = new StrategyReplicator(
                            options,
                            new CashFlowObjective(60),
                            inertia.applyAsDouble(
                                state.getRandom())
                        );

                        state.registerStartable(replicator);


                        for (int strategy = 0; strategy < options.size(); strategy++) {
                            final int finalStrategy = strategy;
                            state.getDailyDataSet().
                                registerGatherer(
                                    "Fishers using strategy " + strategy,
                                    (Gatherer<FishState>) state1 -> {
                                        double count = 0;
                                        for (final Fisher fisher : state1.getFishers()) {
                                            if (((ReplicatorDrivenDestinationStrategy) fisher.getDestinationStrategy()).getStrategyIndex() == finalStrategy)
                                                count++;
                                        }
                                        return count;

                                    }, Double.NaN
                                );
                            final int finalStrategy1 = strategy;
                            state.getDailyDataSet().
                                registerGatherer(
                                    "Fitness of strategy " + strategy,
                                    new Gatherer<FishState>() {
                                        @Override
                                        public Double apply(final FishState state) {
                                            return replicator.getLastObservedFitnesses()[finalStrategy1];
                                        }
                                    }, Double.NaN
                                );
                        }
                        return replicator;
                    }
                }
            );

        final int strategy = state.getRandom().nextInt(replicator.getOptions().size());
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
        final List<AlgorithmFactory<? extends DestinationStrategy>> options
    ) {
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
    public void setInertia(final DoubleParameter inertia) {
        this.inertia = inertia;
    }
}
