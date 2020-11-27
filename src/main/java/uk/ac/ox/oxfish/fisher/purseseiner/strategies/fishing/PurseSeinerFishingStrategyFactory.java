/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.univocity.parsers.common.record.Record;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.ActionWeightsCache;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.BoatValuesByActionFromFileCache.ActionClasses;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.TARGET_YEAR;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.input;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class PurseSeinerFishingStrategyFactory implements AlgorithmFactory<PurseSeinerFishingStrategy> {

    private Path setCompositionWeightsPath = input("set_compositions.csv");
    private double nonAssociatedSetGeneratorSigmoidMidpoint = 100_000;
    private double nonAssociatedSetGeneratorSigmoidSteepness = 1;
    private double dolphinSetGeneratorSigmoidMidpoint = 100_000;
    private double dolphinSetGeneratorSigmoidSteepness = 1;
    private double searchBonus = 0.1;
    private double nonAssociatedSetDetectionProbability = 0.1;
    private double dolphinSetDetectionProbability = 0.1;
    private double opportunisticFadSetDetectionProbability = 0.1;

    private double searchActionSigmoidMidpoint = 0.1;
    private double searchActionSigmoidSteepness = 1;
    private double searchActionDecayConstant = 1;
    private double fadDeploymentActionSigmoidMidpoint = 0.1;
    private double fadDeploymentActionSigmoidSteepness = 1;
    private double fadDeploymentActionDecayConstant = 1;

    private double fadSetExponentialSteepnessCoefficient = 1E-4;
    private double opportunisticFadSetExponentialSteepnessCoefficient = 1E-4;
    private double nonAssociatedSetExponentialSteepnessCoefficient = 1E-4;
    private double dolphinSetExponentialSteepnessCoefficient = 1E-4;

    public double getFadSetExponentialSteepnessCoefficient() { return fadSetExponentialSteepnessCoefficient; }

    public void setFadSetExponentialSteepnessCoefficient(final double fadSetExponentialSteepnessCoefficient) {
        this.fadSetExponentialSteepnessCoefficient = fadSetExponentialSteepnessCoefficient;
    }

    @SuppressWarnings("unused")
    public double getOpportunisticFadSetExponentialSteepnessCoefficient() { return opportunisticFadSetExponentialSteepnessCoefficient; }

    public void setOpportunisticFadSetExponentialSteepnessCoefficient(final double opportunisticFadSetExponentialSteepnessCoefficient) {
        this.opportunisticFadSetExponentialSteepnessCoefficient = opportunisticFadSetExponentialSteepnessCoefficient;
    }

    @SuppressWarnings("unused")
    public double getNonAssociatedSetExponentialSteepnessCoefficient() { return nonAssociatedSetExponentialSteepnessCoefficient; }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetExponentialSteepnessCoefficient(final double nonAssociatedSetExponentialSteepnessCoefficient) {
        this.nonAssociatedSetExponentialSteepnessCoefficient = nonAssociatedSetExponentialSteepnessCoefficient;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetExponentialSteepnessCoefficient() { return dolphinSetExponentialSteepnessCoefficient; }

    @SuppressWarnings("unused")
    public void setDolphinSetExponentialSteepnessCoefficient(final double dolphinSetExponentialSteepnessCoefficient) {
        this.dolphinSetExponentialSteepnessCoefficient = dolphinSetExponentialSteepnessCoefficient;
    }

    public double getSearchActionSigmoidMidpoint() { return searchActionSigmoidMidpoint; }

    public void setSearchActionSigmoidMidpoint(final double searchActionSigmoidMidpoint) {
        this.searchActionSigmoidMidpoint = searchActionSigmoidMidpoint;
    }

    public double getSearchActionSigmoidSteepness() { return searchActionSigmoidSteepness; }

    public void setSearchActionSigmoidSteepness(final double searchActionSigmoidSteepness) {
        this.searchActionSigmoidSteepness = searchActionSigmoidSteepness;
    }

    public double getSearchActionDecayConstant() { return searchActionDecayConstant; }

    public void setSearchActionDecayConstant(final double searchActionDecayConstant) {
        this.searchActionDecayConstant = searchActionDecayConstant;
    }

    public double getFadDeploymentActionSigmoidMidpoint() { return fadDeploymentActionSigmoidMidpoint; }

    public void setFadDeploymentActionSigmoidMidpoint(final double fadDeploymentActionSigmoidMidpoint) {
        this.fadDeploymentActionSigmoidMidpoint = fadDeploymentActionSigmoidMidpoint;
    }

    public double getFadDeploymentActionSigmoidSteepness() { return fadDeploymentActionSigmoidSteepness; }

    public void setFadDeploymentActionSigmoidSteepness(final double fadDeploymentActionSigmoidSteepness) {
        this.fadDeploymentActionSigmoidSteepness = fadDeploymentActionSigmoidSteepness;
    }

    @SuppressWarnings("unused")
    public double getOpportunisticFadSetDetectionProbability() { return opportunisticFadSetDetectionProbability; }

    @SuppressWarnings("unused")
    public void setOpportunisticFadSetDetectionProbability(final double opportunisticFadSetDetectionProbability) {
        this.opportunisticFadSetDetectionProbability = opportunisticFadSetDetectionProbability;
    }

    @SuppressWarnings("unused")
    public double getNonAssociatedSetDetectionProbability() { return nonAssociatedSetDetectionProbability; }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetDetectionProbability(final double nonAssociatedSetDetectionProbability) {
        this.nonAssociatedSetDetectionProbability = nonAssociatedSetDetectionProbability;
    }

    public double getDolphinSetDetectionProbability() { return dolphinSetDetectionProbability; }

    public void setDolphinSetDetectionProbability(final double dolphinSetDetectionProbability) {
        this.dolphinSetDetectionProbability = dolphinSetDetectionProbability;
    }

    public double getSearchBonus() { return searchBonus; }

    public void setSearchBonus(final double searchBonus) { this.searchBonus = searchBonus; }

    public double getNonAssociatedSetGeneratorSigmoidMidpoint() { return nonAssociatedSetGeneratorSigmoidMidpoint; }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetGeneratorSigmoidMidpoint(final double nonAssociatedSetGeneratorSigmoidMidpoint) {
        this.nonAssociatedSetGeneratorSigmoidMidpoint = nonAssociatedSetGeneratorSigmoidMidpoint;
    }

    public double getNonAssociatedSetGeneratorSigmoidSteepness() { return nonAssociatedSetGeneratorSigmoidSteepness; }

    public void setNonAssociatedSetGeneratorSigmoidSteepness(final double nonAssociatedSetGeneratorSigmoidSteepness) {
        this.nonAssociatedSetGeneratorSigmoidSteepness = nonAssociatedSetGeneratorSigmoidSteepness;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetGeneratorSigmoidMidpoint() { return dolphinSetGeneratorSigmoidMidpoint; }

    @SuppressWarnings("unused")
    public void setDolphinSetGeneratorSigmoidMidpoint(final double dolphinSetGeneratorSigmoidMidpoint) {
        this.dolphinSetGeneratorSigmoidMidpoint = dolphinSetGeneratorSigmoidMidpoint;
    }

    public double getDolphinSetGeneratorSigmoidSteepness() { return dolphinSetGeneratorSigmoidSteepness; }

    public void setDolphinSetGeneratorSigmoidSteepness(final double dolphinSetGeneratorSigmoidSteepness) {
        this.dolphinSetGeneratorSigmoidSteepness = dolphinSetGeneratorSigmoidSteepness;
    }

    @Override public PurseSeinerFishingStrategy apply(final FishState fishState) {
        return new PurseSeinerFishingStrategy(
            this::loadAttractionWeights,
            this::makeSetOpportunityLocator,
            makeExponentialSteepnessCoefficients(),
            searchActionSigmoidMidpoint,
            searchActionSigmoidSteepness,
            searchActionDecayConstant,
            fadDeploymentActionSigmoidMidpoint,
            fadDeploymentActionSigmoidSteepness,
            fadDeploymentActionDecayConstant
        );
    }

    private Map<Class<? extends PurseSeinerAction>, Double> loadAttractionWeights(
        Fisher fisher
    ) {
        final Path attractionWeightsFile = ((TunaScenario) fisher.grabState().getScenario()).getAttractionWeightsFile();
        return stream(ActionClasses.values())
            .map(ActionClasses::getActionClass)
            .collect(toImmutableMap(
                identity(),
                actionClass -> ActionWeightsCache.INSTANCE.get(
                    attractionWeightsFile,
                    TARGET_YEAR,
                    fisher,
                    actionClass
                )
            ));
    }

    private SetOpportunityDetector makeSetOpportunityLocator(final Fisher fisher) {

        final ImmutableMap<Class<? extends PurseSeinerAction>, ImmutableMap<Species, Double>>
            setCompositionWeights = loadSetCompositionWeights(fisher.grabState());

        final SetOpportunityGenerator nonAssociatedSetOpportunityGenerator =
            new SetOpportunityGenerator(
                nonAssociatedSetGeneratorSigmoidMidpoint,
                nonAssociatedSetGeneratorSigmoidSteepness,
                setCompositionWeights.get(NonAssociatedSetAction.class),
                NonAssociatedSetAction::new
            );

        final SetOpportunityGenerator dolphinSetOpportunityGenerator =
            new SetOpportunityGenerator(
                dolphinSetGeneratorSigmoidMidpoint,
                dolphinSetGeneratorSigmoidSteepness,
                setCompositionWeights.get(DolphinSetAction.class),
                DolphinSetAction::new
            );

        return new SetOpportunityDetector(
            fisher,
            ImmutableList.of(
                nonAssociatedSetOpportunityGenerator,
                dolphinSetOpportunityGenerator
            ),
            ImmutableMap.of(
                NonAssociatedSetAction.class, nonAssociatedSetDetectionProbability,
                DolphinSetAction.class, dolphinSetDetectionProbability,
                OpportunisticFadSetAction.class, opportunisticFadSetDetectionProbability
            ),
            searchBonus
        );
    }

    private ImmutableMap<Class<? extends AbstractSetAction>, Double> makeExponentialSteepnessCoefficients() {
        return ImmutableMap.of(
            NonAssociatedSetAction.class, nonAssociatedSetExponentialSteepnessCoefficient,
            DolphinSetAction.class, dolphinSetExponentialSteepnessCoefficient,
            FadSetAction.class, fadSetExponentialSteepnessCoefficient,
            OpportunisticFadSetAction.class, opportunisticFadSetExponentialSteepnessCoefficient
        );
    }

    private ImmutableMap<Class<? extends PurseSeinerAction>, ImmutableMap<Species, Double>> loadSetCompositionWeights(
        final FishState fishState
    ) {
        return parseAllRecords(setCompositionWeightsPath)
            .stream()
            .collect(groupingBy(r -> ActionClasses.valueOf(r.getString("set_type")).getActionClass()))
            .entrySet()
            .stream()
            .collect(toImmutableMap(
                Map.Entry::getKey,
                entry -> makeWeightMap(fishState, entry.getValue())
            ));
    }

    private ImmutableMap<Species, Double> makeWeightMap(
        final FishState fishState,
        final Collection<Record> records
    ) {
        return
            records.stream().collect(toImmutableMap(
                r -> {
                    final String speciesCode = r.getString("species_code").toUpperCase();
                    final String speciesName = TunaScenario.speciesNames.get(speciesCode);
                    return fishState.getBiology().getSpecie(speciesName);
                },
                r -> r.getDouble("weight")
            ));

    }

    public Path getSetCompositionWeightsPath() { return setCompositionWeightsPath; }

    public void setSetCompositionWeightsPath(final Path setCompositionWeightsPath) {
        this.setCompositionWeightsPath = setCompositionWeightsPath;
    }

    public double getFadDeploymentActionDecayConstant() { return fadDeploymentActionDecayConstant; }

    public void setFadDeploymentActionDecayConstant(final double fadDeploymentActionDecayConstant) {
        this.fadDeploymentActionDecayConstant = fadDeploymentActionDecayConstant;
    }

}
