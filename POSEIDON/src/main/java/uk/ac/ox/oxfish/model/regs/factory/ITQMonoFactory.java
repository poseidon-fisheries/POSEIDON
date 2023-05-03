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

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.regs.ITQCostManager;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Function;

/**
 * Creates both individual quotas like the IQMonoFactory and a quota market for fishers to trade in
 * Created by carrknight on 8/26/15.
 */
public class ITQMonoFactory implements AlgorithmFactory<MonoQuotaRegulation> {

    /**
     * one market only for each fish-state
     */
    private final Locker<String, ITQMarketBuilder> marketBuilders = new Locker<>();

    /**
     * quota available to each guy
     */
    private DoubleParameter individualQuota = new FixedDoubleParameter(5000);

    public ITQMonoFactory(final double individualQuota) {
        this.individualQuota = new FixedDoubleParameter(individualQuota);
    }

    public ITQMonoFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MonoQuotaRegulation apply(final FishState state) {
        //todo need to make this for multiple species


        final ITQMarketBuilder marketBuilder =
            marketBuilders.presentKey(
                state.getUniqueID(),
                () -> {
                    //if not, create it!
                    final ITQMarketBuilder initializer = new ITQMarketBuilder(0);
                    //make sure it will start with the model
                    state.registerStartable(initializer);
                    return initializer;
                }
            );

        assert marketBuilder != null;

        final ITQCostManager cost = new ITQCostManager(new Function<Species, ITQOrderBook>() {
            @Override
            public ITQOrderBook apply(final Species species) {
                return marketBuilder.getMarket();
            }
        });

        final MonoQuotaRegulation toReturn = new MonoQuotaRegulation(individualQuota.applyAsDouble(state.getRandom())) {

            @Override
            public void start(final FishState model, final Fisher fisher) {
                super.start(model, fisher);
                fisher.getOpportunityCosts().add(cost);
            }

            @Override
            public void turnOff(final Fisher fisher) {
                super.turnOff(fisher);
                fisher.getOpportunityCosts().remove(cost);
            }
        };
        marketBuilder.addTrader(toReturn);
        return toReturn;
    }

    public DoubleParameter getIndividualQuota() {
        return individualQuota;
    }

    public void setIndividualQuota(final DoubleParameter individualQuota) {
        this.individualQuota = individualQuota;
    }
}
