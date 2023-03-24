/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.AdditionalMapFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

/**
 * fads attract linearly, but can be penalized by environmental factors which are read as additional maps
 */
public class LinearEnvironmentalAttractorFactory implements
    AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>> {

    private final Locker<FishState, AbundanceFadInitializer> oneAttractorPerStateLocker =
        new Locker<>();
    private LinkedList<AdditionalMapFactory> environmentalMaps = new LinkedList<>();
    private LinkedList<DoubleParameter> environmentalThresholds = new LinkedList<>();
    private LinkedList<DoubleParameter> environmentalPenalties = new LinkedList<>();

    private AbundanceFiltersFactory abundanceFiltersFactory;

    public LinearEnvironmentalAttractorFactory() {
    }

    public LinearEnvironmentalAttractorFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory
    ) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    private LinkedHashMap<String, Double> maximumCarryingCapacities = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> catchabilities = new LinkedHashMap<>();
    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);
    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);
    private DoubleParameter maximumDaysAttractions = new FixedDoubleParameter(30);
    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);
    private DoubleParameter carryingCapacityMultiplier = new FixedDoubleParameter(1.0);

    {
        final AdditionalMapFactory e = new AdditionalMapFactory();
        environmentalMaps.add(e);
        environmentalThresholds.add(new FixedDoubleParameter(0.15));
        environmentalPenalties.add(new FixedDoubleParameter(2));
    }

    {
        maximumCarryingCapacities.put("Skipjack tuna", 135000d);
        maximumCarryingCapacities.put("Yellowfin tuna", 40000d);
        maximumCarryingCapacities.put("Bigeye tuna", 60000d);
    }

    {
        catchabilities.put("Species 0", 0.001d);
    }

    public static Function<SeaTile, Double> createEnvironmentalPenaltyAndStartEnvironmentalMaps(
        final LinkedList<AdditionalMapFactory> environmentalMaps,
        final LinkedList<DoubleParameter> environmentalPenalties,
        final LinkedList<DoubleParameter> environmentalThresholds,
        final FishState fishState
    ) {
        Function<SeaTile, Double> catchabilityPenaltyFunction = null;
        //start the map
        for (int environmental = 0; environmental < environmentalMaps.size(); environmental++) {

            final AdditionalStartable newMap = environmentalMaps.get(environmental).apply(fishState);
            fishState.registerStartable(newMap);
            final String mapName = environmentalMaps.get(environmental).mapVariableName;
            final double threshold = environmentalThresholds.get(environmental).apply(fishState.getRandom());
            final double penalty = environmentalPenalties.get(environmental).apply(fishState.getRandom());

            final Function<SeaTile, Double> penaltyMultiplier = seaTile -> {
                final double currentHere = fishState.getMap().getAdditionalMaps().get(
                    mapName).get().get(
                    seaTile.getGridX(),
                    seaTile.getGridY()
                );
                return Math.pow(Math.min(1d, currentHere / threshold), penalty);
            };
            if (catchabilityPenaltyFunction == null) catchabilityPenaltyFunction = penaltyMultiplier;
            else {
                final Function<SeaTile, Double> oldPenalty = catchabilityPenaltyFunction;
                catchabilityPenaltyFunction = seaTile -> oldPenalty.apply(seaTile) * penaltyMultiplier.apply(seaTile);
            }

        }
        return catchabilityPenaltyFunction;
    }

    @Override
    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(final FishState fishState) {
        return oneAttractorPerStateLocker.presentKey(
            fishState,
            () -> {
                //make sure the lists are all of the same size!
                Preconditions.checkArgument(environmentalMaps.size() > 0);
                Preconditions.checkArgument(environmentalMaps.size() == environmentalPenalties.size());
                Preconditions.checkArgument(environmentalMaps.size() == environmentalThresholds.size());

                //attractor:
                final MersenneTwisterFast rng = fishState.getRandom();
                final double probabilityOfFadBeingDud = fadDudRate.apply(rng);
                final DoubleSupplier capacityGenerator;
                if (Double.isNaN(probabilityOfFadBeingDud) || probabilityOfFadBeingDud == 0)
                    capacityGenerator = () -> Double.MAX_VALUE;
                else
                    capacityGenerator = () -> {
                        if (rng.nextFloat() <= probabilityOfFadBeingDud)
                            return 0;
                        else
                            return Double.MAX_VALUE;
                    };

                final GlobalBiology globalBiology = fishState.getBiology();
                final DoubleParameter[] carryingCapacities = new DoubleParameter[globalBiology.getSize()];
                for (final Species species : globalBiology.getSpecies()) {
                    carryingCapacities[species.getIndex()] =
                        maximumCarryingCapacities.containsKey(species.getName()) ?
                            new FixedDoubleParameter(
                                maximumCarryingCapacities.get(species.getName()) *
                                    carryingCapacityMultiplier.apply(rng)

                            ) :
                            new FixedDoubleParameter(-1);


                }
                final Function<SeaTile, Double> finalCatchabilityPenaltyFunction = createEnvironmentalPenaltyAndStartEnvironmentalMaps(
                    environmentalMaps, environmentalPenalties, environmentalThresholds, fishState);

                final Function<AbstractFad, double[]> catchabilitySupplier = abstractFad -> {

                    final double[] cachability = new double[globalBiology.getSize()];
                    final SeaTile fadLocation = abstractFad.getLocation();
                    final double penaltyHere = finalCatchabilityPenaltyFunction.apply(fadLocation);
                    if (penaltyHere <= 0 || !Double.isFinite(penaltyHere))
                        return cachability;

                    for (final Species species : globalBiology.getSpecies())
                        cachability[species.getIndex()] = catchabilities.getOrDefault(species.getName(), 0d) *
                            penaltyHere;
                    return cachability;
                };


                return new AbundanceFadInitializer(
                    globalBiology,
                    capacityGenerator,
                    new CatchabilitySelectivityFishAttractor(
                        carryingCapacities,
                        catchabilitySupplier,
                        daysInWaterBeforeAttraction.apply(rng).intValue(),
                        maximumDaysAttractions.apply(rng).intValue(),
                        fishState,
                        abundanceFiltersFactory.apply(fishState).get(FadSetAction.class)
                    ),
                    fishReleaseProbabilityInPercent.apply(rng) / 100d,
                    fishState::getStep
                );
            }

        );


    }

    public LinkedHashMap<String, Double> getCatchabilities() {
        return catchabilities;
    }

    public void setCatchabilities(final LinkedHashMap<String, Double> catchabilities) {
        this.catchabilities = catchabilities;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(final DoubleParameter daysInWaterBeforeAttraction) {
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    public DoubleParameter getMaximumDaysAttractions() {
        return maximumDaysAttractions;
    }

    public void setMaximumDaysAttractions(final DoubleParameter maximumDaysAttractions) {
        this.maximumDaysAttractions = maximumDaysAttractions;
    }

    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    public void setFishReleaseProbabilityInPercent(
        final DoubleParameter fishReleaseProbabilityInPercent
    ) {
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    public DoubleParameter getFadDudRate() {
        return fadDudRate;
    }

    public void setFadDudRate(final DoubleParameter fadDudRate) {
        this.fadDudRate = fadDudRate;
    }


    public LinkedHashMap<String, Double> getMaximumCarryingCapacities() {
        return maximumCarryingCapacities;
    }

    public void setMaximumCarryingCapacities(final LinkedHashMap<String, Double> maximumCarryingCapacities) {
        this.maximumCarryingCapacities = maximumCarryingCapacities;
    }

    public DoubleParameter getCarryingCapacityMultiplier() {
        return carryingCapacityMultiplier;
    }

    public void setCarryingCapacityMultiplier(final DoubleParameter carryingCapacityMultiplier) {
        this.carryingCapacityMultiplier = carryingCapacityMultiplier;
    }

    public LinkedList<AdditionalMapFactory> getEnvironmentalMaps() {
        return environmentalMaps;
    }

    public void setEnvironmentalMaps(final LinkedList<AdditionalMapFactory> environmentalMaps) {
        this.environmentalMaps = environmentalMaps;
    }

    public LinkedList<DoubleParameter> getEnvironmentalThresholds() {
        return environmentalThresholds;
    }

    public void setEnvironmentalThresholds(
        final LinkedList<DoubleParameter> environmentalThresholds
    ) {
        this.environmentalThresholds = environmentalThresholds;
    }

    public LinkedList<DoubleParameter> getEnvironmentalPenalties() {
        return environmentalPenalties;
    }

    public void setEnvironmentalPenalties(
        final LinkedList<DoubleParameter> environmentalPenalties
    ) {
        this.environmentalPenalties = environmentalPenalties;
    }
}
