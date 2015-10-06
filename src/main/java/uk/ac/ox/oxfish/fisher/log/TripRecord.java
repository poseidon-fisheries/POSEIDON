package uk.ac.ox.oxfish.fisher.log;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.DoubleStream;

/**
 * Holds summary statistics of a trip, specifically how much money was made and how much was spent.
 * The plan is to have a "logbook"-looking data for each trip, but this is far simpler
 * Created by carrknight on 6/17/15.
 */
public class TripRecord {

    /**
     * how long did the trip take
     */
    private double durationInHours = 0;


    /**
     * set to true if the regulations forced the fisher home earlier.
     */
    private boolean cutShort = false;

    /**
     * is the trip over?
     */
    private boolean completed = false;

    /**
     * total costs accrued
     */
    private double totalCosts = 0;

    /**
     * costs that are economic but not accounting, that is they do not involve direct loss of money but they try to
     * estimate losses in opportunities
     */
    private double opportunityCosts = 0;


    /**
     * the places where fishing occured
     */
    private final HashMap<SeaTile,Integer> tilesFished = new HashMap<>();


    /**
     * the weight/biomass of everything that was sold at the end of the trip
     */
    private final double[] finalCatch;

    /**
     * the total earnings per specie
     */
    private final double[] earningsPerSpecie;



    public TripRecord(int numberOfSpecies)
    {
        finalCatch = new double[numberOfSpecies];
        earningsPerSpecie = new double[numberOfSpecies];
    }


    /**
     *
     */
    public void recordEarnings(int specieIndex, double biomass, double earnings)
    {

        finalCatch[specieIndex] += biomass;
        earningsPerSpecie[specieIndex] += earnings;
    }




    public void recordFishing(FishingRecord record)
    {

        Integer timesFished = tilesFished.getOrDefault(record.getTileFished(), 0);
        tilesFished.put(record.getTileFished(),timesFished+1);

    }

    public void recordCosts(double newCosts)
    {
        Preconditions.checkState(!completed);
        totalCosts += newCosts;
    }


    public void recordOpportunityCosts(double opportunityCosts)
    {
        Preconditions.checkState(!completed);
        this.opportunityCosts+=opportunityCosts;
    }

    public void recordTripCutShort(){
        Preconditions.checkState(!completed);
        cutShort = true;
    }

    public void completeTrip(double durationInHours)
    {
        this.durationInHours = durationInHours;
        completed = true;
    }

    /**
     * return profit/step; an easy way to compare trip records
     * @return profits/days
     * @param includingOpportunityCosts
     */
    public double getProfitPerHour(boolean includingOpportunityCosts)
    {

        double totalEarnings = DoubleStream.of(earningsPerSpecie).sum();
        Preconditions.checkArgument(durationInHours > 0 == completed);
        if(!includingOpportunityCosts)
            return (totalEarnings - totalCosts) / durationInHours;
        else
            return (totalEarnings - totalCosts - opportunityCosts) / durationInHours;

    }


    /**
     * returns the profit associated with a particular specie
     * @param specie the specie index
     * @param countOpportunityCosts
     * @return NAN if there was nothing caught for this specie, otherwise specie revenue - costs*(proportion of catch that is from this specie)
     */
    public  double getProfitPerSpecie(int specie, boolean countOpportunityCosts)
    {
        if(finalCatch[specie]<= FishStateUtilities.EPSILON)
            return Double.NaN;
        double totalCatch = DoubleStream.of(finalCatch).sum();
        assert  totalCatch > 0;
        assert totalCatch >= finalCatch[specie];
        double catchProportion = finalCatch[specie]/totalCatch;
        assert  catchProportion > 0;
        assert catchProportion <=1.0;

        if(!countOpportunityCosts)
            return earningsPerSpecie[specie]-totalCosts*catchProportion;
        else
            return earningsPerSpecie[specie]-(totalCosts+opportunityCosts)*catchProportion;

    }

    /**
     * profit per specie/ catch of that specie
     * @param specie the index this specie belongs to
     * @return profit per unit of catch
     */
    public double getUnitProfitPerSpecie(int specie)
    {
        if(finalCatch[specie]<= FishStateUtilities.EPSILON)
            return Double.NaN;
        else
            return getProfitPerSpecie(specie, false)/finalCatch[specie];
    }

    public double getTotalTripProfit()
    {
        return DoubleStream.of(earningsPerSpecie).sum() - totalCosts;

    }




    public boolean isCutShort() {
        return cutShort;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Set<SeaTile> getTilesFished() {
        return tilesFished.keySet();
    }

    public SeaTile getMostFishedTileInTrip()
    {

        if(tilesFished.size() == 0)
            return null;
        return tilesFished.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

    }

    public double[] getFinalCatch() {
        return finalCatch;
    }


}