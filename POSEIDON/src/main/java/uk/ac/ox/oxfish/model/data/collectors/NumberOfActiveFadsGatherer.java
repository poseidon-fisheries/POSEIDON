package uk.ac.ox.oxfish.model.data.collectors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.model.data.Gatherer;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.maybeGetFadManager;

public class NumberOfActiveFadsGatherer implements Gatherer<Fisher> {
    public static final String COLUMN_NAME = "Number of active FADs";
    private static final long serialVersionUID = 723996416595525339L;

    @Override
    public Double apply(final Fisher fisher) {
        return maybeGetFadManager(fisher)
            .map(FadManager::getNumDeployedFads)
            .orElse(0)
            .doubleValue();
    }
}
