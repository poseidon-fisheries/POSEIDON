package uk.ac.ox.oxfish.biology;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An object that spreads biomass around as long as the underlying local biology is Logistic
 * Created by carrknight on 9/29/15.
 */
public class BiomassDiffuser  implements Steppable {


    private final NauticalMap map;

    private final MersenneTwisterFast random;


    private final double differentialPercentageToMove;

    private final double percentageLimitOnDailyMovement;

    private final GlobalBiology biology;


    public BiomassDiffuser(
            NauticalMap map, MersenneTwisterFast random,
            GlobalBiology biology, double differentialPercentageToMove,
            double percentageLimitOnDailyMovement) {
        this.map = map;
        this.random = random;
        this.differentialPercentageToMove = differentialPercentageToMove;
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
        this.biology = biology;
    }

    /**
     * we store here for each tile its neighbors. This way we ask the map only once
     */
    private final Map<SeaTile, List<SeaTile>> neighbors = new HashMap<>();

    @Override
    public void step(SimState simState) {

        //get all the tiles that are in the sea
        final List<SeaTile> tiles = map.getAllSeaTilesAsList().stream().filter(tile -> tile.getAltitude()<0).collect(
                Collectors.toList());
        //shuffle them
        Collections.shuffle(tiles, new Random(random.nextLong()));



        //go through them
        for (SeaTile tile : tiles) {


            //grab neighbors
            neighbors.putIfAbsent(tile, getUsefulNeighbors(tile, map));
            List<SeaTile> neighborList = neighbors.get(tile);
            //for each neighbor
            for (SeaTile neighbor : neighborList) {
                //for each specie
                for (int i = 0; i < biology.getSize(); i++) {
                    //if here there are more than there
                    final Specie specie = biology.getSpecie(i);

                    //if your carrying capacity is 0 do not diffuse
                    if(((LogisticLocalBiology) neighbor.getBiology()).getCarryingCapacity(specie)<= FishStateUtilities.EPSILON)
                        continue;
                    //if they are full, do not diffuse
                    if(((LogisticLocalBiology) neighbor.getBiology()).getCarryingCapacity(specie) - neighbor.getBiomass(specie)<= FishStateUtilities.EPSILON)
                        continue;



                    assert tile.getBiomass(specie) >= 0;
                    double differential = tile.getBiomass(specie) - neighbor.getBiomass(specie);
                    //don't transport more than the other is able to accomodate anyway
                    differential = Math.min(differential, ((LogisticLocalBiology) neighbor.getBiology()).getCarryingCapacity(specie)-neighbor.getBiomass(specie));
                    differential = FishStateUtilities.round(differential);
                    if (differential > 0) {
                        //share!
                        double movement = Math.min(differentialPercentageToMove * differential,
                                                   percentageLimitOnDailyMovement * tile.getBiomass(specie));
                        assert movement >= 0 : movement + " --- " + differential + " ------ " + tile.getBiomass(
                                specie) + " ------ " + FishStateUtilities.round(movement);
                        assert tile.getBiomass(specie) >= movement;
                        LogisticLocalBiology here = (LogisticLocalBiology) tile.getBiology();
                        LogisticLocalBiology there = (LogisticLocalBiology) neighbor.getBiology();
                        here.getCurrentBiomass()[i] -= movement;
                        assert here.getCurrentBiomass()[i] >= 0;
                        there.getCurrentBiomass()[i] += movement;

                    }
                }
            }

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
            if (newTile.getAltitude() < 0 && newTile.getBiology() instanceof LogisticLocalBiology)
            {
                toKeep.add(newTile);
            }
        }
        return toKeep;
    }
}
