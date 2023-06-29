package uk.ac.ox.poseidon.regulations.core;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.Collection;
import java.util.Set;

public class ActionSpecificRegulation<C> extends ConditionnalRegulation<C> {

    private final Set<String> actionCodes;

    public ActionSpecificRegulation(
        final Collection<String> actionCodes,
        final Regulation<? super C> delegate
    ) {
        super(delegate);
        this.actionCodes = ImmutableSet.copyOf(actionCodes);
    }

    @Override
    public boolean test(final Action action, final C context) {
        return actionCodes.contains(action.getCode());
    }
}
