package uk.ac.ox.oxfish.geography.mapmakers;

import uk.ac.ox.oxfish.geography.FarOffPortInformation;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedList;
import java.util.List;

public class MapWithFarOffPortsInitializerFactory implements AlgorithmFactory<MapWithFarOffPortsInitializer>{


    private List<FarOffPortInformation> farOffPorts = new LinkedList<>();
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

    private AlgorithmFactory<? extends MapInitializer> delegate =
        new SimpleMapInitializerFactory();


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
