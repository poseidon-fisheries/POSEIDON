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
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.policymakers.SingleSpeciesBiomassTaxman;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.WeakHashMap;

/**
 * Created by carrknight on 9/30/16.
 */
public class ThresholdSingleSpeciesTaxation implements AlgorithmFactory<ProtectedAreasOnly> {


    private final WeakHashMap<FishState, SingleSpeciesBiomassTaxman> taxes = new WeakHashMap<>();
    private int speciesIndex = 0;
    private DoubleParameter biomassThreshold = new FixedDoubleParameter(5000000);
    private DoubleParameter tax = new FixedDoubleParameter(10);
    private boolean taxWhenBelowThreshold = true;

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public ProtectedAreasOnly apply(final FishState fishState) {
        final ProtectedAreasOnly regulations = new ProtectedAreasOnly();

        if (!taxes.containsKey(fishState)) {
            final SingleSpeciesBiomassTaxman taxman = new SingleSpeciesBiomassTaxman(
                fishState.getSpecies().get(speciesIndex),
                tax.applyAsDouble(fishState.getRandom()),
                biomassThreshold.applyAsDouble(fishState.getRandom()),
                taxWhenBelowThreshold
            );

            fishState.registerStartable(taxman);
            taxes.put(fishState, taxman);
        }

        return regulations;

    }

    /**
     * Getter for property 'speciesIndex'.
     *
     * @return Value for property 'speciesIndex'.
     */
    public int getSpeciesIndex() {
        return speciesIndex;
    }

    /**
     * Setter for property 'speciesIndex'.
     *
     * @param speciesIndex Value to set for property 'speciesIndex'.
     */
    public void setSpeciesIndex(final int speciesIndex) {
        this.speciesIndex = speciesIndex;
    }

    /**
     * Getter for property 'biomassThreshold'.
     *
     * @return Value for property 'biomassThreshold'.
     */
    public DoubleParameter getBiomassThreshold() {
        return biomassThreshold;
    }

    /**
     * Setter for property 'biomassThreshold'.
     *
     * @param biomassThreshold Value to set for property 'biomassThreshold'.
     */
    public void setBiomassThreshold(final DoubleParameter biomassThreshold) {
        this.biomassThreshold = biomassThreshold;
    }

    /**
     * Getter for property 'tax'.
     *
     * @return Value for property 'tax'.
     */
    public DoubleParameter getTax() {
        return tax;
    }

    /**
     * Setter for property 'tax'.
     *
     * @param tax Value to set for property 'tax'.
     */
    public void setTax(final DoubleParameter tax) {
        this.tax = tax;
    }

    /**
     * Getter for property 'taxWhenBelowThreshold'.
     *
     * @return Value for property 'taxWhenBelowThreshold'.
     */
    public boolean isTaxWhenBelowThreshold() {
        return taxWhenBelowThreshold;
    }

    /**
     * Setter for property 'taxWhenBelowThreshold'.
     *
     * @param taxWhenBelowThreshold Value to set for property 'taxWhenBelowThreshold'.
     */
    public void setTaxWhenBelowThreshold(final boolean taxWhenBelowThreshold) {
        this.taxWhenBelowThreshold = taxWhenBelowThreshold;
    }
}
