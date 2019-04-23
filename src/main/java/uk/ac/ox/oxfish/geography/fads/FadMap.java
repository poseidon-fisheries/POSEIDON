package uk.ac.ox.oxfish.geography.fads;

import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentMaps;
import uk.ac.ox.oxfish.geography.currents.VectorGrid2D;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is mostly a wrapper around the DriftingObjectsMap class, but it adds a couple bits of functionality:
 * - It's a MASON Steppable, which applies drift when stepped.
 * - It has methods for deploying and removing FADs, setting the appropriate callback in the former case.
 */
public class FadMap implements Startable, Steppable {

    private final DriftingObjectsMap driftingObjectsMap;
    private final NauticalMap nauticalMap;
    private final GlobalBiology globalBiology;
    private final Function<FadManager, Fad> fadFactory;

    private final CurrentMaps currentsMaps;

    private Stoppable stoppable;

    FadMap(
        NauticalMap nauticalMap,
        CurrentMaps currentsMaps,
        GlobalBiology globalBiology,
        Function<FadManager, Fad> fadFactory
    ) {
        this.nauticalMap = nauticalMap;
        this.currentsMaps = currentsMaps;
        this.globalBiology = globalBiology;
        this.fadFactory = fadFactory;
        this.driftingObjectsMap = new DriftingObjectsMap(nauticalMap.getWidth(), nauticalMap.getHeight());
    }

    public GlobalBiology getGlobalBiology() { return globalBiology; }

    @Override
    public void start(FishState model) {
        stoppable = model.scheduleEveryDay(this, StepOrder.DAWN);
    }

    @Override
    public void turnOff() {
        if (stoppable != null) stoppable.stop();
    }

    @Override
    public void step(SimState simState) {
        VectorGrid2D currentsMap = currentsMaps.atSteps(simState.schedule.getSteps());
        driftingObjectsMap.applyDrift(currentsMap::move);
        for (Fad fad : allFads().collect(Collectors.toList())) { // use copy, as FADs can be removed
            final Optional<SeaTile> seaTile = getFadTile(fad)
                .filter(SeaTile::isWater);
            if (seaTile.isPresent())
                fad.aggregateFish(seaTile.get(), globalBiology);
            else
                remove(fad);
        }
    }

    @NotNull
    private Stream<Fad> allFads() {
        return driftingObjectsMap.objects().map(o -> (Fad) o);
    }

    @NotNull
    public Optional<SeaTile> getFadTile(Fad fad) {
        return Optional
            .ofNullable(driftingObjectsMap.getObjectLocation(fad))
            .flatMap(this::getSeaTile);
    }

    public void remove(Fad fad) { driftingObjectsMap.remove(fad); }

    @NotNull
    private Optional<SeaTile> getSeaTile(Double2D location) {
        return Optional.ofNullable(
            nauticalMap.getSeaTile((int) (location.x), (int) (location.y))
        );
    }

    @NotNull
    public Fad deployFad(FadManager owner, Double2D location) {
        Fad fad = fadFactory.apply(owner);
        driftingObjectsMap.add(fad, location, (oldLoc, newLoc) -> {
            if (!newLoc.flatMap(this::getSeaTile).isPresent()) fad.getOwner().loseFad(fad);
        });
        return fad;
    }

    public void remove(Fad fad) { driftingObjectsMap.remove(fad); }

    @NotNull
    public Bag fadsAt(SeaTile seaTile) {
        Int2D location = new Int2D(seaTile.getGridX(), seaTile.getGridY());
        final Bag bag = driftingObjectsMap.getField().getObjectsAtDiscretizedLocation(location);
        return bag == null ? new Bag() : bag;
    }

    /**
     * Returns the Continuous2D field backing the floating objects map. Only public because the GUI
     * portrayal needs to access it.
     */
    @NotNull
    public Continuous2D getField() { return driftingObjectsMap.getField(); }
}
