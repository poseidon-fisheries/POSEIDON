/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.DPL;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class FadDeploymentAction extends PurseSeinerAction implements FadRelatedAction {

    private Fad fad;

    public FadDeploymentAction(final Fisher fisher) {
        this(fisher, fisher.getLocation());
    }

    public FadDeploymentAction(
        final Fisher fisher,
        final SeaTile location
    ) {
        super(
            fisher,
            location,
            5.0 / 60 // see https://github.com/poseidon-fisheries/tuna/issues/6
        );
    }

    @Override
    public ActionResult act(
        final FishState fishState,
        final Fisher fisher,
        final Regulation ignored,
        final double hoursLeft
    ) {
        assert (fisher == getFisher());
        assert (fisher.getLocation() == getLocation());
        final FadManager fadManager = getFadManager(fisher);
        this.fad = fadManager.deployFad(getLocation(), fishState.random);
        setTime(hoursLeft);
        fadManager.reactTo(this);
        return new ActionResult(new Arriving(), Math.max(0, hoursLeft - getDuration()));
    }

    @Override
    public Fad getFad() {
        return fad;
    }

    @Override
    public String getCode() {
        return DPL.name();
    }
}
