/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.actions;

import sim.util.Bag;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.Delaying;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.MasonUtils;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

public class FadSearchAction implements Action {

    private final double hoursDelayIfNoFadFound;

    private final double fadSetDurationTime;

    private final double minimumValueOfFad;

    private final double probabilityOfFindingOtherFads;


    public FadSearchAction(
        final double hoursDelayIfNoFadFound,
        final double fadSetDurationTime,
        final double minimumValueOfFad,
        final double probabilityOfFindingOtherFads
    ) {
        this.hoursDelayIfNoFadFound = hoursDelayIfNoFadFound;
        this.fadSetDurationTime = fadSetDurationTime;
        this.minimumValueOfFad = minimumValueOfFad;
        this.probabilityOfFindingOtherFads = probabilityOfFindingOtherFads;
    }

    @Override
    public ActionResult act(
        final FishState model,
        final Fisher fisher,
        final Regulation regulation,
        final double hoursLeft
    ) {
        //get all the fads at this area

        final Bag fadsHere = model.getFadMap().fadsAt(fisher.getLocation());
        //if there is no fads here, spent some hours wasting time
        if (fadsHere.isEmpty())
            if (hoursDelayIfNoFadFound > 0)
                return new ActionResult(new Delaying(hoursDelayIfNoFadFound), hoursLeft);
            else
                return new ActionResult(new Arriving(), hoursLeft);

        final FadManager fadManager = getFadManager(fisher);
        final double[] prices = fisher.getHomePort().getMarketMap(fisher).getPrices();

        //grab a random, non owned fad
        final List<Fad> fadsThatICanSteal = MasonUtils.<Fad>bagToStream(fadsHere)
            .filter(fad -> fad.getOwner().getFisher() != fisher)
            .filter(fad -> model.getRandom().nextBoolean(probabilityOfFindingOtherFads))
            .filter(fad -> fadManager.getFishValueCalculator().valueOf(fad.getBiology(), prices) >= minimumValueOfFad)
            .collect(toList());

        if (!fadsThatICanSteal.isEmpty())
            return new ActionResult(
                new OpportunisticFadSetAction(
                    oneOf(fadsThatICanSteal, model.getRandom()),
                    fisher,
                    fadSetDurationTime
                ),
                hoursLeft
            );
        else if (hoursDelayIfNoFadFound > 0)
            return new ActionResult(
                new Delaying(hoursDelayIfNoFadFound),
                hoursLeft
            );
        else
            return new ActionResult(new Arriving(), hoursLeft);

    }
}
