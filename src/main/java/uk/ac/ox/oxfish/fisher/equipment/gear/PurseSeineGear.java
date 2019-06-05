package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.apache.sis.measure.Quantities;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;

import javax.measure.quantity.Time;

import static org.apache.sis.measure.Units.HOUR;

public class PurseSeineGear implements Gear {

    // TODO: this should probably be a general Gear property
    public final static Time DURATION_OF_SET = Quantities.create(4, HOUR);

    // See https://github.com/nicolaspayette/tuna/issues/8
    public final static double SUCCESSFUL_SET_PROBABILITY = 0.957;

    private final FadManager fadManager;

    public PurseSeineGear(FadManager fadManager) { this.fadManager = fadManager; }

    public FadManager getFadManager() { return fadManager; }

    @Override public Catch fish(
        Fisher fisher, LocalBiology localBiology, SeaTile context,
        int hoursSpentFishing, GlobalBiology modelBiology
    ) {
        // For now, just assume we catch *all* the biomass from the FAD
        // TODO: should we revise this assumption?
        final double[] catches = modelBiology.getSpecies().stream()
            .mapToDouble(localBiology::getBiomass).toArray();
        return new Catch(catches);
    }

    @Override
    public double getFuelConsumptionPerHourOfFishing(Fisher fisher, Boat boat, SeaTile where) {
        // TODO: see if making a set should consume fuel
        return 0;
    }

    @Override
    public double[] expectedHourlyCatch(
        Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Gear makeCopy() { throw new UnsupportedOperationException(); }

    @Override
    public boolean isSame(Gear o) { return o != null; }

}
