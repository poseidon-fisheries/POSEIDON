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

package uk.ac.ox.oxfish.model.data.collectors;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;

import java.util.LinkedList;
import java.util.List;

/**
 * These are expensive daily collectors we need sometimes (usually when running with GUI) but are quite
 * pointless in many headless simulations where we don't really look at daily steps change anyway
 */
public class AdditionalFishStateDailyCollectors implements AdditionalStartable {



    private List<String> gatherersMade = new LinkedList<>();


    private FishState model;

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

        Preconditions.checkArgument(this.model == null, "already started~!");

        this.model = model;

        for(Species species : model.getSpecies())
        {

            String title = "Biomass " + species.getName();
            model.getDailyDataSet().registerGatherer(title,
                                                     new Gatherer<FishState>() {
                                 @Override
                                 public Double apply(FishState state) {
                                     return state.getTotalBiomass(species);

                                 }
                             },
                                                     0d);
            gatherersMade.add(title);
        }



        model.getDailyDataSet().registerGatherer("Average Cash-Flow", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState observed) {
                ObservableList<Fisher> fishers = observed.getFishers();
                if (fishers.size() == 0)
                    return 0d;

                double sum = 0;
                for (Fisher fisher : observed.getFishers()) {
                    sum += fisher.getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_FLOW_COLUMN);

                }

                return sum / (double) fishers.size();
            }
        }, 0d);



        //fishers who are actually out
        model.getDailyDataSet().registerGatherer("Fishers at Sea", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return model.getFishers().stream().mapToDouble(
                        value -> value.getLocation().equals(value.getHomePort().getLocation()) ? 0 : 1).sum();
            }
        }, 0d);


        //number of fishers
        model.getDailyDataSet().registerGatherer("Number of Fishers", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return (double) model.getFishers().size();
            }
        }, 0d);


//        for(Species species : model.getSpecies())
//        {
//            //get all the markets for this species
//            final List<Market> toAggregate = model.getAllMarketsForThisSpecie(species);
//            List<String> allPossibleColumns = FishStateDailyTimeSeries.getAllMarketColumns(toAggregate);
//
//            //now register each
//
//            for(String columnName : allPossibleColumns) {
//                //todo this would fail if some markets have a column and others don't; too lazy to fix right now
//                model.getDailyDataSet().registerGatherer(species + " " + columnName,
//                                 //so "stream" is a trick from Java 8. In this case it just sums up all the data
//                                 new Gatherer<FishState>() {
//                                     @Override
//                                     public Double apply(FishState model) {
//                                         return toAggregate.stream().mapToDouble(
//                                                 value -> value.getData().getLatestObservation(columnName))
//                                                 .sum();
//                                     }
//                                 }, Double.NaN);
//            }
//
//
//
//
//
//        }
    }


    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

        if(this.model == null)
            return;


        for(String gatherer : gatherersMade)
            model.getDailyDataSet().removeGatherer(gatherer);

        model =null;




    }
}
