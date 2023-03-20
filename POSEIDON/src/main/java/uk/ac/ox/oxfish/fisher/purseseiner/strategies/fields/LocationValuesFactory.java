package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableMap;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.LocationFisherValuesByActionCache;
import uk.ac.ox.oxfish.model.scenario.InputPath;

import java.util.Map;
import java.util.function.Supplier;

import static uk.ac.ox.oxfish.model.scenario.EpoScenario.TARGET_YEAR;

public class LocationValuesFactory
    implements Supplier<Map<Class<? extends PurseSeinerAction>, LocationValues>> {
    private static final LocationFisherValuesByActionCache locationValuesCache =
        new LocationFisherValuesByActionCache();
    private InputPath locationValuesFile;
    private double decayRateOfOpportunisticFadSetLocationValues;
    private double decayRateOfNonAssociatedSetLocationValues;
    private double decayRateOfDolphinSetLocationValues;
    private double decayRateOfDeploymentLocationValues;
    public LocationValuesFactory() {
    }
    public LocationValuesFactory(final InputPath locationValuesFile) {
        this.locationValuesFile = locationValuesFile;
    }

    @SuppressWarnings("unused")
    public InputPath getLocationValuesFile() {
        return locationValuesFile;
    }

    @SuppressWarnings("unused")
    public void setLocationValuesFile(final InputPath locationValuesFile) {
        this.locationValuesFile = locationValuesFile;
    }

    public double getDecayRateOfOpportunisticFadSetLocationValues() {
        return decayRateOfOpportunisticFadSetLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfOpportunisticFadSetLocationValues(final double decayRateOfOpportunisticFadSetLocationValues) {
        this.decayRateOfOpportunisticFadSetLocationValues = decayRateOfOpportunisticFadSetLocationValues;
    }

    public double getDecayRateOfNonAssociatedSetLocationValues() {
        return decayRateOfNonAssociatedSetLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfNonAssociatedSetLocationValues(final double decayRateOfNonAssociatedSetLocationValues) {
        this.decayRateOfNonAssociatedSetLocationValues = decayRateOfNonAssociatedSetLocationValues;
    }

    public double getDecayRateOfDolphinSetLocationValues() {
        return decayRateOfDolphinSetLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfDolphinSetLocationValues(final double decayRateOfDolphinSetLocationValues) {
        this.decayRateOfDolphinSetLocationValues = decayRateOfDolphinSetLocationValues;
    }

    public double getDecayRateOfDeploymentLocationValues() {
        return decayRateOfDeploymentLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfDeploymentLocationValues(final double decayRateOfDeploymentLocationValues) {
        this.decayRateOfDeploymentLocationValues = decayRateOfDeploymentLocationValues;
    }

    @Override
    public Map<Class<? extends PurseSeinerAction>, LocationValues> get() {
        return ImmutableMap.<Class<? extends PurseSeinerAction>, LocationValues>builder()
            .put(
                FadSetAction.class,
                new FadLocationValues()
            )
            .put(
                OpportunisticFadSetAction.class,
                new OpportunisticFadSetLocationValues(
                    fisher -> loadLocationValues(fisher, OpportunisticFadSetAction.class),
                    getDecayRateOfOpportunisticFadSetLocationValues()
                )
            )
            .put(
                NonAssociatedSetAction.class,
                new NonAssociatedSetLocationValues(
                    fisher -> loadLocationValues(fisher, NonAssociatedSetAction.class),
                    getDecayRateOfNonAssociatedSetLocationValues()
                )
            ).put(
                DolphinSetAction.class,
                new DolphinSetLocationValues(
                    fisher -> loadLocationValues(fisher, DolphinSetAction.class),
                    getDecayRateOfDolphinSetLocationValues()
                )
            )
            .put(
                FadDeploymentAction.class,
                new DeploymentLocationValues(
                    fisher -> loadLocationValues(fisher, FadDeploymentAction.class),
                    getDecayRateOfDeploymentLocationValues()
                )
            )
            .build();
    }

    private Map<Int2D, Double> loadLocationValues(
        final Fisher fisher,
        final Class<? extends PurseSeinerAction> actionClass
    ) {
        return locationValuesCache.getLocationValues(
            locationValuesFile.get(),
            TARGET_YEAR,
            fisher,
            actionClass
        );
    }
}
