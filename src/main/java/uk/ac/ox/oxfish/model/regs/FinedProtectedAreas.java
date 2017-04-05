package uk.ac.ox.oxfish.model.regs;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.Map;

/**
 * MPAs with a fine associated for each hour you spend in it
 * Created by carrknight on 2/13/17.
 */
public class FinedProtectedAreas implements Regulation{



    private final MersenneTwisterFast random;

    /**
     * basically a substitute for the "cheater" flag
     */
    private final boolean canContemplateCheating;

    private Map<MasonGeometry, Enforcement> enforcements = new HashMap<>();


    public FinedProtectedAreas(MersenneTwisterFast random,
                               boolean canContemplateCheating) {
        this.random = random;
        this.canContemplateCheating = canContemplateCheating;
    }


    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        FinedProtectedAreas finedProtectedAreas = new FinedProtectedAreas(random, canContemplateCheating);
        finedProtectedAreas.enforcements = this.enforcements;
        return finedProtectedAreas;
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
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model) {
        if(tile.isProtected() && !canContemplateCheating)
            return false;
        return true;
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
            Fisher agent, Species species, FishState model) {
        return Double.MAX_VALUE;
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
    public boolean allowedAtSea(Fisher fisher, FishState model) {
        return true;
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
    public void reactToSale(Species species, Fisher seller, double biomass, double revenue) {

    }

    /**
     * no reaction
     *
     * @param where
     * @param who
     * @param fishCaught
     * @param hoursSpentFishing
     */
    @Override
    public void reactToFishing(
            SeaTile where, Fisher who, Catch fishCaught, int hoursSpentFishing) {

        if(!where.isProtected())
            return;

        assert !who.isExogenousEmergencyOverride(); // you wouldn't have been caught already during this trip!

        Enforcement enforcement = enforcements.get(where.grabMPA());
        Preconditions.checkState(enforcement!= null, "not a registered MPA!");

        for (int i = 0; i < hoursSpentFishing; i++)
            if(random.nextBoolean(enforcement.getHourlyProbabilityOfBeingCaught())) {
            //you pay a fine
                who.spendForTrip(enforcement.getFine());
                //you are sent back home
                who.setExogenousEmergencyOverride(true);

            }
    }


    public void registerEnforcement(MasonGeometry mpa, double hourlyProbabilityOfBeingCaught, double fine)
    {
        Preconditions.checkArgument(!enforcements.containsKey(mpa));
        enforcements.put(mpa, new Enforcement(hourlyProbabilityOfBeingCaught,fine));
    }


    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    private static class Enforcement{

        final private double hourlyProbabilityOfBeingCaught;

        final private double fine;


        public Enforcement(double hourlyProbabilityOfBeingCaught, double fine) {
            this.hourlyProbabilityOfBeingCaught = hourlyProbabilityOfBeingCaught;
            this.fine = fine;
        }


        public double getHourlyProbabilityOfBeingCaught() {
            return hourlyProbabilityOfBeingCaught;
        }

        public double getFine() {
            return fine;
        }
    }
}
