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

package uk.ac.ox.oxfish.geography.mapmakers;

import uk.ac.ox.oxfish.geography.FarOffPortInformation;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedList;
import java.util.List;

public class MapWithFarOffPortsInitializerFactory implements AlgorithmFactory<MapWithFarOffPortsInitializer> {


    private List<FarOffPortInformation> farOffPorts = new LinkedList<>();
    private AlgorithmFactory<? extends MapInitializer> delegate =
        new SimpleMapInitializerFactory();

    {
//        final FarOffPortInformation fakeInfo = new FarOffPortInformation();
//        fakeInfo.setDistanceFromExitInKm(100);
//        fakeInfo.setExitGridX(20);
//        fakeInfo.setExitGridY(20);
//        fakeInfo.setPortName("Ahoy");
//        fakeInfo.setGasPriceAtPort(0);
//        farOffPorts.add(
//                fakeInfo
//
//        );

    }

    @Override
    public MapWithFarOffPortsInitializer apply(FishState state) {
        return new MapWithFarOffPortsInitializer(
            delegate.apply(state),
            farOffPorts
        );


    }

    public List<FarOffPortInformation> getFarOffPorts() {
        return farOffPorts;
    }

    public void setFarOffPorts(List<FarOffPortInformation> farOffPorts) {
        this.farOffPorts = farOffPorts;
    }

    public AlgorithmFactory<? extends MapInitializer> getDelegate() {
        return delegate;
    }

    public void setDelegate(AlgorithmFactory<? extends MapInitializer> delegate) {
        this.delegate = delegate;
    }
}
