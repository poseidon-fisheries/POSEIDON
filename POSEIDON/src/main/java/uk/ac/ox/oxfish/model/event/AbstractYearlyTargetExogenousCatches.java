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

package uk.ac.ox.oxfish.model.event;

import com.google.common.base.Preconditions;
import org.jfree.util.Log;
import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by carrknight on 5/25/17.
 */
public abstract class AbstractYearlyTargetExogenousCatches extends AbstractExogenousCatches {

    private static final int MAX_STEPS = 10000;
    private static final long serialVersionUID = -544958090778764508L;
    protected final LinkedHashMap<Species, Double> exogenousYearlyCatchesInKg;

    public AbstractYearlyTargetExogenousCatches(
        final LinkedHashMap<Species, Double> exogenousYearlyCatchesInKg,
        final String dataColumnName
    ) {
        super(dataColumnName);
        this.exogenousYearlyCatchesInKg = exogenousYearlyCatchesInKg;
    }

    @Override
    public void step(final SimState simState) {
        System.out.println("catching exogenously now " + ((FishState) simState).getDay());
        final FishState model = (FishState) simState;
        final List<? extends LocalBiology> allTiles = getAllCatchableBiologies(model);

        lastExogenousCatchesMade.clear();


        for (final Map.Entry<Species, Double> catches : exogenousYearlyCatchesInKg.entrySet()) {
            double totalBiomassCaught = 0;
            final Double totalToCatch = catches.getValue();
            double toCatch = totalToCatch;
            final Species target = catches.getKey();
            //worry only about tiles that have this fish
            final List<? extends LocalBiology> tiles = allTiles.stream()
                .filter(
                    seaTile -> getFishableBiomass(target, seaTile) > FishStateUtilities.EPSILON)
                .collect(Collectors.toList());

            final double biomassBefore = ((FishState) simState).getTotalBiomass(catches.getKey());

            int steps = 0;
            //as long as there is fish to catch and places with fish
            while (steps < MAX_STEPS && toCatch > FishStateUtilities.EPSILON && !tiles.isEmpty()) {

                //grab a tile at random
                final LocalBiology tile = tiles.get(model.getRandom().nextInt(tiles.size()));


                //each tile we pick, grab this much fish out
                final double step = Math.min(totalToCatch / (double) tiles.size(), toCatch);
                final Catch fish = mortalityEvent(model, target, tile, step);

                //should only fish one species!
                final double biomassCaught = fish.getWeightCaught(target);
                assert biomassCaught == fish.totalCatchWeight();
                //account for it
                toCatch -= biomassCaught;
                totalBiomassCaught += biomassCaught;
                if (biomassCaught <= FishStateUtilities.EPSILON //if there is too little fish to catch (it's all rounding errors)
                    || getFishableBiomass(target, tile) <= FishStateUtilities.EPSILON) //or you consumed it all
                    tiles.remove(tile); //then don't worry about this tile anymore!


                steps++;
            }

            if (steps == MAX_STEPS)
                Log.warn("Failed to fish enough in the exogenous phase");

            lastExogenousCatchesMade.put(target, totalBiomassCaught);

            final double biomassAfter = ((FishState) simState).getTotalBiomass(catches.getKey());

        }

    }

    /**
     * simulate exogenous catch (must call the react to catch function within this)
     *
     * @param simState the model
     * @param target   species to kill
     * @param tile     where to kill it
     * @param step     how much at most to kill
     * @return
     */
    abstract protected Catch mortalityEvent(FishState simState, Species target, LocalBiology tile, double step);


    public void updateExogenousCatches(final Species species, final Double targetYearlyLandings) {
        Preconditions.checkArgument(exogenousYearlyCatchesInKg.containsKey(species));
        exogenousYearlyCatchesInKg.put(species, targetYearlyLandings);
    }
}
