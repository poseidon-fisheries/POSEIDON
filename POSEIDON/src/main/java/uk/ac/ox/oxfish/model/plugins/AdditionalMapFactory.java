/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.biology.tuna.AllocationGrids;
import uk.ac.ox.oxfish.biology.tuna.SimpleAllocationGridsSupplier;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

public class AdditionalMapFactory implements AlgorithmFactory<AdditionalStartable> {
    public String mapVariableName;
    private InputPath gridFile;
    private int mapPeriod = 365;

    public AdditionalMapFactory() {
    }

    public AdditionalMapFactory(
        final String mapVariableName,
        final InputPath gridFile
    ) {
        this(mapVariableName, gridFile, 365);
    }

    public AdditionalMapFactory(
        final String mapVariableName,
        final InputPath gridFile,
        final int mapPeriod
    ) {
        this.mapVariableName = mapVariableName;
        this.gridFile = gridFile;
        this.mapPeriod = mapPeriod;
    }

    public AdditionalMapFactory(final InputPath gridFile) {
        this.gridFile = gridFile;
    }

    @Override
    public AdditionalStartable apply(final FishState model) {
        return new AdditionalStartable() {
            @Override
            public void start(final FishState model) {
                final SimpleAllocationGridsSupplier supplier = new SimpleAllocationGridsSupplier(
                    gridFile.get(),
                    model.getMap().getMapExtent(),
                    mapPeriod,
                    false,
                    mapVariableName
                );

                final AllocationGrids<String> grids = supplier.get();
                model.getMap().getAdditionalMaps().put(
                    mapVariableName,
                    () -> grids.atOrBeforeStep(model.getStep()).get(mapVariableName)
                );
            }

            @Override
            public void turnOff() {
                model.getMap().getAdditionalMaps().remove(mapVariableName);
            }
        };
    }

    public InputPath getGridFile() {
        return gridFile;
    }

    public void setGridFile(final InputPath gridFile) {
        this.gridFile = gridFile;
    }

    public int getMapPeriod() {
        return mapPeriod;
    }

    public void setMapPeriod(final int mapPeriod) {
        this.mapPeriod = mapPeriod;
    }

    public String getMapVariableName() {
        return mapVariableName;
    }

    public void setMapVariableName(final String mapVariableName) {
        this.mapVariableName = mapVariableName;
    }
}
