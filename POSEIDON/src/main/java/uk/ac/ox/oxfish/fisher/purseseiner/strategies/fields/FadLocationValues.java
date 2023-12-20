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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map.Entry;
import java.util.Set;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingDouble;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class FadLocationValues extends ObservableLocationValues {

    private Fisher fisher;

    @Override
    public double getValueAt(final Int2D location) {
        final SeaTile seaTile = fisher.grabState().getMap().getSeaTile(location);
        final FadManager fadManager = getFadManager(fisher);
        final double[] prices = fisher.getHomePort().getMarketMap(fisher).getPrices();
        return fadManager
            .getFadsAt(seaTile)
            .mapToDouble(fad -> fadManager.getFishValueCalculator().valueOf(fad.getBiology(), prices))
            .sum();
    }

    @Override
    public Set<Entry<Int2D, Double>> getValues() {

        final FadManager fadManager = getFadManager(fisher);
        final double[] prices = fisher.getHomePort().getMarketMap(fisher).getPrices();
        final FadMap fadMap = fadManager.getFadMap();

        // noinspection UnstableApiUsage
        return getFadManager(fisher)
            .getDeployedFads()
            .stream()
            .flatMap(fad ->
                stream(fadMap.getFadTile(fad))
                    .map(tile -> entry(
                        new Int2D(tile.getGridX(), tile.getGridY()),
                        fadManager.getFishValueCalculator().valueOf(fad.getBiology(), prices)
                    ))
            )
            .collect(groupingBy(Entry::getKey, summingDouble(Entry::getValue)))
            .entrySet();
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {
        this.fisher = fisher;
    }

}
