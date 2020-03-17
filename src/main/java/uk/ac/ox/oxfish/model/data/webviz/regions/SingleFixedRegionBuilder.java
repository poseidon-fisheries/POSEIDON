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

package uk.ac.ox.oxfish.model.data.webviz.regions;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.utility.MasonUtils;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;

public final class SingleFixedRegionBuilder implements JsonBuilder<Regions> {

    private final int typeId;

    public SingleFixedRegionBuilder(final int typeId) { this.typeId = typeId; }

    private Collection<Integer> seaTileToGridXY(final SeaTile seaTile) {
        return ImmutableList.of(seaTile.getGridX(), seaTile.getGridY());
    }

    @Override public Regions buildJsonObject(final FishState fishState) {
        final NauticalMap map = fishState.getMap();
        final Collection<Collection<Integer>> cells =
            MasonUtils.<SeaTile>bagToStream(map.getAllSeaTiles())
                .filter(seaTile -> seaTile.isWater() || map.isCoastal(seaTile))
                .filter(SeaTile::isProtected)
                .map(this::seaTileToGridXY)
                .collect(toImmutableList());
        final Collection<Period> periods = ImmutableList.of(new Period(0, fishState.getDay()));
        return new Regions(ImmutableList.of(new Region(typeId, periods, cells)));
    }

}
