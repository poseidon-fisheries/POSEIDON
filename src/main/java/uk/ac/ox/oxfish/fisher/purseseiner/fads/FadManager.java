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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import org.apache.commons.collections15.set.ListOrderedSet;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observer;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observers;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;

import javax.measure.quantity.Mass;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear.maybeGetPurseSeineGear;
import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FadManager {

    private static final List<Class<? extends PurseSeinerAction>> POSSIBLE_ACTIONS = ImmutableList.of(
        FadDeploymentAction.class,
        AbstractFadSetAction.class,
        DolphinSetAction.class,
        NonAssociatedSetAction.class
    );
    private final FadMap fadMap;
    private final Observers observers = new Observers();
    private final Optional<GroupingMonitor<Species, BiomassLostEvent, Double, Mass>> biomassLostMonitor;
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
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            Optional.empty(),
            new ActiveActionRegulations()
        );
    }
    public FadManager(
        FadMap fadMap,
        FadInitializer fadInitializer,
        int numFadsInStock,
        Iterable<Observer<FadDeploymentAction>> fadDeploymentObservers,
        Iterable<Observer<AbstractFadSetAction>> fadSetObservers,
        Iterable<Observer<NonAssociatedSetAction>> nonAssociatedSetObservers,
        Optional<GroupingMonitor<Species, BiomassLostEvent, Double, Mass>> biomassLostMonitor,
        ActiveActionRegulations actionSpecificRegulations
    ) {
        checkArgument(numFadsInStock >= 0);
        this.fadMap = fadMap;
        this.fadInitializer = fadInitializer;
        this.numFadsInStock = numFadsInStock;
        this.biomassLostMonitor = biomassLostMonitor;
        this.actionSpecificRegulations = actionSpecificRegulations;

        fadDeploymentObservers.forEach(observer -> registerObserver(FadDeploymentAction.class, observer));
        fadSetObservers.forEach(observer -> registerObserver(AbstractFadSetAction.class, observer));
        nonAssociatedSetObservers.forEach(observer -> registerObserver(NonAssociatedSetAction.class, observer));
        biomassLostMonitor.ifPresent(observer -> registerObserver(BiomassLostEvent.class, observer));
        setActionSpecificRegulations(actionSpecificRegulations);
    }

    public <T> void registerObserver(Class<T> observedClass, Observer<? super T> observer) {
        observers.register(observedClass, observer);
    }

    public static FadManager getFadManager(Fisher fisher) {
        return maybeGetFadManager(fisher).orElseThrow(() -> new IllegalArgumentException(
            "PurseSeineGear required to get FadManager instance. Fisher " +
                fisher + " is using " + fisher.getGear().getClass() + "."
        ));
    }

    public static Optional<FadManager> maybeGetFadManager(Fisher fisher) {
        return maybeGetPurseSeineGear(fisher).map(PurseSeineGear::getFadManager);
    }

    public int getNumDeployedFads() { return deployedFads.size(); }

    public Optional<Fad> oneOfDeployedFads() {
        return getDeployedFads().isEmpty() ?
            Optional.empty() :
            Optional.of(oneOf(deployedFads, getFisher().grabRandomizer()));
    }

    public Set<Fad> getDeployedFads() { return Collections.unmodifiableSet(deployedFads); }

    public Fisher getFisher() { return fisher; }

    public void setFisher(Fisher fisher) {
        this.fisher = fisher;
    }

    public Stream<Fad> getFadsHere() {
        checkNotNull(fisher);
        return getFadsAt(fisher.getLocation());
    }

    public Stream<Fad> getFadsAt(SeaTile location) {
        return bagToStream(fadMap.fadsAt(location));
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

    private SeaTile getSeaTile(Double2D position) { return getSeaTile(position.x, position.y); }

    private SeaTile getSeaTile(double x, double y) { return getSeaTile((int) x, (int) y); }

    private SeaTile getSeaTile(int x, int y) { return fadMap.getNauticalMap().getSeaTile(x, y); }

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

    public Optional<GroupingMonitor<Species, BiomassLostEvent, Double, Mass>> getBiomassLostMonitor() { return biomassLostMonitor; }

    Stream<Fad> fadsAt(Fisher fisher, SeaTile seaTile) {
        return bagToStream(getFadMap().fadsAt(seaTile));
    }

}