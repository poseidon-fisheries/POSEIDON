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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.SelectivityAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticAbundanceFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.RetentionAbundanceFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NullParameter;

/**
 * This is a gear that applies the same Logistic selectivity and Logistic Retention to every species.
 * The default numbers refer to the thornyhead gear
 * Created by carrknight on 3/18/16.
 */
public class LogisticSelectivityGearFactory implements HomogeneousGearFactory {



    private boolean rounding = true;
    /**
     * the selectivity parameter A for the logistic
     */
    private DoubleParameter selectivityAParameter = new FixedDoubleParameter(23.5035);

    /**
     * the selectivity parameter B for the logistic
     */
    private DoubleParameter selectivityBParameter = new FixedDoubleParameter(9.03702);

    /**
     * retention inflection parameter
     */
    private DoubleParameter retentionInflection = new NullParameter();


    /**
     * retention slope parameter
     */
    private DoubleParameter retentionSlope = new NullParameter();


    /**
     * retention slope parameter: if null retention is ignored
     */
    private DoubleParameter retentionAsymptote = new NullParameter();


    private DoubleParameter litersOfGasConsumedPerHour = new FixedDoubleParameter(0);

    private DoubleParameter averageCatchability = new FixedDoubleParameter(0);

    private boolean logBaseTen = true;


    public LogisticSelectivityGearFactory() {
    }

    public LogisticSelectivityGearFactory(
            Double selectivityAParameter, Double selectivityBParameter,
            Double retentionInflection, Double retentionSlope,
            Double retentionAsymptote,
            Double litersOfGasConsumedPerHour,
            Double averageCatchability) {
        this.selectivityAParameter = new FixedDoubleParameter(selectivityAParameter);
        this.selectivityBParameter = new FixedDoubleParameter(selectivityBParameter);
        this.retentionInflection = new FixedDoubleParameter(retentionInflection);
        this.retentionSlope = new FixedDoubleParameter(retentionSlope);
        this.retentionAsymptote = new FixedDoubleParameter(retentionAsymptote);
        this.litersOfGasConsumedPerHour = new FixedDoubleParameter(litersOfGasConsumedPerHour);
        this.averageCatchability = new FixedDoubleParameter(averageCatchability);
    }

