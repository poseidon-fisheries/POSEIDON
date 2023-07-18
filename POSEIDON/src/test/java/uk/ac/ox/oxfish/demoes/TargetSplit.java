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

package uk.ac.ox.oxfish.demoes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.factory.TargetSpeciesObjectiveFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.logging.Logger;

/**
 * If you make fishers only care about red, they'll fish mostly red
 * Created by carrknight on 3/24/16.
 */
public class TargetSplit {

    @Test
    public void targetObjective() throws Exception {
        Logger.getGlobal()
            .info(
                "fishers are given an objective functions that makes them consider only red earnings; they should fish only red then");

        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half


        final SplitInitializerFactory biologyFactory = new SplitInitializerFactory();


        final PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);

        final PerTripImitativeDestinationFactory destinationFactory = new PerTripImitativeDestinationFactory();
        final TargetSpeciesObjectiveFactory objectiveFunction = new TargetSpeciesObjectiveFactory();
        objectiveFunction.setOpportunityCosts(true);
        objectiveFunction.setSpeciesIndex(0);
        destinationFactory.setObjectiveFunction(
            objectiveFunction
        );
        scenario.setDestinationStrategy(destinationFactory);

        final SimpleMapInitializerFactory simpleMap = new SimpleMapInitializerFactory();
        simpleMap.setCoastalRoughness(new FixedDoubleParameter(0d));
        scenario.setMapInitializer(simpleMap);
        scenario.forcePortPosition(new int[]{40, 25});

        scenario.setUsePredictors(false);


        long towsNorth = 0;
        long towsSouth = 0;

        state.start();

        while (state.getYear() < 3) {
            state.schedule.step(state);
            for (int x = 0; x < 50; x++) {
                for (int y = 0; y <= 25; y++) {
                    towsNorth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
                for (int y = 26; y < 50; y++) {
                    towsSouth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
            }
        }

        Logger.getGlobal()
            .info("North vs South : " + towsNorth / ((double) towsNorth + towsSouth) + " I expect it to be at least 70%");
        Assertions.assertTrue(towsNorth / ((double) towsNorth + towsSouth) > .7);

    }
}
