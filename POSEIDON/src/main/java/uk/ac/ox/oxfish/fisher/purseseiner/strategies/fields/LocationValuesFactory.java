package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.LocationFisherValuesByActionCache;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

import java.util.Map;

public abstract class LocationValuesFactory implements AlgorithmFactory<LocationValueByActionClass> {
    private static final LocationFisherValuesByActionCache locationValuesCache =
        new LocationFisherValuesByActionCache();
    private InputPath locationValuesFile;
    private IntegerParameter targetYear;

    @SuppressWarnings("WeakerAccess")
    public LocationValuesFactory() {
    }

    public LocationValuesFactory(
        final InputPath locationValuesFile,
        final IntegerParameter targetYear
    ) {
        this.locationValuesFile = locationValuesFile;
        this.targetYear = targetYear;
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    @SuppressWarnings("unused")
    public InputPath getLocationValuesFile() {
        return locationValuesFile;
    }

    @SuppressWarnings("unused")
    public void setLocationValuesFile(final InputPath locationValuesFile) {
        this.locationValuesFile = locationValuesFile;
    }

    Map<Int2D, Double> loadLocationValues(
        final Fisher fisher,
        final Class<? extends PurseSeinerAction> actionClass
    ) {
        return locationValuesCache.getLocationValues(
            locationValuesFile.get(),
            targetYear.getValue(),
            fisher,
            actionClass
        );
    }
}
