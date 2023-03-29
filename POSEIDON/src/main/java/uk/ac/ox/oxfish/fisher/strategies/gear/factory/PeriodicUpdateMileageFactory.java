/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.strategies.gear.factory;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.strategies.gear.PeriodicUpdateGearStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Gear Strategy  needed to update mileage over time
 * Created by carrknight on 6/13/16.
 */
public class PeriodicUpdateMileageFactory implements AlgorithmFactory<PeriodicUpdateGearStrategy> {


    /**
     * mantains a (weak) set of fish states so that we initialize our data gatherers only once!
     */
    private final Set<FishState> weakStateMap = Collections.newSetFromMap(new WeakHashMap<>());
    private AlgorithmFactory<? extends AdaptationProbability>
        probability = new FixedProbabilityFactory(.2, .6);
    private boolean yearly = false;
    private DoubleParameter minimumGasPerLiter = new FixedDoubleParameter(0);

    private DoubleParameter maximumGasPerLiter = new FixedDoubleParameter(20);

    /**
     * maximum change per update as a proportion of (maximumGasPerLiter-minimumGasPerLiter)
     */
    private DoubleParameter shockSize = new FixedDoubleParameter(0.05);


    /**
     * Applies this function to the given argument.
     *
     * @param model the function argument
     * @return the function result
     */
    @Override
    public PeriodicUpdateGearStrategy apply(final FishState model) {

        final double shock = shockSize.applyAsDouble(model.getRandom());
        final double minTrawlingSpeed = minimumGasPerLiter.applyAsDouble(model.getRandom());
        final double maxTrawlingSpeed = maximumGasPerLiter.applyAsDouble(model.getRandom());

        //add data gathering if necessary
        if (!weakStateMap.contains(model)) {
            weakStateMap.add(model);
            addDataGatherers(model);
            assert weakStateMap.contains(model);
        }

        return new PeriodicUpdateGearStrategy(
            yearly,
            (state, random, fisher, current1) -> {
                Preconditions.checkArgument(
                    current1.getClass().equals(RandomCatchabilityTrawl.class),
                    "PeriodicUpdateMileageFactory works only with RandomCatchabilityTrawl gear"
                );
                assert current1.getClass().equals(RandomCatchabilityTrawl.class);
                final RandomCatchabilityTrawl current = ((RandomCatchabilityTrawl) current1);

                double currentShock = (random.nextDouble() - 0.5) * shock * (maxTrawlingSpeed - minTrawlingSpeed);
                if (random.nextBoolean())
                    currentShock -= currentShock;
                double newMileage = current.getGasPerHourFished() + currentShock;
                newMileage = Math.max(newMileage, minTrawlingSpeed);
                newMileage = Math.min(newMileage, maxTrawlingSpeed);
                return new RandomCatchabilityTrawl(
                    current.getCatchabilityMeanPerSpecie(),
                    current.getCatchabilityDeviationPerSpecie(),
                    newMileage
                );
            }
            ,
            probability.apply(model)

        );
    }

    private void addDataGatherers(final FishState model) {
        //first add data gatherers
        model.getDailyDataSet().registerGatherer("Thrawling Fuel Consumption", state -> {
            final double size = state.getFishers().size();
            if (size == 0)
                return Double.NaN;
            else {
                double total = 0;
                for (final Fisher fisher1 : state.getFishers())
                    total += ((RandomCatchabilityTrawl) fisher1.getGear()).getGasPerHourFished();
                return total / size;
            }
        }, Double.NaN);


        for (int i = 0; i < model.getSpecies().size(); i++) {
            final int finalI = i;
            model.getDailyDataSet().registerGatherer("Trawling Efficiency for Species " + i,
                state -> {
                    final double size = state.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (final Fisher fisher1 : state.getFishers())
                            total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[finalI];
                        return total / size;
                    }
                }, Double.NaN
            );
        }
    }

    /**
     * Getter for property 'probability'.
     *
     * @return Value for property 'probability'.
     */
    public AlgorithmFactory<? extends AdaptationProbability> getProbability() {
        return probability;
    }

    /**
     * Setter for property 'probability'.
     *
     * @param probability Value to set for property 'probability'.
     */
    public void setProbability(
        final AlgorithmFactory<? extends AdaptationProbability> probability
    ) {
        this.probability = probability;
    }

    /**
     * Getter for property 'yearly'.
     *
     * @return Value for property 'yearly'.
     */
    public boolean isYearly() {
        return yearly;
    }

    /**
     * Setter for property 'yearly'.
     *
     * @param yearly Value to set for property 'yearly'.
     */
    public void setYearly(final boolean yearly) {
        this.yearly = yearly;
    }

    /**
     * Getter for property 'minimumGasPerLiter'.
     *
     * @return Value for property 'minimumGasPerLiter'.
     */
    public DoubleParameter getMinimumGasPerLiter() {
        return minimumGasPerLiter;
    }

    /**
     * Setter for property 'minimumGasPerLiter'.
     *
     * @param minimumGasPerLiter Value to set for property 'minimumGasPerLiter'.
     */
    public void setMinimumGasPerLiter(final DoubleParameter minimumGasPerLiter) {
        this.minimumGasPerLiter = minimumGasPerLiter;
    }

    /**
     * Getter for property 'maximumGasPerLiter'.
     *
     * @return Value for property 'maximumGasPerLiter'.
     */
    public DoubleParameter getMaximumGasPerLiter() {
        return maximumGasPerLiter;
    }

    /**
     * Setter for property 'maximumGasPerLiter'.
     *
     * @param maximumGasPerLiter Value to set for property 'maximumGasPerLiter'.
     */
    public void setMaximumGasPerLiter(final DoubleParameter maximumGasPerLiter) {
        this.maximumGasPerLiter = maximumGasPerLiter;
    }

    /**
     * Getter for property 'shockSize'.
     *
     * @return Value for property 'shockSize'.
     */
    public DoubleParameter getShockSize() {
        return shockSize;
    }

    /**
     * Setter for property 'shockSize'.
     *
     * @param shockSize Value to set for property 'shockSize'.
     */
    public void setShockSize(final DoubleParameter shockSize) {
        this.shockSize = shockSize;
    }
}
