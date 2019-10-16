package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * This is the opposite of {@link Anarchy}: nothing is allowed.
 * Useless by itself, but meant to be wrapped be other classes,
 * e.g. {@link TemporaryRegulation} to implement an arbitrary pause.
 */
public class NoFishing implements Regulation {
    @Override public boolean canFishHere(Fisher agent, SeaTile tile, FishState model, int timeStep) { return false; }
    @Override public double maximumBiomassSellable(Fisher agent, Species species, FishState model, int timeStep) { return 0; }
    @Override public boolean allowedAtSea(Fisher fisher, FishState model, int timeStep) { return false; }
    @Override public void reactToFishing(SeaTile where, Fisher who, Catch fishCaught, Catch fishRetained, int hoursSpentFishing, FishState model, int timeStep) { }
    @Override public void reactToSale(Species species, Fisher seller, double biomass, double revenue, FishState model, int timeStep) { }
    @Override public Regulation makeCopy() { return new NoFishing(); }
}
