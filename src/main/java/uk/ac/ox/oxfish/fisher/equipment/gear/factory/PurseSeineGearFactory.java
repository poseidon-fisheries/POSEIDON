package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.PurseSeineGear;
import uk.ac.ox.oxfish.geography.fads.FadInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class PurseSeineGearFactory implements AlgorithmFactory<PurseSeineGear> {

    private int initialNumberOfFads = 999999; // TODO: find plausible value and allow boats to refill
    private FadInitializerFactory fadInitializerFactory = new FadInitializerFactory();

    @SuppressWarnings("unused")
    public FadInitializerFactory getFadInitializerFactory() { return fadInitializerFactory; }

    @SuppressWarnings("unused")
    public void setFadInitializerFactory(
        FadInitializerFactory fadInitializerFactory
    ) { this.fadInitializerFactory = fadInitializerFactory; }

    @SuppressWarnings("unused")
    public int getInitialNumberOfFads() { return initialNumberOfFads; }

    @SuppressWarnings("unused")
    public void setInitialNumberOfFads(int initialNumberOfFads) {
        this.initialNumberOfFads = initialNumberOfFads;
    }

    @Override
    public PurseSeineGear apply(FishState fishState) {
        final FadManager fadManager = new FadManager(
            fishState.getFadMap(),
            fadInitializerFactory.apply(fishState),
            initialNumberOfFads
        );
        return new PurseSeineGear(fadManager);
    }
}
