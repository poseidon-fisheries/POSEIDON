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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.Map;

/**
 * carrying capacity here is generated at random and can differ for each FAD
 */
public class HeterogeneousLinearIntervalAttractor
    extends AbstractAbundanceLinearIntervalAttractor implements FadRemovalListener {


    private final double minAbundanceThreshold;
    private final int daysItTakesToFillUp;
    private final Map<Fad, HashMap<Species, double[][]>> dailyAttractionThreshold = new HashMap<>();
    private final Map<Fad, HashMap<Species, double[][]>> dailyAbundanceAttracted = new HashMap<>();

    public HeterogeneousLinearIntervalAttractor(
        final int daysInWaterBeforeAttraction,
        final int daysItTakesToFillUp,
        final double minAbundanceThreshold,
        final Map<Species, NonMutatingArrayFilter> globalSelectivityCurves,
        final FishState model
    ) {
        super(globalSelectivityCurves, model, daysInWaterBeforeAttraction);
        this.daysItTakesToFillUp = daysItTakesToFillUp;
        this.minAbundanceThreshold = minAbundanceThreshold;
        model.getFadMap().getRemovalListeners().add(this);
    }

    /**
     * function that returns how much this fad will attract today in abundance given that all preliminary checks have
     * passed
     *
     * @param seaTileBiology seatile underneath
     * @param fad            fad object
     */
    @Override
    protected WeightedObject<AbundanceLocalBiology> attractDaily(
        final AbundanceLocalBiology seaTileBiology,
        final AbundanceAggregatingFad fad
    ) {
        if (!dailyAbundanceAttracted.containsKey(fad)) {
            computeFadAttractions(fad);
        }
        final AbundanceLocalBiology toReturn = new AbundanceLocalBiology(dailyAbundanceAttracted.get(fad));
        return new WeightedObject<>(toReturn, toReturn.getTotalBiomass());
    }


    /**
     * compute what the fad ought to attract
     *
     * @param fad the fad to initialize
     * @return the carrying capacity for this fad in kg
     */
    private void computeFadAttractions(
        final AbundanceAggregatingFad fad
    ) {
        assert !dailyAttractionThreshold.containsKey(fad);
        assert !dailyAbundanceAttracted.containsKey(fad);

        final double[] carryingCapacities = fad.getCarryingCapacity().getCarryingCapacities();

        //compute daily kg landed per fad
        final double[] dailyBiomassAttractedPerSpecies = new double[carryingCapacities.length];
        for (int i = 0; i < carryingCapacities.length; i++) {
            dailyBiomassAttractedPerSpecies[i] =
                carryingCapacities[i] / (double) daysItTakesToFillUp;
        }

        //turn that into abundance!
        final HashMap<Species, double[][]> dailyAttractionHere = new HashMap<>();
        final HashMap<Species, double[][]> dailyAttractionThresholdHere = new HashMap<>();
        fillUpAttractionAndThresholdAttractionMatrices(
            dailyBiomassAttractedPerSpecies,
            dailyAttractionHere,
            dailyAttractionThresholdHere,
            minAbundanceThreshold,
            super.globalSelectivityCurves,
            super.getAbundancePerDailyKgLanded()
        );
        dailyAbundanceAttracted.put(fad, dailyAttractionHere);
        dailyAttractionThreshold.put(fad, dailyAttractionThresholdHere);
    }

    static void fillUpAttractionAndThresholdAttractionMatrices(
        final double[] dailyBiomassAttractedPerSpecies, final Map<? super Species, double[][]> dailyAttractionHere,
        final Map<? super Species, double[][]> dailyAttractionThresholdHere,
        final double minAbundanceThreshold,
        final Map<Species, NonMutatingArrayFilter> globalSelectivityCurves,
        final HashMap<Species, double[][]> kgToAbundanceTransformer
    ) {
        for (final Map.Entry<Species, NonMutatingArrayFilter> speciesSelectivity : globalSelectivityCurves.entrySet()) {
            final Species species = speciesSelectivity.getKey();
            final double[][] kgToAbundance = kgToAbundanceTransformer.get(species);
            final double[][] dailyStep = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
            final double[][] dailyThreshold = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
            for (int sub = 0; sub < kgToAbundance.length; sub++) {
                for (int bin = 0; bin < kgToAbundance[0].length; bin++) {
                    //multiply abundance/kg converter by kg
                    dailyStep[sub][bin] =
                        (dailyBiomassAttractedPerSpecies[species.getIndex()] * kgToAbundance[sub][bin]);

                    dailyThreshold[sub][bin] = dailyStep[sub][bin] * minAbundanceThreshold;

                }
            }
            dailyAttractionThresholdHere.put(species, dailyThreshold);
            dailyAttractionHere.put(species, dailyStep);

        }
    }

    /**
     * get the minimum amount of abundance there needs to be in a cell for this species without which the FAD won't
     * attract!
     */
    @Override
    public HashMap<Species, double[][]> getDailyAttractionThreshold(final AbundanceAggregatingFad fad) {
        if (!dailyAbundanceAttracted.containsKey(fad)) {
            computeFadAttractions(fad);
        }
        return dailyAttractionThreshold.get(fad);
    }


    @Override
    public void onFadRemoval(final Fad fad) {
        dailyAttractionThreshold.remove(fad);
        dailyAbundanceAttracted.remove(fad);
    }
}
