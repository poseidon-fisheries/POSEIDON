package uk.ac.ox.oxfish.geography.mapmakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by carrknight on 6/30/17.
 */
public class FromFileMapInitializerFactory implements AlgorithmFactory<FromFileMapInitializer> {


    private Path  mapFile =  Paths.get("inputs","indonesia","indonesia_latlong.csv");


    private DoubleParameter gridWidthInCell = new FixedDoubleParameter(100);

    private boolean header = true;

    private boolean latLong = true;

    public FromFileMapInitializerFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FromFileMapInitializer apply(FishState state) {
        return new FromFileMapInitializer(mapFile,
                                          gridWidthInCell.apply(state.getRandom()).intValue(), header, latLong);
    }

    /**
     * Getter for property 'mapFile'.
     *
     * @return Value for property 'mapFile'.
     */
    public Path getMapFile() {
        return mapFile;
    }

    /**
     * Setter for property 'mapFile'.
     *
     * @param mapFile Value to set for property 'mapFile'.
     */
    public void setMapFile(Path mapFile) {
        this.mapFile = mapFile;
    }

    /**
     * Getter for property 'gridWidthInCell'.
     *
     * @return Value for property 'gridWidthInCell'.
     */
    public DoubleParameter getGridWidthInCell() {
        return gridWidthInCell;
    }

    /**
     * Setter for property 'gridWidthInCell'.
     *
     * @param gridWidthInCell Value to set for property 'gridWidthInCell'.
     */
    public void setGridWidthInCell(DoubleParameter gridWidthInCell) {
        this.gridWidthInCell = gridWidthInCell;
    }

    /**
     * Getter for property 'header'.
     *
     * @return Value for property 'header'.
     */
    public boolean isHeader() {
        return header;
    }

    /**
     * Setter for property 'header'.
     *
     * @param header Value to set for property 'header'.
     */
    public void setHeader(boolean header) {
        this.header = header;
    }

    /**
     * Getter for property 'latLong'.
     *
     * @return Value for property 'latLong'.
     */
    public boolean isLatLong() {
        return latLong;
    }

    /**
     * Setter for property 'latLong'.
     *
     * @param latLong Value to set for property 'latLong'.
     */
    public void setLatLong(boolean latLong) {
        this.latLong = latLong;
    }
}
