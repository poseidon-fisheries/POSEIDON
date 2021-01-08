/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.utility.Measures.toHours;

public class SchoolSetAction extends AbstractSetAction {

    SchoolSetAction(
        final Fisher fisher,
        final CatchSampler catchSampler,
        final double setDuration
    ) {
        this(
            fisher,
            makeSchoolBiology(fisher, catchSampler),
            setDuration
        );
    }

    SchoolSetAction(
        final Fisher fisher,
        final VariableBiomassBasedBiology targetBiology,
        final double setDuration
    ) {
        super(
            fisher,
            setDuration,
            targetBiology
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private static VariableBiomassBasedBiology makeSchoolBiology(
        final Fisher fisher,
        final CatchSampler catchSampler
    ) {
        GlobalBiology globalBiology = fisher.grabState().getBiology();
        LocalBiology seaTileBiology = fisher.getLocation().getBiology();

        final double[] availableBiomass =
            range(0, globalBiology.getSize())
                .mapToDouble(i -> seaTileBiology.getBiomass(globalBiology.getSpecie(i)))
                .toArray();

        final double[] biomassCaught =
            catchSampler.next(availableBiomass).toArray();

        return new BiomassLocalBiology(biomassCaught, biomassCaught);
    }

    static double setDuration(final Fisher fisher, Class<? extends AbstractSetAction> actionClass) {
        final PurseSeineGear purseSeineGear = (PurseSeineGear) fisher.getGear();
        final MersenneTwisterFast rng = fisher.grabRandomizer();
        return toHours(purseSeineGear.nextSetDuration(actionClass, rng));
    }

    @Override boolean checkSuccess() {
        // school sets are always successful since we're sampling from an empirical distribution
        // that includes failed sets with zeros for all species.
        return true;
    }

    @Override public void reactToSuccessfulSet(FishState fishState, SeaTile locationOfSet) {
        // Remove the catches from the underlying biology:
        final Catch catchObject = new Catch(getTargetBiology().getCurrentBiomass());
        locationOfSet.reactToThisAmountOfBiomassBeingFished(
            catchObject,
            catchObject,
            fishState.getBiology()
        );
    }

    @Override public void reactToFailedSet(FishState fishState, SeaTile locationOfSet) {
        throw new IllegalStateException("School sets shouldn't 'fail'.");
    }

    @Override void notify(FadManager fadManager) { fadManager.reactTo(this); }

}
