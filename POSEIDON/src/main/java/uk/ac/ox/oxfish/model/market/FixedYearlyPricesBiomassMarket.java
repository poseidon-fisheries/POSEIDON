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

    private final Map<Integer, Double> yearlyPrices;
    private final int lastYear;
    private boolean defaultingToLastYear = true;
    private Integer currentYear;

    public FixedYearlyPricesBiomassMarket(Map<Integer, Double> yearlyPrices) {
        this.yearlyPrices = ImmutableMap.copyOf(yearlyPrices);
        this.lastYear = yearlyPrices.keySet().stream()
            .max(naturalOrder())
            .orElseThrow(() -> new IllegalStateException("No prices defined."));
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(int currentYear) {
        if (yearlyPrices.containsKey(currentYear)) {
            this.currentYear = currentYear;
        } else if (defaultingToLastYear) {
            this.currentYear = lastYear;
        } else {
            throw new IllegalArgumentException("No prices defined for year " + currentYear);
        }
    }

    @Override
    protected TradeInfo sellFishImplementation(
        double biomass,
        Fisher fisher,
        Regulation regulation,
        FishState fishState,
        Species species
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

    @Override
    public double getMarginalPrice() {
        checkNotNull(currentYear, "Current year not set.");
        return yearlyPrices.get(currentYear);
    }

    @Override
    public void step(SimState simState) {
        final FishState fishState = (FishState) simState;
        setCurrentYear(fishState.getCalendarYear());
    }

    @Override
    public void start(FishState fishState) {
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
    public void setDefaultingToLastYear(boolean defaultingToLastYear) {
        this.defaultingToLastYear = defaultingToLastYear;
    }
}
