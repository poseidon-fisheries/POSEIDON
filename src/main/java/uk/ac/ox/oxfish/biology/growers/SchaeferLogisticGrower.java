/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.growers;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

/**
 * like the common logistic grower but uses LAST year biomass for it
 */
public class SchaeferLogisticGrower extends CommonLogisticGrower {


    double lastYearBiomass;

    public SchaeferLogisticGrower(
            double malthusianParameter, Species species, double distributionalWeight) {
        super(malthusianParameter, species, distributionalWeight);
    }


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {



        super.start(model);


        model.scheduleEveryYear(new Steppable() {
            @Override
            public void step(SimState simState) {
                double current = 0;
                //for each place
                for(VariableBiomassBasedBiology biology : getBiologies())
                {
                    current += biology.getBiomass(species);

                }

                lastYearBiomass = current;
            }
        },
                           StepOrder.AFTER_DATA);
    }


    @Override
    protected double recruit(double current, double capacity, double malthusianParameter) {
        double toGrow = super.recruit(lastYearBiomass, capacity, malthusianParameter);
        return toGrow;
    }

    /**
     * ugly hook to get after recruitment total biomass
     */
    @Override
    protected void afterRecruitmentHook() {

//        double current = 0;
//
//        for(VariableBiomassBasedBiology biology : getBiologies())
//        {
//            current += biology.getBiomass(species);
//
//        }
//
//        lastYearBiomass = current;

    }
}
