/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.EffortStatus;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;

import java.util.Map;

import static uk.ac.ox.oxfish.fisher.strategies.departing.FullSeasonalRetiredDecorator.SEASONALITY_VARIABLE_NAME;

/**
 * creates a bunch of data time series for counting full-time, seasonal and plain inactive/retired agents
 */
public class FullSeasonalRetiredDataCollectors implements AdditionalStartable {


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {

        if (model.getYearlyDataSet().getColumn("Full-time fishers") != null)
            return;


        model.getYearlyDataSet().registerGatherer(
            "Full-time fishers",
            (Gatherer<FishState>) state -> {
                double sum = 0;
                for (final Fisher fisher : state.getFishers()) {

                    final Object status = fisher.getAdditionalVariables().get(SEASONALITY_VARIABLE_NAME);
                    //either you are "full time" or you don't have a seasonal/nonseasonal
                    // variable at which point you are full time as long as you go on at least a trip
                    if (status == EffortStatus.FULLTIME || (status == null &&
                        fisher.hasBeenActiveThisYear()))
                        sum++;

                }
                return sum;

            },
            Double.NaN
        );

        model.getYearlyDataSet().registerGatherer(
            "Seasonal fishers",
            (Gatherer<FishState>) state -> {
                double sum = 0;
                for (final Fisher fisher : state.getFishers()) {

                    final Object status = fisher.getAdditionalVariables().get(SEASONALITY_VARIABLE_NAME);
                    if (status == EffortStatus.SEASONAL)
                        sum++;

                }
                return sum;

            },
            Double.NaN
        );

        model.getYearlyDataSet().registerGatherer(
            "Retired fishers",
            (Gatherer<FishState>) state -> {
                double sum = 0;
                for (final Fisher fisher : state.getFishers()) {

                    final Object status = fisher.getAdditionalVariables().get(SEASONALITY_VARIABLE_NAME);
                    //either you are "full time" or you don't have a seasonal/nonseasonal
                    // variable at which point you are full time as long as you go on at least a trip
                    if (status == EffortStatus.RETIRED || (status == null &&
                        fisher.hasBeenActiveThisYear()))
                        sum++;

                }
                return sum;

            },
            Double.NaN
        );


        for (final Map.Entry<String, FisherFactory> factory : model.getFisherFactories()) {
            model.getYearlyDataSet().registerGatherer(
                "Full-time fishers of " + factory.getKey(),
                (Gatherer<FishState>) state -> {
                    double sum = 0;
                    for (final Fisher fisher : state.getFishers()) {
                        if (!fisher.getTagsList().contains(factory.getKey()))
                            continue;
                        final Object status = fisher.getAdditionalVariables().get(SEASONALITY_VARIABLE_NAME);
                        //either you are "full time" or you don't have a seasonal/nonseasonal
                        // variable at which point you are full time as long as you go on at least a trip
                        if (status == EffortStatus.FULLTIME || (status == null &&
                            fisher.hasBeenActiveThisYear()))
                            sum++;

                    }
                    return sum;

                },
                Double.NaN
            );

            model.getYearlyDataSet().registerGatherer(
                "Seasonal fishers of " + factory.getKey(),
                (Gatherer<FishState>) state -> {
                    double sum = 0;
                    for (final Fisher fisher : state.getFishers()) {
                        if (!fisher.getTagsList().contains(factory.getKey()))
                            continue;
                        final Object status = fisher.getAdditionalVariables().get(SEASONALITY_VARIABLE_NAME);
                        if (status == EffortStatus.SEASONAL)
                            sum++;

                    }
                    return sum;

                },
                Double.NaN
            );

            model.getYearlyDataSet().registerGatherer(
                "Retired fishers of " + factory.getKey(),
                (Gatherer<FishState>) state -> {
                    double sum = 0;
                    for (final Fisher fisher : state.getFishers()) {
                        if (!fisher.getTagsList().contains(factory.getKey()))
                            continue;
                        final Object status = fisher.getAdditionalVariables().get(SEASONALITY_VARIABLE_NAME);
                        //either you are "full time" or you don't have a seasonal/nonseasonal
                        // variable at which point you are full time as long as you go on at least a trip
                        if (status == EffortStatus.RETIRED || (status == null &&
                            !fisher.hasBeenActiveThisYear()))
                            sum++;

                    }
                    return sum;

                },
                Double.NaN
            );


        }


    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

    }
}

