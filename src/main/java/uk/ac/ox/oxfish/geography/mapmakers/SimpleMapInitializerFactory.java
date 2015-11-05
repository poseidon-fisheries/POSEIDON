package uk.ac.ox.oxfish.geography.mapmakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates the Simple Map initializer
 * Created by carrknight on 11/5/15.
 */
public class SimpleMapInitializerFactory implements AlgorithmFactory<SimpleMapInitializer>{

    private DoubleParameter width = new FixedDoubleParameter(50);

    private DoubleParameter height= new FixedDoubleParameter(50);

    private DoubleParameter coastalRoughness= new FixedDoubleParameter(4);

    private DoubleParameter depthSmoothing= new FixedDoubleParameter(1000000);

    private DoubleParameter cellSizeInKilometers= new FixedDoubleParameter(10);


    public SimpleMapInitializerFactory() {
    }


    public SimpleMapInitializerFactory(
            int width, int height,
            int coastalRoughness, int depthSmoothing,
            double cellSizeInKilometers) {
        this.width = new FixedDoubleParameter(width);
        this.height = new FixedDoubleParameter(height);
        this.coastalRoughness = new FixedDoubleParameter(coastalRoughness);
        this.depthSmoothing = new FixedDoubleParameter(depthSmoothing);
        this.cellSizeInKilometers = new FixedDoubleParameter(cellSizeInKilometers);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public SimpleMapInitializer apply(FishState fishState) {
        return new SimpleMapInitializer(
                width.apply(fishState.getRandom()).intValue(),
                height.apply(fishState.getRandom()).intValue(),
                coastalRoughness.apply(fishState.getRandom()).intValue(),
                depthSmoothing.apply(fishState.getRandom()).intValue(),
                cellSizeInKilometers.apply(fishState.getRandom()));

    }


    public DoubleParameter getWidth() {
        return width;
    }

    public void setWidth(DoubleParameter width) {
        this.width = width;
    }

    public DoubleParameter getHeight() {
        return height;
    }

    public void setHeight(DoubleParameter height) {
        this.height = height;
    }

    public DoubleParameter getCoastalRoughness() {
        return coastalRoughness;
    }

    public void setCoastalRoughness(DoubleParameter coastalRoughness) {
        this.coastalRoughness = coastalRoughness;
    }

    public DoubleParameter getDepthSmoothing() {
        return depthSmoothing;
    }

    public void setDepthSmoothing(DoubleParameter depthSmoothing) {
        this.depthSmoothing = depthSmoothing;
    }

    public DoubleParameter getCellSizeInKilometers() {
        return cellSizeInKilometers;
    }

    public void setCellSizeInKilometers(DoubleParameter cellSizeInKilometers) {
        this.cellSizeInKilometers = cellSizeInKilometers;
    }
}
