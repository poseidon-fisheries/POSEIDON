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

public class FixedYearlyPricesBiomassMarket
    extends AbstractBiomassMarket
    implements Steppable {

    private final Map<Integer, Double> yearlyPrices;
    private Integer currentYear;

    public FixedYearlyPricesBiomassMarket(Map<Integer, Double> yearlyPrices) {
        this.yearlyPrices = ImmutableMap.copyOf(yearlyPrices);
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(int currentYear) {
        checkArgument(yearlyPrices.containsKey(currentYear));
        this.currentYear = currentYear;
    }

    @Override
    protected TradeInfo sellFishImplementation(
        double biomass,
        Fisher fisher,
        Regulation regulation,
        FishState fishState,
        Species species
    ) {
        checkArgument(fishState.getCalendarYear() == getCurrentYear());
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

}
