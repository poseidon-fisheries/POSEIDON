/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.strategies.fishing;

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
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.FadDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FadGravityDestinationStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.StrictMath.exp;
import static java.util.Comparator.comparingDouble;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.fadsHere;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.priceOfFishHere;
import static uk.ac.ox.oxfish.utility.Measures.toHours;

public class FadFishingThresholdStrategy implements FishingStrategy, FadManagerUtils {

    private final AtomicLongMap<Class<? extends FadAction>> consecutiveActionCounts = AtomicLongMap.create();
    private Optional<? extends FadAction> nextAction = Optional.empty();
    private double fadDeploymentsCoefficient;
    private double setsOnOtherFadsCoefficient;
    private double unassociatedSetsCoefficient;

    private double fadDeploymentsProbabilityDecay;
    final private double minFadValue;

    public FadFishingThresholdStrategy(
            double unassociatedSetsCoefficient,
            double fadDeploymentsCoefficient,
            double setsOnOtherFadsCoefficient,
            double fadDeploymentsProbabilityDecay,
            double minFadValue) {
        this.unassociatedSetsCoefficient = unassociatedSetsCoefficient;
        this.fadDeploymentsCoefficient = fadDeploymentsCoefficient;
        this.setsOnOtherFadsCoefficient = setsOnOtherFadsCoefficient;
        this.fadDeploymentsProbabilityDecay = fadDeploymentsProbabilityDecay;
        this.minFadValue = minFadValue;
    }

    @Override
    public boolean shouldFish(
            Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip
    ) {


//        if(consecutiveActionCounts.sum()>10)
//            return false;

        if (!nextAction.isPresent()) {
            nextAction = maybeDeployFad(model, fisher);
        }

        if(!nextAction.isPresent()) {
            if (random.nextDouble() < unassociatedSetsCoefficient)
                nextAction = maybeMakeUnassociatedSet(model, fisher);
        }
        if(!nextAction.isPresent()) {
            nextAction = maybeMakeFadSet(model, fisher);
        }

        return nextAction.isPresent();
    }

    private Optional<? extends FadAction> maybeDeployFad(FishState model, Fisher fisher) {

        final Map<SeaTile, Double> deploymentLocationValues =

                fisher.getDestinationStrategy() instanceof FadDestinationStrategy ?

                        ((FadDestinationStrategy) fisher.getDestinationStrategy())
                                .getFadDeploymentDestinationStrategy()
                                .getDeploymentLocationValues() :
                        ((FadGravityDestinationStrategy) fisher.getDestinationStrategy())
                                .getFadDeploymentDestinationStrategy()
                                .getDeploymentLocationValues()
                ;


        return Optional
                .ofNullable(deploymentLocationValues.get(fisher.getLocation()))
                .map(value -> probability(fadDeploymentsCoefficient, value, consecutiveActionCounts.get(DeployFad.class), fadDeploymentsProbabilityDecay))
                .filter(p -> model.getRandom().nextDouble() < p)
                .map(__ -> new DeployFad())
                .filter(action -> action.isAllowed(model, fisher) && action.isPossible(model, fisher));
    }

    private Optional<? extends FadAction> maybeMakeFadSet(FishState model, Fisher fisher) {
        final FadManager manager =  FadManagerUtils.getFadManager(fisher);
        return fadsHere(fisher)
                .filter(fad ->
                                fad.getOwner()== manager ||

                                        model.getRandom().nextDouble() < setsOnOtherFadsCoefficient)
                .map(fad -> new Pair<>(fad, setValue(fad, fisher))).filter(
                        new Predicate<Pair<Fad, Double>>() {
                            @Override
                            public boolean test(
                                    Pair<Fad, Double> fadDoublePair) {
                                //           System.out.println(fadDoublePair.getSecond());
                                return fadDoublePair.getSecond()> minFadValue; //100000;
                            }
                        }
                )
                .sorted(comparingDouble(Pair::getSecond))
                .map(pair -> new MakeFadSet((PurseSeineGear) fisher.getGear(), model.getRandom(), pair.getFirst()))
                .filter(action -> action.isAllowed(model, fisher) && action.isPossible(model, fisher))
                .findFirst();
    }

    private Optional<? extends FadAction> maybeMakeUnassociatedSet(FishState model, Fisher fisher) {
        return Optional.of(new MakeUnassociatedSet((PurseSeineGear) fisher.getGear(), model.getRandom()))
                .filter(action -> action.isAllowed(model, fisher) && action.isPossible(model, fisher));
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

    private double setValue(Fad fad, Fisher fisher) {
        return  priceOfFishHere(fad.getBiology(), getMarkets(fisher));
    }

    private Collection<Market> getMarkets(Fisher fisher) {
        return fisher.getHomePort().getMarketMap(fisher).getMarkets();
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
