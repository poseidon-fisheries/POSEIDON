package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class PurseSeineGearFactory implements AlgorithmFactory<PurseSeineGear> {

    private int initialNumberOfFads = 400;

    public int getInitialNumberOfFads() { return initialNumberOfFads; }

    public void setInitialNumberOfFads(int initialNumberOfFads) {
        this.initialNumberOfFads = initialNumberOfFads;
    }

    @Override
    public PurseSeineGear apply(FishState fishState) {
        final FadManager fadManager = new FadManager(fishState.getFadMap(), initialNumberOfFads);
        return new PurseSeineGear(fadManager);
    }
}
