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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.regs.SpecificQuotaRegulation;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 9/24/15.
 */
public class OpportunityCostsTest {


    //i force the opportunity costs to be huge and the fishers will avoid specie 0 like hell!
    @Test
    public void opportunityCostsMatterSpecie0() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(new SplitInitializerFactory());
        scenario.setUsePredictors(true);
        ITQSpecificFactory regs = new ITQSpecificFactory();
        regs.setSpecieIndex(0);
        regs.setIndividualQuota(new FixedDoubleParameter(5000));
        scenario.setRegulation(regs);
        scenario.setMapMakerDedicatedRandomSeed(100l); //places port around the middle

        state.start();
        for (Fisher fisher : state.getFishers())
            fisher.getOpportunityCosts().add(new Cost() {
                                                 @Override
                                                 public double cost(
                                                     Fisher fisher,
                                                     FishState model,
                                                     TripRecord record,
                                                     double revenue,
                                                     double durationInHours
                                                 ) {
                                                     //account for opportunity costs
                                                     SpecificQuotaRegulation regs = ((SpecificQuotaRegulation) fisher.getRegulation());
                                                     double biomass = record.getSoldCatch()[regs.getProtectedSpecies().getIndex()];
                                                     if (biomass > 0) {
                                                         return 1000 * biomass;
                                                     }
                                                     return 0d;
                                                 }

                                                 @Override
                                                 public double expectedAdditionalCosts(
                                                     Fisher fisher,
                                                     double additionalTripHours,
                                                     double additionalEffortHours,
                                                     double additionalKmTravelled
                                                 ) {
                                                     return 0;
                                                 }
                                             }

            );

        //lspiRun it for two years
        while (state.getYear() < 2) {
            state.schedule.step(state);
        }
        state.schedule.step(state);
        //grab yearly fishing data
        final double protectedSpecie = state.getYearlyDataSet().getColumn(
            state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME).getLatest();
        final double unprotected = state.getYearlyDataSet().getColumn(
            state.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME).getLatest();


        System.out.println(unprotected / (protectedSpecie + unprotected));
        Assertions.assertTrue(unprotected / (protectedSpecie + unprotected) > .9); //90% of all the fishing ought to be from
        //the non-ITQ specie

    }

    //exactly like before, but protect the other specie
    @Test
    public void opportunityCostsMatterSpecie1() throws Exception {


        PrototypeScenario scenario = new PrototypeScenario();
        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(new SplitInitializerFactory());
        scenario.setUsePredictors(true);
        ITQSpecificFactory regs = new ITQSpecificFactory();
        regs.setSpecieIndex(1);
        regs.setIndividualQuota(new FixedDoubleParameter(5000));
        scenario.setRegulation(regs);
        scenario.setMapMakerDedicatedRandomSeed(100l); //places port around the middle

        state.start();
        for (Fisher fisher : state.getFishers())
            fisher.getOpportunityCosts().add(new Cost() {
                @Override
                public double cost(
                    Fisher fisher,
                    FishState model,
                    TripRecord record,
                    double revenue,
                    double durationInHours
                ) {
                    //account for opportunity costs
                    SpecificQuotaRegulation regs = ((SpecificQuotaRegulation) fisher.getRegulation());
                    double biomass = record.getSoldCatch()[regs.getProtectedSpecies().getIndex()];
                    if (biomass > 0) {
                        return 1000 * biomass;
                    }
                    return 0d;
                }

                @Override
                public double expectedAdditionalCosts(
                    Fisher fisher,
                    double additionalTripHours,
                    double additionalEffortHours,
                    double additionalKmTravelled
                ) {
                    return 0;
                }
            });

        //lspiRun it for two years
        while (state.getYear() < 2) {
            state.schedule.step(state);
        }
        state.schedule.step(state);
        //grab yearly fishing data
        final double protectedSpecie = state.getYearlyDataSet().getColumn(
            state.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME).getLatest();
        final double unprotected = state.getYearlyDataSet().getColumn(
            state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME).getLatest();


        System.out.println(unprotected / (protectedSpecie + unprotected));
        Assertions.assertTrue(unprotected / (protectedSpecie + unprotected) > .9); //90% of all the fishing ought to be from
        //the non-ITQ specie

    }
}
