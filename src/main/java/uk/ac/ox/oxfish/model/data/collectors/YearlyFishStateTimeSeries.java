package uk.ac.ox.oxfish.model.data.collectors;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Aggregate data, yearly. Mostly just sums up what the daily data-set discovered
 * Created by carrknight on 6/29/15.
 */
public class YearlyFishStateTimeSeries extends TimeSeries<FishState>
{

    private final FishStateDailyTimeSeries originalGatherer;

    public YearlyFishStateTimeSeries(
            FishStateDailyTimeSeries originalGatherer) {
        super(IntervalPolicy.EVERY_YEAR, StepOrder.AGGREGATE_DATA_GATHERING);
        this.originalGatherer = originalGatherer;
    }


    /**
     * call this to start the observation
     *
     * @param state    model
     * @param observed the object to observe
     */
    @Override
    public void start(FishState state, FishState observed) {
        super.start(state, observed);


        final String fuel = YearlyFisherTimeSeries.FUEL_CONSUMPTION;
        registerGatherer(fuel, state1 -> {
            Double sum = 0d;
            for(Fisher fisher : state1.getFishers())
                sum += fisher.getYearlyData().getColumn(fuel).getLatest();

            return sum;
        },Double.NaN);


        for(Species species : observed.getSpecies())
        {

            final String earnings =  species + " " +AbstractMarket.EARNINGS_COLUMN_NAME;
            final String landings = species + " " + AbstractMarket.LANDINGS_COLUMN_NAME;
            registerGatherer(landings,
                             FishStateUtilities.generateYearlySum(originalGatherer.getColumn(
                                     landings))
                    , Double.NaN);
            registerGatherer(earnings,
                             FishStateUtilities.generateYearlySum(originalGatherer.getColumn(
                                     earnings))
                    , Double.NaN);




        }

        for(Species species : observed.getSpecies())
        {
            final String biomass = "Biomass " + species.getName();
            registerGatherer(biomass,
                             state1 -> originalGatherer.getLatestObservation(biomass)
                    , Double.NaN);
        }

        registerGatherer("Average Cash-Flow", ignored -> observed.getFishers().stream().mapToDouble(
                value -> value.getLatestYearlyObservation(YearlyFisherTimeSeries.CASH_FLOW_COLUMN)).sum()/
                observed.getFishers().size(), 0d);


        registerGatherer("Average Distance From Port", ignored -> observed.getFishers().stream().mapToDouble(
                value -> value.getLatestYearlyObservation(YearlyFisherTimeSeries.FISHING_DISTANCE)).filter(Double::isFinite).sum()/
                observed.getFishers().size(), 0d);

        registerGatherer("Average Number of Trips", ignored -> observed.getFishers().stream().mapToDouble(
                value -> value.getLatestYearlyObservation(YearlyFisherTimeSeries.TRIPS)).sum()/
                observed.getFishers().size(), 0d);

        registerGatherer("Average Gas Expenditure", ignored -> observed.getFishers().stream().mapToDouble(
                value -> value.getLatestYearlyObservation(YearlyFisherTimeSeries.FUEL_EXPENDITURE)).filter(Double::isFinite).sum()/
                observed.getFishers().size(), 0d);

        registerGatherer("Average Trip Duration", ignored -> observed.getFishers().stream().mapToDouble(
                value -> value.getLatestYearlyObservation(YearlyFisherTimeSeries.TRIP_DURATION)).filter(Double::isFinite).sum()/
                observed.getFishers().size(), 0d);

    }



}
