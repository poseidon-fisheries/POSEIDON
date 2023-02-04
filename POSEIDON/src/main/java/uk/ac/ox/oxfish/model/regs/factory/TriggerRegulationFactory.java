/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.TriggerRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class TriggerRegulationFactory implements AlgorithmFactory<TriggerRegulation> {


    private DoubleParameter lowThreshold = new FixedDoubleParameter(1000000);

    private DoubleParameter highThreshold = new FixedDoubleParameter(3000000);;

    private String indicatorName = "Biomass Species 0";

    private AlgorithmFactory<? extends Regulation>
            businessAsUsual = new AnarchyFactory();

    private AlgorithmFactory<? extends Regulation> emergency =
            new FishingSeasonFactory(50,true);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public TriggerRegulation apply(FishState fishState) {
        return new TriggerRegulation(
                lowThreshold.apply(fishState.getRandom()),
                highThreshold.apply(fishState.getRandom()),
                indicatorName,
                businessAsUsual.apply(fishState),
                emergency.apply(fishState)
        );
    }

    /**
     * Getter for property 'lowThreshold'.
     *
     * @return Value for property 'lowThreshold'.
     */
    public DoubleParameter getLowThreshold() {
        return lowThreshold;
    }

    /**
     * Setter for property 'lowThreshold'.
     *
     * @param lowThreshold Value to set for property 'lowThreshold'.
     */
    public void setLowThreshold(DoubleParameter lowThreshold) {
        this.lowThreshold = lowThreshold;
    }

    /**
     * Getter for property 'highThreshold'.
     *
     * @return Value for property 'highThreshold'.
     */
    public DoubleParameter getHighThreshold() {
        return highThreshold;
    }

    /**
     * Setter for property 'highThreshold'.
     *
     * @param highThreshold Value to set for property 'highThreshold'.
     */
    public void setHighThreshold(DoubleParameter highThreshold) {
        this.highThreshold = highThreshold;
    }

    /**
     * Getter for property 'indicatorName'.
     *
     * @return Value for property 'indicatorName'.
     */
    public String getIndicatorName() {
        return indicatorName;
    }

    /**
     * Setter for property 'indicatorName'.
     *
     * @param indicatorName Value to set for property 'indicatorName'.
     */
    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    /**
     * Getter for property 'businessAsUsual'.
     *
     * @return Value for property 'businessAsUsual'.
     */
    public AlgorithmFactory<? extends Regulation> getBusinessAsUsual() {
        return businessAsUsual;
    }

    /**
     * Setter for property 'businessAsUsual'.
     *
     * @param businessAsUsual Value to set for property 'businessAsUsual'.
     */
    public void setBusinessAsUsual(
            AlgorithmFactory<? extends Regulation> businessAsUsual) {
        this.businessAsUsual = businessAsUsual;
    }

    /**
     * Getter for property 'emergency'.
     *
     * @return Value for property 'emergency'.
     */
    public AlgorithmFactory<? extends Regulation> getEmergency() {
        return emergency;
    }

    /**
     * Setter for property 'emergency'.
     *
     * @param emergency Value to set for property 'emergency'.
     */
    public void setEmergency(AlgorithmFactory<? extends Regulation> emergency) {
        this.emergency = emergency;
    }
}
