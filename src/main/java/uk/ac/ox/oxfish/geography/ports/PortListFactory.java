package uk.ac.ox.oxfish.geography.ports;

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;

/**
 * List of ports (actually a map!)
 * Created by carrknight on 3/13/17.
 */
public class PortListFactory implements AlgorithmFactory<PortListInitializer> {

    private LinkedHashMap<String,Coordinate> ports = new LinkedHashMap<>();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PortListInitializer apply(FishState state) {
        return new PortListInitializer(ports);
    }

    /**
     * Getter for property 'ports'.
     *
     * @return Value for property 'ports'.
     */
    public LinkedHashMap<String, Coordinate> getPorts() {
        return ports;
    }

    /**
     * Setter for property 'ports'.
     *
     * @param ports Value to set for property 'ports'.
     */
    public void setPorts(LinkedHashMap<String, Coordinate> ports) {
        this.ports = ports;
    }
}
