package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

public class FixedLocationValuesFactory extends LocationValuesFactory {

    @SuppressWarnings("unused")
    public FixedLocationValuesFactory() {
    }

    public FixedLocationValuesFactory(
        final InputPath locationValuesFile,
        final IntegerParameter targetYear
    ) {
        super(locationValuesFile, targetYear);
    }

    @Override
    public LocationValueByActionClass apply(final FishState fishState) {
        return new LocationValueByActionClass(
            ImmutableMap.<Class<? extends PurseSeinerAction>, LocationValues>builder()
                .put(
                    FadSetAction.class,
                    new FadLocationValues()
                )
                .put(
                    OpportunisticFadSetAction.class,
                    new FixedLocationValues(
                        fisher -> loadLocationValues(fisher, OpportunisticFadSetAction.class)
                    )
                )
                .put(
                    NonAssociatedSetAction.class,
                    new FixedLocationValues(
                        fisher -> loadLocationValues(fisher, NonAssociatedSetAction.class)
                    )
                ).put(
                    DolphinSetAction.class,
                    new FixedLocationValues(
                        fisher -> loadLocationValues(fisher, DolphinSetAction.class)
                    )
                )
                .put(
                    FadDeploymentAction.class,
                    new FixedLocationValues(
                        fisher -> loadLocationValues(fisher, FadDeploymentAction.class)
                    )
                )
                .build()
        );
    }
}
