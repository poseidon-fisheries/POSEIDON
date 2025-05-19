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

package uk.ac.ox.oxfish.model.market;

import com.google.common.collect.ImmutableMap;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Comparator.naturalOrder;

public class FixedYearlyPricesBiomassMarket
    extends AbstractBiomassMarket
    implements Steppable {

    private static final long serialVersionUID = -5976663174160387350L;
    private final Map<Integer, Double> yearlyPrices;
    private final int lastYear;
    private boolean defaultingToLastYear = true;
    private Integer currentYear;

    public FixedYearlyPricesBiomassMarket(final Map<Integer, Double> yearlyPrices) {
        this.yearlyPrices = ImmutableMap.copyOf(yearlyPrices);
        this.lastYear = yearlyPrices.keySet().stream()
            .max(naturalOrder())
            .orElseThrow(() -> new IllegalStateException("No prices defined."));
    }

    @Override
    protected TradeInfo sellFishImplementation(
        final double biomass,
        final Fisher fisher,
        final Regulation regulation,
        final FishState fishState,
        final Species species
    ) {
        if (defaultingToLastYear) {
            checkArgument(
                fishState.getCalendarYear() == getCurrentYear() ||
                    (getCurrentYear() == lastYear &&
                        fishState.getCalendarYear() > getCurrentYear())
            );
        } else {
            checkArgument(fishState.getCalendarYear() == getCurrentYear());
        }
        return Market.defaultMarketTransaction(
            biomass,
            fisher,
            regulation,
            fishState,
            biomassTraded -> biomassTraded * getMarginalPrice(),
            species
        );
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(final int currentYear) {
        if (yearlyPrices.containsKey(currentYear)) {
            this.currentYear = currentYear;
        } else if (defaultingToLastYear) {
            this.currentYear = lastYear;
        } else {
            throw new IllegalArgumentException("No prices defined for year " + currentYear);
        }
    }

    @Override
    public double getMarginalPrice() {
        checkNotNull(currentYear, "Current year not set.");
        return yearlyPrices.get(currentYear);
    }

    @Override
    public void step(final SimState simState) {
        final FishState fishState = (FishState) simState;
        setCurrentYear(fishState.getCalendarYear());
    }

    @Override
    public void start(final FishState fishState) {
        if (!isStarted()) {
            super.start(fishState);
            fishState.scheduleOnce(this, StepOrder.DAWN);
            fishState.scheduleOnce(
                simState -> ((FishState) simState).scheduleEveryXDay(this, StepOrder.DAWN, 365),
                StepOrder.DAWN
            );
        }
    }

    @SuppressWarnings("unused")
    public boolean isDefaultingToLastYear() {
        return defaultingToLastYear;
    }

    @SuppressWarnings("unused")
    public void setDefaultingToLastYear(final boolean defaultingToLastYear) {
        this.defaultingToLastYear = defaultingToLastYear;
    }
}
