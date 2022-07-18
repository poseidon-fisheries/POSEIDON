/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.base.Preconditions;
import sim.util.Bag;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.AbundanceAggregator;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbundanceCatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.*;

/**
 * like the lastmoment fad with abundance, but this fad doesn't sample only its own cell but those around it too.
 * Keeps track of how much it catches in each area and then if fishing does occur, kills off the right amount in each area
 */
public class LastMomentAbundanceFadWithRange extends LastMomentFad<AbundanceLocalBiology, LastMomentAbundanceFadWithRange> {


    private final static AbundanceAggregator AGGREGATOR = new AbundanceAggregator();

    private final int rangeInSeatiles;

    private final Map<Species, NonMutatingArrayFilter> selectivityFilters;

    private final GlobalBiology biology;

    /**
     * here we store the (temporary) mapping linking back the local biology we have extracted (and that could potentially
     * be caught)
     */
    private HashMap<AbundanceLocalBiology,SeaTile> catchPerTile;

    public LastMomentAbundanceFadWithRange(
            TripRecord tripDeployed, int stepDeployed, Int2D locationDeployed,
            double fishReleaseProbability, FadManager<AbundanceLocalBiology, LastMomentAbundanceFadWithRange> owner,
            int daysItTakesToFillUp, int daysInWaterBeforeAttraction, double[] maxCatchabilityPerSpecies,
            boolean isDud, int rangeInSeatiles, Map<Species, NonMutatingArrayFilter> selectivityFilters,
            GlobalBiology biology) {
        super(tripDeployed, stepDeployed, locationDeployed, fishReleaseProbability, owner, daysItTakesToFillUp,
              daysInWaterBeforeAttraction, maxCatchabilityPerSpecies, isDud);
        this.selectivityFilters = selectivityFilters;
        this.biology = biology;

        Preconditions.checkArgument(rangeInSeatiles>=1);
        this.rangeInSeatiles = rangeInSeatiles;
    }

    @Override
    public AbundanceLocalBiology getBiology() {
        //empty local biology
        FishState state = super.getFishState();
        if(state == null) {
            catchPerTile = null;
            return new AbundanceLocalBiology(biology);
        }
        double[] catchability = getCurrentCatchabilityPerSpecies(state);
        if(catchability == null) {
            catchPerTile = null;
            return new AbundanceLocalBiology(biology);
        }

        catchPerTile = new HashMap<>();
        //get all neighbors
        Bag mooreNeighbors = state.getMap().getMooreNeighbors(getLocation(), rangeInSeatiles);
        for (Object mooreNeighbor : mooreNeighbors) {
            LocalBiology neighborBiology = ((SeaTile) mooreNeighbor).getBiology();
            if(AbundanceLocalBiology.class.isAssignableFrom(neighborBiology.getClass()))
            {
                AbundanceLocalBiology catchableBiology = LastMomentAbundanceFad.extractFadBiologyFromLocalBiology(
                        state,
                        catchability,
                        neighborBiology,
                        selectivityFilters
                );
                catchPerTile.put(catchableBiology, (SeaTile) mooreNeighbor);
            }
        }
        //add the origin!
        catchPerTile.put(LastMomentAbundanceFad.extractFadBiologyFromLocalBiology(
                state,
                catchability,
                getLocation().getBiology(),
                selectivityFilters
        ),getLocation());


        return AGGREGATOR.apply(biology,catchPerTile.keySet());

    }


    @Override
    public void reactToBeingFished(FishState state, Fisher fisher, SeaTile location) {

        for (Map.Entry<AbundanceLocalBiology, SeaTile> catches : catchPerTile.entrySet()) {
            AbundanceLocalBiology catchHere = catches.getKey();
            Map.Entry<Catch, AbundanceLocalBiology> caughtHere = getCatchMaker().apply(catchHere,
                                                                                  catchHere);//all of it is caught all the time
            catches.getValue().reactToThisAmountOfBiomassBeingFished(
                    caughtHere.getKey(),caughtHere.getKey(),biology
            );
        }
    }

    @Override
    public void reactToStep(FishState fishState) {
        super.reactToStep(fishState);
        catchPerTile = null;
    }

    @Override
    protected CatchMaker<AbundanceLocalBiology> getCatchMaker() {
        return new AbundanceCatchMaker(biology);
    }
}
