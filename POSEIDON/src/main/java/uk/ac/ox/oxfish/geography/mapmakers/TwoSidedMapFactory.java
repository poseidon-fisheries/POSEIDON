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

    public void setWidth(final DoubleParameter width) {
        this.width = width;
    }

    public DoubleParameter getHeight() {
        return height;
    }

    public void setHeight(final DoubleParameter height) {
        this.height = height;
    }

    public DoubleParameter getCellSizeInKilometers() {
        return cellSizeInKilometers;
    }

    public void setCellSizeInKilometers(final DoubleParameter cellSizeInKilometers) {
        this.cellSizeInKilometers = cellSizeInKilometers;
    }

    @Override
    public TwoSidedMap apply(final FishState fishState) {
        return new TwoSidedMap(
            (int) getWidth().applyAsDouble(fishState.getRandom()),
            (int) getHeight().applyAsDouble(fishState.getRandom()),
            getCellSizeInKilometers().applyAsDouble(fishState.getRandom())
        );
    }
}
