package uk.ac.ox.oxfish.regulation.counters;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.maybeGetFadManager;

public class NumberOfActiveFads implements AlgorithmFactory<Quantity> {

    private static final Quantity NUMBER_OF_ACTIVE_FADS = action -> {
        checkArgument(action instanceof PurseSeinerAction);
        final Fisher fisher = ((PurseSeinerAction) action).getFisher();
        return maybeGetFadManager(fisher)
            .map(FadManager::getNumDeployedFads)
            .orElse(0);
    };

    public NumberOfActiveFads() {
    }

    @Override
    public Quantity apply(final FishState fishState) {
        return NUMBER_OF_ACTIVE_FADS;
    }
}
