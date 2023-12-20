package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

import java.util.Map.Entry;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class CappedMutableLocationValuesFactory extends MutableLocationValuesFactory {

    private IntegerParameter maximumNumberOfLocationValues;

    public CappedMutableLocationValuesFactory(
        final InputPath locationValuesFile,
        final DoubleParameter decayRateOfOpportunisticFadSetLocationValues,
        final DoubleParameter decayRateOfNonAssociatedSetLocationValues,
        final DoubleParameter decayRateOfDolphinSetLocationValues,
        final DoubleParameter decayRateOfDeploymentLocationValues,
        final IntegerParameter targetYear,
        final IntegerParameter maximumNumberOfLocationValues
    ) {
        super(
            locationValuesFile,
            decayRateOfOpportunisticFadSetLocationValues,
            decayRateOfNonAssociatedSetLocationValues,
            decayRateOfDolphinSetLocationValues,
            decayRateOfDeploymentLocationValues,
            targetYear
        );
        this.maximumNumberOfLocationValues = maximumNumberOfLocationValues;
    }

    @SuppressWarnings("unused")
    public CappedMutableLocationValuesFactory() {
    }

    @SuppressWarnings("unused")
    public IntegerParameter getMaximumNumberOfLocationValues() {
        return maximumNumberOfLocationValues;
    }

    @SuppressWarnings("unused")
    public void setMaximumNumberOfLocationValues(final IntegerParameter maximumNumberOfLocationValues) {
        this.maximumNumberOfLocationValues = maximumNumberOfLocationValues;
    }

    @Override
    public LocationValueByActionClass apply(final FishState fishState) {
        return new LocationValueByActionClass(
            super
                .apply(fishState)
                .asMap()
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    Entry::getKey,
                    entry -> {
                        if (entry.getValue() instanceof MutableLocationValues)
                            return new CappedLocationValuesDecorator<>(
                                (MutableLocationValues<?>) entry.getValue(),
                                maximumNumberOfLocationValues.getIntValue()
                            );
                        else return entry.getValue();
                    }
                ))
        );
    }
}
