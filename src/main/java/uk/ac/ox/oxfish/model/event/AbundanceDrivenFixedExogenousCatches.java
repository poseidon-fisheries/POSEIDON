package uk.ac.ox.oxfish.model.event;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.HeterogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HomogeneousGearFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Basically you are given a number of fish to kill each year and you do that
 * on the "abundance" side of catches
 * Created by carrknight on 3/23/17.
 */
public class AbundanceDrivenFixedExogenousCatches implements ExogenousCatches
{


    private final Map<Species,Double> exogenousYearlyCatchesInKg;


    private final Map<Species,Double> lastExogenousCatches = new HashMap<>();


    public AbundanceDrivenFixedExogenousCatches(
            Map<Species, Double> exogenousYearlyCatchesInKg) {
        this.exogenousYearlyCatchesInKg = exogenousYearlyCatchesInKg;
    }

    @Override
    public void step(SimState simState) {
        List<SeaTile> allTiles = ((FishState) simState).getMap().getAllSeaTilesExcludingLandAsList();

        lastExogenousCatches.clear();

        for (Map.Entry<Species, Double> catches : exogenousYearlyCatchesInKg.entrySet())
        {
            double totalBiomassCaught = 0;
            Double totalToCatch = catches.getValue();
            double toCatch = totalToCatch;
            final Species target = catches.getKey();
            //worry only about tiles that have this fish
            List<SeaTile> tiles =  allTiles.stream().filter(
                    seaTile -> seaTile.getBiomass( target)> FishStateUtilities.EPSILON).collect(Collectors.toList());

            //as long as there is fish to catch and places with fish
            while(toCatch > FishStateUtilities.EPSILON && !tiles.isEmpty())
            {
                //each tile we pick, grab this much fish out
                double step = Math.min(totalToCatch / (double) tiles.size(),toCatch);
                //grab a tile at random
                SeaTile tile = tiles.get(((FishState) simState).getRandom().nextInt(tiles.size()));

                //take it as a fixed proportion catchability (and never more than it is available anyway)
                assert tile.getBiomass(target) > FishStateUtilities.EPSILON;
                double proportionToCatch = Math.min(1,step/tile.getBiomass(target));
                //simulate the catches as a fixed proportion gear
                HomogeneousAbundanceGear simulatedGear = new HomogeneousAbundanceGear(0,
                                                                                      new FixedProportionFilter(
                                                                                              proportionToCatch));
                //hide it in an heterogeneous abundance gear so that only one species at a time gets aught!
                HeterogeneousAbundanceGear gear = new HeterogeneousAbundanceGear(
                        new Pair<>(target,simulatedGear)
                );
                //catch it
                Catch fish = gear.fish(null, tile, 1, ((FishState) simState).getBiology());
                //should only fish one species!
                double biomassCaught = fish.getPoundsCaught(target);
                assert biomassCaught ==fish.totalCatchWeight();
                //account for it
                toCatch-= biomassCaught;
                totalBiomassCaught += biomassCaught;
                if(biomassCaught == 0 //if there is too little fish to catch (it's all rounding errors)
                || tile.getBiomass(target) <= FishStateUtilities.EPSILON) //or you consumed it all
                    tiles.remove(tile); //then don't worry about this tile anymore!



            }
            lastExogenousCatches.put(target,totalBiomassCaught);





        }

    }

    private Stoppable stoppable;

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
                AbundanceDrivenFixedExogenousCatches.this.step(model);
                stoppable = model.scheduleEveryYear(AbundanceDrivenFixedExogenousCatches.this,
                                        StepOrder.BIOLOGY_PHASE);
            }
        },StepOrder.BIOLOGY_PHASE,364);


        for(Species species : exogenousYearlyCatchesInKg.keySet())
        {
            model.getYearlyDataSet().registerGatherer(
                    "Exogenous catches of " + species,
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
}
