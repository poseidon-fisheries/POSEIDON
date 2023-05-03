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

package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.LogisticRecruitmentProcess;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/8/17.
 */
public class LogisticRecruitmentFactory implements AlgorithmFactory<LogisticRecruitmentProcess> {

    private DoubleParameter carryingCapacity = new FixedDoubleParameter(100000000);

    private DoubleParameter malthusianParameter = new FixedDoubleParameter(.6);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public LogisticRecruitmentProcess apply(final FishState fishState) {
        return new LogisticRecruitmentProcess(carryingCapacity.applyAsDouble(fishState.getRandom()),
            malthusianParameter.applyAsDouble(fishState.getRandom()), false
        );
    }

    public DoubleParameter getCarryingCapacity() {
        return carryingCapacity;
    }

    public void setCarryingCapacity(final DoubleParameter carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    public DoubleParameter getMalthusianParameter() {
        return malthusianParameter;
    }

    public void setMalthusianParameter(final DoubleParameter malthusianParameter) {
        this.malthusianParameter = malthusianParameter;
    }
}
