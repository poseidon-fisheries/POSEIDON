package uk.ac.ox.oxfish.geography.mapmakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class TwoSidedMapFactory implements AlgorithmFactory<TwoSidedMap> {


    private DoubleParameter width = new FixedDoubleParameter(20);

    private DoubleParameter height = new FixedDoubleParameter(20);

    private DoubleParameter cellSizeInKilometers = new FixedDoubleParameter(40);


    public DoubleParameter getWidth() {
        return width;
    }

    public DoubleParameter getHeight() {
        return height;
    }

    public DoubleParameter getCellSizeInKilometers() {
        return cellSizeInKilometers;
    }


    @Override
    public TwoSidedMap apply(FishState fishState) {
        return new TwoSidedMap(
                getWidth().apply(fishState.getRandom()).intValue(),
                getHeight().apply(fishState.getRandom()).intValue(),
                getCellSizeInKilometers().apply(fishState.getRandom())
        );
    }

    public void setWidth(DoubleParameter width) {
        this.width = width;
    }

    public void setHeight(DoubleParameter height) {
        this.height = height;
    }

    public void setCellSizeInKilometers(DoubleParameter cellSizeInKilometers) {
        this.cellSizeInKilometers = cellSizeInKilometers;
    }
}
