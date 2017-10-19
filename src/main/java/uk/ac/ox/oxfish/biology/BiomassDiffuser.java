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

package uk.ac.ox.oxfish.biology;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;
import java.util.function.Predicate;
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

    //todo make movement an array rather than this!
    private final int[] speciesThatMove;


    public BiomassDiffuser(
            NauticalMap map, MersenneTwisterFast random,
            GlobalBiology biology, double differentialPercentageToMove,
            double percentageLimitOnDailyMovement) {
        this.map = map;
        this.random = random;
        this.differentialPercentageToMove = differentialPercentageToMove;
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
        this.biology = biology;
        speciesThatMove = new int[biology.getSize()];
        for(int i=0; i<speciesThatMove.length; i++)
            speciesThatMove[i] = i;
    }

    public BiomassDiffuser(
            NauticalMap map, MersenneTwisterFast random,
            GlobalBiology biology, double differentialPercentageToMove,
            double percentageLimitOnDailyMovement,int... speciesThatMove) {
        this.map = map;
        this.random = random;
        this.differentialPercentageToMove = differentialPercentageToMove;
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
        this.biology = biology;
        this.speciesThatMove = speciesThatMove;
    }

    /**
     * we store here for each tile its neighbors. This way we ask the map only once
     */
    private final Map<SeaTile, List<SeaTile>> neighbors = new HashMap<>();

    @Override
    public void step(SimState simState) {

        //get all the tiles that are in the sea
        final List<SeaTile> tiles = map.getAllSeaTilesAsList().stream().filter(new Predicate<SeaTile>() {
            @Override
            public boolean test(SeaTile tile) {
                return tile.getAltitude() < 0;
            }
        }).collect(
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
                for (int i : speciesThatMove) {
                    //if here there are more than there
                    final Species species = biology.getSpecie(i);

                    //if your carrying capacity is 0 do not diffuse
                    if(((BiomassLocalBiology) neighbor.getBiology()).getCarryingCapacity(species)<= FishStateUtilities.EPSILON)
                        continue;
                    //if they are full, do not diffuse
                    if(((BiomassLocalBiology) neighbor.getBiology()).getCarryingCapacity(species) - neighbor.getBiomass(
                            species)<= FishStateUtilities.EPSILON)
                        continue;



                    assert tile.getBiomass(species) >= 0;
                    double differential = tile.getBiomass(species) - neighbor.getBiomass(species);
                    //don't transport more than the other is able to accomodate anyway
                    differential = Math.min(differential, ((BiomassLocalBiology) neighbor.getBiology()).getCarryingCapacity(
                            species)-neighbor.getBiomass(species));
                    differential = FishStateUtilities.round(differential);
                    if (differential > 0) {
                        //share!
                        double movement = Math.min(differentialPercentageToMove * differential,
                                                   percentageLimitOnDailyMovement * tile.getBiomass(species));
                        assert movement >= 0 : movement + " --- " + differential + " ------ " + tile.getBiomass(
                                species) + " ------ " + FishStateUtilities.round(movement);
                        assert tile.getBiomass(species) >= movement;
                        BiomassLocalBiology here = (BiomassLocalBiology) tile.getBiology();
                        BiomassLocalBiology there = (BiomassLocalBiology) neighbor.getBiology();
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
    public static List<SeaTile> getUsefulNeighbors(SeaTile tile, NauticalMap map)
    {
        final Bag mooreNeighbors = map.getMooreNeighbors(tile, 1);
        List<SeaTile> toKeep = new LinkedList<>();
        for(Object inBag : mooreNeighbors)
        {
            SeaTile newTile = (SeaTile) inBag;
            if (newTile.getAltitude() < 0 && newTile.getBiology() instanceof BiomassLocalBiology)
            {
                toKeep.add(newTile);
            }
        }
        return toKeep;
    }
}
