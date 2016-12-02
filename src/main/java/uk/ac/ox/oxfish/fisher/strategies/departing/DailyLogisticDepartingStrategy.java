package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Decides to go out based on a given logistic classifier.
 * If the classifier says no, wait 24 hours before continuing
 * Created by carrknight on 12/2/16.
 */
public class DailyLogisticDepartingStrategy implements DepartingStrategy {


    /**
     * the classifier/logit object that does all the work
     */
    private final LogisticClassifier classifier;


    /**
     * whenever you decide not to go out, increment this by one
     */
    private int daysToWait = 0;


    /**
     * Getter for property 'daysToWait'.
     *
     * @return Value for property 'daysToWait'.
     */
    public int getDaysToWait() {
        return daysToWait;
    }

    public DailyLogisticDepartingStrategy(LogisticClassifier classifier) {
        this.classifier = classifier;
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        //ignored
    }

    @Override
    public void turnOff(Fisher fisher) {
        //ignored
    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     * @param fisher
     * @param model
     * @param random
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(
            Fisher fisher, FishState model, MersenneTwisterFast random) {

        //if you are waiting because you failed a check before, keep waiting
        if(fisher.getHoursAtPort() < daysToWait * 24)
            return false;
        //time for a check!
        boolean check = classifier.test(fisher,model,null,random);
        if(check)
            daysToWait=0;
        else
            daysToWait++;
        return check;
    }

    /**
     * Getter for property 'classifier'.
     *
     * @return Value for property 'classifier'.
     */
    public LogisticClassifier getClassifier() {
        return classifier;
    }
}
