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

package uk.ac.ox.oxfish.model.event;

import com.google.common.base.Preconditions;
import org.jfree.util.Log;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by carrknight on 5/25/17.
 */
public abstract class AbstractExogenousCatches implements ExogenousCatches {

    protected final LinkedHashMap<Species,Double> exogenousYearlyCatchesInKg;
    private final LinkedHashMap<Species,Double> lastExogenousCatches = new LinkedHashMap<>();
    private final String columnName;
    private Stoppable stoppable;

    private final int MAXSTEPS = 100000;

    public AbstractExogenousCatches(
            LinkedHashMap<Species, Double> exogenousYearlyCatchesInKg, final String dataColumnName) {
        this.exogenousYearlyCatchesInKg = exogenousYearlyCatchesInKg;
        columnName = dataColumnName;
    }

    @Override
    public void step(SimState simState) {
        FishState model = (FishState) simState;
        List<SeaTile> allTiles = model.getMap().getAllSeaTilesExcludingLandAsList();

        lastExogenousCatches.clear();


        for (Map.Entry<Species, Double> catches : exogenousYearlyCatchesInKg.entrySet())
        {
            double totalBiomassCaught = 0;
            Double totalToCatch = catches.getValue();
            double toCatch = totalToCatch;
            final Species target = catches.getKey();
            //worry only about tiles that have this fish
            List<SeaTile> tiles =  allTiles.stream().filter(
                    seaTile -> getFishableBiomass(target, seaTile) > FishStateUtilities.EPSILON).collect(Collectors.toList());



            int steps = 0;
            //as long as there is fish to catch and places with fish
            while(steps < MAXSTEPS && toCatch > FishStateUtilities.EPSILON && !tiles.isEmpty() )
            {

                //grab a tile at random
                SeaTile tile = tiles.get(model.getRandom().nextInt(tiles.size()));


                //each tile we pick, grab this much fish out
                double step = Math.min(totalToCatch / (double) tiles.size(),toCatch);
                Catch fish = mortalityEvent(model, target, tile, step);

                //should only fish one species!
                double biomassCaught = fish.getWeightCaught(target);
                assert biomassCaught ==fish.totalCatchWeight();
                //account for it
                toCatch-= biomassCaught;
                totalBiomassCaught += biomassCaught;
                if(biomassCaught <= FishStateUtilities.EPSILON //if there is too little fish to catch (it's all rounding errors)
                || getFishableBiomass(target, tile) <= FishStateUtilities.EPSILON) //or you consumed it all
                    tiles.remove(tile); //then don't worry about this tile anymore!


                steps++;
            }

            if(steps==MAXSTEPS)
                Log.warn("Failed to fish enough in the exogenous phase");

            lastExogenousCatches.put(target,totalBiomassCaught);





        }

    }

    protected Double getFishableBiomass(Species target, SeaTile seaTile) {
        return seaTile.getBiomass( target);
    }

    /**
     * simulate exogenous catch (must call the react to catch function within this)
     * @param simState the model
     * @param target species to kill
     * @param tile where to kill it
     * @param step how much at most to kill
     * @return
     */
    abstract protected Catch mortalityEvent(FishState simState, Species target, SeaTile tile, double step);



    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

        //schedule yourself at the end of the year!
        model.scheduleOnceInXDays(new Steppable() {
            @Override
            public void step(SimState simState) {
                AbstractExogenousCatches.this.step(model);
                stoppable = model.scheduleEveryYear(AbstractExogenousCatches.this,
                                                    StepOrder.BIOLOGY_PHASE);
            }
        },StepOrder.BIOLOGY_PHASE,364);


        for(Species species : exogenousYearlyCatchesInKg.keySet())
        {
            model.getYearlyDataSet().registerGatherer(
                    columnName + species,
                    new Gatherer<FishState>() {
                        @Override
                        public Double apply(FishState state) {
                            return lastExogenousCatches.get(species);
                        }
                    },
                    0
            );
        }
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

        if(stoppable!= null)
            stoppable.stop();
    }


    public void updateExogenousCatches(Species species,Double targetYearlyLandings ){
        Preconditions.checkArgument(exogenousYearlyCatchesInKg.containsKey(species));
        exogenousYearlyCatchesInKg.put(species,targetYearlyLandings);
    }
}
