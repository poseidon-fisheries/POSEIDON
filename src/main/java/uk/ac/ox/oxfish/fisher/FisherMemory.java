package uk.ac.ox.oxfish.fisher;

import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.log.LocationMemories;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripLogger;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.data.*;

import java.io.Serializable;

public class FisherMemory implements Serializable, FisherStartable {
    /**
     * the data gatherer that fires once a year
     */
    final YearlyFisherTimeSeries yearlyTimeSeries;

    public YearlyFisherTimeSeries getYearlyTimeSeries() {
        return yearlyTimeSeries;
    }

    /**
     * the data gatherer that fires every day
     */
    private DailyFisherTimeSeries dailyTimeSeries;

    public DailyFisherTimeSeries getDailyTimeSeries() {
        return dailyTimeSeries;
    }


    final Counter yearlyCounter;

    public Counter getYearlyCounter() {
        return yearlyCounter;
    }

    FisherDailyCounter dailyCounter;

    public FisherDailyCounter getDailyCounter() {
        return dailyCounter;
    }

    public void setDailyCounter(FisherDailyCounter dailyCounter) {
        this.dailyCounter = dailyCounter;
    }

    final LocationMemories<Catch> catchMemories;

    public LocationMemories<Catch> getCatchMemories() {
        return catchMemories;
    }

    final LocationMemories<TripRecord> tripMemories;

    public LocationMemories<TripRecord> getTripMemories() {
        return tripMemories;
    }

    /**
     * stores trip information
     */
    final TripLogger tripLogger = new TripLogger();

    public TripLogger getTripLogger() {
        return tripLogger;
    }

    public FisherMemory() {
        yearlyTimeSeries = new YearlyFisherTimeSeries();
        yearlyCounter = new Counter(IntervalPolicy.EVERY_YEAR);
        catchMemories = new LocationMemories<Catch>(.99, 300, 2);
        tripMemories = new LocationMemories<TripRecord>(.99, 300, 2);
    }

    public FisherMemory(
            YearlyFisherTimeSeries yearlyTimeSeries, DailyFisherTimeSeries dailyTimeSeries,
            Counter yearlyCounter, FisherDailyCounter dailyCounter,
            LocationMemories<Catch> catchMemories,
            LocationMemories<TripRecord> tripMemories) {
        this.yearlyTimeSeries = yearlyTimeSeries;
        this.dailyTimeSeries = dailyTimeSeries;
        this.yearlyCounter = yearlyCounter;
        this.dailyCounter = dailyCounter;
        this.catchMemories = catchMemories;
        this.tripMemories = tripMemories;
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        dailyTimeSeries = new DailyFisherTimeSeries(model.getSpecies().size());
        dailyTimeSeries.start(model,fisher);
        yearlyCounter.addColumn(YearlyFisherTimeSeries.FUEL_CONSUMPTION);
        yearlyTimeSeries.start(model, fisher);
        dailyCounter = new FisherDailyCounter(model.getSpecies().size());
        tripLogger.start(model);
        catchMemories.start(model);
        tripMemories.start(model);
        tripLogger.addTripListener(new TripListener() {
            @Override
            public void reactToFinishedTrip(TripRecord record) {
                SeaTile mostFishedTileInTrip = record.getMostFishedTileInTrip();
                if (mostFishedTileInTrip != null)
                    tripMemories.memorize(record, mostFishedTileInTrip);
            }
        });
    }

    @Override
    public void turnOff() {
        tripMemories.turnOff();
        catchMemories.turnOff();
        tripLogger.turnOff();;
        dailyCounter.turnOff();
        yearlyCounter.turnOff();

        yearlyTimeSeries.turnOff();
        dailyTimeSeries.turnOff();

    }

    public TripRecord getLastFinishedTrip() {
            return tripLogger.getLastFinishedTrip();

    }


    public double balanceXDaysAgo(int daysAgo)
    {
        //    Preconditions.checkArgument(dailyTimeSeries.numberOfObservations() >daysAgo);
        return dailyTimeSeries.getColumn(YearlyFisherTimeSeries.CASH_COLUMN).getDatumXDaysAgo(daysAgo);
    }

}