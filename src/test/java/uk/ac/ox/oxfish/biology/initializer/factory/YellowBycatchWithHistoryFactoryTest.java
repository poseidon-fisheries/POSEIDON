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

package uk.ac.ox.oxfish.biology.initializer.factory;

import com.google.common.collect.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 3/17/17.
 */
public class YellowBycatchWithHistoryFactoryTest {


    @Test
    public void virgin() throws Exception {

        //numbers numbers numbers
        YellowBycatchWithHistoryFactory factory = new YellowBycatchWithHistoryFactory();
        factory.setHistoricalTargetBiomass(Lists.newArrayList(527154d, 527154d, 527154d, 527154d, 527154d, 527154d));
        factory.setTargetRho(new FixedDoubleParameter(1.03));
        factory.setTargetNaturalSurvivalRate(new FixedDoubleParameter(0.92311));
        factory.setTargetRecruitmentSteepness(new FixedDoubleParameter(0.6));
        factory.setTargetRecruitmentLag(new FixedDoubleParameter(3));
        factory.setTargetWeightAtRecruitment(new FixedDoubleParameter(1.03313));
        factory.setTargetWeightAtRecruitmentMinus1(new FixedDoubleParameter(1.01604));
        factory.setTargetVirginBiomass(new FixedDoubleParameter(527154d));
        factory.setTargetInitialRecruits(new FixedDoubleParameter(29728.8));

        factory.setVerticalSeparator(new FixedDoubleParameter(0));
        factory.setHistoricalBycatchBiomass(null);


        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(factory);
        scenario.setFishers(0);
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(4));
        mapInitializer.setWidth(new FixedDoubleParameter(4));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(mapInitializer);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        while(state.getYear()<=3){
            state.schedule.step(state);
        }
        assertEquals(state.getTotalBiomass(state.getSpecies().get(0)),527154d, .001);

    }

    @Test
    public void virginByNull() throws Exception {

        //numbers numbers numbers
        YellowBycatchWithHistoryFactory factory = new YellowBycatchWithHistoryFactory();
        factory.setHistoricalTargetBiomass(null);
        factory.setTargetRho(new FixedDoubleParameter(1.03));
        factory.setTargetNaturalSurvivalRate(new FixedDoubleParameter(0.92311));
        factory.setTargetRecruitmentSteepness(new FixedDoubleParameter(0.6));
        factory.setTargetRecruitmentLag(new FixedDoubleParameter(3));
        factory.setTargetWeightAtRecruitment(new FixedDoubleParameter(1.03313));
        factory.setTargetWeightAtRecruitmentMinus1(new FixedDoubleParameter(1.01604));
        factory.setTargetVirginBiomass(new FixedDoubleParameter(527154d));
        factory.setTargetInitialRecruits(new FixedDoubleParameter(29728.8));

        factory.setVerticalSeparator(new FixedDoubleParameter(0));
        factory.setHistoricalBycatchBiomass(null);


        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(factory);
        scenario.setFishers(0);
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(4));
        mapInitializer.setWidth(new FixedDoubleParameter(4));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(mapInitializer);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        while(state.getYear()<=3){
            state.schedule.step(state);
        }
        assertEquals(state.getTotalBiomass(state.getSpecies().get(0)),527154d, .001);

    }


    @Test
    public void onePercentFishingMortality() throws Exception {

        //numbers numbers numbers
        YellowBycatchWithHistoryFactory factory = new YellowBycatchWithHistoryFactory();
        factory.setHistoricalTargetBiomass(null);
        factory.setTargetRho(new FixedDoubleParameter(1.03));
        factory.setTargetNaturalSurvivalRate(new FixedDoubleParameter(0.92311));
        factory.setTargetRecruitmentSteepness(new FixedDoubleParameter(0.6));
        factory.setTargetRecruitmentLag(new FixedDoubleParameter(3));
        factory.setTargetWeightAtRecruitment(new FixedDoubleParameter(1.03313));
        factory.setTargetWeightAtRecruitmentMinus1(new FixedDoubleParameter(1.01604));
        factory.setTargetVirginBiomass(new FixedDoubleParameter(527154d));
        factory.setTargetInitialRecruits(new FixedDoubleParameter(29728.8));

        factory.setVerticalSeparator(new FixedDoubleParameter(0));
        factory.setHistoricalBycatchBiomass(null);


        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(factory);
        scenario.setFishers(0);
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(4));
        mapInitializer.setWidth(new FixedDoubleParameter(4));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(mapInitializer);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        Species target = state.getSpecies().get(0);

        while(state.getYear()<=3){
            state.schedule.step(state);

            if(state.getDayOfTheYear()==100) { // at day 100 kill off one % of all the target
                System.out.println(state.getTotalBiomass(target));
                for (SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList())
                    tile.reactToThisAmountOfBiomassBeingFished(
                            new Catch(target,tile.getBiomass(target)*.01,state.getBiology()),
                            null,state.getBiology());
            }
        }
        assertEquals(state.getTotalBiomass(target), 509166.4d, .1);

    }


    @Test
    public void startatyear12() throws Exception {

        //numbers numbers numbers
        YellowBycatchWithHistoryFactory factory = new YellowBycatchWithHistoryFactory();
        factory.setHistoricalTargetBiomass(Lists.newArrayList(505317.4, 501682.3, 498248.3, 495003.3, 491936.2, 489036.4));
        factory.setHistoricalTargetSurvival(Lists.newArrayList(0.9138789,0.9138789));
        factory.setTargetRho(new FixedDoubleParameter(1.03));
        factory.setTargetNaturalSurvivalRate(new FixedDoubleParameter(0.92311));
        factory.setTargetRecruitmentSteepness(new FixedDoubleParameter(0.6));
        factory.setTargetRecruitmentLag(new FixedDoubleParameter(3));
        factory.setTargetWeightAtRecruitment(new FixedDoubleParameter(1.03313));
        factory.setTargetWeightAtRecruitmentMinus1(new FixedDoubleParameter(1.01604));
        factory.setTargetVirginBiomass(new FixedDoubleParameter(527154d));
        factory.setTargetInitialRecruits(new FixedDoubleParameter(29444.10));

        factory.setVerticalSeparator(new FixedDoubleParameter(0));
        factory.setHistoricalBycatchBiomass(null);


        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(factory);
        scenario.setFishers(0);
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(4));
        mapInitializer.setWidth(new FixedDoubleParameter(4));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(mapInitializer);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        Species target = state.getSpecies().get(0);

        while(state.getYear()<=3){
            state.schedule.step(state);

            if(state.getDayOfTheYear()==100) { // at day 100 kill off one % of all the target
                System.out.println(state.getTotalBiomass(target));
                for (SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList())
                    tile.reactToThisAmountOfBiomassBeingFished(
                            new Catch(target,tile.getBiomass(target)*.01,state.getBiology()),
                            null,state.getBiology());
            }
        }
        assertEquals(state.getTotalBiomass(target), 478923.5d, .1);

    }


}