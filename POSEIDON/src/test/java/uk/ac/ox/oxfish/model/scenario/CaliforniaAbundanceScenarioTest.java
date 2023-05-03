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

package uk.ac.ox.oxfish.model.scenario;

import com.esotericsoftware.minlog.Log;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.factory.FishingSeasonFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by carrknight on 3/17/16.
 */
public class CaliforniaAbundanceScenarioTest {


    @Test
    public void readsTheRightAmountOfShortspineBiomass() throws Exception {

        double target = 274210064.370047;
        CaliforniaAbundanceScenario scenario = new CaliforniaAbundanceScenario();
        FishState model = new FishState(System.currentTimeMillis());
        model.setScenario(scenario);
        model.start();
        List<SeaTile> tiles = model.getMap().getAllSeaTilesAsList();
        double totalShortspineBiomass = 0;
        assertEquals(model.getBiology().getSpecies().get(0).getName(),"Dover Sole");
        Species shortSpine = model.getBiology().getSpecie("Shortspine Thornyhead");
        for(SeaTile tile : tiles)
        {
            totalShortspineBiomass += tile.getBiomass(shortSpine);
        }

        //different by at most 5%
        Log.info("From data we know that the shortspine biomass is " + target + ", after many transformations our" +
                         "model has " + totalShortspineBiomass + " in the model  ");
        Log.info("They differ by " + 100*Math.abs(totalShortspineBiomass-target)/target + "% the maximum allowed is 5%");
        assertEquals(totalShortspineBiomass,target,target*.05);



        //Morro Bay: Easting: 695337, Northing: 3915757.7
        //San Francisco: 553454.67, 4178621.91
        SeaTile morro = model.getMap().getSeaTile(new Coordinate(695337, 3915757.7));
        SeaTile sf = model.getMap().getSeaTile(new Coordinate(553454.67, 4178621.91));
        double km = model.getMap().distance(morro, sf);
        Log.info("the distance between Morro Bay and San Francisco is about 300km, the distance calculator thinks it is "+ km);
        assertEquals(km,300,10);



        morro = model.getMap().getSeaTile(29,104);
        for(int i=0; i<10; i++)
        {
            Log.info("Distance " + i + " cells away from Morro Bay is " +
                             model.getMap().distance(morro,model.getMap().getSeaTile(29-i,104)));
        }
    }


    @Test
    public void checkBiomass() throws Exception {
        CaliforniaAbundanceScenario scenario = new CaliforniaAbundanceScenario();

        FishingSeasonFactory regulation = new FishingSeasonFactory();
        //no fishing whatsoever
        regulation.setSeasonLength(new FixedDoubleParameter(0));
        scenario.setRegulationPreReset(regulation);
        scenario.setRegulationPostReset(regulation);
        scenario.getExogenousCatches().clear();
        scenario.setResetBiologyAtYear1(true);

        FishState state = new FishState(System.currentTimeMillis());

        state.setScenario(scenario);
        state.start();


        while(state.getYear()<15) {
            state.schedule.step(state);
            if(state.getDayOfTheYear()==1)
                System.out.println(state.getYear() + " ----- " + state.getTotalBiomass(state.getBiology().getSpecie("Sablefish"))/1000);
        }


    }


    @Test
    public void spinsUpCorrectly() throws Exception {


        CaliforniaAbundanceScenario scenario = new CaliforniaAbundanceScenario();


        scenario.getExogenousCatches().clear();
        scenario.getExogenousCatches().put("Dover Sole", String.valueOf(12345678d));

        scenario.setResetBiologyAtYear1(true);
        scenario.setRegulationPreReset(new FishingSeasonFactory(15,true));
        scenario.setRegulationPostReset(new FishingSeasonFactory(0,true));
   //     scenario.setRegulationPreReset(new AnarchyFactory());
   //     scenario.setRegulationPostReset(new AnarchyFactory());
        FishState state = new FishState(System.currentTimeMillis());

        state.setScenario(scenario);
        state.start();
        NauticalMap map = state.getMap();

        Species sole = state.getBiology().getSpecie("Dover Sole");
        final double initialBiomasses = map.getAllSeaTilesAsList().stream().mapToDouble(
                value -> value.getBiomass(sole)).sum();


        state.scheduleOnceInXDays(new Steppable() {
            @Override
            public void step(SimState simState) {
                double biomass = map.getAllSeaTilesAsList().stream().mapToDouble(
                        value -> value.getBiomass(sole)).sum();

                assertNotEquals(initialBiomasses,
                                biomass,
                                .0001 * initialBiomasses
                );

            }
        }, StepOrder.FISHER_PHASE, 363);


        state.scheduleOnceInXDays(new Steppable() {
            @Override
            public void step(SimState simState) {
                assertEquals(initialBiomasses,
                             map.getAllSeaTilesAsList().stream().mapToDouble(
                                     value -> value.getBiomass(sole)).sum(),
                             .0001 * initialBiomasses
                );

            }
        }, StepOrder.FISHER_PHASE, 366);


        for (int i = 0; i < 366; i++)
            state.schedule.step(state);

        //innaccurate +-10kg
        assertEquals(state.getYearlyDataSet().getLatestObservation("Exogenous catches of Dover Sole"),
                     12345678d,10d);

    }
}