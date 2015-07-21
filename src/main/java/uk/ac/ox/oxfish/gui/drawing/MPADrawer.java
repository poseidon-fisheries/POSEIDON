package uk.ac.ox.oxfish.gui.drawing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.shape.GeometricShapeBuilder;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.grid.FastObjectGridPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.gui.FishGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;

/**
 * Drawing mpas over the display2D: useful for a little bit of interaction. The basic drawing of the rectangle I adapted from:
 * http://stackoverflow.com/questions/1115359/how-to-draw-a-rectangle-on-a-java-applet-using-mouse-drag-event-and-make-it-stay
 * but the real trick is always to convert pixels to model coordinates and then to geographical coordinates.
 * Created by carrknight on 7/20/15.
 */
public class MPADrawer implements MouseListener, MouseMotionListener
{


    private boolean hasDragged = false;
    private int startX = 0; private int startY;
    private int endX = 0; private int endY;
    private boolean attached = false;


    private final Display2D fishDisplay;
    private final CoordinateTransformer transformer;
    private final NauticalMap map;
    private final FastObjectGridPortrayal2D bathymetryPortrayal;
    private final FishGUI scheduler;

    private final GeometricShapeFactory geometryFactory = new GeometricShapeFactory();

    private MouseListener[] listeners;

    public MPADrawer(
            Display2D fishGUI,
            CoordinateTransformer transformer, NauticalMap map,
            FastObjectGridPortrayal2D mapPortrayal, FishGUI scheduler)
    {
        this.fishDisplay = fishGUI;
        this.transformer =transformer;
        this.map = map;
        bathymetryPortrayal = mapPortrayal;
        this.scheduler = scheduler;


    }


    public void attach(){
        //fishDisplay.getMouseListeners();
        //  final MouseListener[] mouseListeners = fishDisplay.getMouseListeners();
        //   for(MouseListener listener : mouseListeners)

        if(!attached)
        {
            attached = true;
            fishDisplay.insideDisplay.addMouseListener(this);
            fishDisplay.insideDisplay.addMouseMotionListener(this);

        }

    }

    public void detach()
    {
        if(attached)
        {
            attached = false;
            fishDisplay.insideDisplay.removeMouseListener(this);
            fishDisplay.insideDisplay.removeMouseMotionListener(this);

        }
    }


    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     *
     * @param e a click of the mouse
     */
    @Override
    public void mouseClicked(MouseEvent e) {


        fishDisplay.repaint();
        final int[] displacements = computeDisplacements();
        System.out.println(Arrays.toString(displacements));
        System.out.println("converted: " + transformer.guiToGridPosition(e.getX(), e.getY()));
        System.out.println("transformed: " +transformer.guiToGridPosition(e.getX() + displacements[0],
                                                         e.getY() + displacements[1]));
        System.out.println(e.getX() + " --- " + e.getY());
        System.out.println("----------------------------------------------");
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e
     */
    @Override
    public void mousePressed(MouseEvent e) {
        hasDragged=false;
        int[] displacements = computeDisplacements();

        startX = e.getX() + displacements[0];
        startY = e.getY() + displacements[1];

        fishDisplay.repaint();
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e
     */
    @Override
    public void mouseReleased(MouseEvent e) {



        if(hasDragged)
        {


            System.out.println("rectangle from : " + transformer.cellHere(startX,startY) );
            System.out.println("rectangle to : " + transformer.cellHere(endX, endY));

            final Point startPoint = transformer.guiToJTSPoint(startX, startY);
            final Point endPoint = transformer.guiToJTSPoint(endX, endY);
            //notice the - cellSize; the fact is that coordinates represent the center of the sea-tile while you want
            //the MPA rectangle to start from the lower left
            double lowerLeftX = Math.min(startPoint.getX(), endPoint.getX()) - transformer.getCellWidthInJTS()/2;
            double lowerLeftY = Math.min(startPoint.getY(), endPoint.getY()) - transformer.getCellHeightInJTS()/2;

            geometryFactory.setBase(new Coordinate(lowerLeftX, lowerLeftY));
            geometryFactory.setHeight(Math.abs(startPoint.getY() - endPoint.getY()) + transformer.getCellWidthInJTS());
            geometryFactory.setWidth(Math.abs(startPoint.getX() - endPoint.getX()) + transformer.getCellHeightInJTS() );

            final Polygon rectangle = geometryFactory.createRectangle();
            System.out.println(rectangle);



            synchronized (scheduler.state.schedule) {
                map.getMpaVectorField().addGeometry(new MasonGeometry(rectangle));
                map.recomputeTilesMPA();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        boolean original = bathymetryPortrayal.isImmutableField();
                        bathymetryPortrayal.setImmutableField(false);
                        fishDisplay.repaint();
                        bathymetryPortrayal.setImmutableField(original);
                    }
                });
            }

            hasDragged =false;

        }
    }


    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p>
     * Due to platform-dependent Drag&amp;Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&amp;Drop operation.
     *
     * @param e
     */
    @Override
    public void mouseDragged(MouseEvent e) {

        //these are basically a way to adapt when the model displayed is smaller than the display size
        //in which case mason centers it weirdly in the middle
        int[] displacements = computeDisplacements();


        endX = e.getX() + displacements[0];
        endY = e.getY() + displacements[1];
        hasDragged=true;
        fishDisplay.repaint();

    }

    /**
     * if the containing inside display is larger than the grid to be displayed then MASON centers it. Unfortunately it
     * doesn't let me see what the values are for the centering so I need to recompute it here at every click
     * @return
     */
    private int[] computeDisplacements() {
        int[] displacements = new int[2];


        displacements[0] = fishDisplay.insideDisplay.width < fishDisplay.insideDisplay.getWidth()/fishDisplay.getScale() ?
                (int) ((fishDisplay.insideDisplay.width - fishDisplay.insideDisplay.getWidth()/fishDisplay.getScale()) / (2/fishDisplay.getScale())) : 0;

        displacements[1] = fishDisplay.insideDisplay.height < fishDisplay.insideDisplay.getHeight()/fishDisplay.getScale() ?
                (int) ((fishDisplay.insideDisplay.height - fishDisplay.insideDisplay.getHeight()/fishDisplay.getScale()) / (2/fishDisplay.getScale())) : 0;


        return displacements;
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e
     */
    @Override
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e
     */
    @Override
    public void mouseExited(MouseEvent e) {
        int[] displacements = computeDisplacements();
        endX = e.getX() + displacements[0];
        endY = e.getY() + displacements[1];

    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     *
     * @param e
     */
    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
