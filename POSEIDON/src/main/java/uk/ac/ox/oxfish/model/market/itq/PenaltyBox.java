/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.market.itq;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;

import java.util.HashMap;

/**
 * A utility class that keeps track of who has traded and forbids him to trade for a fixed number of days
 * Created by carrknight on 12/16/15.
 */
public class PenaltyBox implements Steppable {


    private static final long serialVersionUID = -57133641329366083L;
    /**
     * we keep here all the fishers that need to stay out of trading
     */
    private final HashMap<Fisher, Integer> penaltyBox = new HashMap<>();


    /**
     * how many days the trader is not allowed to trade further
     */
    private final int duration;


    public PenaltyBox(final int duration) {
        Preconditions.checkArgument(duration >= 0);
        this.duration = duration;
    }


    /**
     * will put the trader in the penalty box. If the trader is already in, it refreshes its penalty duration
     *
     * @param trader trader to put in the box
     */
    public void registerTrader(final Fisher trader) {
        if (duration > 0)
            penaltyBox.put(trader, duration);
    }


    /**
     * check if the trader is in the penalty box!
     *
     * @param fisher
     * @return
     */
    public boolean has(final Fisher fisher) {
        return penaltyBox.containsKey(fisher);
    }

    /**
     * decrease duration by 1, removing all traders whose remaining duration is 0
     *
     * @param simState
     */
    @Override
    public void step(final SimState simState) {

        if (duration == 0)
            return;

        //List<Fisher> toRemove = new LinkedList<>();
        for (final Fisher fisher : penaltyBox.keySet()) {
            penaltyBox.merge(fisher, 0, (c, one) -> c - 1);
        }
        penaltyBox.entrySet().removeIf(entry -> entry.getValue() <= 0);

    }
}
