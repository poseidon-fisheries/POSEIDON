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
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.Optional;
import java.util.function.BiConsumer;
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
    private Stoppable stoppable;
    FadMap(
        NauticalMap nauticalMap,
        CurrentVectors currentVectors,
        GlobalBiology globalBiology
    ) {
        this.nauticalMap = nauticalMap;
        this.globalBiology = globalBiology;
        this.driftingObjectsMap = new DriftingObjectsMap(currentVectors, nauticalMap);
    }

    public NauticalMap getNauticalMap() { return nauticalMap; }

    public DriftingObjectsMap getDriftingObjectsMap() { return driftingObjectsMap; }

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
        driftingObjectsMap.applyDrift(((FishState) simState).getStep());
        allFads().forEach(fad -> {
            final Optional<VariableBiomassBasedBiology> seaTileBiology =
                getFadTile(fad).flatMap(FadMap::getVariableBiomassBasedBiology);
            if (seaTileBiology.isPresent()) {
                fad.aggregateFish(seaTileBiology.get(), globalBiology);
                fad.maybeReleaseFish(globalBiology.getSpecies(), seaTileBiology.get(), simState.random);
            } else {
                fad.maybeReleaseFish(globalBiology.getSpecies(), simState.random);
            }
        });
    }

    @NotNull
    public Stream<Fad> allFads() {
        return driftingObjectsMap.objects().map(o -> (Fad) o);
    }

    @NotNull
    public Optional<SeaTile> getFadTile(Fad fad) {
        return getFadLocation(fad).flatMap(this::getSeaTile);
    }

    @NotNull
    private static Optional<VariableBiomassBasedBiology> getVariableBiomassBasedBiology(SeaTile seaTile) {
        return Optional.of(seaTile)
            .map(SeaTile::getBiology)
            .filter(biology -> biology instanceof VariableBiomassBasedBiology)
            .map(biology -> (VariableBiomassBasedBiology) biology);
    }

    @NotNull
    private Optional<Double2D> getFadLocation(Fad fad) {
        return Optional.ofNullable(driftingObjectsMap.getObjectLocation(fad));
    }

    @NotNull
    private Optional<SeaTile> getSeaTile(Double2D location) {
        return Optional.ofNullable(
            nauticalMap.getSeaTile((int) (location.x), (int) (location.y))
        );
    }

    public void deployFad(Fad fad, int timeStep, Double2D location) {
        driftingObjectsMap.add(fad, timeStep, location, onMove(fad));
    }

    @NotNull
    private BiConsumer<Double2D, Optional<Double2D>> onMove(Fad fad) {
        return (oldLoc, newLoc) -> {
            final Optional<SeaTile> newSeaTile = newLoc.flatMap(this::getSeaTile);
            if (newSeaTile.isPresent()) {
                if (newSeaTile.get().isLand()) {
                    // When the FAD hits land, we need to release the aggregated fish in the sea tile it
                    // previously occupied and then tell the drifting object map that the FAD should be removed
                    // (which will in turn trigger another call back to this function).
                    getSeaTile(oldLoc).ifPresent(seaTile ->
                        fad.releaseFish(globalBiology.getSpecies(), seaTile.getBiology())
                    );
                    remove(fad);
                }
            } else {
                // The FAD does not have a location anymore, either because is has drifted off the map
                // or because it was explicitly removed. In that case, all that's left to do is to tell
                // the FAD's owner about it.
                fad.getOwner().loseFad(fad);
            }
        };
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

    public double getTotalBiomass(Species species) {
        return allFads().mapToDouble(fad -> fad.getBiology().getBiomass(species)).sum();
    }
}
