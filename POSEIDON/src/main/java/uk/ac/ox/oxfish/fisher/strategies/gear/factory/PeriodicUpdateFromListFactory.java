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

package uk.ac.ox.oxfish.fisher.strategies.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.strategies.gear.PeriodicUpdateGearStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Present the fisher a fixed portfolio of gears that he can choose from
 * Created by carrknight on 6/13/16.
 */
public class PeriodicUpdateFromListFactory implements AlgorithmFactory<PeriodicUpdateGearStrategy> {

    private List<AlgorithmFactory<? extends Gear>> availableGears = new LinkedList<>();


    private AlgorithmFactory<? extends AdaptationProbability>
        probability = new FixedProbabilityFactory(.2, .6);


    private boolean yearly = true;


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PeriodicUpdateGearStrategy apply(FishState state) {
        List<Gear> options = new LinkedList<>();
        for (AlgorithmFactory<? extends Gear> gearFactory : availableGears)
            options.add(gearFactory.apply(state));

        return new PeriodicUpdateGearStrategy(
            yearly,
            options,
            probability.apply(state)
        );
    }


    /**
     * Getter for property 'availableGears'.
     *
     * @return Value for property 'availableGears'.
     */
    public List<AlgorithmFactory<? extends Gear>> getAvailableGears() {
        return availableGears;
    }

    /**
     * Setter for property 'availableGears'.
     *
     * @param availableGears Value to set for property 'availableGears'.
     */
    public void setAvailableGears(
        List<AlgorithmFactory<? extends Gear>> availableGears
    ) {
        this.availableGears = availableGears;
    }

    /**
     * Getter for property 'probability'.
     *
     * @return Value for property 'probability'.
     */
    public AlgorithmFactory<? extends AdaptationProbability> getProbability() {
        return probability;
    }

    /**
     * Setter for property 'probability'.
     *
     * @param probability Value to set for property 'probability'.
     */
    public void setProbability(
        AlgorithmFactory<? extends AdaptationProbability> probability
    ) {
        this.probability = probability;
    }

    /**
     * Getter for property 'yearly'.
     *
     * @return Value for property 'yearly'.
     */
    public boolean isYearly() {
        return yearly;
    }

    /**
     * Setter for property 'yearly'.
     *
     * @param yearly Value to set for property 'yearly'.
     */
    public void setYearly(boolean yearly) {
        this.yearly = yearly;
    }
}
