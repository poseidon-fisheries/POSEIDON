/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.equipment.fads;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import ec.util.MersenneTwisterFast;
import org.apache.commons.collections15.set.ListOrderedSet;
import sim.util.Bag;
import sim.util.Double2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.DeployFad;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeFadSet;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeUnassociatedSet;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.SetAction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.DriftingPath;
import uk.ac.ox.oxfish.geography.fads.DriftingObjectsMap;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.data.monitors.Observer;
import uk.ac.ox.oxfish.model.data.monitors.PerSpeciesMonitor;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;

import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FadManager {

    private final FadMap fadMap;
    private final ListOrderedSet<Fad> deployedFads = new ListOrderedSet<>();
    private final Iterable<Observer<DeployFad>> fadDeploymentObservers;
    private final Iterable<Observer<SetAction>> setObservers;
    private final Iterable<Observer<MakeFadSet>> fadSetObservers;
    private final Iterable<Observer<MakeUnassociatedSet>> unassociatedSetObservers;
    private final Optional<PerSpeciesMonitor<BiomassLostEvent, Double>> biomassLostMonitor;
    private ActiveActionRegulations actionSpecificRegulations;
    private FadInitializer fadInitializer;
    private Fisher fisher;
    private int numFadsInStock;

    private FadManager(
        FadMap fadMap,
        FadInitializer fadInitializer,
        int numFadsInStock,
        Iterable<Observer<DeployFad>> fadDeploymentObservers,
        Iterable<Observer<SetAction>> setObservers,
        Iterable<Observer<MakeFadSet>> fadSetObservers,
        Iterable<Observer<MakeUnassociatedSet>> unassociatedSetObservers,
        Optional<PerSpeciesMonitor<BiomassLostEvent, Double>> biomassLostMonitor,
        ActiveActionRegulations actionSpecificRegulations
    ) {
        this.fadMap = fadMap;
        this.fadInitializer = fadInitializer;
        this.numFadsInStock = numFadsInStock;
        this.fadDeploymentObservers = fadDeploymentObservers;
        this.setObservers = setObservers;
        this.fadSetObservers = fadSetObservers;
        this.unassociatedSetObservers = unassociatedSetObservers;
        this.biomassLostMonitor = biomassLostMonitor;
        this.actionSpecificRegulations = actionSpecificRegulations;
    }

    public Optional<PerSpeciesMonitor<BiomassLostEvent, Double>> getBiomassLostMonitor() { return biomassLostMonitor; }

    public int getNumDeployedFads() { return deployedFads.size(); }

    public Optional<Fad> oneOfDeployedFads() {
        return getDeployedFads().isEmpty() ?
            Optional.empty() :
            Optional.of(oneOf(getDeployedFads(), getFisher().grabRandomizer()));
    }

    private ListOrderedSet<Fad> getDeployedFads() { return deployedFads; }

    public Fisher getFisher() { return fisher; }

    public void setFisher(Fisher fisher) {
        this.fisher = fisher;
    }

    public Bag getFadsHere() {
        checkNotNull(fisher);
        return fadMap.fadsAt(fisher.getLocation());
    }

    public int getNumFadsInStock() { return numFadsInStock; }

    public void loseFad(Fad fad) {
        checkArgument(deployedFads.contains(fad));
        deployedFads.remove(fad);
    }

    public Fad deployFad(SeaTile seaTile, int timeStep) {
        final Fad newFad = initFad();
        fadMap.deployFad(newFad, timeStep, seaTile);
        return newFad;
    }

    private Fad initFad() {
        checkState(numFadsInStock >= 1);
        numFadsInStock--;
        final Fad newFad = fadInitializer.apply(this);
        deployedFads.add(newFad);
        return newFad;
    }

    /**
     * Deploys a FAD at a random position in the given sea tile
     */
    public void deployFad(SeaTile seaTile, int timeStep, MersenneTwisterFast random) {
        deployFad(new Double2D(
            seaTile.getGridX() + random.nextDouble(),
            seaTile.getGridY() + random.nextDouble()
        ), timeStep);
    }

    private void deployFad(Double2D location, int timeStep) {
        final Fad newFad = initFad();
        fadMap.deployFad(newFad, timeStep, location);
    }

    public void pickUpFad(Fad fad) {
        fadMap.remove(fad);
        numFadsInStock++;
    }

    public FadMap getFadMap() { return fadMap; }

    public ImmutableSetMultimap<SeaTile, Fad> deployedFadsByTileAtStep(int timeStep) {
        final ImmutableSetMultimap.Builder<SeaTile, Fad> builder = new ImmutableSetMultimap.Builder<>();
        final DriftingObjectsMap driftingObjectsMap = fadMap.getDriftingObjectsMap();
        deployedFads.forEach(fad ->
            driftingObjectsMap.getObjectPath(fad)
                .position(timeStep)
                .map(this::getSeaTile)
                .ifPresent(seaTile -> builder.put(seaTile, fad))
        );
        return builder.build();
    }

    private SeaTile getSeaTile(Double2D position) { return getSeaTile(position.x, position.y); }

    private SeaTile getSeaTile(double x, double y) { return getSeaTile((int) x, (int) y); }

    private SeaTile getSeaTile(int x, int y) { return fadMap.getNauticalMap().getSeaTile(x, y); }

    public ImmutableSet<SeaTile> fadLocationsInTimeStepRange(int startStep, int endStep) {
        ImmutableSet.Builder<SeaTile> builder = new ImmutableSet.Builder<>();
        final DriftingObjectsMap driftingObjectsMap = fadMap.getDriftingObjectsMap();
        deployedFads.forEach(fad -> {
            final DriftingPath path = driftingObjectsMap.getObjectPath(fad);
            for (int t = startStep; t <= endStep; t++) {
                path.position(t).map(this::getSeaTile).ifPresent(builder::add);
            }
        });
        return builder.build();
    }

    public ActiveActionRegulations getActionSpecificRegulations() {
        return actionSpecificRegulations;
    }

    public void setActionSpecificRegulations(Stream<ActionSpecificRegulation> actionSpecificRegulations) {
        setActionSpecificRegulations(new ActiveActionRegulations(actionSpecificRegulations));
    }

    private void setActionSpecificRegulations(ActiveActionRegulations actionSpecificRegulations) {
        this.actionSpecificRegulations = actionSpecificRegulations;
    }

    public void reactTo(DeployFad action) {
        fadDeploymentObservers.forEach(observer -> observer.observe(action));
    }

    public void reactTo(MakeFadSet action) {
        setObservers.forEach(observer -> observer.observe(action));
        fadSetObservers.forEach(observer -> observer.observe(action));
    }

    public void reactTo(MakeUnassociatedSet action) {
        setObservers.forEach(observer -> observer.observe(action));
        unassociatedSetObservers.forEach(observer -> observer.observe(action));
    }

    void reactTo(BiomassLostEvent event) {
        biomassLostMonitor.ifPresent(monitor -> monitor.observe(event));
    }

    public static class Builder {

        private final FadMap fadMap;
        private final FadInitializer fadInitializer;
        ImmutableList.Builder<Observer<DeployFad>> fadDeploymentObservers =
            new ImmutableList.Builder<>();
        ImmutableList.Builder<Observer<SetAction>> setObservers =
            new ImmutableList.Builder<>();
        ImmutableList.Builder<Observer<MakeFadSet>> fadSetObservers =
            new ImmutableList.Builder<>();
        ImmutableList.Builder<Observer<MakeUnassociatedSet>> unassociatedSetObservers =
            new ImmutableList.Builder<>();
        Optional<PerSpeciesMonitor<BiomassLostEvent, Double>> biomassLostMonitor = Optional.empty();
        ImmutableSet.Builder<ActionSpecificRegulation> actionSpecificRegulations =
            new ImmutableSet.Builder<>();
        private int initialNumberOfFadsInStock = 0;

        public Builder(
            FadMap fadMap,
            FadInitializer fadInitializer
        ) {
            this.fadMap = fadMap;
            this.fadInitializer = fadInitializer;
        }

        public Builder addFadDeploymentObservers(
            Iterable<? extends Observer<DeployFad>> fadDeploymentObservers
        ) {
            this.fadDeploymentObservers.addAll(fadDeploymentObservers);
            return this;
        }

        public Builder addSetObservers(
            Iterable<? extends Observer<SetAction>> setObservers
        ) {
            this.setObservers.addAll(setObservers);
            return this;
        }

        public Builder addFadSetObservers(
            Iterable<? extends Observer<MakeFadSet>> fadSetObservers
        ) {
            this.fadSetObservers.addAll(fadSetObservers);
            return this;
        }

        public Builder addUnassociatedSetObservers(
            Iterable<? extends Observer<MakeUnassociatedSet>> unassociatedSetObservers
        ) {
            this.unassociatedSetObservers.addAll(unassociatedSetObservers);
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setBiomassLostMonitor(PerSpeciesMonitor<BiomassLostEvent, Double> biomassLostMonitor) {
            this.biomassLostMonitor = Optional.ofNullable(biomassLostMonitor);
            return this;
        }

        public Builder addActionSpecificRegulations(
            ActionSpecificRegulation... actionSpecificRegulations
        ) {
            this.actionSpecificRegulations.add(actionSpecificRegulations);
            return this;
        }

        @SuppressWarnings("unused")
        public Builder addActionSpecificRegulations(
            Iterable<? extends ActionSpecificRegulation> actionSpecificRegulations
        ) {
            this.actionSpecificRegulations.addAll(actionSpecificRegulations);
            return this;
        }

        public FadManager build() {
            final ActiveActionRegulations regulations = new ActiveActionRegulations(actionSpecificRegulations.build());
            return new FadManager(
                fadMap,
                fadInitializer,
                initialNumberOfFadsInStock,
                fadDeploymentObservers.add(regulations::reactToAction).build(),
                setObservers.add(regulations::reactToAction).build(),
                fadSetObservers.add(regulations::reactToAction).build(),
                unassociatedSetObservers.add(regulations::reactToAction).build(),
                biomassLostMonitor,
                regulations
            );
        }

        public Builder setInitialNumberOfFadsInStock(int initialNumberOfFadsInStock) {
            checkArgument(initialNumberOfFadsInStock >= 0);
            this.initialNumberOfFadsInStock = initialNumberOfFadsInStock;
            return this;
        }

    }

}
