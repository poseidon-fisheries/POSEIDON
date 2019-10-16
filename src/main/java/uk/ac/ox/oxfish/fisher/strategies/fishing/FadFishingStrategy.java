package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.actions.fads.DeployFad;
import uk.ac.ox.oxfish.fisher.actions.fads.FadAction;
import uk.ac.ox.oxfish.fisher.actions.fads.MakeFadSet;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.FadDestinationStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Map;
import java.util.Optional;

import static java.lang.StrictMath.exp;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparingDouble;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.fadsHere;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;
import static uk.ac.ox.oxfish.utility.Measures.toHours;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FadFishingStrategy implements FishingStrategy, FadManagerUtils {

    private Optional<? extends FadAction> nextAction = Optional.empty();
    private double fadsDeploymentCoefficient;
    private double ownFadsSettingCoefficient;
    private double otherFadsSettingCoefficient;
    private double probabilityDecayCoefficient;

    private int numConsecutiveActions = 0;

    public FadFishingStrategy(
        double fadsDeploymentCoefficient,
        double ownFadsSettingCoefficient,
        double otherFadsSettingCoefficient,
        double probabilityDecayCoefficient
    ) {
        this.fadsDeploymentCoefficient = fadsDeploymentCoefficient;
        this.ownFadsSettingCoefficient = ownFadsSettingCoefficient;
        this.otherFadsSettingCoefficient = otherFadsSettingCoefficient;
        this.probabilityDecayCoefficient = probabilityDecayCoefficient;
    }

    private double probability(double coefficient, double value) {
        return (1.0 - exp(-coefficient * (value + 1))) *
            (1.0 / (1.0 + (probabilityDecayCoefficient * numConsecutiveActions)));
    }

    @Override
    public boolean shouldFish(
        Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip
    ) {
        if (!nextAction.isPresent()) {
            nextAction = maybeMakeFadSet(model, fisher);
            if (!nextAction.isPresent()) {
                nextAction = maybeDeployFad(model, fisher);
            }
        }
        return nextAction.isPresent();
    }

    private double fadSetProbability(Fad fad, Fisher fisher) {
        final double coefficient = fad.getOwner() == getFadManager(fisher) ?
            ownFadsSettingCoefficient :
            otherFadsSettingCoefficient;
        // TODO: use fish prices instead of biomass
        final double fadBiomass = stream(fad.getBiology().getCurrentBiomass()).sum();
        return probability(coefficient, fadBiomass);
    }

    private Optional<? extends FadAction> maybeMakeFadSet(FishState model, Fisher fisher) {
        return fadsHere(fisher)
            .map(fad -> new Pair<>(fad, fadSetProbability(fad, fisher)))
            .sorted(comparingDouble(Pair::getSecond))
            .filter(pair -> model.getRandom().nextDouble() < pair.getSecond())
            .map(pair -> new MakeFadSet(pair.getFirst()))
            .findFirst()
            .filter(action -> action.isAllowed(model, fisher) && action.isPossible(model, fisher));
    }

    private Optional<? extends FadAction> maybeDeployFad(FishState model, Fisher fisher) {

        final Map<SeaTile, Double> deploymentLocationValues =
            ((FadDestinationStrategy) fisher.getDestinationStrategy())
                .getFadDeploymentDestinationStrategy()
                .getDeploymentLocationValues();
        return Optional
            .ofNullable(deploymentLocationValues.get(fisher.getLocation()))
            .map(value -> probability(fadsDeploymentCoefficient, value))
            .filter(p -> model.getRandom().nextDouble() < p)
            .map(__ -> new DeployFad(fisher.getLocation()))
            .filter(action -> action.isAllowed(model, fisher) && action.isPossible(model, fisher));
    }

    @Override
    @NotNull
    public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        nextAction = nextAction.filter(action -> hoursLeft >= toHours(action.getDuration()));
        numConsecutiveActions = nextAction.isPresent() ? numConsecutiveActions + 1 : 0;
        final ActionResult actionResult = nextAction
            .map(action -> new ActionResult(action, hoursLeft))
            .orElse(new ActionResult(new Arriving(), 0));
        nextAction = Optional.empty();
        return actionResult;
    }

    @Override
    public void start(FishState model, Fisher fisher) { }

    @Override
    public void turnOff(Fisher fisher) { }

}
