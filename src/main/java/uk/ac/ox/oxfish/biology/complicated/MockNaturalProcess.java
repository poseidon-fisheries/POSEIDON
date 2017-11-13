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

package uk.ac.ox.oxfish.biology.complicated;

import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * a natural process that actually does nothing
 * Created by carrknight on 3/22/17.
 */
public class MockNaturalProcess extends SingleSpeciesNaturalProcesses {
    public MockNaturalProcess(
            Species species) {
        super(null, null, species, true, null, new NoAbundanceDiffusion() );
    }


    /**
     * schedules itself every year
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
    }

    /**
     * Recruitment + Mortality + Aging + Allocation of new Recruits <br>
     * New recruits are allocated proportional to the areas that have more biomass.
     *
     * @param simState
     */
    @Override
    public void step(SimState simState) {
    }

    /**
     * register this biology so that it can be accessed by recruits and so on
     *
     * @param abundanceBasedLocalBiology
     * @param tile
     */
    @Override
    public void add(AbundanceBasedLocalBiology abundanceBasedLocalBiology, SeaTile tile) {
    }

    @Override
    public double getLastRecruits() {
        return 0;
    }


    /**
     * give a function to generate noise as % of recruits this year
     *
     * @param noiseMaker the function that generates percentage changes. 1 means no noise.
     */
    @Override
    public void addNoise(NoiseMaker noiseMaker) {
    }

    /**
     * Getter for property 'diffuser'.
     *
     * @return Value for property 'diffuser'.
     */
    @Override
    public AbundanceDiffuser getDiffuser() {
        return null;
    }


    /**
     * Getter for property 'recruitsAllocator'.
     *
     * @return Value for property 'recruitsAllocator'.
     */
    @Override
    public BiomassAllocator getRecruitsAllocator() {
        return null;
    }

    /**
     * Setter for property 'recruitsAllocator'.
     *
     * @param recruitsAllocator Value to set for property 'recruitsAllocator'.
     */
    @Override
    public void setRecruitsAllocator(BiomassAllocator recruitsAllocator) {
    }
}
