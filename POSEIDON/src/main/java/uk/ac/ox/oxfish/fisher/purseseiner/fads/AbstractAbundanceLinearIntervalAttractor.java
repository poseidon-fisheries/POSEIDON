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
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Inspired somewhat by the IOTC paper: a FAD that "activates" after a certain number of days and then attracts at a
 * fixed rate until full. Whether anything gets attracted depends on there being enough local biomass (for example 100
 * times more abundance than what would be attracted in a day).
 * <p>
 * This for now uses a form of relatively questionable selectivity that updates daily to be correct in a "global" sense:
 * fundamentally all this abstract class does is each time step it looks at the global population and derive what the
 * selectivity curve implies we should catch for each 1kg of landings in terms of # of fish per bin.
 */
public abstract class AbstractAbundanceLinearIntervalAttractor
    implements FishAttractor<AbundanceLocalBiology, AbundanceAggregatingFad>,
    Steppable {

    private static final long serialVersionUID = -4518063352770580734L;
    protected final Map<Species, NonMutatingArrayFilter> globalSelectivityCurves;
    protected final FishState model;
    private final int daysInWaterBeforeAttraction;
    /**
     * each step we compute how many # of fish we need to catch for each age bin to fulfill 1kg of landings (per
     * species); this can then be retrieved for each fad. Basically it is a selectivity-driven transformer from kg
     * caught to abundance caught
     */
    private HashMap<Species, double[][]> abundancePerDailyKgLanded;
    /**
     * if this predicate is not true, there will be no further attraction this day
     */
    private Predicate<SeaTile> additionalAttractionHurdle = seaTile -> true;

    public AbstractAbundanceLinearIntervalAttractor(
        final Map<Species, NonMutatingArrayFilter> globalSelectivityCurves,
        final FishState model,
        final int daysInWaterBeforeAttraction
    ) {
        this.globalSelectivityCurves = globalSelectivityCurves;
        checkArgument(daysInWaterBeforeAttraction >= 0);
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        model.scheduleEveryDay(this, StepOrder.DAWN);
        this.model = model;
    }

    @Override
    public WeightedObject<AbundanceLocalBiology> attractImplementation(
        final LocalBiology seaTileBiology,
        final AbundanceAggregatingFad fad
    ) {

        if (!(seaTileBiology instanceof AbundanceLocalBiology)) {
            return null;
        } else {
            final AbundanceLocalBiology abundanceLocalBiology =
                (AbundanceLocalBiology) seaTileBiology;
            return
                shouldICancelTheAttractionToday(abundanceLocalBiology, fad)
                    ? null
                    // you passed all checks! attract
                    : attractDaily(abundanceLocalBiology, fad);
        }
    }

    protected boolean shouldICancelTheAttractionToday(
        final AbundanceLocalBiology seaTileBiology,
        final AbundanceAggregatingFad fad
    ) {
        checkArgument(
            fad.getCarryingCapacity() instanceof PerSpeciesCarryingCapacity,
            "This attractor only works with per-species carrying capacities."
        );
        if (!additionalAttractionHurdle.test(fad.getLocation()))
            return true;

        // attract nothing before spending enough steps in
        if (model.getDay() - fad.getStepDeployed() < daysInWaterBeforeAttraction || !fad.isActive())
            return true;
        // start weighing stuff
        // don't bother attracting if full
        final double[] currentFadBiomass = fad.getBiology().getCurrentBiomass();
        final double[] carryingCapacitiesPerSpecies =
            ((PerSpeciesCarryingCapacity) fad.getCarryingCapacity()).getCarryingCapacities();
        for (int i = 0; i < carryingCapacitiesPerSpecies.length; i++) {
            if (carryingCapacitiesPerSpecies[i] == 0) // todo test that they fill up and then move no further!
                continue;
            // if one is full, they are all full
            if (currentFadBiomass[i] >= carryingCapacitiesPerSpecies[i])
                return true;

        }
        // don't bother attracting if there is less abundance than the threshold
        // don't bother attracting if any abundance bin is below threshold
        final HashMap<Species, double[][]> thresholds = getDailyAttractionThreshold(fad);
        Preconditions.checkState(thresholds != null);
        Preconditions.checkState(!thresholds.isEmpty());
        for (final Map.Entry<Species, StructuredAbundance> speciesAbundance : seaTileBiology.getStructuredAbundance()
            .entrySet()) {
            final double[][] abundanceInTile = speciesAbundance.getValue().asMatrix();
            final double[][] threshold = thresholds.get(speciesAbundance.getKey());
            for (int subdivision = 0; subdivision < threshold.length; subdivision++) {
                for (int bin = 0; bin < threshold[subdivision].length; bin++) {
                    if (threshold[subdivision][bin] > abundanceInTile[subdivision][bin])
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * function that returns how much this fad will attract today in abundance given that all preliminary checks have
     * passed
     *
     * @param seaTileBiology seatile underneath
     * @param fad            fad object
     * @return
     */
    protected abstract WeightedObject<AbundanceLocalBiology> attractDaily(
        AbundanceLocalBiology seaTileBiology,
        AbundanceAggregatingFad fad
    );

    /**
     * get the minimum amount of abundance there needs to be in a cell for this species without which the FAD won't
     * attract!
     */
    public abstract HashMap<Species, double[][]> getDailyAttractionThreshold(AbundanceAggregatingFad fad);

    @Override
    public void step(final SimState simState) {
        abundancePerDailyKgLanded = turnSelectivityIntoBiomassToAbundanceConverter(
            ((FishState) simState), globalSelectivityCurves
        );

    }

    public static HashMap<Species, double[][]> turnSelectivityIntoBiomassToAbundanceConverter(
        final FishState state,
        final Map<Species, NonMutatingArrayFilter> globalSelectivityCurves
    ) {
        final HashMap<Species, double[][]> abundancePerDailyKgLanded = new HashMap<>();
        //   dailyAttractionThreshold = new HashMap<>();
        // update your daily attraction given selectivity curves
        for (final Map.Entry<Species, NonMutatingArrayFilter> speciesSelectivity : globalSelectivityCurves.entrySet()) {
            final Species species = speciesSelectivity.getKey();
            final double[][] oceanAbundance = state.getTotalAbundance(species);
            final double[][] selectedAbundance = speciesSelectivity.getValue().filter(species, oceanAbundance);
            // here we store the WEIGHT of the fish that would be selected had we applied the selectivity curve to the
            // whole ocean
            // ignoring (i.e. setting to 1) catchability
            final double[][] selectedWeight = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
            double totalSelectedWeight = 0;
            // weigh the abundance of the filtered matrix for all bins
            for (int sub = 0; sub < selectedAbundance.length; sub++) {
                for (int bin = 0; bin < selectedAbundance[0].length; bin++) {
                    selectedWeight[sub][bin] = selectedAbundance[sub][bin] * species.getWeight(sub, bin);
                    totalSelectedWeight += selectedWeight[sub][bin];
                }
            }
            // now given the weight per bin and total weight, find how many we need to take for each bin to perform a
            // daily step
            final double[][] dailyStep = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
            for (int sub = 0; sub < selectedAbundance.length; sub++) {
                for (int bin = 0; bin < selectedAbundance[0].length; bin++) {
                    // weight that should come from this bin
                    dailyStep[sub][bin] =
                        (selectedWeight[sub][bin] / totalSelectedWeight)
                            // and now turn it all into abundance:
                            / species.getWeight(sub, bin);

                    dailyStep[sub][bin] = Math.max(0, dailyStep[sub][bin]);
                }
            }
            abundancePerDailyKgLanded.put(species, dailyStep);

        }
        return abundancePerDailyKgLanded;

    }

    protected HashMap<Species, double[][]> getAbundancePerDailyKgLanded() {
        return abundancePerDailyKgLanded;
    }

    public Predicate<SeaTile> getAdditionalAttractionHurdle() {
        return additionalAttractionHurdle;
    }

    public void setAdditionalAttractionHurdle(
        final Predicate<SeaTile> additionalAttractionHurdle
    ) {
        this.additionalAttractionHurdle = additionalAttractionHurdle;
    }
}
