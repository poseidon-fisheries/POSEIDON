package uk.ac.ox.oxfish.regulations.factories;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.ToIntFunction;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.maybeGetFadManager;

public class ActiveFadsCounter implements AlgorithmFactory<ToIntFunction<Action>> {

    private static final ToIntFunction<Action> numberOfActiveFads = action -> {
        checkArgument(action instanceof PurseSeinerAction);
        final Fisher fisher = ((PurseSeinerAction) action).getFisher();
        return maybeGetFadManager(fisher)
            .map(FadManager::getNumDeployedFads)
            .orElse(0);
    };

    public ActiveFadsCounter() {
    }

    @Override
    public ToIntFunction<Action> apply(final FishState fishState) {
        return numberOfActiveFads;
    }
}
