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
import com.google.common.collect.ImmutableMap;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.PluggableSelectivity;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.AdditionalMapFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * fads attract linearly, but can be penalized by environmental factors which are read as additional maps
 */
public class LinearEnvironmentalAttractorFactory  implements
        AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>>, PluggableSelectivity {



    private LinkedList<AdditionalMapFactory> environmentalMaps = new LinkedList<>();

    private LinkedList<DoubleParameter>  environmentalThresholds = new LinkedList<>();

    private LinkedList<DoubleParameter>  environmentalPenalties = new LinkedList<>();

    {
        AdditionalMapFactory e = new AdditionalMapFactory();
        environmentalMaps.add(e);
        environmentalThresholds.add(new FixedDoubleParameter(0.15));
        environmentalPenalties.add(new FixedDoubleParameter(2));
    }

    private Map<Species, NonMutatingArrayFilter> selectivityFilters = ImmutableMap.of();


    private LinkedHashMap<String,Double> maximumCarryingCapacities = new LinkedHashMap<>();
    {
        maximumCarryingCapacities.put("Skipjack tuna",135000d);
        maximumCarryingCapacities.put("Yellowfin tuna",40000d);
        maximumCarryingCapacities.put("Bigeye tuna",60000d);
    }

    private LinkedHashMap<String,Double> catchabilities = new LinkedHashMap<>();
    {
        catchabilities.put("Species 0", 0.001d);
    }

    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);


    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);

    private DoubleParameter maximumDaysAttractions = new FixedDoubleParameter(30);

    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);

    private DoubleParameter carryingCapacityMultiplier = new FixedDoubleParameter(1.0);


    private final Locker<FishState, AbundanceFadInitializer> oneAttractorPerStateLocker =
            new Locker<>();


    @Override
    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(FishState fishState) {
        return oneAttractorPerStateLocker.presentKey(
                fishState,
                new Supplier<AbundanceFadInitializer>() {
                    @Override
                    public AbundanceFadInitializer get() {
                        //make sure the lists are all of the same size!
                        Preconditions.checkArgument(environmentalMaps.size()>0);
                        Preconditions.checkArgument(environmentalMaps.size()==environmentalPenalties.size());
                        Preconditions.checkArgument(environmentalMaps.size()==environmentalThresholds.size());

                        Function<SeaTile,Double> catchabilityPenaltyFunction = null;

                        //start the map
                        for (int environmental = 0; environmental < environmentalMaps.size(); environmental++) {

                            AdditionalStartable newMap = environmentalMaps.get(environmental).apply(fishState);
                            fishState.registerStartable(newMap);
                            final String mapName = environmentalMaps.get(environmental).mapVariableName;
                            final double threshold = environmentalThresholds.get(environmental).apply(fishState.getRandom());
                            final double penalty = environmentalPenalties.get(environmental).apply(fishState.getRandom());

                            final Function<SeaTile, Double> penaltyMultiplier = seaTile -> {
                                double currentHere = fishState.getMap().getAdditionalMaps().get(
                                        mapName).get().get(
                                        seaTile.getGridX(),
                                        seaTile.getGridY()
                                );
                                return Math.pow(Math.min(1d, currentHere / threshold), penalty);
                            };
                            if(catchabilityPenaltyFunction == null) catchabilityPenaltyFunction = penaltyMultiplier;
                            else {
                                Function<SeaTile, Double> oldPenalty = catchabilityPenaltyFunction;
                                catchabilityPenaltyFunction = seaTile -> oldPenalty.apply(seaTile) * penaltyMultiplier.apply(seaTile);
                            }

                        }

                        //attractor:
                        final double probabilityOfFadBeingDud = fadDudRate.apply(fishState.getRandom());
                        DoubleSupplier capacityGenerator;
                        if(Double.isNaN(probabilityOfFadBeingDud) || probabilityOfFadBeingDud==0)
                            capacityGenerator = () -> Double.MAX_VALUE;
                        else
                            capacityGenerator = () -> {
                                if(fishState.getRandom().nextFloat()<=probabilityOfFadBeingDud)
                                    return 0;
                                else
                                    return Double.MAX_VALUE;
                            };

                        DoubleParameter[] carryingCapacities = new DoubleParameter[fishState.getBiology().getSize()];
                        for (Species species : fishState.getBiology().getSpecies()) {
                            carryingCapacities[species.getIndex()] =
                                    maximumCarryingCapacities.containsKey(species.getName()) ?
                                            new FixedDoubleParameter(
                                                    maximumCarryingCapacities.get(species.getName()) *
                                                            carryingCapacityMultiplier.apply(fishState.getRandom())

                                            ) :
                                            new FixedDoubleParameter(-1);


                        }
                        final Function<SeaTile, Double> finalCatchabilityPenaltyFunction = catchabilityPenaltyFunction;
                        Function<AbstractFad,double[]> catchabilitySupplier = abstractFad -> {

                            double[] cachability = new double[fishState.getBiology().getSize()];
                            SeaTile fadLocation = abstractFad.getLocation();
                            double penaltyHere = finalCatchabilityPenaltyFunction.apply(fadLocation);

                            for (Species species : fishState.getBiology().getSpecies())
                                cachability[species.getIndex()] = catchabilities.getOrDefault(species.getName(),0d) *
                                        penaltyHere;
                            return cachability;
                        };


                        return new AbundanceFadInitializer(
                                fishState.getBiology(),
                                capacityGenerator,
                                new CatchabilitySelectivityFishAttractor(
                                        carryingCapacities,
                                        catchabilitySupplier,
                                        daysInWaterBeforeAttraction.apply(fishState.getRandom()).intValue(),
                                        maximumDaysAttractions.apply(fishState.getRandom()).intValue(),
                                        fishState,
                                        selectivityFilters

                                ),
                                fishReleaseProbabilityInPercent.apply(fishState.getRandom()) / 100d,
                                fishState::getStep
                        );
                    }
                }

        );


    }

    public Map<Species, NonMutatingArrayFilter> getSelectivityFilters() {
        return selectivityFilters;
    }

    @Override
    public void setSelectivityFilters(
            Map<Species, NonMutatingArrayFilter> selectivityFilters) {
        this.selectivityFilters = selectivityFilters;
    }


    public LinkedHashMap<String, Double> getCatchabilities() {
        return catchabilities;
    }

    public void setCatchabilities(LinkedHashMap<String, Double> catchabilities) {
        this.catchabilities = catchabilities;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(DoubleParameter daysInWaterBeforeAttraction) {
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    public DoubleParameter getMaximumDaysAttractions() {
        return maximumDaysAttractions;
    }

    public void setMaximumDaysAttractions(DoubleParameter maximumDaysAttractions) {
        this.maximumDaysAttractions = maximumDaysAttractions;
    }

    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    public void setFishReleaseProbabilityInPercent(
            DoubleParameter fishReleaseProbabilityInPercent) {
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    public DoubleParameter getFadDudRate() {
        return fadDudRate;
    }

    public void setFadDudRate(DoubleParameter fadDudRate) {
        this.fadDudRate = fadDudRate;
    }


    public LinkedHashMap<String, Double> getMaximumCarryingCapacities() {
        return maximumCarryingCapacities;
    }

    public void setMaximumCarryingCapacities(LinkedHashMap<String, Double> maximumCarryingCapacities) {
        this.maximumCarryingCapacities = maximumCarryingCapacities;
    }

    public DoubleParameter getCarryingCapacityMultiplier() {
        return carryingCapacityMultiplier;
    }

    public void setCarryingCapacityMultiplier(DoubleParameter carryingCapacityMultiplier) {
        this.carryingCapacityMultiplier = carryingCapacityMultiplier;
    }

    public LinkedList<AdditionalMapFactory> getEnvironmentalMaps() {
        return environmentalMaps;
    }

    public void setEnvironmentalMaps(LinkedList<AdditionalMapFactory> environmentalMaps) {
        this.environmentalMaps = environmentalMaps;
    }

    public LinkedList<DoubleParameter> getEnvironmentalThresholds() {
        return environmentalThresholds;
    }

    public void setEnvironmentalThresholds(
            LinkedList<DoubleParameter> environmentalThresholds) {
        this.environmentalThresholds = environmentalThresholds;
    }

    public LinkedList<DoubleParameter> getEnvironmentalPenalties() {
        return environmentalPenalties;
    }

    public void setEnvironmentalPenalties(
            LinkedList<DoubleParameter> environmentalPenalties) {
        this.environmentalPenalties = environmentalPenalties;
    }
}
