package uk.ac.ox.oxfish.fisher.strategies.fishing;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AtomicLongMap;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.actions.fads.DeployFad;
import uk.ac.ox.oxfish.fisher.actions.fads.FadAction;
import uk.ac.ox.oxfish.fisher.actions.fads.MakeFadSet;
import uk.ac.ox.oxfish.fisher.actions.fads.MakeUnassociatedSet;
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

    private final AtomicLongMap<Class<? extends FadAction>> consecutiveActionCounts = AtomicLongMap.create();
    private Optional<? extends FadAction> nextAction = Optional.empty();
    private double fadDeploymentsCoefficient;
    private double setsOnOwnFadsCoefficient;
    private double setsOnOtherFadsCoefficient;
    private double unassociatedSetsCoefficient;

    private double fadDeploymentsProbabilityDecay;
    private double fadSetsProbabilityDecay;
    private double unassociatedSetsProbabilityDecay;

    private final ImmutableList<BiFunction<FishState, Fisher, Optional<? extends FadAction>>> actionSequence =
        ImmutableList.of(
            this::maybeDeployFad,
            this::maybeMakeFadSet,
            this::maybeMakeUnassociatedSet
        );

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
        Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip
    ) {
        if (fisher.getLocation().isLand()) return false;
        if (!nextAction.isPresent()) {
            nextAction = actionSequence.stream().flatMap(a -> stream(a.apply(model, fisher))).findFirst();
        }
        return nextAction.isPresent();
    }

    Optional<? extends FadAction> maybeDeployFad(FishState model, Fisher fisher) {
        final Map<SeaTile, Double> deploymentLocationValues =
            ((FadDestinationStrategy) fisher.getDestinationStrategy())
                .getFadDeploymentRouteSelector()
                .getDeploymentLocationValues();
        return Optional
            .ofNullable(deploymentLocationValues.get(fisher.getLocation()))
            .map(value -> probability(fadDeploymentsCoefficient, value, consecutiveActionCounts.get(DeployFad.class), fadDeploymentsProbabilityDecay))
            .filter(model.getRandom()::nextBoolean)
            .map(__ -> new DeployFad(model, fisher))
            .filter(action -> action.isAllowed() && action.isPossible());
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

    private Optional<? extends FadAction> maybeMakeFadSet(FishState model, Fisher fisher) {
        return fadsHere(fisher)
            .map(fad -> new Pair<>(fad, fadSetProbability(fad, fisher)))
            .filter(pair -> model.getRandom().nextDouble() < pair.getSecond())
            .sorted(comparingDouble(Pair::getSecond))
            .map(pair -> new MakeFadSet(model, fisher, pair.getFirst()))
            .filter(action -> action.isAllowed() && action.isPossible())
            .findFirst();
    }

    private double fadSetProbability(Fad fad, Fisher fisher) {
        final double coefficient = fad.getOwner() == getFadManager(fisher) ?
            setsOnOwnFadsCoefficient :
            setsOnOtherFadsCoefficient;
        final long numConsecutiveActions = consecutiveActionCounts.get(MakeFadSet.class);
        return probability(coefficient, fad.valueOfSet(fisher), numConsecutiveActions, fadSetsProbabilityDecay);
    }

    private Optional<? extends FadAction> maybeMakeUnassociatedSet(FishState model, Fisher fisher) {
        return Optional.of(new MakeUnassociatedSet(model, fisher))
            .filter(action -> action.isAllowed() && action.isPossible())
            .filter(action -> {
                final double priceOfFishHere = priceOfFishHere(fisher.getLocation().getBiology(), getMarkets(fisher));
                final long numConsecutiveActions = consecutiveActionCounts.get(MakeUnassociatedSet.class);
                final double probability = probability(unassociatedSetsCoefficient, priceOfFishHere, numConsecutiveActions, unassociatedSetsProbabilityDecay);
                return model.getRandom().nextDouble() < probability;
            });
    }

    @Override
    @NotNull
    public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        nextAction = nextAction.filter(action -> hoursLeft >= toHours(action.getDuration()));
        // If we have a next action, increment its counter
        nextAction.map(FadAction::getClass).ifPresent(consecutiveActionCounts::incrementAndGet);
        if (!nextAction.isPresent()) consecutiveActionCounts.clear();
        final ActionResult actionResult = nextAction
            .map(action -> new ActionResult(action, hoursLeft))
            .orElse(new ActionResult(new Arriving(), 0));
        nextAction = Optional.empty();
        return actionResult;
    }

}
