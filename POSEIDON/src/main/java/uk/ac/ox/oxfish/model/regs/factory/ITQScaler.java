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

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;

/**
 * Created by carrknight on 7/20/17.
 */
class ITQScaler implements Startable {

    private final MultiQuotaRegulation toScale;

    public ITQScaler(MultiQuotaRegulation toScale) {
        this.toScale = toScale;
    }

    @Override
    public void start(FishState model) {
        for (int i = 0; i < model.getSpecies().size(); i++) {
            double availableQuota = toScale.getQuotaRemaining(i);
            if (Double.isFinite(availableQuota))
                toScale.setYearlyQuota(
                    i,
                    availableQuota /
                        (double) model.getNumberOfFishers()
                );
        }

    }

    @Override
    public void turnOff() {

    }

}
