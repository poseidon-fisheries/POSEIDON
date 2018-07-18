/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.log;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
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
     * how many hours have been spent at port waiting/preparing for this trip
     */
    private double hoursSinceLastTrip;


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
     * the places where fishing occured (and the hours spent fishing there)
     */
    private final HashMap<SeaTile,FishingRecord> tilesFished = new HashMap<>();

    private final HashMap<SeaTile,FishingRecord> lastFishingRecordOfTile = new HashMap<>();



    private double litersOfGasConsumed = 0;

    /**
     * the weight/biomass of everything that was sold at the end of the trip
     */
    private final double[] soldCatch;

    /**
     * the weight of everything caught
     */
    private final double[] totalCatch;

    /**
     * the total earnings per specie
     */
    private final double[] earningsPerSpecie;

    private double distanceTravelled = 0;

    private Port terminal;

    public TripRecord(int numberOfSpecies, double hoursSpentAtPort)
    {
        soldCatch = new double[numberOfSpecies];
        earningsPerSpecie = new double[numberOfSpecies];
        totalCatch = new double[numberOfSpecies];
        this.hoursSinceLastTrip = hoursSpentAtPort;
    }


    /**
     *
     */
    public void recordEarnings(int specieIndex, double biomass, double earnings)
    {

        soldCatch[specieIndex] += biomass;
        earningsPerSpecie[specieIndex] += earnings;
    }




    public void recordFishing(FishingRecord record)
    {
        //sum new catch!
        for(int i=0; i<totalCatch.length; i++)
            totalCatch[i]+=record.getFishCaught().getWeightCaught(i);

        tilesFished.merge(record.getTileFished(),
                          record,
                          FishingRecord::sumRecords);
        lastFishingRecordOfTile.put(record.getTileFished(),record);


    }

    public void recordCosts(double newCosts)
    {
        totalCosts += newCosts;
    }


    public void recordOpportunityCosts(double opportunityCosts)
    {
        //it's possible for opportunity costs to be computed at the end of the trip
        // Preconditions.checkState(!completed);
        this.opportunityCosts+=opportunityCosts;
    }

    public void recordTripCutShort(){
        Preconditions.checkState(!completed);
        cutShort = true;
    }

    public void completeTrip(double durationInHours, Port terminal)
    {
        this.durationInHours = durationInHours;
        this.terminal = terminal;
        completed = true;
    }

    /**
     * return profit/step; an easy way to compare trip records
     * @return profits/days
     * @param includingOpportunityCosts
     */
    public double getProfitPerHour(boolean includingOpportunityCosts)
    {

        double totalEarnings = getEarnings();
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
        assert FishState.round(soldCatch[specie],3) <=
                FishState.round(totalCatch[specie],3); //never sells more than it has!
        if(soldCatch[specie]<= FishStateUtilities.EPSILON)
            return Double.NaN;
        double totalWeightSold = DoubleStream.of(soldCatch).sum();
        assert  totalWeightSold > 0;
        assert totalWeightSold >= soldCatch[specie];
        double catchProportion = soldCatch[specie]/totalWeightSold;
        assert  catchProportion > 0;
        assert catchProportion <=1.0;

        if(!countOpportunityCosts)
            return earningsPerSpecie[specie]-totalCosts*catchProportion;
        else
            return earningsPerSpecie[specie]-(totalCosts+opportunityCosts)*catchProportion;

    }

    /**
     * returns the earnings made from selling a specific species
     * @param species the species sold
     * @return the amount of money made on that species
     */
    public double getEarningsOfSpecies(int species)
    {
        return earningsPerSpecie[species];
    }

    public double getTotalCosts() {
        return totalCosts;
    }

    public double getOpportunityCosts() {
        return opportunityCosts;
    }

    /**
     * profit per specie/ catch of that specie
     * @param specie the index this specie belongs to
     * @return profit per unit of catch
     */
    public double getUnitProfitPerSpecie(int specie)
    {
        if(soldCatch[specie]<= FishStateUtilities.EPSILON)
            return Double.NaN;
        else
            return getProfitPerSpecie(specie, false)/ soldCatch[specie];
    }

    public double getTotalTripProfit()
    {
        return getEarnings() - totalCosts;

    }

    public double getEarnings() {
        return DoubleStream.of(earningsPerSpecie).sum();
    }

    /**
     * earnings/landings for a specific species
     * @param species the species you want to know the price of
     * @return the ratio
     */
    public double getImplicitPriceReceived(Species species)
    {
        return getImplicitPriceReceived(species.getIndex());
    }


    /**
     * earnings/landings for a specific specie
     * @param species specie you are considering
     * @return ratio
     */
    public double getImplicitPriceReceived(int species)
    {
        return earningsPerSpecie[species]/ soldCatch[species];
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

    public Set<Map.Entry<SeaTile,FishingRecord>> getFishingRecords() {
        return tilesFished.entrySet();
    }

    public SeaTile getMostFishedTileInTrip()
    {

        if(tilesFished.size() == 0)
            return null;
        if(tilesFished.size()==1)
            return tilesFished.keySet().iterator().next();
        return tilesFished.entrySet().stream().max((entry1, entry2) -> entry1.getValue().getHoursSpentFishing() > entry2.getValue().getHoursSpentFishing() ? 1 : -1).
                get().getKey();

    }

    public double[] getSoldCatch() {
        return soldCatch;
    }


    public Port getTerminal() {
        return terminal;
    }

    public double getDurationInHours() {
        return durationInHours;
    }

    public double getHoursSinceLastTrip() {
        return hoursSinceLastTrip;
    }



    public int getEffort() {

        int sum = 0;
        for(FishingRecord record : tilesFished.values())
            sum+=record.getHoursSpentFishing();
        return sum;
    }

    /**
     * just divide total catch by hours spent fishing
     * @return
     */
    @Nullable
    public double[] getTotalCPUE(){

        double[] cpue = new double[totalCatch.length];
        double effort = getEffort();
        if(effort==0)
            return null;

        for(int i=0; i<cpue.length; i++)
            cpue[i] = totalCatch[i]/ effort;

        return cpue;

    }

    public double[] getEarningsPerSpecie() {
        return earningsPerSpecie;
    }

    /**
     * Getter for property 'distanceTravelled'.
     *
     * @return Value for property 'distanceTravelled'.
     */
    public double getDistanceTravelled() {
        return distanceTravelled;
    }

    public void addToDistanceTravelled(double distance)
    {
        distanceTravelled+=distance;
    }


    /**
     * Getter for property 'litersOfGasConsumed'.
     *
     * @return Value for property 'litersOfGasConsumed'.
     */
    public double getLitersOfGasConsumed() {
        return litersOfGasConsumed;
    }

    public void recordGasConsumption(double litersConsumed){
        litersOfGasConsumed+= litersConsumed;
    }

    public double[] getTotalCatch() {
        return totalCatch;
    }

    /**
     * Getter for property 'lastFishingRecordOfTile'.
     *
     * @return Value for property 'lastFishingRecordOfTile'.
     */
    public FishingRecord getLastFishingRecordOfTile(SeaTile tile) {
        return lastFishingRecordOfTile.get(tile);
    }
}