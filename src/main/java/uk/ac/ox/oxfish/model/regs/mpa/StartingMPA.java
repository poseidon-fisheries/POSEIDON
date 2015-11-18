package uk.ac.ox.oxfish.model.regs.mpa;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.gui.drawing.CoordinateTransformer;

/**
 * A simple MPA rectangle to be constructed
 * Created by carrknight on 11/18/15.
 */
public class StartingMPA {

    private int topLeftX;

    private int topLeftY;

    private int width;

    private int height;

    public StartingMPA() {
    }

    public StartingMPA(int topLeftX, int topLeftY, int width, int height) {
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.width = width;
        this.height = height;
    }

    public void buildMPA(NauticalMap map){

        //create the coordinate transformer (null the gui, no need)
        CoordinateTransformer transformer = new CoordinateTransformer(null,map);

        int lowerLeftX = topLeftX;
        int lowerLeftY = Math.min(topLeftY + height,map.getHeight()-1);
        Point point = transformer.gridToJTSPoint(lowerLeftX, lowerLeftY);
        //correct (JTS transformer gets you the centroid, you want the lower corner)
        double correctedX= point.getX()-transformer.getCellWidthInJTS()/2;
        double correctedY= point.getY()-transformer.getCellHeightInJTS()/2;
        //setup the factory
        GeometricShapeFactory geometryFactory = new GeometricShapeFactory();
        geometryFactory.setBase(point.getCoordinate());
        geometryFactory.setHeight(transformer.getCellHeightInJTS() * height + transformer.getCellHeightInJTS());
        geometryFactory.setWidth(transformer.getCellWidthInJTS() * width + transformer.getCellWidthInJTS() );
        //build it
        final Polygon rectangle = geometryFactory.createRectangle();
        //add to map
        map.getMpaVectorField().addGeometry(new MasonGeometry(rectangle));
        map.recomputeTilesMPA();
    }

    public int getTopLeftX() {
        return topLeftX;
    }

    public void setTopLeftX(int topLeftX) {
        this.topLeftX = topLeftX;
    }

    public int getTopLeftY() {
        return topLeftY;
    }

    public void setTopLeftY(int topLeftY) {
        this.topLeftY = topLeftY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
