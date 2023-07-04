/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.boxcars;

import com.google.common.base.Predicate;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.AbundanceFilter;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;

public class FishingMortalityAgent implements AdditionalStartable, Steppable {


    private static final long serialVersionUID = -5314260056907711315L;
    /**
     * given the total count of fish everywhere, returns the vulnerable part. This ought to be a selectivity curve
     */
    private final AbundanceFilter vulnerabilityFilter;

    private final Species species;
    /**
     * do we bother computing landings every day?
     */
    private final boolean computeDailyFishingMortality;
    /**
     * here we track of all the landings (IN WEIGHT) for the year. For yearly mortality rate
     */
    double[][] yearlyCatchesInWeight;
    private StochasticCatchSampler dailyCatchSampler;
    private double lastDailyMortality;
    private Stoppable dailyStoppable;

    private Stoppable yearlyStoppable;


    private double[][] lastMeasuredYearlyAbundance = null;

    public FishingMortalityAgent(
        final AbundanceFilter vulnerabilityFilter,
        final Species species,
        final boolean computeDailyFishingMortality
    ) {
        this.vulnerabilityFilter = vulnerabilityFilter;
        this.species = species;
        this.computeDailyFishingMortality = computeDailyFishingMortality;
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if (dailyStoppable != null) {
            dailyStoppable.stop();
            yearlyStoppable.stop();
            dailyCatchSampler.turnOff();
        }
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {

        //let's look at abundance now
        lastMeasuredYearlyAbundance = model.getTotalAbundance(species);

        dailyCatchSampler = new StochasticCatchSampler(
            (Predicate<Fisher>) input -> true,
            species,
            null
        );
        dailyCatchSampler.start(model);
        yearlyCatchesInWeight = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
        dailyStoppable =
            model.scheduleEveryDay(this, StepOrder.DAILY_DATA_GATHERING);
        yearlyStoppable =
            model.scheduleEveryYear((Steppable) simState -> {
                //observe abundance
                lastMeasuredYearlyAbundance = model.getTotalAbundance(species);


                //clear landings observed
                for (int subdivision = 0; subdivision < species.getNumberOfSubdivisions(); subdivision++)
                    Arrays.fill(yearlyCatchesInWeight[subdivision], 0);
            }, StepOrder.DATA_RESET);


        if (computeDailyFishingMortality) {
            final DataColumn dailyColumn = model.getDailyDataSet().registerGatherer("Daily Fishing Mortality " + species,
                (Gatherer<FishState>) fishState -> lastDailyMortality, Double.NaN
            );


            //yearly you account for the average
            model.getYearlyDataSet().registerGatherer(
                "Average Daily Fishing Mortality " + species,
                FishStateUtilities.generateYearlyAverage(dailyColumn),
                Double.NaN
            )
            ;

        }

        model.getYearlyDataSet().registerGatherer("Yearly Fishing Mortality " + species,
            (Gatherer<FishState>) fishState -> computeYearlyMortality(), Double.NaN
        );


    }

    /**
     * compute daily mortality rate
     *
     * @return
     */
    public double computeYearlyMortality() {


        return computeMortality(
            CatchSample.convertLandingsToAbundance(
                species,
                yearlyCatchesInWeight
            ),
            lastMeasuredYearlyAbundance
        );


    }

    /**
     * compute mortality rate - log(1- catches/vulnerables)
     *
     * @return
     */
    public double computeMortality(
        final double[][] catches,
        final double[][] totalAbundance
    ) {

        final double[][] vulnerable = vulnerabilityFilter.filter(species, totalAbundance);

        double numerator = 0;
        double denominator = 0;
        assert (vulnerable.length == catches.length);
        assert (vulnerable[0].length == catches[0].length);
        for (int subdivision = 0; subdivision < species.getNumberOfSubdivisions(); subdivision++) {
            for (int bin = 0; bin < species.getNumberOfBins(); bin++) {

                numerator += catches[subdivision][bin];
                denominator += vulnerable[subdivision][bin];

            }

        }

        return -Math.log(1d - numerator / denominator);


    }

    @Override
    public void step(final SimState simState) {


        dailyCatchSampler.resetCatchObservations();
        dailyCatchSampler.observeDaily();
        if (computeDailyFishingMortality)
            lastDailyMortality = computeDailyMortality((FishState) simState);
        final double[][] abundance = dailyCatchSampler.getLandings();

        for (int subdivision = 0; subdivision < species.getNumberOfSubdivisions(); subdivision++)
            for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
                yearlyCatchesInWeight[subdivision][bin] += abundance[subdivision][bin];
            }

    }

    /**
     * compute daily mortality rate
     *
     * @param model
     * @return
     */
    public double computeDailyMortality(final FishState model) {


        return computeMortality(
            dailyCatchSampler.getAbundance(),
            model.getTotalAbundance(species)
        );


    }


}
