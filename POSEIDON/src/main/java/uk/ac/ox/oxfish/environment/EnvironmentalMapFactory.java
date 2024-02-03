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

package uk.ac.ox.oxfish.environment;

import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;

public class EnvironmentalMapFactory implements AlgorithmFactory<AdditionalStartable> {

    public StringParameter mapVariableName;
    private InputPath gridFile;
    private IntegerParameter mapPeriod;

    public EnvironmentalMapFactory() {
    }

    public EnvironmentalMapFactory(
        final StringParameter mapVariableName,
        final InputPath gridFile
    ) {
        this(mapVariableName, gridFile, new IntegerParameter(365));
    }

    public EnvironmentalMapFactory(
        final StringParameter mapVariableName,
        final InputPath gridFile,
        final IntegerParameter mapPeriod
    ) {
        this.mapVariableName = mapVariableName;
        this.gridFile = gridFile;
        this.mapPeriod = mapPeriod;
    }

    public StringParameter getMapVariableName() {
        return mapVariableName;
    }

    public void setMapVariableName(final StringParameter mapVariableName) {
        this.mapVariableName = mapVariableName;
    }

    public IntegerParameter getMapPeriod() {
        return mapPeriod;
    }

    public void setMapPeriod(final IntegerParameter mapPeriod) {
        this.mapPeriod = mapPeriod;
    }

    @Override
    public AdditionalStartable apply(final FishState model) {

        return new AdditionalStartable() {

            @Override
            public void start(final FishState model) {
                final SimpleGridsSupplier supplier = new SimpleGridsSupplier(
                    gridFile.get(),
                    model.getMap().getMapExtent(),
                    mapPeriod.getValue(),
                    false,
                    mapVariableName.getValue()
                );

                final GenericGrids<String> grids = supplier.get();
                model.getMap().getAdditionalMaps().put(
                    mapVariableName.getValue(),
                    () -> grids.atOrBeforeStep(model.getStep()).get(mapVariableName.getValue())
                );
            }

            @Override
            public void turnOff() {
                model.getMap().getAdditionalMaps().remove(mapVariableName.getValue());
            }
        };

    }

    public InputPath getGridFile() {
        return gridFile;
    }

    public void setGridFile(final InputPath gridFile) {
        this.gridFile = gridFile;
    }

}
