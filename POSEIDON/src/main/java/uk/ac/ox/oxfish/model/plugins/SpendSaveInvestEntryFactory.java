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

package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class SpendSaveInvestEntryFactory implements AlgorithmFactory<SpendSaveInvestEntry> {

    private DoubleParameter moneyNeededForANewEntry = new FixedDoubleParameter(25000000);

    private DoubleParameter yearlyExpenses = new FixedDoubleParameter(5000000);


    /**
     * which population of boats are we growing; this has to be both the name of the fishery factory and a tag so that we know
     * which boats belong to it
     */
    private String populationName = "population0";


    @Override
    public SpendSaveInvestEntry apply(final FishState fishState) {
        return
            new SpendSaveInvestEntry(
                moneyNeededForANewEntry.applyAsDouble(fishState.getRandom()),
                yearlyExpenses.applyAsDouble(fishState.getRandom()),
                populationName
            );
    }

    public DoubleParameter getMoneyNeededForANewEntry() {
        return moneyNeededForANewEntry;
    }

    public void setMoneyNeededForANewEntry(final DoubleParameter moneyNeededForANewEntry) {
        this.moneyNeededForANewEntry = moneyNeededForANewEntry;
    }

    public DoubleParameter getYearlyExpenses() {
        return yearlyExpenses;
    }

    public void setYearlyExpenses(final DoubleParameter yearlyExpenses) {
        this.yearlyExpenses = yearlyExpenses;
    }

    public String getPopulationName() {
        return populationName;
    }

    public void setPopulationName(final String populationName) {
        this.populationName = populationName;
    }
}
