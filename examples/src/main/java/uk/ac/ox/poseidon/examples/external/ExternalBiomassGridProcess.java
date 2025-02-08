/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.examples.external;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fishing.BiomassGridServiceGrpc;
import fishing.Fishing;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.fishing.FishingAction;
import uk.ac.ox.poseidon.biology.biomass.BiomassGrid;
import uk.ac.ox.poseidon.core.events.AbstractListener;

import java.util.List;

@SuppressFBWarnings("Se")
public class ExternalBiomassGridProcess
    extends AbstractListener<FishingAction>
    implements Steppable {

    private final BiomassGrid internalBiomassGrid;
    private final BiomassGridServiceGrpc.BiomassGridServiceBlockingStub stub;
    private ImmutableList.Builder<FishingAction> accumulator = new ImmutableList.Builder<>();

    @SuppressFBWarnings("EI2")
    public ExternalBiomassGridProcess(
        final BiomassGrid internalBiomassGrid,
        final BiomassGridServiceGrpc.BiomassGridServiceBlockingStub stub
    ) {
        super(FishingAction.class);
        this.internalBiomassGrid = internalBiomassGrid;
        this.stub = stub;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void step(final SimState simState) {
        // report catches to external service
        stub.applyCatches(
            Fishing.Catches
                .newBuilder()
                .addAllCatches(accumulator.build().stream()
                    .map(fishingAction -> Fishing.Catches.Catch
                        .newBuilder()
                        .setX(
                            internalBiomassGrid
                                .getGridExtent()
                                .toCell(fishingAction.getCoordinate()).x
                        )
                        .setY(
                            internalBiomassGrid
                                .getGridExtent()
                                .toCell(fishingAction.getCoordinate()).y
                        )
                        .setBiomassCaught(
                            fishingAction
                                .getFishCaught()
                                .getTotalBiomass()
                                .asKg()
                        )
                        .build())
                    ::iterator
                )
                .build()
        );
        // reset fishing action accumulator
        accumulator = new ImmutableList.Builder<>();
        // apply growth process and update our internal grid
        final List<Fishing.Grid.Column> cols = stub
            .applyGrowth(Fishing.Void.getDefaultInstance())
            .getColumnsList();
        for (int x = 0; x < cols.size(); x++) {
            final List<Double> col = cols.get(x).getValuesList();
            for (int y = 0; y < col.size(); y++) {
                internalBiomassGrid.setBiomass(new Int2D(x, y), col.get(y));
            }
        }
    }

    @Override
    public void receive(final FishingAction event) {
        accumulator.add(event);
    }

}
