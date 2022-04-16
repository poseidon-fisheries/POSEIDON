package uk.ac.ox.oxfish.geography.fads;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadRemovalListener;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

/**
 * This class is mostly a wrapper around the DriftingObjectsMap class, but it adds a couple bits of
 * functionality: - It's a MASON Steppable, which applies drift when stepped. - It has methods for
 * deploying and removing FADs, setting the appropriate callback in the former case.
 */
public class FadMap<B extends LocalBiology, F extends Fad<B, F>>
    implements AdditionalStartable, Steppable {

    private final DriftingObjectsMap driftingObjectsMap;
    private final NauticalMap nauticalMap;
    private final GlobalBiology globalBiology;
    private final Class<B> localBiologyClass;
    private final Class<F> fadClass;
    private final AbundanceLostObserver abundanceLostObserver = new AbundanceLostObserver();
    private Stoppable stoppable;
    final private LinkedList<FadRemovalListener> removalListeners = new LinkedList<>();

    public FadMap(
        final NauticalMap nauticalMap,
        final CurrentVectors currentVectors,
        final GlobalBiology globalBiology,
        final Class<B> localBiologyClass,
        final Class<F> fadClass
    ) {
        this.nauticalMap = nauticalMap;
        this.globalBiology = globalBiology;
        this.localBiologyClass = localBiologyClass;
        this.fadClass = fadClass;
        this.driftingObjectsMap = new DriftingObjectsMap(currentVectors, nauticalMap);
    }

    public NauticalMap getNauticalMap() {
        return nauticalMap;
    }

    public DriftingObjectsMap getDriftingObjectsMap() {
        return driftingObjectsMap;
    }

    public GlobalBiology getGlobalBiology() {
        return globalBiology;
    }

    @Override
    public void start(final FishState model) {
        model.setFadMap(this);
        stoppable = model.scheduleEveryDay(this, StepOrder.DAWN);
    }

    @Override
    public void turnOff() {
        if (stoppable != null) {
            stoppable.stop();
        }
    }

    @Override
    public void step(final SimState simState) {
        final FishState fishState = (FishState) simState;
        driftingObjectsMap.applyDrift(fishState.getStep());
        allFads().forEach(fad -> {
            final Optional<B> seaTileBiology = getFadTile(fad).flatMap(this::getTileBiology);
            if (seaTileBiology.isPresent()) {
                fad.aggregateFish(seaTileBiology.get(), globalBiology);
                fad.maybeReleaseFish(
                    globalBiology.getSpecies(),
                    seaTileBiology.get(),
                    fishState.getRandom()
                );
            } else {
                fad.maybeReleaseFish(globalBiology.getSpecies(), fishState.getRandom());
            }
        });
    }

    public Stream<F> allFads() {
        return driftingObjectsMap.objects()
            .filter(fadClass::isInstance)
            .map(fadClass::cast);
    }

    public Bag allFadsAsList() {
        return driftingObjectsMap.getAllObjects();
    }

    @NotNull
    public Optional<SeaTile> getFadTile(final Fad<?, ?> fad) {
        return getFadLocation(fad).flatMap(this::getSeaTile);
    }

    @NotNull
    private Optional<Double2D> getFadLocation(final Fad<?, ?> fad) {
        return Optional.ofNullable(driftingObjectsMap.getObjectLocation(fad));
    }

    @NotNull
    private Optional<SeaTile> getSeaTile(final Double2D location) {
        return Optional.ofNullable(
            nauticalMap.getSeaTile((int) (location.x), (int) (location.y))
        );
    }

    @NotNull
    private Optional<B> getTileBiology(final SeaTile seaTile) {
        return Optional.of(seaTile)
            .map(SeaTile::getBiology)
            .filter(localBiologyClass::isInstance)
            .map(localBiologyClass::cast);
    }

    /**
     * Deploys a FAD in the middle of the given sea tile, i.e., at the 0.5, 0.5 point inside the
     * tile
     */
    public void deployFad(final F fad, final SeaTile seaTile) {
        deployFad(fad, new Double2D(seaTile.getGridX() + 0.5, seaTile.getGridY() + 0.5));
    }

    public void deployFad(final F fad, final Double2D location) {
        driftingObjectsMap.add(fad, location, onMove(fad));
    }

    @NotNull
    private BiConsumer<Double2D, Optional<Double2D>> onMove(final F fad) {
        return (oldLoc, newLoc) -> {
            final Optional<SeaTile> newSeaTile = newLoc.flatMap(this::getSeaTile);
            if (newSeaTile.isPresent()) {
                if (newSeaTile.get().isLand()) {
                    // When the FAD hits land, we need to release the aggregated fish in the sea
                    // tile it previously occupied and then tell the drifting object map that the
                    // FAD should be removed (which will in turn trigger another call back to
                    // this function).
                    getSeaTile(oldLoc).ifPresent(seaTile ->
                        fad.releaseFish(globalBiology.getSpecies(), seaTile.getBiology())
                    );
                    remove(fad);
                }
            } else {
                // The FAD does not have a location anymore, either because is has drifted off
                // the map or because it was explicitly removed. In that case, all that's left to
                // do is to release the fish into the void and tell the FAD's owner about it.
                reactToLostFad(fad);
            }
        };
    }

    public void destroyFad(Fad fad) {
        remove(fad);
    //    reactToLostFad(fad);
    }


    private void reactToLostFad(F fad) {
        fad.releaseFish(globalBiology.getSpecies());
        if(fad.getOwner()!=null)
            fad.getOwner().loseFad(fad);
    }

    public void remove(final Fad fad) {

        driftingObjectsMap.remove(fad);
        for (FadRemovalListener removalListener : getRemovalListeners()) {
            removalListener.onFadRemoval(fad);
        }
    }

    @NotNull
    public Bag fadsAt(final SeaTile seaTile) {
        return fadsAt(seaTile.getGridX(), seaTile.getGridY());
    }

    @NotNull
    private Bag fadsAt(final int x, final int y) {
        final Int2D location = new Int2D(x, y);
        final Bag bag = driftingObjectsMap.getField().getObjectsAtDiscretizedLocation(location);
        return bag == null ? new Bag() : bag;
    }

    /**
     * Returns the Continuous2D field backing the floating objects map. Only public because the GUI
     * portrayal needs to access it.
     */
    @NotNull
    public Continuous2D getField() {
        return driftingObjectsMap.getField();
    }

    public double getTotalBiomass(final Species species) {
        return allFads().mapToDouble(fad -> fad.getBiology().getBiomass(species)).sum();
    }

    public AbundanceLostObserver getAbundanceLostObserver() {
        return abundanceLostObserver;
    }

    public boolean isStarted(){
        return stoppable !=null;
    }


    public LinkedList<FadRemovalListener> getRemovalListeners() {
        return removalListeners;
    }


}
