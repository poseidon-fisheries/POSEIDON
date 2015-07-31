package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.IndependentLogisticLocalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * The logistic local biologies now daily share their biomass with their poorer neighbors
 * Created by carrknight on 6/22/15.
 */
public class DiffusingLogisticInitializer extends IndependentLogisticInitializer
{

    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private final double percentageLimitOnDailyMovement;

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private final double differentialPercentageToMove;

    /**
     * we store here for each tile its neighbors. This way we ask the map only once
     */
    private final Map<SeaTile,List<SeaTile>> neighbors = new HashMap<>();




    public DiffusingLogisticInitializer(
            DoubleParameter carryingCapacity, DoubleParameter steepness, double percentageLimitOnDailyMovement,
            double differentialPercentageToMove) {
        super(carryingCapacity, steepness);
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
        this.differentialPercentageToMove = differentialPercentageToMove;
    }

    /**
     * Call the independent logistic initializer but add a steppable to call to smooth fish around
     *  @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random  mersenne randomizer
     * @param model
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model) {
        super.processMap(biology, map, random, model);

        //get the all the tiles
        final Bag allSeaTiles = map.getAllSeaTiles();
        for(Object inBag : allSeaTiles)
        {
            final SeaTile tile = (SeaTile) inBag;
            if(tile.getAltitude()<0)
                //every day
                model.scheduleEveryDay(new Steppable() {
                    @Override
                    public void step(SimState simState) {

                        //grab neighbors
                        neighbors.putIfAbsent(tile,getUsefulNeighbors(tile,map));
                        List<SeaTile> neighborList = neighbors.get(tile);
                        //for each neighbor
                        for(SeaTile neighbor : neighborList)
                        {
                            //for each specie
                            for(int i=0; i<biology.getSize(); i++)
                            {
                                //if here there are more than there
                                final Specie specie = biology.getSpecie(i);
                                double differential = tile.getBiomass(specie) - neighbor.getBiomass(specie);
                                if(differential > 0 )
                                {
                                    //share!
                                    double movement =  Math.min(differentialPercentageToMove * differential,
                                                                percentageLimitOnDailyMovement * tile.getBiomass(specie));
                                    IndependentLogisticLocalBiology here = (IndependentLogisticLocalBiology) tile.getBiology();
                                    IndependentLogisticLocalBiology there = (IndependentLogisticLocalBiology) neighbor.getBiology();
                                    here.getCurrentBiomass()[i]-=movement;
                                    assert here.getCurrentBiomass()[i] >= 0;
                                    there.getCurrentBiomass()[i]+=movement;

                                }
                            }
                        }




                    }
                }, StepOrder.DAWN);
        }
    }

    /**
     * get all the neighbors of a given tile that have the right local biology and are above water
     * @param tile the tile we want the neighbors of
     * @param map the map object
     * @return a bag with all the neighbors
     */
    private List<SeaTile> getUsefulNeighbors(SeaTile tile, NauticalMap map)
    {
        final Bag mooreNeighbors = map.getMooreNeighbors(tile, 1);
        List<SeaTile> toKeep = new LinkedList<>();
        for(Object inBag : mooreNeighbors)
        {
            SeaTile newTile = (SeaTile) inBag;
            if (newTile.getAltitude() < 0 && newTile.getBiology() instanceof IndependentLogisticLocalBiology)
            {
                toKeep.add(newTile);
            }
        }
        return toKeep;
    }
}
