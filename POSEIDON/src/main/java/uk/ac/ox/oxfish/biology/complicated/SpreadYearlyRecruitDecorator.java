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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.LinkedHashMap;

import static uk.ac.ox.oxfish.biology.complicated.YearlyRecruitmentProcess.YEARLY_RECRUITMENT_SPAWNING_DAY;

/**
 * takes a yearly recruitment object but calls it on specific days and shocks its
 * result to add noise
 */
public class SpreadYearlyRecruitDecorator implements RecruitmentProcess {


    /**
     * imagine this needs to spawn twice a year at day 100 and day 200, this would be:
     * 100 -- fixedDoubleParameter 0.5
     * 200 -- fixedDoubleParmater 0.5
     */
    private final LinkedHashMap<Integer, DoubleParameter> spawningDayToProportion;

    /**
     * the formula that actually computes yearly recruits
     */
    private final YearlyRecruitmentProcess delegate;


    private final MersenneTwisterFast random;


    public SpreadYearlyRecruitDecorator(
        final LinkedHashMap<Integer, DoubleParameter> spawningDayToProportion,
        final YearlyRecruitmentProcess delegate, final MersenneTwisterFast random
    ) {
        this.spawningDayToProportion = spawningDayToProportion;
        this.delegate = delegate;
        this.random = random;
        Preconditions.checkArgument(
            !delegate.isRecruitEveryday(),
            "This decorator assumes the formula is not already divided by 365"
        );
        //todo this wouldn't be too difficult to fix. just multiply by 365/daysSimulated

    }


    @Override
    public double recruit(
        final Species species, final Meristics meristics,
        final StructuredAbundance abundance,
        final int dayOfTheYear,
        final int daysSimulated
    ) {
        Preconditions.checkArgument(daysSimulated == 1);

        //not a spawning day
        if (!spawningDayToProportion.containsKey(dayOfTheYear))
            return 0d;
        else {
            //spawning day
            final DoubleParameter scaling = spawningDayToProportion.get(dayOfTheYear);


            return scaling.applyAsDouble(random) * delegate.recruit(
                species, meristics, abundance,
                YEARLY_RECRUITMENT_SPAWNING_DAY,
                daysSimulated
            );

        }

    }


    public MersenneTwisterFast getRandom() {
        return random;
    }

    @Override
    public void addNoise(final NoiseMaker noiseMaker) {
        delegate.addNoise(noiseMaker);
    }

    public LinkedHashMap<Integer, DoubleParameter> getSpawningDayToProportion() {
        return spawningDayToProportion;
    }

    public YearlyRecruitmentProcess getDelegate() {
        return delegate;
    }
}
