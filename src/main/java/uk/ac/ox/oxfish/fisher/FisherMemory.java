package uk.ac.ox.oxfish.fisher;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.erotetic.FeatureExtractor;
import uk.ac.ox.oxfish.fisher.erotetic.FeatureExtractors;
import uk.ac.ox.oxfish.fisher.log.*;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.data.collectors.*;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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


    private DiscretizedLocationMemory discretizedLocationMemory;

    /**
     * any other thing I want the fisher to remember I will have to store in this very general object
     */
    private HashMap<String, Object> database = new HashMap<>();






    public FisherMemory() {
        this(new LocationMemories<>(.99, 3000, 2),
             new LocationMemories<>(.99, 3000, 2));
    }

    public FisherMemory(
            LocationMemories<Catch> catchMemories,
            LocationMemories<TripRecord> tripMemories) {
        yearlyTimeSeries = new YearlyFisherTimeSeries();
        yearlyCounter = new Counter(IntervalPolicy.EVERY_YEAR);
        this.dailyTimeSeries = new DailyFisherTimeSeries();
        this.catchMemories = catchMemories;
        this.tripMemories = tripMemories;
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        yearlyCounter.addColumn(YearlyFisherTimeSeries.FUEL_CONSUMPTION);
        yearlyCounter.addColumn(YearlyFisherTimeSeries.FUEL_EXPENDITURE);
        yearlyCounter.addColumn(YearlyFisherTimeSeries.TRIPS);
        yearlyCounter.addColumn(YearlyFisherTimeSeries.EFFORT);
        dailyCounter = new FisherDailyCounter(model.getSpecies().size());

        dailyTimeSeries.start(model, fisher);
        yearlyTimeSeries.start(model, fisher);
        yearlyCounter.start(model);
        dailyCounter.start(model);
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
    public void turnOff(Fisher fisher) {
        tripMemories.turnOff();
        catchMemories.turnOff();
        tripLogger.turnOff();
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



    public int numberOfDailyObservations()
    {
        return dailyTimeSeries.numberOfObservations();
    }

    /**
     * Ask the fisher what is the best tile with respect to catches made
     * @param comparator how should the fisher compare each tile remembered
     */
    public SeaTile getBestSpotForCatchesRemembered(
            Comparator<LocationMemory<Catch>> comparator) {
        return getCatchMemories().getBestFishingSpotInMemory(comparator);
    }

    /**
     *
     * @param location
     * @return
     */
    public TripRecord rememberLastTripHere(SeaTile location)
    {

        return getTripMemories().getMemory(location);
    }

    public Map<SeaTile,LocationMemory<TripRecord>> rememberAllTrips()
    {
        return getTripMemories().getMemories();
    }

    /**
     * Ask the fisher what is the best tile with respect to trips made
     * @param comparator how should the fisher compare each tile remembered
     */
    public SeaTile getBestSpotForTripsRemembered(
            Comparator<LocationMemory<TripRecord>> comparator) {
        return getTripMemories().getBestFishingSpotInMemory(comparator);
    }



    /**
     * an object to extract from seatiles a feature
     */
    private FeatureExtractors<SeaTile> tileRepresentation = new FeatureExtractors<>();


    public void addFeatureExtractor(
            String nameOfFeature,
            FeatureExtractor<SeaTile> extractor) {
        tileRepresentation.addFeatureExtractor(nameOfFeature, extractor);
    }

    public FeatureExtractor<SeaTile> removeFeatureExtractor(String nameOfFeature) {
        return tileRepresentation.removeFeatureExtractor(nameOfFeature);
    }

    /**
     * Getter for property 'tileRepresentation'.
     *
     * @return Value for property 'tileRepresentation'.
     */
    public FeatureExtractors<SeaTile> getTileRepresentation() {
        return tileRepresentation;
    }


    /**
     * keep that memory in the database. The key cannot be currently in use!
     * @param key the key for the object
     * @param item the object to store
     */
    public void memorize(String key, Object item)
    {
        Object previous = database.put(key, item);
        Preconditions.checkState(previous == null, "The database already contains this key");
    }

    /**
     * removes the memory associated with that key
     * @param key
     */
    public void forget(String key)
    {
        database.remove(key);
    }

    /**
     * returns the object associated with this key
     * @param key
     */
    public Object remember(String key)
    {
        return database.get(key);
    }


    /**
     * registers visit (if the memory exists)
     */
    public void registerVisit(int group, int day) {
        if(discretizedLocationMemory!=null)
            discretizedLocationMemory.registerVisit(group, day);
    }
    /**
     * registers visit (if the memory exists)
     */
    public void registerVisit(SeaTile tile, int day) {
        if(discretizedLocationMemory!=null)
            discretizedLocationMemory.registerVisit(tile, day);
    }

    public DiscretizedLocationMemory getDiscretizedLocationMemory() {
        return discretizedLocationMemory;
    }

    public void setDiscretizedLocationMemory(DiscretizedLocationMemory discretizedLocationMemory) {
        Preconditions.checkArgument(this.discretizedLocationMemory == null, "Rewriting a discretized location memory with another. Probably not what you want!");
        this.discretizedLocationMemory = discretizedLocationMemory;
    }
}