    public LogisticSelectivityGearFactory(
            Double selectivityAParameter, Double selectivityBParameter,
            Double litersOfGasConsumedPerHour,
            Double averageCatchability) {
        this.selectivityAParameter = new FixedDoubleParameter(selectivityAParameter);
        this.selectivityBParameter = new FixedDoubleParameter(selectivityBParameter);
        this.retentionInflection = null;
        this.retentionSlope = null;
        this.retentionAsymptote = null;
        this.litersOfGasConsumedPerHour = new FixedDoubleParameter(litersOfGasConsumedPerHour);
        this.averageCatchability = new FixedDoubleParameter(averageCatchability);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public HomogeneousAbundanceGear apply(FishState fishState) {
        MersenneTwisterFast random = fishState.getRandom();
        if(retentionAsymptote != null &&
                !(retentionAsymptote instanceof NullParameter) &&
                retentionSlope != null &&
                !(retentionSlope instanceof NullParameter) &&
                retentionInflection != null &&
                !(retentionInflection instanceof NullParameter)
                ) {
            return new SelectivityAbundanceGear(litersOfGasConsumedPerHour.apply(random),
                                                new FixedProportionFilter(averageCatchability.apply(random), rounding),
                                                new LogisticAbundanceFilter(selectivityAParameter.apply(random),
                                                                            selectivityBParameter.apply(random),
                                                                            true, rounding, logBaseTen),
                                                new RetentionAbundanceFilter(true,
                                                                             retentionInflection.apply(random),
                                                                             retentionSlope.apply(random),
                                                                             retentionAsymptote.apply(random), rounding)
            );
        }
        else
        {
            return new SelectivityAbundanceGear(litersOfGasConsumedPerHour.apply(random),
                                                new FixedProportionFilter(averageCatchability.apply(random), rounding),
                                                new LogisticAbundanceFilter(selectivityAParameter.apply(random),
                                                                            selectivityBParameter.apply(random),
                                                                            true, rounding, logBaseTen));
        }
    }


    /**
     * Getter for property 'selectivityAParameter'.
     *
     * @return Value for property 'selectivityAParameter'.
     */
    public DoubleParameter getSelectivityAParameter() {
        return selectivityAParameter;
    }

    /**
     * Setter for property 'selectivityAParameter'.
     *
     * @param selectivityAParameter Value to set for property 'selectivityAParameter'.
     */
    public void setSelectivityAParameter(DoubleParameter selectivityAParameter) {
        this.selectivityAParameter = selectivityAParameter;
    }

    /**
     * Getter for property 'selectivityBParameter'.
     *
     * @return Value for property 'selectivityBParameter'.
     */
    public DoubleParameter getSelectivityBParameter() {
        return selectivityBParameter;
    }

    /**
     * Setter for property 'selectivityBParameter'.
     *
     * @param selectivityBParameter Value to set for property 'selectivityBParameter'.
     */
    public void setSelectivityBParameter(DoubleParameter selectivityBParameter) {
        this.selectivityBParameter = selectivityBParameter;
    }

    /**
     * Getter for property 'retentionInflection'.
     *
     * @return Value for property 'retentionInflection'.
     */
    public DoubleParameter getRetentionInflection() {
        return retentionInflection;
    }

    /**
     * Setter for property 'retentionInflection'.
     *
     * @param retentionInflection Value to set for property 'retentionInflection'.
     */
    public void setRetentionInflection(DoubleParameter retentionInflection) {
        this.retentionInflection = retentionInflection;
    }

    /**
     * Getter for property 'retentionSlope'.
     *
     * @return Value for property 'retentionSlope'.
     */
    public DoubleParameter getRetentionSlope() {
        return retentionSlope;
    }

    /**
     * Setter for property 'retentionSlope'.
     *
     * @param retentionSlope Value to set for property 'retentionSlope'.
     */
    public void setRetentionSlope(DoubleParameter retentionSlope) {
        this.retentionSlope = retentionSlope;
    }

    /**
     * Getter for property 'retentionAsymptote'.
     *
     * @return Value for property 'retentionAsymptote'.
     */
    public DoubleParameter getRetentionAsymptote() {
        return retentionAsymptote;
    }

    /**
     * Setter for property 'retentionAsymptote'.
     *
     * @param retentionAsymptote Value to set for property 'retentionAsymptote'.
     */
    public void setRetentionAsymptote(DoubleParameter retentionAsymptote) {
        this.retentionAsymptote = retentionAsymptote;
    }

    /**
     * Getter for property 'litersOfGasConsumedPerHour'.
     *
     * @return Value for property 'litersOfGasConsumedPerHour'.
     */
    public DoubleParameter getLitersOfGasConsumedPerHour() {
        return litersOfGasConsumedPerHour;
    }

    /**
     * Setter for property 'litersOfGasConsumedPerHour'.
     *
     * @param litersOfGasConsumedPerHour Value to set for property 'litersOfGasConsumedPerHour'.
     */
    public void setLitersOfGasConsumedPerHour(
            DoubleParameter litersOfGasConsumedPerHour) {
        this.litersOfGasConsumedPerHour = litersOfGasConsumedPerHour;
    }

    /**
     * Getter for property 'averageCatchability'.
     *
     * @return Value for property 'averageCatchability'.
     */
    public DoubleParameter getAverageCatchability() {
        return averageCatchability;
    }

    /**
     * Setter for property 'averageCatchability'.
     *
     * @param averageCatchability Value to set for property 'averageCatchability'.
     */
    public void setAverageCatchability(DoubleParameter averageCatchability) {
        this.averageCatchability = averageCatchability;
    }


    /**
     * Getter for property 'rounding'.
     *
     * @return Value for property 'rounding'.
     */
    public boolean isRounding() {
        return rounding;
    }

    /**
     * Setter for property 'rounding'.
     *
     * @param rounding Value to set for property 'rounding'.
     */
    public void setRounding(boolean rounding) {
        this.rounding = rounding;
    }

    public boolean isLogBaseTen() {
        return logBaseTen;
    }

    public void setLogBaseTen(boolean logBaseTen) {
        this.logBaseTen = logBaseTen;
    }
}
