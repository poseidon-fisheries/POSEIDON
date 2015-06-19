package uk.ac.ox.oxfish.fisher.log;

import com.google.common.base.Preconditions;

/**
 * Holds summary statistics of a trip, specifically how much money was made and how much was spent.
 * The plan is to have a "logbook"-looking data for each trip, but this is far simpler
 * Created by carrknight on 6/17/15.
 */
public class TripRecord {

    /**
     * how long did the trip take
     */
    private int  durationInStep = 0;


    /**
     * set to true if the regulations forced the fisher home earlier.
     */
    private boolean cutShort = false;

    /**
     * is the trip over?
     */
    private boolean completed = false;

    /**
     * total cash earned
     */
    private double totalEarnings = 0;

    /**
     * total costs accrued
     */
    private double totalCosts = 0;


    public TripRecord()
    {
    }


    public void recordEarnings(double newEarnings)
    {
        Preconditions.checkState(!completed);
        totalEarnings += newEarnings;
    }

    public void recordCosts(double newCosts)
    {
        Preconditions.checkState(!completed);
        totalCosts += newCosts;
    }

    public void recordTripCutShort(){
        Preconditions.checkState(!completed);
        cutShort = true;
    }

    public void completeTrip(int durationInStep)
    {
        this.durationInStep = durationInStep;
        completed = true;
    }

    /**
     * return profit/step; an easy way to compare trip records
     * @return profits/days
     */
    public double getProfitPerStep()
    {
        return (totalEarnings - totalCosts) / durationInStep;
    }

    public boolean isCutShort() {
        return cutShort;
    }

    public boolean isCompleted() {
        return completed;
    }
}