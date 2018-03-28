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

package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * This class supersedes the older SplitInitializer, HalfBycach and WellMixed. It basically assumes that there
 * are going to be two species: species 1 lives inside the box, species 0 lives outside (and possibly inside as well)
 * Created by carrknight on 1/4/16.
 */
public class TwoSpeciesBoxInitializer extends  AbstractBiologyInitializer {

    /**
     * the smallest x that is inside the box
     */
    private int lowestX;

    /**
     * the smallest y that is inside the box
     */
    private int lowestY;

    /**
     * the height of the box
     */
    private int boxHeight;

    /**
     * the width of the box
     */
    private int boxWidth;

    /**
     * is the Species 0 also inside the box or not?
     */
    private final boolean species0InsideTheBox;

    /**
     * max capacity for first species in each box
     */
    private final DoubleParameter firstSpeciesCapacity;

    /**
     * ratio of maxCapacitySecond/maxCapacityFirst
     */
    private final DoubleParameter ratioFirstToSecondSpecies;




    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private final double percentageLimitOnDailyMovement;

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private final double differentialPercentageToMove;

    /**
     * get the list of all the logistic local biologies
     */
    private Map<SeaTile,BiomassLocalBiology> biologies = new HashMap<>();

    private final LogisticGrowerInitializer grower;


    public TwoSpeciesBoxInitializer(
            int lowestX, int lowestY, int boxHeight, int boxWidth, boolean species0InsideTheBox,
            DoubleParameter firstSpeciesCapacity,
            DoubleParameter ratioFirstToSecondSpecies,  double percentageLimitOnDailyMovement,
            double differentialPercentageToMove,
            LogisticGrowerInitializer grower) {
        this.lowestX = lowestX;
        this.lowestY = lowestY;
        this.boxHeight = boxHeight;
        this.boxWidth = boxWidth;
        this.species0InsideTheBox = species0InsideTheBox;
        this.firstSpeciesCapacity = firstSpeciesCapacity;
        this.ratioFirstToSecondSpecies = ratioFirstToSecondSpecies;
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
        this.differentialPercentageToMove = differentialPercentageToMove;
        this.grower = grower;
    }


    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *  @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     * @param map
     */
    @Override
    public LocalBiology generateLocal(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells, NauticalMap map) {

        if(seaTile.getAltitude() > 0)
            return new EmptyLocalBiology();

        //start by assuming both species are in
        double firstSpeciesCapacity = this.firstSpeciesCapacity.apply(random);
        double secondSpeciesRatio = ratioFirstToSecondSpecies.apply(random);
        Preconditions.checkArgument(firstSpeciesCapacity > 0);
        Preconditions.checkArgument(secondSpeciesRatio>=0);
   //     Preconditions.checkArgument(secondSpeciesRatio<=1);
        double secondSpeciesCapacity;
        if(secondSpeciesRatio == 1)
            secondSpeciesCapacity = firstSpeciesCapacity;
        else if(secondSpeciesRatio>1)
        {
            secondSpeciesCapacity = secondSpeciesRatio* firstSpeciesCapacity;

        }
        else
            secondSpeciesCapacity = firstSpeciesCapacity *
                    secondSpeciesRatio/(1-secondSpeciesRatio);

        //and if it's inside the box
        if(isInsideTheBox(seaTile))
        {
            if(!species0InsideTheBox)
                firstSpeciesCapacity = 0;
        }
        else
        {
            secondSpeciesCapacity = 0;
        }


        BiomassLocalBiology toReturn =  new BiomassLocalBiology(
                new Double[]{random.nextDouble() * firstSpeciesCapacity,random.nextDouble() * secondSpeciesCapacity},
                new Double[]{firstSpeciesCapacity,secondSpeciesCapacity});
        biologies.put(seaTile,toReturn);
        return toReturn;
    }

    private boolean isInsideTheBox(SeaTile where)
    {

        Preconditions.checkArgument(boxHeight >= 0, "height of biobox can't be negative");
        Preconditions.checkArgument(boxWidth >= 0, "width of biobox can't be negative");
        int x=where.getGridX();
        int y=where.getGridY();
        if(x >= lowestX &&(boxWidth == Integer.MAX_VALUE || x< lowestX + boxWidth)) {
            if (y >= lowestY && ( boxHeight == Integer.MAX_VALUE ||y < lowestY + boxHeight))
                return true;
        }
        return false;

    }

    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random
     * @param model   the model: it is in the process of being initialized so it should be only used to schedule stuff rather
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model)
    {



        grower.initializeGrower(biologies,model,random);

        BiomassDiffuserContainer diffuser = new BiomassDiffuserContainer(map, random, biology, differentialPercentageToMove, percentageLimitOnDailyMovement);

        model.scheduleEveryDay(diffuser, StepOrder.BIOLOGY_PHASE);

    }

    /**
     * "Species 0" and "Species 1"
     *
     * @return
     */
    @Override
    public String[] getSpeciesNames() {

        return new String[]{"Species 0", "Species 1"};
    }

    public int getLowestX() {
        return lowestX;
    }

    public int getLowestY() {
        return lowestY;
    }

    public int getBoxHeight() {
        return boxHeight;
    }

    public int getBoxWidth() {
        return boxWidth;
    }

    public boolean isSpecies0InsideTheBox() {
        return species0InsideTheBox;
    }

    public DoubleParameter getFirstSpeciesCapacity() {
        return firstSpeciesCapacity;
    }

    public DoubleParameter getRatioFirstToSecondSpecies() {
        return ratioFirstToSecondSpecies;
    }


    public double getPercentageLimitOnDailyMovement() {
        return percentageLimitOnDailyMovement;
    }

    public double getDifferentialPercentageToMove() {
        return differentialPercentageToMove;
    }

    protected void setLowestX(int lowestX) {
        this.lowestX = lowestX;
    }

    protected void setLowestY(int lowestY) {
        this.lowestY = lowestY;
    }

    protected void setBoxHeight(int boxHeight) {
        this.boxHeight = boxHeight;
    }

    protected void setBoxWidth(int boxWidth) {
        this.boxWidth = boxWidth;
    }
}

