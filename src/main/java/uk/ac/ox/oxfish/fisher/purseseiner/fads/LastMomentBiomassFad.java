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

import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.BiomassCatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

public class LastMomentBiomassFad extends LastMomentFad<BiomassLocalBiology, LastMomentBiomassFad> {

    private final GlobalBiology globalBiology;


    public LastMomentBiomassFad(
            TripRecord tripDeployed,
            int stepDeployed,
            Int2D locationDeployed,
            double fishReleaseProbability,
            FadManager<BiomassLocalBiology,
                    LastMomentBiomassFad> owner,
            int daysItTakesToFillUp,
            int daysInWaterBeforeAttraction,
            double[] maxCatchabilityPerSpecies,
            boolean isDud, GlobalBiology globalBiology) {
        super(tripDeployed,
              stepDeployed,
              locationDeployed,
              fishReleaseProbability,
              owner,
              daysItTakesToFillUp,
              daysInWaterBeforeAttraction,
              maxCatchabilityPerSpecies,
              isDud
              );
        this.globalBiology = globalBiology;
    }

    @Override
    public BiomassLocalBiology getBiology() {
        FishState state = super.getFishState();
        if(state == null)
            return new BiomassLocalBiology(new double[globalBiology.getSize()]);
        double[] catchability = getCurrentCatchabilityPerSpecies(state);
        if(catchability==null)
            return new BiomassLocalBiology(new double[globalBiology.getSize()]);

        double[] caught = new double[state.getBiology().getSize()];
        //for each species, same operation
        for (Species species : state.getBiology().getSpecies()) {
            if (catchability[species.getIndex()] > 0)
                caught[species.getIndex()] = FishStateUtilities.catchSpecieGivenCatchability(
                        super.getLocation(),
                        1,
                        species,
                        catchability[species.getIndex()]
                );

        }
        return new BiomassLocalBiology(caught);
    }

    @Override
    protected CatchMaker<BiomassLocalBiology> getCatchMaker() {
        return new BiomassCatchMaker(super.getFishState().getBiology());
    }
}