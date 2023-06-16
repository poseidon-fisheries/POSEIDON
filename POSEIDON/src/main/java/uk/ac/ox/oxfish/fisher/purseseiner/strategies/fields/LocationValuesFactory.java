package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.LocationFisherValuesByActionCache;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;

public class LocationValuesFactory
    implements AlgorithmFactory<Map<Class<? extends PurseSeinerAction>, LocationValues>> {
    private static final LocationFisherValuesByActionCache locationValuesCache =
        new LocationFisherValuesByActionCache();
    private InputPath locationValuesFile;
    private DoubleParameter decayRateOfOpportunisticFadSetLocationValues;
    private DoubleParameter decayRateOfNonAssociatedSetLocationValues;
    private DoubleParameter decayRateOfDolphinSetLocationValues;
    private DoubleParameter decayRateOfDeploymentLocationValues;
    private int targetYear;

    public LocationValuesFactory(
        final InputPath locationValuesFile,
        final DoubleParameter decayRateOfOpportunisticFadSetLocationValues,
        final DoubleParameter decayRateOfNonAssociatedSetLocationValues,
        final DoubleParameter decayRateOfDolphinSetLocationValues,
        final DoubleParameter decayRateOfDeploymentLocationValues,
        final int targetYear
    ) {
        this.locationValuesFile = locationValuesFile;
        this.decayRateOfOpportunisticFadSetLocationValues = decayRateOfOpportunisticFadSetLocationValues;
        this.decayRateOfNonAssociatedSetLocationValues = decayRateOfNonAssociatedSetLocationValues;
        this.decayRateOfDolphinSetLocationValues = decayRateOfDolphinSetLocationValues;
        this.decayRateOfDeploymentLocationValues = decayRateOfDeploymentLocationValues;
        this.targetYear = targetYear;
    }

    public LocationValuesFactory() {
    }

    public int getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final int targetYear) {
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

    @Override
    public Map<Class<? extends PurseSeinerAction>, LocationValues> apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        return ImmutableMap.<Class<? extends PurseSeinerAction>, LocationValues>builder()
            .put(
                FadSetAction.class,
                new FadLocationValues()
            )
            .put(
                OpportunisticFadSetAction.class,
                new OpportunisticFadSetLocationValues(
                    fisher -> loadLocationValues(fisher, OpportunisticFadSetAction.class),
                    getDecayRateOfOpportunisticFadSetLocationValues().applyAsDouble(rng)
                )
            )
            .put(
                NonAssociatedSetAction.class,
                new NonAssociatedSetLocationValues(
                    fisher -> loadLocationValues(fisher, NonAssociatedSetAction.class),
                    getDecayRateOfNonAssociatedSetLocationValues().applyAsDouble(rng)
                )
            ).put(
                DolphinSetAction.class,
                new DolphinSetLocationValues(
                    fisher -> loadLocationValues(fisher, DolphinSetAction.class),
                    getDecayRateOfDolphinSetLocationValues().applyAsDouble(rng)
                )
            )
            .put(
                FadDeploymentAction.class,
                new DeploymentLocationValues(
                    fisher -> loadLocationValues(fisher, FadDeploymentAction.class),
                    getDecayRateOfDeploymentLocationValues().applyAsDouble(rng)
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
            targetYear,
            fisher,
            actionClass
        );
    }

    public DoubleParameter getDecayRateOfOpportunisticFadSetLocationValues() {
        return decayRateOfOpportunisticFadSetLocationValues;
    }

    public void setDecayRateOfOpportunisticFadSetLocationValues(final DoubleParameter decayRateOfOpportunisticFadSetLocationValues) {
        this.decayRateOfOpportunisticFadSetLocationValues = decayRateOfOpportunisticFadSetLocationValues;
    }

    public DoubleParameter getDecayRateOfNonAssociatedSetLocationValues() {
        return decayRateOfNonAssociatedSetLocationValues;
    }

    public void setDecayRateOfNonAssociatedSetLocationValues(final DoubleParameter decayRateOfNonAssociatedSetLocationValues) {
        this.decayRateOfNonAssociatedSetLocationValues = decayRateOfNonAssociatedSetLocationValues;
    }

    public DoubleParameter getDecayRateOfDolphinSetLocationValues() {
        return decayRateOfDolphinSetLocationValues;
    }

    public void setDecayRateOfDolphinSetLocationValues(final DoubleParameter decayRateOfDolphinSetLocationValues) {
        this.decayRateOfDolphinSetLocationValues = decayRateOfDolphinSetLocationValues;
    }

    public DoubleParameter getDecayRateOfDeploymentLocationValues() {
        return decayRateOfDeploymentLocationValues;
    }

    public void setDecayRateOfDeploymentLocationValues(final DoubleParameter decayRateOfDeploymentLocationValues) {
        this.decayRateOfDeploymentLocationValues = decayRateOfDeploymentLocationValues;
    }

}
