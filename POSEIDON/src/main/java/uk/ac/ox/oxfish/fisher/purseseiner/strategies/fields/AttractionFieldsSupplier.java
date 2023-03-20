package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.PurseSeinerActionClassToDouble;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.operators.LogisticFunctionSupplier;

import java.util.Map;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class AttractionFieldsSupplier implements Supplier<Set<AttractionField>> {
    private LocationValuesSupplier locationValuesSupplier;
    private InputPath maxCurrentSpeedsFile;
    private Supplier<? extends DoubleUnaryOperator>
        pctHoldSpaceLeftModulationFunction =
        new LogisticFunctionSupplier(0.15670573908905225, 5);
    private Supplier<? extends DoubleUnaryOperator>
        pctSetsRemainingModulationFunction =
        new LogisticFunctionSupplier(EPSILON, 10);
    private Supplier<? extends DoubleUnaryOperator>
        numFadsInStockModulationFunction =
        new LogisticFunctionSupplier(465.76938287575837, 5);
    private Supplier<? extends DoubleUnaryOperator>
        fadDeploymentPctActiveFadsLimitModulationFunction =
        new LogisticFunctionSupplier(0.817463635675281, 5);
    private Supplier<? extends DoubleUnaryOperator>
        pctTravelTimeLeftModulationFunction =
        new LogisticFunctionSupplier(0.10183241937374361, 5);
    private Supplier<? extends DoubleUnaryOperator>
        opportunisticFadSetTimeSinceLastVisitModulationFunction =
        new LogisticFunctionSupplier(73.32224086132372, 5);
    private Supplier<? extends DoubleUnaryOperator>
        nonAssociatedSetTimeSinceLastVisitModulationFunction =
        new LogisticFunctionSupplier(51.91162666081563, 5);
    private Supplier<? extends DoubleUnaryOperator>
        dolphinSetTimeSinceLastVisitModulationFunction =
        new LogisticFunctionSupplier(72.28852668100924, 5);
    private double actionDistanceExponent = 10;
    private double destinationDistanceExponent = 2;

    public AttractionFieldsSupplier() {
    }

    public AttractionFieldsSupplier(
        final LocationValuesSupplier locationValuesSupplier,
        final InputPath maxCurrentSpeedsFile
    ) {
        this.locationValuesSupplier = locationValuesSupplier;
        this.maxCurrentSpeedsFile = maxCurrentSpeedsFile;
    }

    public LocationValuesSupplier getLocationValuesSupplier() {
        return locationValuesSupplier;
    }

    public void setLocationValuesSupplier(final LocationValuesSupplier locationValuesSupplier) {
        this.locationValuesSupplier = locationValuesSupplier;
    }

    public Supplier<? extends DoubleUnaryOperator> getPctHoldSpaceLeftModulationFunction() {
        return pctHoldSpaceLeftModulationFunction;
    }

    public void setPctHoldSpaceLeftModulationFunction(final Supplier<? extends DoubleUnaryOperator> pctHoldSpaceLeftModulationFunction) {
        this.pctHoldSpaceLeftModulationFunction = pctHoldSpaceLeftModulationFunction;
    }

    public Supplier<? extends DoubleUnaryOperator> getPctSetsRemainingModulationFunction() {
        return pctSetsRemainingModulationFunction;
    }

    public void setPctSetsRemainingModulationFunction(final Supplier<? extends DoubleUnaryOperator> pctSetsRemainingModulationFunction) {
        this.pctSetsRemainingModulationFunction = pctSetsRemainingModulationFunction;
    }

    public Supplier<? extends DoubleUnaryOperator> getNumFadsInStockModulationFunction() {
        return numFadsInStockModulationFunction;
    }

    public void setNumFadsInStockModulationFunction(final Supplier<? extends DoubleUnaryOperator> numFadsInStockModulationFunction) {
        this.numFadsInStockModulationFunction = numFadsInStockModulationFunction;
    }

    public Supplier<? extends DoubleUnaryOperator> getFadDeploymentPctActiveFadsLimitModulationFunction() {
        return fadDeploymentPctActiveFadsLimitModulationFunction;
    }

    public void setFadDeploymentPctActiveFadsLimitModulationFunction(final Supplier<? extends DoubleUnaryOperator> fadDeploymentPctActiveFadsLimitModulationFunction) {
        this.fadDeploymentPctActiveFadsLimitModulationFunction = fadDeploymentPctActiveFadsLimitModulationFunction;
    }

    public Supplier<? extends DoubleUnaryOperator> getPctTravelTimeLeftModulationFunction() {
        return pctTravelTimeLeftModulationFunction;
    }

    public void setPctTravelTimeLeftModulationFunction(final Supplier<? extends DoubleUnaryOperator> pctTravelTimeLeftModulationFunction) {
        this.pctTravelTimeLeftModulationFunction = pctTravelTimeLeftModulationFunction;
    }

    public Supplier<? extends DoubleUnaryOperator> getOpportunisticFadSetTimeSinceLastVisitModulationFunction() {
        return opportunisticFadSetTimeSinceLastVisitModulationFunction;
    }

    public void setOpportunisticFadSetTimeSinceLastVisitModulationFunction(final Supplier<? extends DoubleUnaryOperator> opportunisticFadSetTimeSinceLastVisitModulationFunction) {
        this.opportunisticFadSetTimeSinceLastVisitModulationFunction = opportunisticFadSetTimeSinceLastVisitModulationFunction;
    }

    public Supplier<? extends DoubleUnaryOperator> getNonAssociatedSetTimeSinceLastVisitModulationFunction() {
        return nonAssociatedSetTimeSinceLastVisitModulationFunction;
    }

    public void setNonAssociatedSetTimeSinceLastVisitModulationFunction(final Supplier<? extends DoubleUnaryOperator> nonAssociatedSetTimeSinceLastVisitModulationFunction) {
        this.nonAssociatedSetTimeSinceLastVisitModulationFunction = nonAssociatedSetTimeSinceLastVisitModulationFunction;
    }

    public Supplier<? extends DoubleUnaryOperator> getDolphinSetTimeSinceLastVisitModulationFunction() {
        return dolphinSetTimeSinceLastVisitModulationFunction;
    }

    public void setDolphinSetTimeSinceLastVisitModulationFunction(final Supplier<? extends DoubleUnaryOperator> dolphinSetTimeSinceLastVisitModulationFunction) {
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

    public InputPath getMaxCurrentSpeedsFile() {
        return maxCurrentSpeedsFile;
    }

    public void setMaxCurrentSpeedsFile(final InputPath maxCurrentSpeedsFile) {
        this.maxCurrentSpeedsFile = maxCurrentSpeedsFile;
    }

    @Override
    public Set<AttractionField> get() {
        final Map<Class<? extends PurseSeinerAction>, LocationValues> locationValues =
            locationValuesSupplier.get();
        final PurseSeinerActionClassToDouble maxCurrentSpeed =
            PurseSeinerActionClassToDouble.fromFile(
                getMaxCurrentSpeedsFile().get(), "action", "speed"
            );

        final GlobalSetAttractionModulator globalSetAttractionModulator =
            new GlobalSetAttractionModulator(
                pctHoldSpaceLeftModulationFunction.get(),
                pctSetsRemainingModulationFunction.get()
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
                        opportunisticFadSetTimeSinceLastVisitModulationFunction.get(),
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
                        nonAssociatedSetTimeSinceLastVisitModulationFunction.get(),
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
                        dolphinSetTimeSinceLastVisitModulationFunction.get(),
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
                        fadDeploymentPctActiveFadsLimitModulationFunction.get(),
                        numFadsInStockModulationFunction.get()
                    ),
                    FadDeploymentAction.class,
                    actionDistanceExponent,
                    destinationDistanceExponent
                )
            )
            .add(
                new PortAttractionField(
                    new PortAttractionModulator(
                        pctHoldSpaceLeftModulationFunction.get(),
                        pctTravelTimeLeftModulationFunction.get()
                    ),
                    actionDistanceExponent,
                    destinationDistanceExponent
                )
            )
            .build();
    }
}
