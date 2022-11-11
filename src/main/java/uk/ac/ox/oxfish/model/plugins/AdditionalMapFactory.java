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

import com.google.common.base.Supplier;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.tuna.AllocationGrids;
import uk.ac.ox.oxfish.biology.tuna.SimpleAllocationGridsSupplier;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Paths;

public class AdditionalMapFactory implements AlgorithmFactory<AdditionalStartable> {



    public String mapVariableName = "Clorophill";
    private String pathToGridFile = "inputs/tests/clorophill.csv";

    private int mapPeriod = 365;

    public AdditionalMapFactory() {
    }

    public AdditionalMapFactory(String mapVariableName, String pathToGridFile) {
        this(mapVariableName, pathToGridFile, 365);
    }

    public AdditionalMapFactory(String mapVariableName, String pathToGridFile, int mapPeriod) {
        this.mapVariableName = mapVariableName;
        this.pathToGridFile = pathToGridFile;
        this.mapPeriod = mapPeriod;
    }

    public AdditionalMapFactory(String pathToClorophillFile) {
        this.pathToGridFile = pathToClorophillFile;
    }

    @Override
    public AdditionalStartable apply(FishState model) {

        return new AdditionalStartable(){

            @Override
            public void start(FishState model) {
                SimpleAllocationGridsSupplier supplier = new SimpleAllocationGridsSupplier(
                        Paths.get(pathToGridFile),
                        model.getMap().getMapExtent(),
                        mapPeriod,
                        false,
                        mapVariableName
                );

                AllocationGrids<String> grids = supplier.get();
                model.getMap().getAdditionalMaps().put(
                        mapVariableName,
                        (Supplier<DoubleGrid2D>) () -> grids.atOrBeforeStep(model.getStep()).get(mapVariableName)
                );


            }

            @Override
            public void turnOff() {
                model.getMap().getAdditionalMaps().remove(mapVariableName);
            }
        };

    }

    public String getPathToGridFile() {
        return pathToGridFile;
    }

    public void setPathToGridFile(String pathToGridFile) {
        this.pathToGridFile = pathToGridFile;
    }

    public int getMapPeriod() {
        return mapPeriod;
    }

    public void setMapPeriod(int mapPeriod) {
        this.mapPeriod = mapPeriod;
    }

    public String getMapVariableName() {
        return mapVariableName;
    }

    public void setMapVariableName(String mapVariableName) {
        this.mapVariableName = mapVariableName;
    }
}
