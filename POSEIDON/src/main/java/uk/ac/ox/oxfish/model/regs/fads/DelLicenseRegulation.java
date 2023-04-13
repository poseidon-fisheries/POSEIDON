package uk.ac.ox.oxfish.model.regs.fads;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;

public class DelLicenseRegulation implements ActionSpecificRegulation {
    @Override
    public ImmutableSet<Class<? extends PurseSeinerAction>> getApplicableActions() {
        return ImmutableSet.of(DolphinSetAction.class);
    }

    @Override
    public boolean isForbidden(final Class<? extends PurseSeinerAction> action, final Fisher fisher) {
        return !fisher.getTags().contains("has_del_license");
    }
}
