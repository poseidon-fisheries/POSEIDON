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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear.maybeGetPurseSeineGear;
import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.measure.quantity.Mass;
import org.apache.commons.collections15.set.ListOrderedSet;
import sim.util.Bag;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
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

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FadManager<B extends LocalBiology, F extends Fad<B, F>> {

    private static final List<Class<? extends PurseSeinerAction>> POSSIBLE_ACTIONS =
        ImmutableList.of(
            FadDeploymentAction.class,
            AbstractFadSetAction.class,
            DolphinSetAction.class,
            NonAssociatedSetAction.class
        );
    private final FadMap<B, F> fadMap;
    private final Observers observers = new Observers();
    private final Optional<GroupingMonitor<Species, BiomassLostEvent, Double, Mass>>
        biomassLostMonitor;
    private final ListOrderedSet<F> deployedFads = new ListOrderedSet<>();
    private final FadInitializer<B, F> fadInitializer;
    private ActiveActionRegulations actionSpecificRegulations;
    private Fisher fisher;
    private int numFadsInStock;

    public FadManager(
        final FadMap<B, F> fadMap,
        final FadInitializer<B, F> fadInitializer
    ) {
        this(
            fadMap,
            fadInitializer,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            Optional.empty(),
            new ActiveActionRegulations()
        );
    }

    /**
     * Technical note: we're using raw types for the observer type parameters because those need to
     * be supertypes of whatever class object we register the observers with, and because of type
     * erasure, Java is unable to infer that relationship when generics come into play.
     */
    @SuppressWarnings("rawtypes")
    public FadManager(
        final FadMap<B, F> fadMap,
        final FadInitializer<B, F> fadInitializer,
        final Iterable<Observer<FadDeploymentAction>> fadDeploymentObservers,
        final Iterable<Observer<AbstractFadSetAction>> fadSetObservers,
        final Iterable<Observer<NonAssociatedSetAction>> nonAssociatedSetObservers,
        final Iterable<Observer<DolphinSetAction>> dolphinSetObservers,
        final Optional<GroupingMonitor<Species, BiomassLostEvent, Double, Mass>> biomassLostMonitor,
        final ActiveActionRegulations actionSpecificRegulations
    ) {
        this.fadMap = fadMap;
        this.fadInitializer = fadInitializer;
        this.biomassLostMonitor = biomassLostMonitor;
        this.actionSpecificRegulations = actionSpecificRegulations;

        fadDeploymentObservers.forEach(observer -> registerObserver(
            FadDeploymentAction.class,
            observer
        ));
        fadSetObservers.forEach(observer -> {
            registerObserver(FadSetAction.class, observer);
            registerObserver(OpportunisticFadSetAction.class, observer);
        });
        nonAssociatedSetObservers.forEach(observer -> registerObserver(
            NonAssociatedSetAction.class,
            observer
        ));
        dolphinSetObservers.forEach(observer -> registerObserver(
            DolphinSetAction.class,
            observer
        ));
        biomassLostMonitor.ifPresent(observer -> registerObserver(
            BiomassLostEvent.class,
            observer
        ));
        setActionSpecificRegulations(actionSpecificRegulations);
    }

    public <T> void registerObserver(
        final Class<T> observedClass,
        final Observer<? super T> observer
    ) {
        observers.register(observedClass, observer);
    }

    public static FadManager<? extends LocalBiology, ? extends Fad<?, ?>> getFadManager(
        final Fisher fisher
    ) {
        return maybeGetFadManager(fisher).orElseThrow(() -> new IllegalArgumentException(
            "PurseSeineGear required to get FadManager instance. Fisher "
                + fisher + " is using " + fisher.getGear().getClass() + "."
        ));
    }

    public static Optional<
        FadManager<? extends LocalBiology, ? extends Fad<?, ?>>
        > maybeGetFadManager(
        final Fisher fisher
    ) {
        return maybeGetPurseSeineGear(fisher).map(PurseSeineGear::getFadManager);
    }

    public int getNumFadsInStock() {
        return numFadsInStock;
    }

    public void setNumFadsInStock(final int numFadsInStock) {
        this.numFadsInStock = numFadsInStock;
    }

    public int getNumDeployedFads() {
        return deployedFads.size();
    }

    public Set<F> getDeployedFads() {
        return Collections.unmodifiableSet(deployedFads);
    }

    public Fisher getFisher() {
        return fisher;
    }

    public void setFisher(final Fisher fisher) {
        this.fisher = fisher;
    }

    public Stream<F> getFadsAt(final SeaTile location) {
        return bagToStream(fadMap.fadsAt(location));
    }

    public void loseFad(final F fad) {
        checkArgument(deployedFads.contains(fad));
        deployedFads.remove(fad);
    }

    public F deployFad(final SeaTile seaTile) {
        final F newFad = initFad(seaTile);
        fadMap.deployFad(newFad, seaTile);
        return newFad;
    }

    private F initFad(SeaTile tile) {
        checkState(numFadsInStock >= 1, "No FADs in stock!");
        numFadsInStock--;
        final F newFad = fadInitializer.makeFad(this,
                fisher,
                tile
                );
        deployedFads.add(newFad);
        return newFad;
    }

    /**
     * Deploys a FAD at a random position in the given sea tile.
     */
    public void deployFad(final SeaTile seaTile, final MersenneTwisterFast random) {
        Double2D location = new Double2D(
                seaTile.getGridX() + random.nextDouble(),
                seaTile.getGridY() + random.nextDouble()
        );
        final F newFad = initFad(seaTile);
        fadMap.deployFad(newFad, location);
    }



    public ActiveActionRegulations getActionSpecificRegulations() {
        return actionSpecificRegulations;
    }

    public void setActionSpecificRegulations(
        final Stream<ActionSpecificRegulation> actionSpecificRegulations
    ) {
        setActionSpecificRegulations(new ActiveActionRegulations(actionSpecificRegulations));
    }

    private void setActionSpecificRegulations(
        final ActiveActionRegulations actionSpecificRegulations
    ) {
        observers.unregister(this.actionSpecificRegulations);
        this.actionSpecificRegulations = actionSpecificRegulations;
        POSSIBLE_ACTIONS.forEach(actionClass -> registerObserver(
            actionClass,
            actionSpecificRegulations
        ));
    }

    public <O> void reactTo(final O observable) {
        this.observers.reactTo(observable);
    }

    public Optional<
        GroupingMonitor<Species, BiomassLostEvent, Double, Mass>
        > getBiomassLostMonitor() {
        return biomassLostMonitor;
    }

    public Bag fadsAt(final SeaTile seaTile) {
        return getFadMap().fadsAt(seaTile);
    }

    public FadMap<B, F> getFadMap() {
        return fadMap;
    }

}