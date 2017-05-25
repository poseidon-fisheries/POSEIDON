package uk.ac.ox.oxfish.model.event;

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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by carrknight on 5/25/17.
 */
public abstract class AbstractExogenousCatches implements ExogenousCatches {
    protected final Map<Species,Double> exogenousYearlyCatchesInKg;
    private final Map<Species,Double> lastExogenousCatches = new HashMap<>();
    private Stoppable stoppable;

    public AbstractExogenousCatches(
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

                //grab a tile at random
                SeaTile tile = tiles.get(((FishState) simState).getRandom().nextInt(tiles.size()));


                //each tile we pick, grab this much fish out
                double step = Math.min(totalToCatch / (double) tiles.size(),toCatch);
                Catch fish = mortalityEvent((FishState) simState, target, tile, step);


                //should only fish one species!
                double biomassCaught = fish.getWeightCaught(target);
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

    /**
     * simulate exogenous catch
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
