package uk.ac.ox.oxfish.fisher.strategies.destination;

import static java.util.stream.Collectors.toCollection;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.oneOfDeployedFads;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.fads.DeployFad;
import uk.ac.ox.oxfish.fisher.actions.fads.FadAction;
import uk.ac.ox.oxfish.fisher.actions.fads.MakeFadSet;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.Pair;

public class RandomPlanFadDestinationStrategy extends FadDestinationStrategy {

    private List<Pair<
        Function<Fisher, FadAction>,
        Function<Fisher, Boolean>
        >> possibleActions = new ArrayList<>();

    private int numberOfStepsToPlan;

    public RandomPlanFadDestinationStrategy(NauticalMap map, int numberOfStepsToPlan) {
        super(map);
        this.numberOfStepsToPlan = numberOfStepsToPlan;

        /*
           Note that the filter conditions are a bit naive for now: the number of FADs in stock
           taken into account is the one before the trip starts, so it's entirely possible that
           the fisher runs out while at sea. Conversely, the deployed FADs are also the
           one before the trip starts, so the fisher doesn't get to plan on setting on a FAD
           deployed in the same trip.
         */
        possibleActions.add(new Pair<>(
            fisher -> new DeployFad(map.getRandomBelowWaterLineSeaTile(fisher.grabRandomizer())),
            fisher -> getFadManager(fisher).getNumFadsInStock() > 0
        ));
        possibleActions.add(new Pair<>(
            fisher -> new MakeFadSet(oneOfDeployedFads(fisher)
                .orElseThrow(() -> new RuntimeException("No deployed FAD!"))),
            fisher -> !getFadManager(fisher).getDeployedFads().isEmpty()
        ));
    }

    @Override
    void makeNewPlan(Fisher fisher) {

        actionQueue.addAll(Stream
            .generate(() -> oneOf(possibleActions, fisher.grabRandomizer())
                .orElseThrow(() -> new RuntimeException("No possible action!"))
            )
            .filter(pair -> pair.getSecond().apply(fisher))
            .map(pair -> pair.getFirst().apply(fisher))
            .limit(numberOfStepsToPlan)
            .collect(toCollection(ArrayList::new)));
    }

}
