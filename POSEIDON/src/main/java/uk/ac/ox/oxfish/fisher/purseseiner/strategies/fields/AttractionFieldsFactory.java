package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.PurseSeinerActionClassToDouble;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.operators.LogisticFunctionFactory;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

import java.util.Map;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class AttractionFieldsFactory implements AlgorithmFactory<Set<AttractionField>> {

    private IntegerParameter targetYear;
    private LocationValuesFactory locationValuesFactory;
    private InputPath maxCurrentSpeedsFile;
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        pctHoldSpaceLeftModulationFunction =
        new LogisticFunctionFactory(0.15670573908905225, 5);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        pctSetsRemainingModulationFunction =
        new LogisticFunctionFactory(EPSILON, 10);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        numFadsInStockModulationFunction =
        new LogisticFunctionFactory(465.76938287575837, 5);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        fadDeploymentPctActiveFadsLimitModulationFunction =
        new LogisticFunctionFactory(0.817463635675281, 5);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        pctTravelTimeLeftModulationFunction =
        new LogisticFunctionFactory(0.10183241937374361, 5);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        opportunisticFadSetTimeSinceLastVisitModulationFunction =
        new LogisticFunctionFactory(73.32224086132372, 5);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        nonAssociatedSetTimeSinceLastVisitModulationFunction =
        new LogisticFunctionFactory(51.91162666081563, 5);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        dolphinSetTimeSinceLastVisitModulationFunction =
        new LogisticFunctionFactory(72.28852668100924, 5);
    private double actionDistanceExponent = 10;
    private double destinationDistanceExponent = 2;

    public AttractionFieldsFactory() {
    }

    public AttractionFieldsFactory(
        final LocationValuesFactory locationValuesFactory,
        final InputPath maxCurrentSpeedsFile,
        final IntegerParameter targetYear
    ) {
        this.locationValuesFactory = locationValuesFactory;
        this.maxCurrentSpeedsFile = maxCurrentSpeedsFile;
        this.targetYear = targetYear;
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    public LocationValuesFactory getLocationValuesSupplier() {
        return locationValuesFactory;
    }

    public void setLocationValuesSupplier(final LocationValuesFactory locationValuesFactory) {
        this.locationValuesFactory = locationValuesFactory;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getPctHoldSpaceLeftModulationFunction() {
        return pctHoldSpaceLeftModulationFunction;
    }

    public void setPctHoldSpaceLeftModulationFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> pctHoldSpaceLeftModulationFunction) {
        this.pctHoldSpaceLeftModulationFunction = pctHoldSpaceLeftModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getPctSetsRemainingModulationFunction() {
        return pctSetsRemainingModulationFunction;
    }

    public void setPctSetsRemainingModulationFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> pctSetsRemainingModulationFunction) {
        this.pctSetsRemainingModulationFunction = pctSetsRemainingModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getNumFadsInStockModulationFunction() {
        return numFadsInStockModulationFunction;
    }

    public void setNumFadsInStockModulationFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> numFadsInStockModulationFunction) {
        this.numFadsInStockModulationFunction = numFadsInStockModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getFadDeploymentPctActiveFadsLimitModulationFunction() {
        return fadDeploymentPctActiveFadsLimitModulationFunction;
    }

    public void setFadDeploymentPctActiveFadsLimitModulationFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> fadDeploymentPctActiveFadsLimitModulationFunction) {
        this.fadDeploymentPctActiveFadsLimitModulationFunction = fadDeploymentPctActiveFadsLimitModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getPctTravelTimeLeftModulationFunction() {
        return pctTravelTimeLeftModulationFunction;
    }

    public void setPctTravelTimeLeftModulationFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> pctTravelTimeLeftModulationFunction) {
        this.pctTravelTimeLeftModulationFunction = pctTravelTimeLeftModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getOpportunisticFadSetTimeSinceLastVisitModulationFunction() {
        return opportunisticFadSetTimeSinceLastVisitModulationFunction;
    }

    public void setOpportunisticFadSetTimeSinceLastVisitModulationFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> opportunisticFadSetTimeSinceLastVisitModulationFunction) {
        this.opportunisticFadSetTimeSinceLastVisitModulationFunction = opportunisticFadSetTimeSinceLastVisitModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getNonAssociatedSetTimeSinceLastVisitModulationFunction() {
        return nonAssociatedSetTimeSinceLastVisitModulationFunction;
    }

    public void setNonAssociatedSetTimeSinceLastVisitModulationFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> nonAssociatedSetTimeSinceLastVisitModulationFunction) {
        this.nonAssociatedSetTimeSinceLastVisitModulationFunction = nonAssociatedSetTimeSinceLastVisitModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getDolphinSetTimeSinceLastVisitModulationFunction() {
        return dolphinSetTimeSinceLastVisitModulationFunction;
    }

    public void setDolphinSetTimeSinceLastVisitModulationFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> dolphinSetTimeSinceLastVisitModulationFunction) {
        this.dolphinSetTimeSinceLastVisitModulationFunction = dolphinSetTimeSinceLastVisitModulationFunction;
    }

    public double getActionDistanceExponent() {
        return actionDistanceExponent;
    }

    public void setActionDistanceExponent(final double actionDistanceExponent) {
        this.actionDistanceExponent = actionDistanceExponent;
    }

    public double getDestinationDistanceExponent() {
        return destinationDistanceExponent;
    }

    public void setDestinationDistanceExponent(final double destinationDistanceExponent) {
        this.destinationDistanceExponent = destinationDistanceExponent;
    }

    @Override
    public Set<AttractionField> apply(final FishState fishState) {
        final Map<Class<? extends PurseSeinerAction>, LocationValues> locationValues =
            locationValuesFactory.apply(fishState);
        final PurseSeinerActionClassToDouble maxCurrentSpeed =
            PurseSeinerActionClassToDouble.fromFile(
                getMaxCurrentSpeedsFile().get(), targetYear.getValue(), "action", "speed"
            );

        final GlobalSetAttractionModulator globalSetAttractionModulator =
            new GlobalSetAttractionModulator(
                pctHoldSpaceLeftModulationFunction.apply(fishState),
                pctSetsRemainingModulationFunction.apply(fishState)
            );

        return ImmutableSet.<AttractionField>builder()
            .add(
                new ActionAttractionField(
                    locationValues.get(FadSetAction.class),
                    LocalCanFishThereAttractionModulator.INSTANCE,
                    globalSetAttractionModulator,
                    FadSetAction.class,
                    actionDistanceExponent,
                    destinationDistanceExponent
                )
            )
            .add(
                new ActionAttractionField(
                    locationValues.get(OpportunisticFadSetAction.class),
                    new LocalSetAttractionModulator(
                        opportunisticFadSetTimeSinceLastVisitModulationFunction.apply(fishState),
                        maxCurrentSpeed.applyAsDouble(OpportunisticFadSetAction.class)
                    ),
                    globalSetAttractionModulator,
                    OpportunisticFadSetAction.class,
                    actionDistanceExponent,
                    destinationDistanceExponent
                )
            )
            .add(
                new ActionAttractionField(
                    locationValues.get(NonAssociatedSetAction.class),
                    new LocalSetAttractionModulator(
                        nonAssociatedSetTimeSinceLastVisitModulationFunction.apply(fishState),
                        maxCurrentSpeed.applyAsDouble(NonAssociatedSetAction.class)
                    ),
                    globalSetAttractionModulator,
                    NonAssociatedSetAction.class,
                    actionDistanceExponent,
                    destinationDistanceExponent
                )
            )
            .add(
                new ActionAttractionField(
                    locationValues.get(DolphinSetAction.class),
                    new LocalSetAttractionModulator(
                        dolphinSetTimeSinceLastVisitModulationFunction.apply(fishState),
                        maxCurrentSpeed.applyAsDouble(DolphinSetAction.class)
                    ),
                    globalSetAttractionModulator,
                    DolphinSetAction.class,
                    actionDistanceExponent,
                    destinationDistanceExponent
                )
            )
            .add(
                new ActionAttractionField(
                    locationValues.get(FadDeploymentAction.class),
                    LocalCanFishThereAttractionModulator.INSTANCE,
                    new GlobalDeploymentAttractionModulator(
                        fadDeploymentPctActiveFadsLimitModulationFunction.apply(fishState),
                        numFadsInStockModulationFunction.apply(fishState)
                    ),
                    FadDeploymentAction.class,
                    actionDistanceExponent,
                    destinationDistanceExponent
                )
            )
            .add(
                new PortAttractionField(
                    new PortAttractionModulator(
                        pctHoldSpaceLeftModulationFunction.apply(fishState),
                        pctTravelTimeLeftModulationFunction.apply(fishState)
                    ),
                    actionDistanceExponent,
                    destinationDistanceExponent
                )
            )
            .build();
    }

    public InputPath getMaxCurrentSpeedsFile() {
        return maxCurrentSpeedsFile;
    }

    public void setMaxCurrentSpeedsFile(final InputPath maxCurrentSpeedsFile) {
        this.maxCurrentSpeedsFile = maxCurrentSpeedsFile;
    }

}
