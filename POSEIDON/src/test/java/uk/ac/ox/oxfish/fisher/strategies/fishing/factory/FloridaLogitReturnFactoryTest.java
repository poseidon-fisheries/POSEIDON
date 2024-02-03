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

package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.fishing.DailyReturnDecorator;
import uk.ac.ox.oxfish.fisher.strategies.fishing.LogitReturnStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.MaximumDaysDecorator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 5/25/17.
 */
public class FloridaLogitReturnFactoryTest {


    @Test
    public void logitReturnsRightProbability() throws Exception {
        //scaled $/kg
        final FloridaLogitReturnFactory handlinerFishingStrategy = new FloridaLogitReturnFactory();
        handlinerFishingStrategy.setIntercept(new FixedDoubleParameter(-3.47701));
        handlinerFishingStrategy.setPriceRedGrouper(new FixedDoubleParameter(0.92395));
        handlinerFishingStrategy.setPriceGagGrouper(new FixedDoubleParameter(-0.65122));
        handlinerFishingStrategy.setRatioCatchToFishHold(new FixedDoubleParameter(4.37828));
        handlinerFishingStrategy.setWeekendDummy(new FixedDoubleParameter(-0.24437));

        final FishState state = mock(FishState.class, RETURNS_DEEP_STUBS);
        final Species gag = mock(Species.class);
        when((state.getBiology().getSpeciesByCaseInsensitiveName("GagGrouper"))).thenReturn(gag);
        final Species red = mock(Species.class);
        when((state.getBiology().getSpeciesByCaseInsensitiveName("RedGrouper"))).thenReturn(red);
        final DailyReturnDecorator strategy = handlinerFishingStrategy.apply(state);
        final LogitReturnStrategy logit = (LogitReturnStrategy) ((MaximumDaysDecorator) strategy.accessDecorated()).accessDecorated();


        final SeaTile tile = mock(SeaTile.class);
        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);

        when(fisher.getHomePort().getMarginalPrice(gag, fisher)).thenReturn(10d);
        when(fisher.getHomePort().getMarginalPrice(red, fisher)).thenReturn(3d);
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(100d);
        when(fisher.getMaximumHold()).thenReturn(200d);


        final double probability = logit.getShouldIReturnClassifier().getProbability(
            fisher,
            30 * 24, //not a weekend!
            state,
            tile
        );
        System.out.println(probability);
        //grabbed from wolfram alpha
        Assertions.assertEquals(0.006507830733273149, probability, .001);


    }
}
