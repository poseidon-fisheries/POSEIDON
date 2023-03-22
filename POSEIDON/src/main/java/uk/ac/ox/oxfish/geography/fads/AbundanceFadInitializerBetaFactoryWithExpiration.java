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

import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Supplier;

public class AbundanceFadInitializerBetaFactoryWithExpiration
        extends AbundanceFadInitializerBetaFactory {

    private DoubleParameter daysOfFadActivity = new FixedDoubleParameter(50);

    public AbundanceFadInitializerBetaFactoryWithExpiration() {
    }

    public AbundanceFadInitializerBetaFactoryWithExpiration(
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final String... speciesNames
    ) {
        super(speciesCodesSupplier, speciesNames);
    }

    @Override
    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(
            FishState fishState) {
        return new ExpirationDecoratorFadInitializer<>(
                daysOfFadActivity.apply(fishState.getRandom()).intValue(),
                super.apply(fishState));
    }

    public DoubleParameter getDaysOfFadActivity() {
        return daysOfFadActivity;
    }

    public void setDaysOfFadActivity(DoubleParameter daysOfFadActivity) {
        this.daysOfFadActivity = daysOfFadActivity;
    }
}
