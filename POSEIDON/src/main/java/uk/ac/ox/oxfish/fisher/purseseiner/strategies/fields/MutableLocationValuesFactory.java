package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

public class MutableLocationValuesFactory extends LocationValuesFactory {
    private DoubleParameter decayRateOfOpportunisticFadSetLocationValues;
    private DoubleParameter decayRateOfNonAssociatedSetLocationValues;
    private DoubleParameter decayRateOfDolphinSetLocationValues;
    private DoubleParameter decayRateOfDeploymentLocationValues;

    @SuppressWarnings("WeakerAccess")
    public MutableLocationValuesFactory(
        final InputPath locationValuesFile,
        final DoubleParameter decayRateOfOpportunisticFadSetLocationValues,
        final DoubleParameter decayRateOfNonAssociatedSetLocationValues,
        final DoubleParameter decayRateOfDolphinSetLocationValues,
        final DoubleParameter decayRateOfDeploymentLocationValues,
        final IntegerParameter targetYear
    ) {
        super(locationValuesFile, targetYear);
        this.decayRateOfOpportunisticFadSetLocationValues = decayRateOfOpportunisticFadSetLocationValues;
        this.decayRateOfNonAssociatedSetLocationValues = decayRateOfNonAssociatedSetLocationValues;
        this.decayRateOfDolphinSetLocationValues = decayRateOfDolphinSetLocationValues;
        this.decayRateOfDeploymentLocationValues = decayRateOfDeploymentLocationValues;
    }

    @SuppressWarnings("WeakerAccess")
    public MutableLocationValuesFactory() {
    }

    @Override
    public LocationValueByActionClass apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        return new LocationValueByActionClass(
            ImmutableMap.<Class<? extends PurseSeinerAction>, LocationValues>builder()
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
                .build()
        );
    }

    @SuppressWarnings("WeakerAccess")
    public DoubleParameter getDecayRateOfOpportunisticFadSetLocationValues() {
        return decayRateOfOpportunisticFadSetLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfOpportunisticFadSetLocationValues(final DoubleParameter decayRateOfOpportunisticFadSetLocationValues) {
        this.decayRateOfOpportunisticFadSetLocationValues = decayRateOfOpportunisticFadSetLocationValues;
    }

    @SuppressWarnings("WeakerAccess")
    public DoubleParameter getDecayRateOfNonAssociatedSetLocationValues() {
        return decayRateOfNonAssociatedSetLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfNonAssociatedSetLocationValues(final DoubleParameter decayRateOfNonAssociatedSetLocationValues) {
        this.decayRateOfNonAssociatedSetLocationValues = decayRateOfNonAssociatedSetLocationValues;
    }

    @SuppressWarnings("WeakerAccess")
    public DoubleParameter getDecayRateOfDolphinSetLocationValues() {
        return decayRateOfDolphinSetLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfDolphinSetLocationValues(final DoubleParameter decayRateOfDolphinSetLocationValues) {
        this.decayRateOfDolphinSetLocationValues = decayRateOfDolphinSetLocationValues;
    }

    @SuppressWarnings("WeakerAccess")
    public DoubleParameter getDecayRateOfDeploymentLocationValues() {
        return decayRateOfDeploymentLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfDeploymentLocationValues(final DoubleParameter decayRateOfDeploymentLocationValues) {
        this.decayRateOfDeploymentLocationValues = decayRateOfDeploymentLocationValues;
    }

}
