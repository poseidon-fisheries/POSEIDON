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

package uk.ac.ox.oxfish.fisher.strategies.fishing;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AtomicLongMap;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.DeployFad;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeFadSet;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeUnassociatedSet;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.fad.FadDestinationStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.google.common.collect.Streams.stream;
import static java.lang.StrictMath.exp;
import static java.util.Comparator.comparingDouble;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.fadsHere;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getMarkets;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.priceOfFishHere;
import static uk.ac.ox.oxfish.utility.Measures.toHours;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "UnstableApiUsage"})
public class FadFishingStrategy implements FishingStrategy, FadManagerUtils {

    private final double fadDeploymentsCoefficient;
    private final double setsOnOwnFadsCoefficient;
    private final double setsOnOtherFadsCoefficient;
    private final double unassociatedSetsCoefficient;
    private final double fadDeploymentsProbabilityDecay;
    private final double fadSetsProbabilityDecay;
    private final double unassociatedSetsProbabilityDecay;
    private final AtomicLongMap<Class<? extends PurseSeinerAction>> consecutiveActionCounts = AtomicLongMap.create();
    private final ImmutableList<BiFunction<FishState, Fisher, Optional<? extends PurseSeinerAction>>> actionSequence =
        ImmutableList.of(
            this::maybeDeployFad,
            this::maybeMakeFadSet,
            this::maybeMakeUnassociatedSet
        );
    private Optional<? extends PurseSeinerAction> nextAction = Optional.empty();

    public FadFishingStrategy(
        double unassociatedSetsCoefficient,
        double fadDeploymentsCoefficient,
        double setsOnOwnFadsCoefficient,
        double setsOnOtherFadsCoefficient,
        double fadDeploymentsProbabilityDecay,
        double fadSetsProbabilityDecay,
        double unassociatedSetsProbabilityDecay
    ) {
        this.unassociatedSetsCoefficient = unassociatedSetsCoefficient;
        this.fadDeploymentsCoefficient = fadDeploymentsCoefficient;
        this.setsOnOwnFadsCoefficient = setsOnOwnFadsCoefficient;
        this.setsOnOtherFadsCoefficient = setsOnOtherFadsCoefficient;
        this.fadDeploymentsProbabilityDecay = fadDeploymentsProbabilityDecay;
        this.fadSetsProbabilityDecay = fadSetsProbabilityDecay;
        this.unassociatedSetsProbabilityDecay = unassociatedSetsProbabilityDecay;
    }

    @Override
    public boolean shouldFish(
        @NotNull Fisher fisher, MersenneTwisterFast random, FishState fishState, TripRecord currentTrip
    ) {
        if (fisher.getLocation().isLand()) return false;
        return updateAndReturnNextAction(fishState, fisher).isPresent();
    }

    Optional<? extends PurseSeinerAction> updateAndReturnNextAction(FishState fishState, Fisher fisher) {
        if (!nextAction.isPresent()) {
            nextAction = actionSequence.stream().flatMap(a -> stream(a.apply(fishState, fisher))).findFirst();
        }
        return nextAction;
    }

    Optional<? extends PurseSeinerAction> maybeDeployFad(@NotNull FishState model, @NotNull Fisher fisher) {
        final Map<SeaTile, Double> deploymentLocationValues =
            ((FadDestinationStrategy) fisher.getDestinationStrategy())
                .getFadDeploymentRouteSelector()
                .getDeploymentLocationValues();
        return Optional
            .ofNullable(deploymentLocationValues.get(fisher.getLocation()))
            .map(value -> probability(
                fadDeploymentsCoefficient,
                value,
                consecutiveActionCounts.get(DeployFad.class),
                fadDeploymentsProbabilityDecay
            ))
            .filter(model.getRandom()::nextBoolean)
            .map(__ -> new DeployFad(model, fisher))
            .filter(PurseSeinerAction::canHappen);
    }

    private double probability(
        double coefficient,
        double value,
        double numConsecutiveActions,
        double probabilityDecayCoefficient
    ) {
        return (1.0 - exp(-coefficient * (value + 1))) /
            (1.0 + (probabilityDecayCoefficient * numConsecutiveActions));
    }

    @NotNull private Optional<? extends PurseSeinerAction> maybeMakeFadSet(FishState model, Fisher fisher) {
        return fadsHere(fisher)
            .map(fad -> new Pair<>(fad, fadSetProbability(fad, fisher)))
            .filter(pair -> model.getRandom().nextDouble() < pair.getSecond())
            .sorted(comparingDouble(Pair::getSecond))
            .map(pair -> new MakeFadSet(model, fisher, pair.getFirst()))
            .filter(PurseSeinerAction::canHappen)
            .findFirst();
    }

    private double fadSetProbability(@NotNull Fad fad, Fisher fisher) {
        final double coefficient = fad.getOwner() == getFadManager(fisher) ?
            setsOnOwnFadsCoefficient :
            setsOnOtherFadsCoefficient;
        final long numConsecutiveActions = consecutiveActionCounts.get(MakeFadSet.class);
        return probability(coefficient, fad.valueOfSet(fisher), numConsecutiveActions, fadSetsProbabilityDecay);
    }

    private Optional<? extends PurseSeinerAction> maybeMakeUnassociatedSet(FishState model, Fisher fisher) {
        return Optional.of(new MakeUnassociatedSet(model, fisher))
            .filter(PurseSeinerAction::canHappen)
            .filter(action -> {
                final double priceOfFishHere = priceOfFishHere(fisher.getLocation().getBiology(), getMarkets(fisher));
                final long numConsecutiveActions = consecutiveActionCounts.get(MakeUnassociatedSet.class);
                final double probability = probability(
                    unassociatedSetsCoefficient,
                    priceOfFishHere,
                    numConsecutiveActions,
                    unassociatedSetsProbabilityDecay
                );
                return model.getRandom().nextDouble() < probability;
            });
    }

    @Override
    @NotNull
    public ActionResult act(
        FishState model, Fisher fisher, Regulation ignored, double hoursLeft
    ) {
        nextAction = nextAction.filter(action -> hoursLeft >= toHours(action.getDuration()));
        // If we have a next action, increment its counter
        nextAction.map(PurseSeinerAction::getClass).ifPresent(consecutiveActionCounts::incrementAndGet);
        if (!nextAction.isPresent()) consecutiveActionCounts.clear();
        final ActionResult actionResult = nextAction
            .map(action -> new ActionResult(action, hoursLeft))
            .orElse(new ActionResult(new Arriving(), 0));
        nextAction = Optional.empty();
        return actionResult;
    }

}
