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

package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.HeterogeneousLinearIntervalAttractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.WeibullDoubleParameter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class WeibullLinearIntervalAttractorFactory implements
        AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>>, PluggableSelectivity {


    private Map<Species, NonMutatingArrayFilter> selectivityFilters = ImmutableMap.of();

    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);

    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);

    private LinkedHashMap<String,Double> carryingCapacityShapeParameters = new LinkedHashMap<>();
    {
        carryingCapacityShapeParameters.put("Species 0", 0.5d);
    }
    private LinkedHashMap<String,Double> carryingCapacityScaleParameters = new LinkedHashMap<>();
    {
        carryingCapacityScaleParameters.put("Species 0", 100000d);
    }

    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);

    private DoubleParameter daysItTakesToFillUp = new FixedDoubleParameter(30);

    private DoubleParameter minAbundanceThreshold = new FixedDoubleParameter(100);

    private Locker<FishState, AbundanceFadInitializer > oneAttractorPerStateLocker = new Locker<>();

    @Override
    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(FishState fishState) {
        return oneAttractorPerStateLocker.presentKey(fishState,
                                              new Supplier<AbundanceFadInitializer>() {
                                                  @Override
                                                  public AbundanceFadInitializer get() {
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
                                                          carryingCapacities[species.getIndex()] = new WeibullDoubleParameter(
                                                                  carryingCapacityShapeParameters.get(species.getName()),
                                                                  carryingCapacityScaleParameters.get(species.getName())
                                                          );
                                                      }


                                                      return new AbundanceFadInitializer(
                                                              fishState.getBiology(),
                                                              capacityGenerator,
                                                              new HeterogeneousLinearIntervalAttractor(
                                                                      daysInWaterBeforeAttraction.apply(fishState.getRandom()).intValue(),
                                                                      daysItTakesToFillUp.apply(fishState.getRandom()).intValue(),
                                                                      minAbundanceThreshold.apply(fishState.getRandom()),
                                                                      selectivityFilters,
                                                                      fishState,
                                                                      carryingCapacities

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

    public void setSelectivityFilters(
            Map<Species, NonMutatingArrayFilter> selectivityFilters) {
        oneAttractorPerStateLocker.reset();

        this.selectivityFilters = selectivityFilters;
    }

    public DoubleParameter getFadDudRate() {
        return fadDudRate;
    }

    public void setFadDudRate(DoubleParameter fadDudRate) {

        oneAttractorPerStateLocker.reset();

        this.fadDudRate = fadDudRate;
    }

    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    public void setFishReleaseProbabilityInPercent(
            DoubleParameter fishReleaseProbabilityInPercent) {
        oneAttractorPerStateLocker.reset();

        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    public LinkedHashMap<String, Double> getCarryingCapacityShapeParameters() {
        return carryingCapacityShapeParameters;
    }

    public void setCarryingCapacityShapeParameters(
            LinkedHashMap<String, Double> carryingCapacityShapeParameters) {
        oneAttractorPerStateLocker.reset();

        this.carryingCapacityShapeParameters = carryingCapacityShapeParameters;
    }

    public LinkedHashMap<String, Double> getCarryingCapacityScaleParameters() {
        return carryingCapacityScaleParameters;
    }

    public void setCarryingCapacityScaleParameters(
            LinkedHashMap<String, Double> carryingCapacityScaleParameters) {
        oneAttractorPerStateLocker.reset();

        this.carryingCapacityScaleParameters = carryingCapacityScaleParameters;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(DoubleParameter daysInWaterBeforeAttraction) {
        oneAttractorPerStateLocker.reset();

        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    public DoubleParameter getDaysItTakesToFillUp() {
        return daysItTakesToFillUp;
    }

    public void setDaysItTakesToFillUp(DoubleParameter daysItTakesToFillUp) {
        oneAttractorPerStateLocker.reset();

        this.daysItTakesToFillUp = daysItTakesToFillUp;
    }

    public DoubleParameter getMinAbundanceThreshold() {
        return minAbundanceThreshold;
    }

    public void setMinAbundanceThreshold(DoubleParameter minAbundanceThreshold) {
        oneAttractorPerStateLocker.reset();

        this.minAbundanceThreshold = minAbundanceThreshold;
    }
}
