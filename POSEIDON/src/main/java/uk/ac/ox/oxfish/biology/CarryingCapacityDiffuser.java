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

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;

/**
 * Like biomass diffuser, but moving carrying capacity, usually in a biased way
 * Created by carrknight on 4/6/17.
 */
public class CarryingCapacityDiffuser implements Steppable, Startable {


    private final double dailyMigration;

    /**
     * this can only ever be +1,-1 or 0; it represents the direction of carrying capacity migration
     */
    private final int biasNorth;

    /**
     * this can only ever be +1,-1 or 0; it represents the direction of carrying capacity migration
     */
    private final int biasWest;


    /**
     * species that is moving
     */
    private final int speciesIndex;
    /**
     * we store here for each tile its neighbors. This way we ask the map only once
     */
    private final Map<SeaTile, List<SeaTile>> neighbors = new HashMap<>();
    private Stoppable stoppable;


    public CarryingCapacityDiffuser(
        double dailyMigration, int biasNorth, int biasWest, int speciesIndex
    ) {
        Preconditions.checkArgument(biasNorth == 0 || biasNorth == 1 || biasNorth == -1);
        Preconditions.checkArgument(biasWest == 0 || biasWest == 1 || biasWest == -1);
        this.dailyMigration = dailyMigration;
        this.biasNorth = biasNorth;
        this.biasWest = biasWest;
        this.speciesIndex = speciesIndex;
    }

    @Override
    public void step(SimState simState) {

        FishState model = (FishState) simState;


        //get all the tiles that are in the sea
        final List<SeaTile> tiles = new ArrayList<>(model.getMap().getAllSeaTilesExcludingLandAsList());
        //shuffle them
        Collections.shuffle(tiles, new Random(model.getRandom().nextLong()));


        Species species = model.getSpecies().get(speciesIndex);
        //go through them
        tileloop:
        for (SeaTile here : tiles) {


            //grab neighbors
            neighbors.putIfAbsent(here, BiomassDiffuserContainer.getUsefulNeighbors(
                here,
                model.getMap()
            ));
            List<SeaTile> neighborList = neighbors.get(here);
            //for each neighbour
            for (SeaTile neighbour : neighborList) {

                for (Direction direction : Direction.getDirection(here, neighbour)) {
                    //if your carrying capacity is 0 do not diffuse
                    if (((VariableBiomassBasedBiology) here.getBiology()).getCarryingCapacity(species) <= FishStateUtilities.EPSILON)
                        continue tileloop;

                    if (direction.getBiasNorth() == biasNorth)
                        migrate(
                            ((BiomassLocalBiology) here.getBiology()),
                            ((BiomassLocalBiology) neighbour.getBiology()),
                            species
                        );
                    if (direction.getBiasWest() == biasWest)
                        migrate(
                            ((BiomassLocalBiology) here.getBiology()),
                            ((BiomassLocalBiology) neighbour.getBiology()),
                            species
                        );

                }

            }

        }
    }

    private void migrate(
        BiomassLocalBiology from, BiomassLocalBiology to,
        Species species
    ) {

        //differential is just a proportion of the carrying apacity or epsilon if there is very little carrying capacity left
        double differential = from.getCarryingCapacity(species) <= FishStateUtilities.EPSILON ? from.getCarryingCapacity(
            species) :
            from.getCarryingCapacity(species) * dailyMigration;
        double biomassMovement = Math.min(from.getBiomass(species), differential);

        //move the fish out
        from.setCurrentBiomass(species, from.getBiomass(species) - biomassMovement);
        //destroy habitat
        from.setCarryingCapacity(species, from.getCarryingCapacity(species) - differential);
        to.setCarryingCapacity(species, to.getCarryingCapacity(species) + differential);
        //move the fish in
        to.setCurrentBiomass(species, to.getBiomass(species) + biomassMovement);


    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        Preconditions.checkArgument(stoppable == null, "already started");
        stoppable = model.scheduleEveryDay(this, StepOrder.BIOLOGY_PHASE);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

        if (stoppable != null)
            stoppable.stop();
    }

    private static enum Direction {

        NORTH,

        EAST,

        SOUTH,

        WEST;

        static {
            NORTH.opposite = SOUTH;
            SOUTH.opposite = NORTH;
            EAST.opposite = WEST;
            WEST.opposite = EAST;

            NORTH.biasNorth = +1;
            SOUTH.biasNorth = -1;
            WEST.biasNorth = 999;
            EAST.biasNorth = 999;

            WEST.biasWest = +1;
            EAST.biasWest = -1;
            NORTH.biasWest = 999;
            SOUTH.biasWest = 999;
        }

        private Direction opposite;
        private int biasNorth;
        private int biasWest;

        public static List<Direction> getDirection(SeaTile from, SeaTile to) {
            List<Direction> directions = new LinkedList<>();
            //directions!
            if (to.getGridX() > from.getGridX())
                directions.add(EAST);
            else if (to.getGridX() < from.getGridX())
                directions.add(WEST);
            //flipped on the Y
            if (to.getGridY() > from.getGridY())
                directions.add(SOUTH);
            else if (to.getGridY() < from.getGridY())
                directions.add(NORTH);

            return directions;
        }

        public Direction getOppositeDirection() {
            return opposite;
        }

        /**
         * Getter for property 'biasNorth'.
         *
         * @return Value for property 'biasNorth'.
         */
        public int getBiasNorth() {
            return biasNorth;
        }

        /**
         * Getter for property 'biasWest'.
         *
         * @return Value for property 'biasWest'.
         */
        public int getBiasWest() {
            return biasWest;
        }
    }
}
