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

import org.jetbrains.annotations.Nullable;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.Map;

/**
 * this is a FAD that doesn't "store" fish in itself. It creates local biologies "on the fly" by attracting whatever
 * it finds under the particular cell it is in at that moment. <br>
 * Instead of loading up on fish, what it does is simply increasing its catchability
 *
 * @param <B>
 * @param <F>
 */
public abstract class LastMomentFad<B extends LocalBiology, F extends AbstractFad<B, F>> extends AbstractFad<B, F> {


    //all this stuff really belongs to an attractor object, if I come up with different rules:

    private final int daysItTakesToFillUp;

    private final int daysInWaterBeforeAttraction;


    private final double[] maxCatchabilityPerSpecies;
    private FishState state;

    public LastMomentFad(
        final TripRecord tripDeployed, final int stepDeployed, final Int2D locationDeployed,
        final double fishReleaseProbability, final FadManager<B, F> owner,
        final int daysItTakesToFillUp,
        final int daysInWaterBeforeAttraction,
        final double[] maxCatchabilityPerSpecies,
        final boolean isDud
    ) {
        super(tripDeployed, stepDeployed, locationDeployed, fishReleaseProbability, owner, isDud);
        this.daysItTakesToFillUp = daysItTakesToFillUp;
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        this.maxCatchabilityPerSpecies = maxCatchabilityPerSpecies;
    }

    @Override
    public void aggregateFish(final B seaTileBiology, final GlobalBiology globalBiology, final int currentStep) {
        //ignored
    }

    @Override
    public void releaseFish(final Collection<Species> allSpecies, final LocalBiology seaTileBiology) {
        //nothing to release
    }

    @Override
    public void releaseFish(final Collection<Species> allSpecies) {
        //nothing to release
    }

    @Override
    public void reactToBeingFished(final FishState state, final Fisher fisher, final SeaTile location) {

        //basically everything that is in the biology needs to be turned into a catch and then destroyed
        final B fishUnderTheFad = getBiology();
        final Map.Entry<Catch, B> caught = getCatchMaker().apply(fishUnderTheFad, fishUnderTheFad);
        location.reactToThisAmountOfBiomassBeingFished(caught.getKey(), caught.getKey(), state.getBiology());
    }

    protected abstract CatchMaker<B> getCatchMaker();

    @Override
    public void reactToStep(final FishState fishState) {
        super.reactToStep(fishState);
        this.state = fishState; //hang on to this link if possible
    }

    @Nullable
    protected double[] getCurrentCatchabilityPerSpecies() {

        double multiplier = 0;
        if (this.state != null) //you must have at least step once!
        {
            final int soakTimeInDays = super.soakTimeInDays(state);
            if (soakTimeInDays >= daysInWaterBeforeAttraction) {
                if (soakTimeInDays >= daysInWaterBeforeAttraction + daysItTakesToFillUp)
                    multiplier = 1;
                else
                    multiplier = (soakTimeInDays - daysInWaterBeforeAttraction) / ((double) daysItTakesToFillUp);
            }

        }
        if (multiplier == 0)
            return null;

        final double[] currentCatchabilities = new double[maxCatchabilityPerSpecies.length];
        for (int i = 0; i < currentCatchabilities.length; i++) {
            currentCatchabilities[i] = maxCatchabilityPerSpecies[i] * multiplier;
        }
        return currentCatchabilities;
    }

    @Override
    public void lose() {
        super.lose();
        this.state = null;
    }

    protected FishState getFishState() {
        return state;
    }
}