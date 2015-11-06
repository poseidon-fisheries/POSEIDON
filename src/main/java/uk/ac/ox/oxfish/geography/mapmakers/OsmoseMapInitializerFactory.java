package uk.ac.ox.oxfish.geography.mapmakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates the Osmose Map Initializer
 * Created by carrknight on 11/5/15.
 */
public class OsmoseMapInitializerFactory implements AlgorithmFactory<OsmoseMapInitializer>
{
    private DoubleParameter cellSizeInKilometers= new FixedDoubleParameter(10);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public OsmoseMapInitializer apply(FishState fishState) {
        return new OsmoseMapInitializer(cellSizeInKilometers.apply(fishState.getRandom()));
    }

    public DoubleParameter getCellSizeInKilometers() {
        return cellSizeInKilometers;
    }

    public void setCellSizeInKilometers(DoubleParameter cellSizeInKilometers) {
        this.cellSizeInKilometers = cellSizeInKilometers;
    }
}
