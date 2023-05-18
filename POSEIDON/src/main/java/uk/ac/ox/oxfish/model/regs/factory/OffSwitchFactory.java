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

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.OffSwitchDecorator;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class OffSwitchFactory implements AlgorithmFactory<OffSwitchDecorator> {


    private AlgorithmFactory<? extends Regulation> delegate = new AnarchyFactory();


    private boolean startsOff = false;


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public OffSwitchDecorator apply(FishState fishState) {
        return new OffSwitchDecorator(
            delegate.apply(fishState),
            startsOff
        );
    }

    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public AlgorithmFactory<? extends Regulation> getDelegate() {
        return delegate;
    }

    /**
     * Setter for property 'delegate'.
     *
     * @param delegate Value to set for property 'delegate'.
     */
    public void setDelegate(AlgorithmFactory<? extends Regulation> delegate) {
        this.delegate = delegate;
    }


    /**
     * Getter for property 'startsOff'.
     *
     * @return Value for property 'startsOff'.
     */
    public boolean isStartsOff() {
        return startsOff;
    }

    /**
     * Setter for property 'startsOff'.
     *
     * @param startsOff Value to set for property 'startsOff'.
     */
    public void setStartsOff(boolean startsOff) {
        this.startsOff = startsOff;
    }
}
