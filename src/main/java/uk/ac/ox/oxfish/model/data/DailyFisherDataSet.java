package uk.ac.ox.oxfish.model.data;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Dataset for each fisher being updated once a day
 * Created by carrknight on 8/4/15.
 */
public class DailyFisherDataSet extends DataSet<Fisher> {


    public static final String CASH_COLUMN = YearlyFisherDataSet.CASH_COLUMN;

    public DailyFisherDataSet() {
        super(IntervalPolicy.EVERY_DAY);
    }

    /**
     * call this to start the observation
     *
     * @param state    model
     * @param observed the object to observe
     */
    @Override
    public void start(FishState state, Fisher observed) {

        registerGatherer(CASH_COLUMN,Fisher::getBankBalance, Double.NaN);
        super.start(state, observed);


    }
}
