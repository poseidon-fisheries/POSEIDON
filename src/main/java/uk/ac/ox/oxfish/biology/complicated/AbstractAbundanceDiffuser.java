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

package uk.ac.ox.oxfish.biology.complicated;

import com.beust.jcommander.internal.Lists;
import ec.util.MersenneTwisterFast;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.*;

/**
 * Deals with the basics that are common for all diffusers
 * Created by carrknight on 7/6/17.
 */
public abstract class AbstractAbundanceDiffuser implements AbundanceDiffuser {


    /**
     * how many cells distant can this species move in a day?
     */
    private final int diffusingRange;

    private final boolean rounding;

    /**
     * we store here for each tile its neighbors. This way we ask the map only once
     */
    private final Map<SeaTile, List<SeaTile>> neighbors = new HashMap<>();


    public AbstractAbundanceDiffuser(
            int diffusingRange, boolean rounding) {
        this.diffusingRange = diffusingRange;
        this.rounding = rounding;
    }


    @Override
    public void step(
            Species species,
            Map<SeaTile, AbundanceLocalBiology> biologies,
            FishState model) {

        //turn it into a list and shuffle it
        List<Map.Entry<SeaTile, AbundanceLocalBiology>> locals = Lists.newArrayList(biologies.entrySet());
        Collections.shuffle(locals, new Random(model.getRandom().nextLong()));


        for (Map.Entry<SeaTile, AbundanceLocalBiology> here : locals) {
            neighbors.putIfAbsent(here.getKey(),
                                  getNeighborsWithAbundanceBasedLocalBiology(here.getKey(), model.getMap(),
                                                                             biologies));
            List<SeaTile> potential = neighbors.get(here.getKey());
            if(potential.size()==0)
                continue;
            //shuffle neighbors
            Collections.shuffle(potential);
            for(SeaTile there : potential) {
                assert biologies.containsKey(there);
                AbundanceLocalBiology thereBiology = biologies.get(there);
                StructuredAbundance abundanceHere = here.getValue().getAbundance(species);
                StructuredAbundance abundanceThere = thereBiology.getAbundance(species);
                assert abundanceHere.getSubdivisions() == abundanceThere.getSubdivisions();
                assert abundanceHere.getBins() == abundanceThere.getBins();
                for(int subdivision = 0; subdivision<abundanceHere.getSubdivisions(); subdivision++)
                {
                    //check for difference in abundance between each bin
                    for (int bin = 0; bin < abundanceHere.getBins(); bin++) {
                        //move male
                        double fishHere = abundanceHere.getAbundance(subdivision, bin);
                        double fishThere = abundanceThere.getAbundance(subdivision, bin);
                        if (rounding) {
                            fishHere = (int) fishHere;
                            fishThere = (int) fishThere;
                        }
                        double delta = fishHere -
                                fishThere;
                        //move always get called, regardless of what the delta is!
                        move(species, here.getKey(),
                             abundanceHere, there, abundanceThere, delta, fishHere, fishThere, bin,
                             model.getRandom(),
                             rounding, subdivision, here.getValue(), thereBiology);


                    }
                }
            }

        }



    }


    /**
     * ask implementation how to move. This gets called iff there is a positive delta (that is, there are more fish here than there)
     *  @param species species moving
     * @param here departing point
     * @param abundanceHere departing local biology
     * @param there arriving point
     * @param abundanceThere arriving local biology
     * @param delta number of fish here - number of fish there (always positive or this isn't called)
     * @param fishHere
     * @param fishThere
     * @param bin bin/age studied
     * @param random
     * @param rounding
     * @param subdivision
     * @param biologyHere departing local biology
     * @param biologyThere arriving local biology
     */
    public abstract void move(
            Species species,
            SeaTile here,
            StructuredAbundance abundanceHere,
            SeaTile there,
            StructuredAbundance abundanceThere,
            double delta,
            double fishHere, double fishThere, int bin,
            MersenneTwisterFast random, boolean rounding, int subdivision,
            AbundanceLocalBiology biologyHere,
            AbundanceLocalBiology biologyThere);



    /**
     * get all the neighbors of a given tile that have the right local biology and are above water
     * @param tile the tile we want the neighbors of
     * @param map the map object
     * @param biologies
     * @return a bag with all the neighbors
     */
    private List<SeaTile> getNeighborsWithAbundanceBasedLocalBiology(
            SeaTile tile,
            NauticalMap map,
            Map<SeaTile, AbundanceLocalBiology> biologies)
    {
        final Bag mooreNeighbors = map.getMooreNeighbors(tile, diffusingRange);
        List<SeaTile> toKeep = new LinkedList<>();
        for(Object inBag : mooreNeighbors)
        {
            SeaTile newTile = (SeaTile) inBag;
            if (biologies.containsKey(newTile))
            {
                assert newTile.getAltitude() <= 0;
                assert newTile.getBiology() instanceof AbundanceLocalBiology;
                toKeep.add(newTile);
            }
        }
        return toKeep;
    }
}
