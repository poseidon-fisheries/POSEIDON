/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.geography.fads;

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

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * This class is mostly a wrapper around the DriftingObjectsMap class, but it adds a couple bits of functionality: -
 * It's a MASON Steppable, which applies drift when stepped. - It has methods for deploying and removing FADs, setting
 * the appropriate callback in the former case.
 */
public class FadMap
    implements AdditionalStartable, Steppable {

    private static final long serialVersionUID = -4923511779083700503L;
    private final DriftingObjectsMap driftingObjectsMap;
    private final NauticalMap nauticalMap;
    private final GlobalBiology globalBiology;
    private final Class<? extends LocalBiology> localBiologyClass;
    private final AbundanceLostObserver abundanceLostObserver = new AbundanceLostObserver();
    final private LinkedList<FadRemovalListener> removalListeners = new LinkedList<>();
    private Stoppable stoppable;

    public FadMap(
        final NauticalMap nauticalMap,
        final CurrentVectors currentVectors,
        final GlobalBiology globalBiology,
        final Class<? extends LocalBiology> localBiologyClass
    ) {
        this.nauticalMap = nauticalMap;
        this.globalBiology = globalBiology;
        this.localBiologyClass = localBiologyClass;
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
            fad.reactToStep(fishState);
            final Optional<LocalBiology> seaTileBiology =
                getFadTile(fad)
                    .flatMap(this::getTileBiology);
            if (seaTileBiology.isPresent()) {
                fad.aggregateFish(seaTileBiology.get(), globalBiology, fishState.getStep());
                fad.maybeReleaseFishIntoTile(
                    seaTileBiology.get(),
                    fishState.getRandom()
                );
            } else {
                fad.maybeReleaseFishIntoTheVoid(fishState.getRandom());
            }
        });
    }

    public Stream<Fad> allFads() {
        return driftingObjectsMap.objects()
            .filter(Fad.class::isInstance)
            .map(Fad.class::cast);
    }

    public Optional<SeaTile> getFadTile(final Fad fad) {
        return getFadLocation(fad).flatMap(this::getSeaTile);
    }

    private Optional<LocalBiology> getTileBiology(final SeaTile seaTile) {
        return Optional.of(seaTile)
            .map(SeaTile::getBiology)
            .filter(localBiologyClass::isInstance)
            .map(localBiologyClass::cast);
    }

    public Optional<Double2D> getFadLocation(final Fad fad) {
        return Optional.ofNullable(driftingObjectsMap.getObjectLocation(fad));
    }

    private Optional<SeaTile> getSeaTile(final Double2D location) {
        return Optional.ofNullable(
            nauticalMap.getSeaTile((int) (location.x), (int) (location.y))
        );
    }

    /**
     * Deploys a FAD in the middle of the given sea tile, i.e., at the 0.5, 0.5 point inside the tile
     */
    public void deployFad(
        final Fad fad,
        final SeaTile seaTile
    ) {
        deployFad(fad, new Double2D(seaTile.getGridX() + 0.5, seaTile.getGridY() + 0.5));
    }

    public void deployFad(
        final Fad fad,
        final Double2D location
    ) {
        driftingObjectsMap.add(fad, location, onMove(fad));
    }

    private BiConsumer<Double2D, Optional<Double2D>> onMove(final Fad fad) {
        return (oldLoc, newLoc) -> {
            final Optional<SeaTile> newSeaTile = newLoc.flatMap(this::getSeaTile);
            if (newSeaTile.isPresent()) {
                if (newSeaTile.get().isLand()) {
                    // When the FAD hits land, we need to release the aggregated fish in the sea
                    // tile it previously occupied and then tell the drifting object map that the
                    // FAD should be removed (which will in turn trigger another call back to
                    // this function).
                    getSeaTile(oldLoc).ifPresent(seaTile ->
                        fad.releaseFishIntoTile(globalBiology.getSpecies(), seaTile.getBiology())
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

    public void remove(final Fad fad) {
        driftingObjectsMap.remove(fad);
        for (final FadRemovalListener removalListener : getRemovalListeners()) {
            removalListener.onFadRemoval(fad);
        }
    }

    private void reactToLostFad(final Fad fad) {
        fad.releaseFishIntoTheVoid(globalBiology.getSpecies());
        if (fad.getOwner() != null)
            fad.getOwner().loseFad(fad);
    }

    public LinkedList<FadRemovalListener> getRemovalListeners() {
        return removalListeners;
    }

    public void destroyFad(final Fad fad) {
        remove(fad);
    }

    public Bag fadsAt(final SeaTile seaTile) {
        return fadsAt(seaTile.getGridX(), seaTile.getGridY());
    }

    private Bag fadsAt(
        final int x,
        final int y
    ) {
        final Int2D location = new Int2D(x, y);
        final Bag bag = driftingObjectsMap.getField().getObjectsAtDiscretizedLocation(location);
        return bag == null ? new Bag() : bag;
    }

    /**
     * Returns the Continuous2D field backing the floating objects map. Only public because the GUI portrayal needs to
     * access it.
     */
    public Continuous2D getField() {
        return driftingObjectsMap.getField();
    }

    public double getTotalBiomass(final Species species) {
        return allFads().mapToDouble(fad -> fad.getBiology().getBiomass(species)).sum();
    }

    public AbundanceLostObserver getAbundanceLostObserver() {
        return abundanceLostObserver;
    }

    public boolean isStarted() {
        return stoppable != null;
    }

}
