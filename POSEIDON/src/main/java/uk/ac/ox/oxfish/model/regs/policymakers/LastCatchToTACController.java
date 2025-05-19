/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.regs.policymakers;

import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.UnchangingPastSensor;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * reads last year catches, multiplies it and uses as TAC forever
 */
public class LastCatchToTACController implements AlgorithmFactory<AdditionalStartable> {

    private DoubleParameter catchesToTargetMultiplier = new FixedDoubleParameter(1);

    /**
     * does the quota affect all species, or is it specific to one? (notice that when that species quota is over, the whole fishery closes)
     */
    private String targetedSpecies = "";


    private String catchColumnName = "Species 0 Landings";

    private int startingYear = 10;


    @Override
    public AdditionalStartable apply(FishState fishState) {

        return model -> fishState.scheduleOnceInXDays(
            (Steppable) simState -> {
                TargetToTACController controller;
                if (targetedSpecies.trim().isEmpty())
                    controller = new TargetToTACController(
                        new UnchangingPastSensor(
                            catchColumnName,
                            catchesToTargetMultiplier.applyAsDouble(fishState.getRandom()),
                            1
                        ),
                        365
                    );
                else
                    controller = new TargetToTACController(
                        new UnchangingPastSensor(
                            catchColumnName,
                            catchesToTargetMultiplier.applyAsDouble(fishState.getRandom()),
                            1
                        ),
                        365,
                        targetedSpecies
                    );
                controller.start(model);
                controller.step(model);
            },
            StepOrder.DAWN,
            365 * startingYear + 1
        );

    }

    public DoubleParameter getCatchesToTargetMultiplier() {
        return catchesToTargetMultiplier;
    }

    public void setCatchesToTargetMultiplier(DoubleParameter catchesToTargetMultiplier) {
        this.catchesToTargetMultiplier = catchesToTargetMultiplier;
    }

    public String getCatchColumnName() {
        return catchColumnName;
    }

    public void setCatchColumnName(String catchColumnName) {
        this.catchColumnName = catchColumnName;
    }

    public int getStartingYear() {
        return startingYear;
    }

    public void setStartingYear(int startingYear) {
        this.startingYear = startingYear;
    }

    public String getTargetedSpecies() {
        return targetedSpecies;
    }

    public void setTargetedSpecies(String targetedSpecies) {
        this.targetedSpecies = targetedSpecies;
    }
}
