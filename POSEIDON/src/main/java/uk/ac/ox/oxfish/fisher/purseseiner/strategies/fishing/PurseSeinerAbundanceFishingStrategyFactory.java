/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbundanceCatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.SetDurationSamplersFactory;
import uk.ac.ox.oxfish.model.scenario.InputPath;

import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public class PurseSeinerAbundanceFishingStrategyFactory
    extends PurseSeinerFishingStrategyFactory<AbundanceLocalBiology, AbundanceFad> {

    private boolean ageBasedSetDecisions = false;

    public PurseSeinerAbundanceFishingStrategyFactory() {
        super(AbundanceLocalBiology.class, AbundanceFad.class);
    }

    public PurseSeinerAbundanceFishingStrategyFactory(
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final InputPath actionWeightsFile,
        final AbundanceCatchSamplersFactory catchSamplersFactory,
        final SetDurationSamplersFactory setDurationSamplersFactory,
        final InputPath maxCurrentSpeedsFile,
        final InputPath setCompositionWeightsFile
    ) {
        super(
            AbundanceLocalBiology.class,
            AbundanceFad.class,
            speciesCodesSupplier,
            actionWeightsFile,
            catchSamplersFactory,
            setDurationSamplersFactory,
            maxCurrentSpeedsFile,
            setCompositionWeightsFile
        );
    }

    @NotNull
    @Override
    protected PurseSeinerFishingStrategy<AbundanceLocalBiology> callConstructor(
        final Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>> attractionWeights,
        final Function<Fisher, SetOpportunityDetector<AbundanceLocalBiology>> opportunityDetector,
        final Map<Class<? extends PurseSeinerAction>, DoubleUnaryOperator> actionValueFunctions,
        final ToDoubleFunction<Class<? extends PurseSeinerAction>> maxCurrentSpeeds,
        final double searchActionDecayConstant,
        final double fadDeploymentActionDecayConstant,
        final double movingThreshold
    ) {
        if (ageBasedSetDecisions) {
            return new AgeBasedPurseSeinerFishingStrategy<>(
                attractionWeights,
                opportunityDetector,
                actionValueFunctions,
                maxCurrentSpeeds,
                searchActionDecayConstant,
                fadDeploymentActionDecayConstant,
                movingThreshold
            );
        } else {
            return super.callConstructor(
                attractionWeights,
                opportunityDetector,
                actionValueFunctions,
                maxCurrentSpeeds,
                searchActionDecayConstant,
                fadDeploymentActionDecayConstant,
                movingThreshold
            );
        }
    }

    @Override
    CatchMaker<AbundanceLocalBiology> getCatchMaker(final GlobalBiology globalBiology) {
        return new AbundanceCatchMaker(globalBiology);
    }

    @SuppressWarnings("unused")
    public boolean isAgeBasedSetDecisions() {
        return ageBasedSetDecisions;
    }

    @SuppressWarnings("unused")
    public void setAgeBasedSetDecisions(final boolean ageBasedSetDecisions) {
        this.ageBasedSetDecisions = ageBasedSetDecisions;
    }
}
