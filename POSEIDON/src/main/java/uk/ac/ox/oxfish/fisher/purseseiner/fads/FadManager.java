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
import sim.util.Bag;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observers;
import uk.ac.ox.poseidon.agents.api.YearlyActionCounter;
import uk.ac.ox.poseidon.common.api.Observer;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import javax.measure.quantity.Mass;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear.maybeGetPurseSeineGear;
import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FadManager {

    private static final List<Class<? extends PurseSeinerAction>> POSSIBLE_ACTIONS =
        ImmutableList.of(
            FadDeploymentAction.class,
            AbstractFadSetAction.class,
            DolphinSetAction.class,
            NonAssociatedSetAction.class
        );
    private final Regulation regulation;
    private final FadMap fadMap;
    private final Observers observers = new Observers();
    private final YearlyActionCounter yearlyActionCounter;
    private final Optional<GroupingMonitor<Species, BiomassLostEvent, Double, Mass>>
        biomassLostMonitor;
    private final ListOrderedSet<Fad> deployedFads = new ListOrderedSet<>();
    private final FadInitializer<?, ?> fadInitializer;
    private final FishValueCalculator fishValueCalculator;
    private Fisher fisher;
    private int numFadsInStock;

    public FadManager(
        final Regulation regulation,
        final FadMap fadMap,
        final FadInitializer<?, ?> fadInitializer,
        final YearlyActionCounter yearlyActionCounter,
        final FishValueCalculator fishValueCalculator
    ) {
        this(
            regulation,
            fadMap,
            fadInitializer,
            yearlyActionCounter,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            Optional.empty(),
            fishValueCalculator
        );
    }

    /**
     * Technical note: we're using raw types for the observer type parameters because those need to
     * be supertypes of whatever class object we register the observers with, and because of type
     * erasure, Java is unable to infer that relationship when generics come into play.
     */
    @SuppressWarnings("rawtypes")
    public FadManager(
        final Regulation regulation,
        final FadMap fadMap,
        final FadInitializer<?, ?> fadInitializer,
        final YearlyActionCounter yearlyActionCounter,
        final Iterable<Observer<FadDeploymentAction>> fadDeploymentObservers,
        final Iterable<Observer<AbstractSetAction>> allSetsObservers,
        final Iterable<Observer<AbstractFadSetAction>> fadSetObservers,
        final Iterable<Observer<NonAssociatedSetAction>> nonAssociatedSetObservers,
        final Iterable<Observer<DolphinSetAction>> dolphinSetObservers,
        final Optional<GroupingMonitor<Species, BiomassLostEvent, Double, Mass>> biomassLostMonitor,
        final FishValueCalculator fishValueCalculator
    ) {
        this.regulation = regulation;
        this.fadMap = fadMap;
        this.fadInitializer = fadInitializer;
        this.yearlyActionCounter = yearlyActionCounter;
        this.biomassLostMonitor = biomassLostMonitor;
        this.fishValueCalculator = fishValueCalculator;

        if (yearlyActionCounter != null) {
            Stream.of(
                FadDeploymentAction.class,
                FadSetAction.class,
                OpportunisticFadSetAction.class,
                NonAssociatedSetAction.class,
                DolphinSetAction.class
            ).forEach(actionClass ->
                registerObserver(actionClass, yearlyActionCounter)
            );
        }

        fadDeploymentObservers.forEach(observer -> registerObserver(
            FadDeploymentAction.class,
            observer
        ));
        allSetsObservers.forEach(observer -> {
            registerObserver(FadSetAction.class, observer);
            registerObserver(OpportunisticFadSetAction.class, observer);
            registerObserver(NonAssociatedSetAction.class, observer);
            registerObserver(DolphinSetAction.class, observer);
        });
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
    }

    public <T> void registerObserver(
        final Class<T> observedClass,
        final Observer<? super T> observer
    ) {
        observers.register(observedClass, observer);
    }

    public static FadManager getFadManager(
        final Fisher fisher
    ) {
        return maybeGetFadManager(fisher).orElseThrow(() -> new IllegalArgumentException(
            "PurseSeineGear required to get FadManager instance. Fisher "
                + fisher + " is using " + fisher.getGear().getClass() + "."
        ));
    }

    public static Optional<FadManager> maybeGetFadManager(
        final Fisher fisher
    ) {
        return maybeGetPurseSeineGear(fisher).map(PurseSeineGear::getFadManager);
    }

    public YearlyActionCounter getYearlyActionCounter() {
        return yearlyActionCounter;
    }

    public Regulation getRegulation() {
        return regulation;
    }

    public FishValueCalculator getFishValueCalculator() {
        return fishValueCalculator;
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

    public Set<Fad> getDeployedFads() {
        return Collections.unmodifiableSet(deployedFads);
    }


    public Fisher getFisher() {
        return fisher;
    }

    public void setFisher(final Fisher fisher) {
        this.fisher = fisher;
    }

    public Stream<Fad> getFadsAt(final SeaTile location) {
        return bagToStream(fadMap.fadsAt(location));
    }

    public void loseFad(final Fad fad) {
        checkArgument(deployedFads.contains(fad));
        deployedFads.remove(fad);
        fad.lose();
    }

    public Fad deployFadInCenterOfTile(final SeaTile seaTile, final MersenneTwisterFast rng) {
        final Double2D location = new Double2D(seaTile.getGridX() + 0.5, seaTile.getGridY() + 0.5);
        return deployFad(seaTile, location, rng);
    }

    public Fad deployFad(final SeaTile seaTile, final Double2D location, final MersenneTwisterFast rng) {
        final Fad newFad = initFad(seaTile, rng);
        fadMap.deployFad(newFad, location);
        return newFad;
    }

    private Fad initFad(final SeaTile tile, final MersenneTwisterFast rng) {
        checkState(numFadsInStock >= 1, "No FADs in stock!");
        numFadsInStock--;
        final Fad newFad = fadInitializer.makeFad(
            this,
            fisher,
            tile,
            rng
        );
        deployedFads.add(newFad);
        return newFad;
    }

    /**
     * Deploys a FAD at a random position in the given sea tile.
     */
    public Fad deployFad(final SeaTile seaTile, final MersenneTwisterFast random) {
        final Double2D location = new Double2D(
            seaTile.getGridX() + random.nextDouble(),
            seaTile.getGridY() + random.nextDouble()
        );
        return deployFad(seaTile, location, random);
    }

    public <O> void reactTo(final O observable) {
        this.observers.reactTo(observable);
    }

    public <O> void reactTo(final Class<O> observedClass, final Supplier<O> observableSupplier) {
        this.observers.reactTo(observedClass, observableSupplier);
    }

    public Optional<
        GroupingMonitor<Species, BiomassLostEvent, Double, Mass>
        > getBiomassLostMonitor() {
        return biomassLostMonitor;
    }

    public Bag fadsAt(final SeaTile seaTile) {
        return getFadMap().fadsAt(seaTile);
    }

    public FadMap getFadMap() {
        return fadMap;
    }

    /**
     * how many active fads  can the owner of this manager still drop?
     */
    public int getHowManyActiveFadsCanWeStillDeploy() {
        // TODO: reimplement with new regulation system
        throw new RuntimeException("Needs to be reimplemented with new regulation system.");
    }

    public int getNumberOfRemainingYearlyActions(final Class<? extends PurseSeinerAction> purseSeinerAction) {
        // TODO: reimplement with new regulation system
        throw new RuntimeException("Needs to be reimplemented with new regulation system.");
    }

    /**
     * Increments the number of FADs in stock by one.
     */
    public void putFadBackInStock() {
        numFadsInStock++;
    }

}