/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.boxcars;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.AbundanceFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FormulaAbundanceFilter;

import static org.junit.Assert.*;

public class FishingMortalityAgentTest {


    @Test
    public void fishingMortality() {




        //vulnerability filter is just 0.6 in bin 1 and 0 in bin 0
        AbundanceFilter vulnerabilityFilter = new FormulaAbundanceFilter(false,false){
            /**
             * the method that gives the probability matrix for each age class and each sex of not filtering the
             * abundance away
             *
             * @param species
             * @return
             */
            @Override
            protected double[][] computeSelectivity(Species species) {
                double[][] selectivity = new double[2][2];
                selectivity[0][1] = 0.6d;
                selectivity[1][1] = 0.6d;

                return selectivity;
            }
        };


        Species species = new Species("test",
                                   new FromListMeristics(new double[]{1, 2}, 2));


        FishingMortalityAgent agent = new FishingMortalityAgent(vulnerabilityFilter,
                                                                                species,
                false);


        double[][] catches = new double[2][2];
        catches[0][1] = 500;
        catches[1][1] = 200;
        double[][] abundance = new double[2][2];
        abundance[0][0] = 1000;
        abundance[0][1] = 1000;
        abundance[1][0] = 1000;
        abundance[1][1] = 1000;


        double mortality = agent.computeMortality(
                catches,
                abundance
        );
        assertEquals(0.8754687,mortality,.001);


    }
}