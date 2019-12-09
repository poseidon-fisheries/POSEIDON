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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.Moving;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class FadGravityDestinationStrategy implements DestinationStrategy {


    private FadDeploymentDestinationStrategy fadDeploymentDestinationStrategy;

    private final double gravitationalConstraint ;




    public FadGravityDestinationStrategy(
            double gravitationalConstraint, FadDeploymentDestinationStrategy fadDeploymentDestinationStrategy
    ) {
        this.gravitationalConstraint = gravitationalConstraint;
        this.fadDeploymentDestinationStrategy = fadDeploymentDestinationStrategy;
    }

    public FadDeploymentDestinationStrategy getFadDeploymentDestinationStrategy() {
        return fadDeploymentDestinationStrategy;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentDestinationStrategy(FadDeploymentDestinationStrategy fadDeploymentDestinationStrategy) {
        this.fadDeploymentDestinationStrategy = fadDeploymentDestinationStrategy;
    }


    @Override
    public SeaTile chooseDestination(Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction) {

        if (currentAction instanceof Moving) {
            return fisher.getDestination(); // don't change destination while we're moving
        }
        if (fisher.isAtPort()) {
            fadDeploymentDestinationStrategy.resetRoute();
        }


        return fadDeploymentDestinationStrategy.nextDestination(fisher, model)
                .orElseGet(() -> greedyPull(fisher,model));

    }



    private SeaTile greedyPull(Fisher fisher, FishState model)
    {
        SeaTile here = fisher.getLocation();

        HashMap<SeaTile,Double> valueMap = new HashMap<>();



        //get the map (you need to link back FADs to where they are)

        final FadMap fadMap = FadManagerUtils.getFadManager(fisher).getFadMap();

        //grab about 20 deployed fads
        for(int i=0; i<20; i++)
        {
            Optional<Fad> fad = FadManagerUtils.oneOfDeployedFads(fisher);
            //find where they are, see which are is more valuable
            if(fad.isPresent())
            {
                SeaTile there = fadMap.getFadTile(fad.get()).get();
                valueMap.putIfAbsent(
                        there,
                        computeValueOfFad(fisher,
                                          model,
                                          here,
                                          there)
                );
            }
            else{
          //      System.out.println("failed to find a fad!");
            }
        }

//        double probability =
//                Math.min(
//                1 * 1d/(double)FadManagerUtils.getFadManager(fisher).getNumDeployedFads(),
//                1);
//        final FadMap fadMap = FadManagerUtils.getFadManager(fisher).getFadMap();
//        fadMap.allFads().filter(
//                new Predicate<Fad>() {
//                    @Override
//                    public boolean test(Fad fad) {
//                        return model.getRandom().nextDouble()<(probability);
//                    }
//                }
//        ).forEach(
//                new Consumer<Fad>() {
//                    @Override
//                    public void accept(Fad fad) {
//                        SeaTile there = fadMap.getFadTile(fad).get();
//                        valueMap.putIfAbsent(
//                                there,
//                                computeValueOfFad(fisher,
//                                                  model,
//                                                  here,
//                                                  there)
//                        );
//                    }
//                }
//        );




        try {
            return Collections.max(valueMap.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
        }
        catch (NoSuchElementException e){
            return fisher.getHomePort().getLocation();
        }

//        try {
//            SeaTile bestTile = Collections.max(valueMap.entrySet(),
//                                               Comparator.comparingDouble(Map.Entry::getValue)).getKey();
//            int moveX = (int) Math.signum(bestTile.getGridX()-here.getGridX());
//            int moveY = (int) Math.signum(bestTile.getGridY()-here.getGridY());
//            SeaTile next = model.getMap().getSeaTile(here.getGridX() + moveX, here.getGridY());
//            if(!next.isLand())
//                return next;
//            else
//
//                return here;
//        }
//        catch (NoSuchElementException e){
//            return here;
//        }





    }

    private double computeValueOfFad(Fisher fisher, FishState model, SeaTile here, SeaTile newTile) {
        double distance = model.getMap().distance(here, newTile)+1;

        Preconditions.checkArgument(distance>0);
        FadManager fadManager = FadManagerUtils.getFadManager(fisher);
        double biomassValue = FadManagerUtils.fadsAt(fisher, newTile).filter(
                fad -> fad.getOwner() == fadManager
        ).
                mapToDouble(new ToDoubleFunction<Fad>() {
            @Override
            public double applyAsDouble(Fad fad) {

                double[] currentBiomass = fad.getBiology().getCurrentBiomass();
                double sum = 0;
                for (int i = 0; i < currentBiomass.length; i++) {

                    sum += currentBiomass[i];
                }
                return sum;
            }
        }).sum();

        return gravitationalConstraint * biomassValue / Math.pow(distance,2);
    }

}
