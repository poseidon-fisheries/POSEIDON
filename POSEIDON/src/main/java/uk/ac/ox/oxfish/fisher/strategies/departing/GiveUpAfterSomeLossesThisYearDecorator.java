package uk.ac.ox.oxfish.fisher.strategies.departing;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import static uk.ac.ox.oxfish.fisher.strategies.departing.FullSeasonalRetiredDecorator.SEASONALITY_VARIABLE_NAME;

/**
 * decorator: a boat that repeatedly makes losses within a year will give up until the beginning of the next
 */
public class GiveUpAfterSomeLossesThisYearDecorator implements DepartingStrategy, TripListener {

    private final int howManyBadTripsBeforeGivingUp;

    private int badTrips = 0;

    private final double minimumProfitPerTripRequired;

    private boolean givenUp = false;

    private final DepartingStrategy delegate;

    private Fisher fisherIAmConnectedTo;

    private boolean disabled = false;

    public GiveUpAfterSomeLossesThisYearDecorator(int howManyBadTripsBeforeGivingUp,
                                                  double minimumProfitPerTripRequired,
                                                  DepartingStrategy delegate) {
        this.howManyBadTripsBeforeGivingUp = howManyBadTripsBeforeGivingUp;
        this.minimumProfitPerTripRequired = minimumProfitPerTripRequired;
        this.delegate = delegate;
    }

    @Override
    public boolean shouldFisherLeavePort(Fisher fisher, FishState model, MersenneTwisterFast random) {


        return  (disabled || !givenUp) && delegate.shouldFisherLeavePort(fisher, model, random);
    }


    private Stoppable receipt = null;

    @Override
    public void start(FishState model, Fisher fisher) {

        this.fisherIAmConnectedTo = fisher;
        this.fisherIAmConnectedTo.addTripListener(this);

        receipt = model.scheduleEveryYear(
                new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        reset();
                    }
                },
                StepOrder.DAWN
        );
        delegate.start(model, fisher);
    }

    @Override
    public void turnOff(Fisher fisher) {
        this.fisherIAmConnectedTo.removeTripListener(this);
        this.fisherIAmConnectedTo = null;
        if(this.receipt!=null)
            receipt.stop();
        delegate.turnOff(fisher);
    }

    @Override
    public void reactToFinishedTrip(TripRecord record, Fisher fisher) {
        if(disabled)
            return;

        //if you have given up you should have done no trips, actually.
        //however it is possible that something forced your hand
        assert !givenUp;
        Preconditions.checkArgument(fisher==fisherIAmConnectedTo,
                "Listening to multiple fishers is not allowed by this decorator");

        if(record.getTotalTripProfit()<minimumProfitPerTripRequired)
            badTrips++;
        if(badTrips>=howManyBadTripsBeforeGivingUp) {
            givenUp = true;
            this.fisherIAmConnectedTo.getAdditionalVariables().put(SEASONALITY_VARIABLE_NAME,EffortStatus.RETIRED);
        }



    }

    /**
     * stops giving up!
     */
    public void reset(){
//        if(givenUp==true)
//            this.fisherIAmConnectedTo.getAdditionalVariables().put(SEASONALITY_VARIABLE_NAME,null);

        givenUp = false;
        badTrips = 0;
    }

    /**
     * basically stop stopping the fisher
     */
    public void disable(){
        disabled=true;
    }


    public int getHowManyBadTripsBeforeGivingUp() {
        return howManyBadTripsBeforeGivingUp;
    }

    public int getBadTrips() {
        return badTrips;
    }

    public double getMinimumProfitPerTripRequired() {
        return minimumProfitPerTripRequired;
    }

    public boolean isGivenUp() {
        return givenUp;
    }

    public DepartingStrategy getDelegate() {
        return delegate;
    }
}
