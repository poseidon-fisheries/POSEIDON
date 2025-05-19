/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.Lists;
import org.jfree.util.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.List;
import java.util.ServiceLoader;

import static com.google.common.collect.Streams.stream;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 8/12/16.
 */
public class ITQCostManagerTest {

    @Test
    public void factoriesCreateIt() throws Exception {

        final List<String> regulations = Lists.newArrayList(
            "Mono-ITQ",
            "Multi-ITQ",
            "Multi-ITQ by List",
            "Partial-ITQ"
        );

        for (final String regulation : regulations)
            testThatCostManagerHasBeenInitialized(regulation);

    }

    private void testThatCostManagerHasBeenInitialized(final String regulationName) {
        Log.info("Testing that " + regulationName + " creates 1 cost manager");
        final FishState state = initialize(regulationName);
        int count = 0;
        for (final Cost cost : state.getFishers().get(0).getOpportunityCosts())
            if (cost instanceof ITQCostManager)
                count++;

        Assertions.assertEquals(count, 1);
    }

    @SuppressWarnings("unchecked")
    private FishState initialize(final String regulationName) {
        final PrototypeScenario scenario = new PrototypeScenario();
        final SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setWidth(new FixedDoubleParameter(15));
        mapInitializer.setHeight(new FixedDoubleParameter(1));
        scenario.setBiologyInitializer(new SplitInitializerFactory());
        scenario.setMapInitializer(mapInitializer);
        scenario.setRegulation(
            (AlgorithmFactory<? extends Regulation>) stream(ServiceLoader.load(FactorySupplier.class))
                .filter(factorySupplier -> factorySupplier.getFactoryName().equals(regulationName))
                .findFirst()
                .get()
                .get()
        );
        scenario.setFishers(1);

        final FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        state.schedule.step(state);
        return state;
    }

    @Test
    public void cost() throws Exception {
        final ITQOrderBook first = mock(ITQOrderBook.class);
        when(first.getLastClosingPrice()).thenReturn(1d);
        final ITQOrderBook second = mock(ITQOrderBook.class);
        when(second.getLastClosingPrice()).thenReturn(10d);

        final Species firstSpecies = mock(Species.class);
        when(firstSpecies.getIndex()).thenReturn(0);
        final Species secondSpecies = mock(Species.class);
        when(secondSpecies.getIndex()).thenReturn(1);
        final FishState model = mock(FishState.class);
        when(model.getSpecies()).thenReturn(Lists.newArrayList(firstSpecies, secondSpecies));

        final ITQCostManager costManager = new ITQCostManager(species -> {
            if (species == firstSpecies)
                return first;
            else {
                assert species == secondSpecies;
                return second;
            }
        });

        final TripRecord record = mock(TripRecord.class);
        when(record.getSoldCatch()).thenReturn(new double[]{5, 3});
        Assertions.assertEquals(
            35,
            costManager.cost(mock(Fisher.class), model, record, -1, 1),
            .001
        );

    }
}
