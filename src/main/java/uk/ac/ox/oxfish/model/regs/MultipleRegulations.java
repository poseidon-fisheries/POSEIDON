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

package uk.ac.ox.oxfish.model.regs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is given a map of regulation factories it calls at its start() to fill
 * in its list of regs. Only the regulations with the right tag will be instantiated for each fisher.
 * All regs are assumed to apply.
 *
 * Created by carrknight on 4/4/17.
 */
//todo quotaPerSpecieRegulation implementation is disgusting. It needs to go
public class MultipleRegulations implements Regulation, QuotaPerSpecieRegulation {

    /**
     * regulations active, filled by the map
     */
    private final List<Regulation> regulations;

    /**
     * the factories provided
     */
    private final Map<AlgorithmFactory<? extends Regulation>,String> factories;

    private boolean started = false;

    public MultipleRegulations(
            Map<AlgorithmFactory<? extends Regulation>, String> factories) {
        Preconditions.checkArgument(!factories.isEmpty(), "empty factories!");

        this.regulations = new LinkedList<>();
        this.factories = factories;
    }





    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        Preconditions.checkArgument(!factories.isEmpty(), "turned off!");
        return new MultipleRegulations(factories);
    }

    /**
     * when this tag is found
     */
    public static String TAG_FOR_ALL = "all";

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("regulations", regulations)
                .toString();
    }


    @Override
    public void start(FishState model, Fisher fisher) {
        //you shouldn't have started already
        Preconditions.checkArgument(!started, "Started already!");
        Preconditions.checkArgument(regulations.isEmpty(), "Started already!");
        Preconditions.checkArgument(!factories.isEmpty(), "No factories to instantiate!");


        for(Map.Entry<AlgorithmFactory<? extends Regulation>,String> factory : factories.entrySet())
        {
            if(factory.getValue().equals(TAG_FOR_ALL) || fisher.getTags().contains(factory.getValue()))
            {
                Regulation item = factory.getKey().apply(model);
                regulations.add(
                        item
                );
                item.start(model,fisher);
            }

        }



        Preconditions.checkArgument(!regulations.isEmpty(), "No regulations, not even anarchy, for fisher" +
                fisher);
        //clear to make sure you don't do it twice!
        started = true;
    }


    /**
     * can the agent fish at this location?
     *
     * @param agent the agent that wants to fish
     * @param tile  the tile the fisher is trying to fish on
     * @param model a link to the model
     * @return true if the fisher can fish
     */
    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model, int timeStep) {
        assert started;
        for(Regulation regulation : regulations)
        {
            if(!regulation.canFishHere(agent,tile,model,timeStep))
                return false;
        }
        return true;
    }


    /**
     * tell the regulation object this much has been caught
     *  @param where             where the fishing occurred
     * @param who               who did the fishing
     * @param fishCaught        catch object
     * @param fishRetained
     * @param hoursSpentFishing how many hours were spent fishing
     */
    @Override
    public void reactToFishing(
            SeaTile where, Fisher who, Catch fishCaught, Catch fishRetained,
            int hoursSpentFishing, FishState model, int timeStep) {
        assert started;
        for(Regulation regulation : regulations)
            regulation.reactToFishing(where, who, fishCaught,fishRetained , hoursSpentFishing, model, timeStep);
    }


    /**
     * tell the regulation object this much of this species has been sold
     *
     * @param species the species of fish sold
     * @param seller  agent selling the fish
     * @param biomass how much biomass has been sold
     * @param revenue how much money was made off it
     */
    @Override
    public void reactToSale(Species species, Fisher seller, double biomass, double revenue, FishState model, int timeStep) {
        assert started;
        for(Regulation regulation : regulations)
            regulation.reactToSale(species,seller,biomass,revenue,model,timeStep);
    }

    /**
     * how much of this species biomass is sellable. Zero means it is unsellable
     *
     * @param agent   the fisher selling its catch
     * @param species the species we are being asked about
     * @param model   a link to the model
     * @return a positive biomass if it sellable. Zero if you need to throw everything away
     */
    @Override
    public double maximumBiomassSellable(
            Fisher agent, Species species, FishState model, int timeStep) {
        double max = Double.MAX_VALUE;
        for(Regulation regulation : regulations)
        {

            max = Math.min(max,regulation.maximumBiomassSellable(agent,species,model,timeStep));
        }

        return max;
    }

    /**
     * Can this fisher be at sea?
     *
     * @param fisher the  fisher
     * @param model  the model
     * @return true if it can be out. When it's false the fisher can't leave port and ought to go back to port if he is
     * at sea
     */
    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model, int timeStep) {
        assert started;
        for(Regulation regulation : regulations)
        {
            if(!regulation.allowedAtSea(fisher,model,timeStep))
                return false;
        }
        return true;
    }

    @Override
    public void turnOff(Fisher fisher) {
        factories.clear();
        for(Regulation regulation : regulations)
            regulation.turnOff(fisher);
        regulations.clear();
    }

    private QuotaPerSpecieRegulation delegateHack = null;

    private QuotaPerSpecieRegulation getQuotaDelegate() {
        if (delegateHack == null) {
            delegateHack = getRegulations().stream()
                .filter(r -> r instanceof QuotaPerSpecieRegulation)
                .map(r -> (QuotaPerSpecieRegulation) r)
                .findFirst().orElse(null);
        }
        return delegateHack;
    }

    @Override
    public double getQuotaRemaining(int specieIndex) {
        QuotaPerSpecieRegulation quotaDelegate = getQuotaDelegate();
        if(quotaDelegate!= null)
            return quotaDelegate.getQuotaRemaining(specieIndex);
        else
            return Double.POSITIVE_INFINITY;
    }

    @Override
    public void setQuotaRemaining(int specieIndex, double newQuotaValue) {
        getQuotaDelegate().setQuotaRemaining(specieIndex,newQuotaValue);
    }

    /**
     * Getter for property 'regulations'.
     *
     * @return Value for property 'regulations'.
     */
    @VisibleForTesting
    public List<Regulation> getRegulations() {
        return regulations;
    }

    public boolean containsRegulation(Regulation regulation){
        return regulations.contains(regulation);
    }
}
