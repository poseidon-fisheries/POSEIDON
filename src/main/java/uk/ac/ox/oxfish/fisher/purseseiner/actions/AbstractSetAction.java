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

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Optional;

import static java.lang.Double.min;
import static java.lang.Math.exp;
import static java.lang.Math.max;
import static java.lang.Math.round;
import static java.util.Arrays.stream;

public abstract class AbstractSetAction extends PurseSeinerAction {

    private final VariableBiomassBasedBiology targetBiology;
    private Catch catchesKept;

    AbstractSetAction(
        final Fisher fisher,
        final double duration,
        final VariableBiomassBasedBiology targetBiology,
        final double exponentialSteepnessCoefficient,
        final boolean permitted
    ) {
        super(fisher, duration, computeValue(fisher, targetBiology, exponentialSteepnessCoefficient), permitted);
        this.targetBiology = targetBiology;
    }

    private static double computeValue(
        final Fisher fisher,
        final VariableBiomassBasedBiology targetBiology,
        final double exponentialSteepnessCoefficient
    ) {
        final Hold hold = fisher.getHold();
        final double capacity = hold.getMaximumLoad() - hold.getTotalWeightOfCatchInHold();
        final double catchableProportion = min(1, capacity / targetBiology.getTotalBiomass());
        final Catch potentialCatch = new Catch(
            stream(targetBiology.getCurrentBiomass())
                .map(biomass -> biomass * catchableProportion)
                .toArray()
        );
        final double valueOfPotentialCatch = new FishValueCalculator(fisher).valueOf(potentialCatch);
        return 1 - exp(exponentialSteepnessCoefficient * -valueOfPotentialCatch);
    }

    public Optional<Catch> getCatchesKept() { return Optional.ofNullable(catchesKept); }

    @Override public ActionResult act(
        final FishState fishState,
        final Fisher fisher,
        final Regulation regulation,
        final double hoursLeft
    ) {
        assert (fisher == getFisher());
        assert (fisher.getLocation() == getLocation());

        final PurseSeineGear purseSeineGear = (PurseSeineGear) fisher.getGear();

        // fisher.fishHere weirdly wants an int duration, so we have to round it
        final int duration = (int) max(1.0, round(getDuration()));

        if (checkSuccess()) {
            final GlobalBiology globalBiology = fishState.getBiology();
            catchesKept = fisher.fishHere(globalBiology, duration, fishState, targetBiology).getSecond();
            fishState.recordFishing(getLocation()); // TODO: make listener
            reactToSuccessfulSet(fishState, getLocation());
        } else {
            reactToFailedSet(fishState, getLocation());
        }
        notify(purseSeineGear.getFadManager());
        return new ActionResult(new Arriving(), hoursLeft - duration);
    }

    abstract boolean checkSuccess();

    abstract void reactToSuccessfulSet(FishState fishState, SeaTile locationOfSet);

    abstract void reactToFailedSet(FishState model, SeaTile locationOfSet);

    abstract void notify(FadManager fadManager);

    public VariableBiomassBasedBiology getTargetBiology() { return targetBiology; }

}
