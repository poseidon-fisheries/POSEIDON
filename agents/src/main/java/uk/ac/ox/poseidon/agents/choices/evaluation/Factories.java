/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.choices.evaluation;

import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.choices.MutableOptionValues;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;

public final class Factories {

    private Factories() {
    }

    public static ProfitPerHourDestinationEvaluationProviderFactory profitPerHour(
        final String currencyCode
    ) {
        return new ProfitPerHourDestinationEvaluationProviderFactory(currencyCode);
    }

    public static TotalBiomassCaughtPerHourDestinationEvaluationProviderFactory totalBiomassCaughtPerHour() {
        return new TotalBiomassCaughtPerHourDestinationEvaluationProviderFactory();
    }

    public static TripEvaluatorFactory tripEvaluator(
        final Factory<? super VesselScope, ? extends MutableOptionValues<Int2D>> optionValues,
        final Factory<? super VesselScope, ? extends EvaluationProvider<Int2D>> evaluationProvider
    ) {
        return new TripEvaluatorFactory(optionValues, evaluationProvider);
    }
}
