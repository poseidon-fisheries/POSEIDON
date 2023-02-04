package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public class MaximumTripLengthRegulation implements Regulation {

    private final double maxNumberOfHoursOut;


    public MaximumTripLengthRegulation(double maxNumberOfHoursOut) {
        this.maxNumberOfHoursOut = maxNumberOfHoursOut;
    }

    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model, int timeStep) {

        return agent.getHoursAtSea() <= maxNumberOfHoursOut;
    }

    @Override
    public double maximumBiomassSellable(Fisher agent, Species species, FishState model, int timeStep) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model, int timeStep) {

        return fisher.getHoursAtSea() <= maxNumberOfHoursOut;

    }

    @Override
    public Regulation makeCopy() {
        return new MaximumTripLengthRegulation(maxNumberOfHoursOut);
    }

    public double getMaxNumberOfHoursOut() {
        return maxNumberOfHoursOut;
    }
}
