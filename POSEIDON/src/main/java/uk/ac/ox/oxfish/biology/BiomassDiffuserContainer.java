/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * An object that spreads biomass around as long as the underlying local biology is Logistic
 * Created by carrknight on 9/29/15.
 */
public class BiomassDiffuserContainer implements Steppable {


    private static final long serialVersionUID = 5603405335493670502L;
    private final NauticalMap map;

    private final MersenneTwisterFast random;

    private final GlobalBiology biology;

    private final Map<Species, BiomassMovementRule> movementRules = new HashMap<>();
    /**
     * we store here for each tile its neighbors. This way we ask the map only once
     */
    private final Map<SeaTile, List<SeaTile>> neighbors = new HashMap<>();

    /**
     * assumes all species move at the same rate!
     *
     * @param map
     * @param random
     * @param biology
     */
    @SuppressWarnings("unchecked")
    public BiomassDiffuserContainer(
        final NauticalMap map, final MersenneTwisterFast random,
        final GlobalBiology biology,
        final Entry<Species, BiomassMovementRule>... movementRules
    ) {
        this.map = map;
        this.random = random;

        this.biology = biology;

        if (movementRules != null)
            for (final Entry<Species, BiomassMovementRule> movementRule : movementRules) {
                if (movementRule.getValue() instanceof NoMovement)
                    continue;
                Preconditions.checkArgument(
                    !this.movementRules.containsKey(movementRule.getKey()),
                    "Already provided movement rule for this species!"
                );
                this.movementRules.put(movementRule.getKey(), movementRule.getValue());
            }

    }


    @Deprecated
    public BiomassDiffuserContainer(
        final NauticalMap map, final MersenneTwisterFast random,
        final GlobalBiology biology,
        final double differentialPercentageToMove,
        final double percentageLimitOnDailyMovement
    ) {
        this.map = map;
        this.random = random;

        this.biology = biology;
        if (differentialPercentageToMove > 0) {
            final SmoothMovementRule smoothMovementRule = new SmoothMovementRule(
                differentialPercentageToMove,
                percentageLimitOnDailyMovement
            );
            for (final Species species : biology.getSpecies()) {

                this.movementRules.put(
                    species,
                    smoothMovementRule
                );
            }
        }
    }

    @Override
    public void step(final SimState simState) {


        if (movementRules.isEmpty())
            return;


        //get all the tiles that are in the sea
        final List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList()
            .stream()
            .filter(tile -> tile.isWater() && tile.getBiology() instanceof BiomassLocalBiology)
            .collect(
                Collectors.toList());
        //shuffle them
        Collections.shuffle(tiles, new Random(random.nextLong()));


        //go through them
        for (final SeaTile tile : tiles) {


            //grab neighbors
            neighbors.putIfAbsent(tile, getUsefulNeighbors(tile, map));
            final List<SeaTile> neighborList = neighbors.get(tile);
            //for each neighbor
            for (final SeaTile neighbor : neighborList) {
                //for each specie
                for (final Entry<Species, BiomassMovementRule> movementRuleEntry : movementRules.entrySet()) {

                    //if here there are more than there
                    final Species species = movementRuleEntry.getKey();
                    final BiomassLocalBiology biologyHere = (BiomassLocalBiology) tile.getBiology();
                    final VariableBiomassBasedBiology biologyThere = (VariableBiomassBasedBiology) neighbor.getBiology();

                    //if your carrying capacity is 0 do not diffuse
                    final double biomassHere = tile.getBiomass(species);
                    final double carryingCapacityHere = biologyHere.getCarryingCapacity(species);
                    if (carryingCapacityHere <= FishStateUtilities.EPSILON)
                        continue;
                    //do not bother if the other carrying capacity is 0
                    final double carryingCapacityThere = biologyThere.getCarryingCapacity(species);
                    final double biomassThere = neighbor.getBiomass(species);
                    if (carryingCapacityThere <= FishStateUtilities.EPSILON)
                        continue;
                    //if they are full, do not diffuse
                    if (carryingCapacityThere - biomassThere <= FishStateUtilities.EPSILON)
                        continue;


                    assert biomassHere >= 0;
                    final double delta = biomassHere - biomassThere;

                    movementRuleEntry.getValue().move(
                        species,
                        tile,
                        biologyHere.getBiomass(species),
                        neighbor,
                        biomassThere,
                        delta,
                        carryingCapacityHere,
                        carryingCapacityThere,
                        biologyHere,
                        biologyThere

                    );


                }
            }

        }


    }


    /**
     * get all the neighbors of a given tile that have the right local biology and are above water
     *
     * @param tile the tile we want the neighbors of
     * @param map  the map object
     * @return a bag with all the neighbors
     */
    public static List<SeaTile> getUsefulNeighbors(final SeaTile tile, final NauticalMap map) {
        final Bag mooreNeighbors = map.getMooreNeighbors(tile, 1);
        final List<SeaTile> toKeep = new LinkedList<>();
        for (final Object inBag : mooreNeighbors) {
            final SeaTile newTile = (SeaTile) inBag;
            if (newTile.isWater() && newTile.getBiology() instanceof BiomassLocalBiology) {
                toKeep.add(newTile);
            }
        }
        return toKeep;
    }

    /**
     * Getter for property 'movementRules'.
     *
     * @return Value for property 'movementRules'.
     */
    public Map<Species, BiomassMovementRule> getMovementRules() {
        return movementRules;
    }
}
