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

/**
 * Drawing mpas over the display2D: useful for a little bit of interaction. The basic drawing of the rectangle I adapted from:
 * http://stackoverflow.com/questions/1115359/how-to-draw-a-rectangle-on-a-java-applet-using-mouse-drag-event-and-make-it-stay
 * but the real trick is always to convert pixels to model coordinates and then to geographical coordinates.
 * Created by carrknight on 7/20/15.
 */
public class MPADrawer implements MouseListener, MouseMotionListener
{



    private boolean isNewRectangle = false;
    private boolean hasDragged = false;
    private int startX = 0; private int startY;
    private int endX = 0; private int endY;


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

        JPanel selectionPanel = new JPanel(){
            @Override
            public void paint(Graphics g) {
                if(hasDragged)
                    g.drawRect(
                            Math.min(startX, endX),
                            Math.min(startY, endY),
                            Math.abs(startX - endX), Math.abs(startY - endY));
            }
        };
        selectionPanel.setOpaque(true);
        selectionPanel.setSize(fishDisplay.getSize());
        fishDisplay.add(selectionPanel,0);


    }


    public void attach(){
        //fishDisplay.getMouseListeners();
        //  final MouseListener[] mouseListeners = fishDisplay.getMouseListeners();
        //   for(MouseListener listener : mouseListeners)

        fishDisplay.insideDisplay.addMouseListener(this);
        fishDisplay.insideDisplay.addMouseMotionListener(this);


    }

    public void detach()
    {
        fishDisplay.insideDisplay.removeMouseListener(this);
        fishDisplay.insideDisplay.removeMouseMotionListener(this);
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
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e
     */
    @Override
    public void mousePressed(MouseEvent e) {
        isNewRectangle = true;
        hasDragged=false;
        startX = e.getX(); //do I need to add offset?
        startY = e.getY(); //do I need to add offset?
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
            double lowerLeftX = Math.min(startPoint.getX(), endPoint.getX());
            double lowerLeftY = Math.min(startPoint.getY(), endPoint.getY());

            geometryFactory.setBase(new Coordinate(lowerLeftX, lowerLeftY));
            geometryFactory.setHeight(Math.abs(startPoint.getY() - endPoint.getY()));
            geometryFactory.setWidth(Math.abs(startPoint.getX() - endPoint.getX()));

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
        endX = e.getX();
        endY = e.getY();
        hasDragged=true;
        isNewRectangle = true;
        fishDisplay.repaint();

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
        endX = e.getX();
        endY = e.getY();

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
