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

package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.SnapshotBiologyResetter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class SnapshotBiomassResetterFactory implements AlgorithmFactory<SnapshotBiologyResetter> {


    private int yearsToReset = 1;

    @Override
    public SnapshotBiologyResetter apply(FishState state) {
        return SnapshotBiologyResetter.biomassResetter(state.getBiology(), yearsToReset);
    }


    public int getYearsToReset() {
        return yearsToReset;
    }

    public void setYearsToReset(int yearsToReset) {
        this.yearsToReset = yearsToReset;
    }
}
