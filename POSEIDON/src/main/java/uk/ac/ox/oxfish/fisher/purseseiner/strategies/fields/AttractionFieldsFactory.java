/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.PurseSeinerActionClassToDouble;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.operators.LogisticFunctionFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void setLocationValuesSupplier(final LocationValuesFactory locationValuesFactory) {
        this.locationValuesFactory = locationValuesFactory;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getPctHoldSpaceLeftModulationFunction() {
        return pctHoldSpaceLeftModulationFunction;
    }

    @SuppressWarnings("unused")
    public void setPctHoldSpaceLeftModulationFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> pctHoldSpaceLeftModulationFunction) {
        this.pctHoldSpaceLeftModulationFunction = pctHoldSpaceLeftModulationFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getPctSetsRemainingModulationFunction() {
        return pctSetsRemainingModulationFunction;
    }

    @SuppressWarnings("unused")
    public void setPctSetsRemainingModulationFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> pctSetsRemainingModulationFunction) {
        this.pctSetsRemainingModulationFunction = pctSetsRemainingModulationFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getNumFadsInStockModulationFunction() {
        return numFadsInStockModulationFunction;
    }

    @SuppressWarnings("unused")
    public void setNumFadsInStockModulationFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> numFadsInStockModulationFunction) {
        this.numFadsInStockModulationFunction = numFadsInStockModulationFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getFadDeploymentPctActiveFadsLimitModulationFunction() {
        return fadDeploymentPctActiveFadsLimitModulationFunction;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentPctActiveFadsLimitModulationFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> fadDeploymentPctActiveFadsLimitModulationFunction
    ) {
        this.fadDeploymentPctActiveFadsLimitModulationFunction = fadDeploymentPctActiveFadsLimitModulationFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getPctTravelTimeLeftModulationFunction() {
        return pctTravelTimeLeftModulationFunction;
    }

    @SuppressWarnings("unused")
    public void setPctTravelTimeLeftModulationFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> pctTravelTimeLeftModulationFunction) {
        this.pctTravelTimeLeftModulationFunction = pctTravelTimeLeftModulationFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getOpportunisticFadSetTimeSinceLastVisitModulationFunction() {
        return opportunisticFadSetTimeSinceLastVisitModulationFunction;
    }

    @SuppressWarnings("unused")
    public void setOpportunisticFadSetTimeSinceLastVisitModulationFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> opportunisticFadSetTimeSinceLastVisitModulationFunction
    ) {
        this.opportunisticFadSetTimeSinceLastVisitModulationFunction =
            opportunisticFadSetTimeSinceLastVisitModulationFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getNonAssociatedSetTimeSinceLastVisitModulationFunction() {
        return nonAssociatedSetTimeSinceLastVisitModulationFunction;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetTimeSinceLastVisitModulationFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> nonAssociatedSetTimeSinceLastVisitModulationFunction
    ) {
        this.nonAssociatedSetTimeSinceLastVisitModulationFunction =
            nonAssociatedSetTimeSinceLastVisitModulationFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getDolphinSetTimeSinceLastVisitModulationFunction() {
        return dolphinSetTimeSinceLastVisitModulationFunction;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetTimeSinceLastVisitModulationFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> dolphinSetTimeSinceLastVisitModulationFunction
    ) {
        this.dolphinSetTimeSinceLastVisitModulationFunction = dolphinSetTimeSinceLastVisitModulationFunction;
    }

    @SuppressWarnings("unused")
    public double getActionDistanceExponent() {
        return actionDistanceExponent;
    }

    @SuppressWarnings("unused")
    public void setActionDistanceExponent(final double actionDistanceExponent) {
        this.actionDistanceExponent = actionDistanceExponent;
    }

    @SuppressWarnings("unused")
    public double getDestinationDistanceExponent() {
        return destinationDistanceExponent;
    }

    @SuppressWarnings("unused")
    public void setDestinationDistanceExponent(final double destinationDistanceExponent) {
        this.destinationDistanceExponent = destinationDistanceExponent;
    }

    @Override
    public Set<AttractionField> apply(final FishState fishState) {
        final LocationValueByActionClass locationValues =
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

    @SuppressWarnings("WeakerAccess")
    public InputPath getMaxCurrentSpeedsFile() {
        return maxCurrentSpeedsFile;
    }

    @SuppressWarnings("unused")
    public void setMaxCurrentSpeedsFile(final InputPath maxCurrentSpeedsFile) {
        this.maxCurrentSpeedsFile = maxCurrentSpeedsFile;
    }

}
