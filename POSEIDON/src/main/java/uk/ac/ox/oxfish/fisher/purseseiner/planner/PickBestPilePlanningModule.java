package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.List;
import java.util.PriorityQueue;

/**
 * weighs each fad by an abstract function and always picks the discretized area that maximizes the SUM
 * of weights
 */
public abstract class PickBestPilePlanningModule extends DiscretizedOwnFadPlanningModule {
    public PickBestPilePlanningModule(OwnFadSetDiscretizedActionGenerator optionsGenerator) {
        super(optionsGenerator);
    }

    @Override
    protected PlannedAction chooseFadSet(
        Plan currentPlanSoFar, Fisher fisher,
        FishState model, NauticalMap map,
        OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {

        final int now = model.getStep();

        List<Pair<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>, Integer>> options =
            optionsGenerator.peekAllFads();

        //if there are no options, don't bother
        if (options == null || options.isEmpty())
            return null;
        //if there is only one option, also don't bother
        if (options.size() == 1) {
            if (options.get(0).getSecond() > 0)
                return optionsGenerator.chooseFad(options.get(0).getSecond());
            else return null;
        }


        //go through every valid discretized list of fads
        double bestWeight = -1;
        int fadGroupChosen = -1;
        for (Pair<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>, Integer> option : options) {
            double weightHere =
                option.getFirst().stream().mapToDouble(
                    valuedFad -> {

                        return weighFad(now, valuedFad);
                    }
                ).sum();
            if (weightHere > bestWeight) {
                bestWeight = weightHere;
                fadGroupChosen = option.getSecond();
            }
        }


        //all fads are empty, don't bother setting on any!
        if (fadGroupChosen < 0 ||
            fadGroupChosen >= optionsGenerator.getNumberOfGroups())
            return null;
        return optionsGenerator.chooseFad(fadGroupChosen);
    }

    protected abstract double weighFad(
        int currentModelStep,
        OwnFadSetDiscretizedActionGenerator.ValuedFad valuedFad
    );
}
