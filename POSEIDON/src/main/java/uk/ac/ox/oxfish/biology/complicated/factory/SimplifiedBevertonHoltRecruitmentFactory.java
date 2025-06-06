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

package uk.ac.ox.oxfish.biology.complicated.factory;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentBySpawningBiomass;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.List;

/**
 * this is the formula used in MERA for recruitment where there is no relative fecundity, and "PHI" is just the ratio
 * SSB0/Virgin recruits
 */
public class SimplifiedBevertonHoltRecruitmentFactory implements AlgorithmFactory<RecruitmentBySpawningBiomass> {


    private List<Double> maturity = Lists.newArrayList(.0d, .5d, .1d);


    /**
     * the number of recruits you'd get in a "virgin" state.
     */
    private DoubleParameter virginRecruits = new FixedDoubleParameter(40741397);


    /**
     * logistic growth parameter
     */
    private DoubleParameter steepness = new FixedDoubleParameter(0.6);


    private DoubleParameter spawningStockBiomass = new FixedDoubleParameter(10000);

    @Override
    public RecruitmentBySpawningBiomass apply(final FishState fishState) {
        return new RecruitmentBySpawningBiomass(
            (int) virginRecruits.applyAsDouble(fishState.getRandom()),
            steepness.applyAsDouble(fishState.getRandom()),
            spawningStockBiomass.applyAsDouble(fishState.getRandom()) /
                virginRecruits.applyAsDouble(fishState.getRandom()),
            false,
            Doubles.toArray(maturity),
            null,
            0,
            false
        );


    }

    public List<Double> getMaturity() {
        return maturity;
    }

    public void setMaturity(final List<Double> maturity) {
        this.maturity = maturity;
    }

    public DoubleParameter getVirginRecruits() {
        return virginRecruits;
    }

    public void setVirginRecruits(final DoubleParameter virginRecruits) {
        this.virginRecruits = virginRecruits;
    }

    public DoubleParameter getSteepness() {
        return steepness;
    }

    public void setSteepness(final DoubleParameter steepness) {
        this.steepness = steepness;
    }

    public DoubleParameter getSpawningStockBiomass() {
        return spawningStockBiomass;
    }

    public void setSpawningStockBiomass(final DoubleParameter spawningStockBiomass) {
        this.spawningStockBiomass = spawningStockBiomass;
    }
}
