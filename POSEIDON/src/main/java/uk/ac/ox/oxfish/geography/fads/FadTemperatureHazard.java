/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.geography.fads;

import com.google.common.base.Supplier;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.function.Consumer;

public class FadTemperatureHazard implements AdditionalStartable, Steppable {

    private final int minimumDaysBeforeHazardCanTakePlace;

    private final double valueBelowWhichHazardHappens;

    private final double hazardProbability;

    private final String nameOfMapToCheck;

    public FadTemperatureHazard(
            int minimumDaysBeforeHazardCanTakePlace,
            double valueBelowWhichHazardHappens,
            double hazardProbability,
            String nameOfMapToCheck) {
        this.minimumDaysBeforeHazardCanTakePlace = minimumDaysBeforeHazardCanTakePlace;
        this.valueBelowWhichHazardHappens = valueBelowWhichHazardHappens;
        this.hazardProbability = hazardProbability;
        this.nameOfMapToCheck = nameOfMapToCheck;
    }

    private Stoppable stoppable;

    @Override
    public void start(FishState model) {
        stoppable = model.scheduleEveryDay(this, StepOrder.POLICY_UPDATE);
    }

    @Override
    public void step(SimState simState) {

        FishState model = (FishState) simState;
        DoubleGrid2D temperatureMap =
                model.getMap().getAdditionalMaps().get(nameOfMapToCheck).get();

        model.getFadMap().allFads().forEach(
                (Consumer<AbstractFad<? extends LocalBiology, ? extends AbstractFad<?, ?>>>) abstractFad -> {

                    //if it is active and in the water long enough
                    if(abstractFad.isActive() &&
                            abstractFad.getStepDeployed() >= minimumDaysBeforeHazardCanTakePlace
                    ){
                        //if it is too cold
                        SeaTile location = abstractFad.getLocation();
                        if(temperatureMap.get(location.getGridX(),
                                              location.getGridY()) <= valueBelowWhichHazardHappens &&
                                //you have a chance
                                ((FishState) simState).getRandom().nextDouble() <= hazardProbability
                        )
                            //to lose the fish!
                            abstractFad.releaseFish(((FishState) simState).getSpecies());
                    }

                }
        );


    }

    @Override
    public void turnOff() {
        if(stoppable!=null)
            stoppable.stop();
    }
}
