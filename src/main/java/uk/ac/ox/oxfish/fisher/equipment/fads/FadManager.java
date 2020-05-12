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
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.DeployFad;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeFadSet;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeUnassociatedSet;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.PurseSeinerAction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.DriftingPath;
import uk.ac.ox.oxfish.geography.fads.DriftingObjectsMap;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observer;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observers;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FadManager {

    private static List<Class<? extends PurseSeinerAction>> POSSIBLE_ACTIONS = ImmutableList.of(
        DeployFad.class, MakeFadSet.class, MakeUnassociatedSet.class
    );

    private final FadMap fadMap;
    private final Observers observers = new Observers();
    private final Optional<GroupingMonitor<Species, BiomassLostEvent, Double>> biomassLostMonitor;
    private final ListOrderedSet<Fad> deployedFads = new ListOrderedSet<>();
    private final FadInitializer fadInitializer;
    private ActiveActionRegulations actionSpecificRegulations;
    private Fisher fisher;
    private int numFadsInStock;

    public FadManager(
        FadMap fadMap,
        FadInitializer fadInitializer,
        int numFadsInStock
    ) {
        this(
            fadMap,
            fadInitializer,
            numFadsInStock,
            new HashSet<>(),
            new HashSet<>(),
            new HashSet<>(),
            Optional.empty(),
            new ActiveActionRegulations()
        );
    }

    public FadManager(
        FadMap fadMap,
        FadInitializer fadInitializer,
        int numFadsInStock,
        Iterable<Observer<DeployFad>> fadDeploymentObservers,
        Iterable<Observer<MakeFadSet>> fadSetObservers,
        Iterable<Observer<MakeUnassociatedSet>> unassociatedSetObservers,
        Optional<GroupingMonitor<Species, BiomassLostEvent, Double>> biomassLostMonitor,
        ActiveActionRegulations actionSpecificRegulations
    ) {
        checkArgument(numFadsInStock >= 0);
        this.fadMap = fadMap;
        this.fadInitializer = fadInitializer;
        this.numFadsInStock = numFadsInStock;
        this.biomassLostMonitor = biomassLostMonitor;
        this.actionSpecificRegulations = actionSpecificRegulations;

        fadDeploymentObservers.forEach(observer -> registerObserver(DeployFad.class, observer));
        fadSetObservers.forEach(observer -> registerObserver(MakeFadSet.class, observer));
        unassociatedSetObservers.forEach(observer -> registerObserver(MakeUnassociatedSet.class, observer));
        biomassLostMonitor.ifPresent(observer -> registerObserver(BiomassLostEvent.class, observer));
        setActionSpecificRegulations(actionSpecificRegulations);
    }

    public <T> void registerObserver(Class<T> observedClass, Observer<? super T> observer) {
        observers.register(observedClass, observer);
    }

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
        observers.unregister(this.actionSpecificRegulations);
        this.actionSpecificRegulations = actionSpecificRegulations;
        POSSIBLE_ACTIONS.forEach(actionClass -> registerObserver(actionClass, actionSpecificRegulations));
    }

    public <O> void reactTo(final O observable) {
        this.observers.reactTo(observable);
    }

    public Optional<GroupingMonitor<Species, BiomassLostEvent, Double>> getBiomassLostMonitor() { return biomassLostMonitor; }

}