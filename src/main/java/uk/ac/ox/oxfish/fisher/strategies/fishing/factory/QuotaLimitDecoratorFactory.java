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

package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.QuotaLimitDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 7/27/17.
 */
public class QuotaLimitDecoratorFactory implements AlgorithmFactory<QuotaLimitDecorator>{

    private AlgorithmFactory<? extends FishingStrategy> decorated =
            new TowLimitFactory();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public QuotaLimitDecorator apply(FishState state) {
        return new QuotaLimitDecorator(decorated.apply(state));
    }

    /**
     * Getter for property 'decorated'.
     *
     * @return Value for property 'decorated'.
     */
    public AlgorithmFactory<? extends FishingStrategy> getDecorated() {
        return decorated;
    }

    /**
     * Setter for property 'decorated'.
     *
     * @param decorated Value to set for property 'decorated'.
     */
    public void setDecorated(
            AlgorithmFactory<? extends FishingStrategy> decorated) {
        this.decorated = decorated;
    }
}
