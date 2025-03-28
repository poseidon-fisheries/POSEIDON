/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.market;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.util.Map;

public class BiomassMarkets extends ForwardingMap<Port, BiomassMarket> {

    private final Map<Port, BiomassMarket> map;

    public BiomassMarkets(final Map<Port, BiomassMarket> map) {
        this.map = ImmutableMap.copyOf(map);
    }

    @Override
    @NonNull
    protected Map<Port, BiomassMarket> delegate() {
        return map;
    }
}
