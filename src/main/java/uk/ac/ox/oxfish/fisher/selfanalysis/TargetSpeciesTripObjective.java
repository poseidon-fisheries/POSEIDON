package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.log.TripRecord;

/**
 * Ignores earnings by all fish that isn't the one targeted
 * Created by carrknight on 3/24/16.
 */
public class TargetSpeciesTripObjective extends TripBasedObjectiveFunction {


    private final Species species;

    private final boolean opportunityCosts;

    public TargetSpeciesTripObjective(Species species, boolean opportunityCosts) {
        this.species = species;
        this.opportunityCosts = opportunityCosts;
    }

    /**
     * the utility is earnings for selected species - total costs
     * @param tripRecord
     * @return
     */
    @Override
    protected double extractUtilityFromTrip(TripRecord tripRecord) {
        double profits = tripRecord.getEarningsOfSpecies(species.getIndex()) - tripRecord.getTotalCosts();
        profits= opportunityCosts ? profits-tripRecord.getOpportunityCosts() :profits;
        profits/=  tripRecord.getDurationInHours();
        return profits;
    }
}
