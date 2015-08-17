package uk.ac.ox.oxfish.model.data;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.AbstractMarket;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Aggregate data, yearly. Mostly just sums up what the daily data-set discovered
 * Created by carrknight on 6/29/15.
 */
public class FishStateYearlyTimeSeries extends TimeSeries<FishState>
{

    private final FishStateDailyTimeSeries originalGatherer;

    public FishStateYearlyTimeSeries(
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

        for(Specie specie : observed.getSpecies())
        {
            final String earnings =  specie + " " +AbstractMarket.EARNINGS_COLUMN_NAME;
            final String landings = specie + " " + AbstractMarket.LANDINGS_COLUMN_NAME;
            registerGatherer(landings,
                             columnSummer(originalGatherer.getColumn(
                                     landings))
                    , Double.NaN);
            registerGatherer(earnings,
                             columnSummer(originalGatherer.getColumn(
                                     earnings))
                    , Double.NaN);




        }

        for(Specie specie : observed.getSpecies())
        {
            final String biomass = "Biomass " + specie.getName();
            registerGatherer(biomass,
                             state1 -> originalGatherer.getLatestObservation(biomass)
                    , Double.NaN);
        }

    }

    public Function<FishState, Double> columnSummer(final DataColumn column) {

        return new Function<FishState, Double>() {
            @Override
            public Double apply(FishState state) {
                //get the iterator
                final Iterator<Double> iterator = column.descendingIterator();
                if(!iterator.hasNext()) //not ready/year 1
                    return Double.NaN;
                double sum = 0;
                for(int i=0; i<364; i++)
                    sum += iterator.next();

                return sum;
            }
        };
    }
}
