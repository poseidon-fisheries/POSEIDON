/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.vessels.accounts;

import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.vessels.Fleet;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.function.Function;

/** Factories for a vessel's {@link Account} and for {@link sim.engine.Steppable}s that draw on it. */
public class Factories {

    private Factories() {}

    /**
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for an
     * {@link Account}
     * @see Account
     */
    public static AccountFactory account() {
        return new AccountFactory();
    }

    /**
     * @param fleet         the fleet whose active vessels get charged
     * @param costExtractor computes the amount to subtract from each vessel's account
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link FixedCostCollector}
     * @see FixedCostCollector
     */
    public static <S extends Scope> FixedCostCollectorFactory<S> fixedCostCollector(
        final Factory<? super S, ? extends Fleet> fleet,
        final Factory<? super S, ? extends Function<? super Vessel, ? extends Money>> costExtractor
    ) {
        return new FixedCostCollectorFactory<>(fleet, costExtractor);
    }

}
