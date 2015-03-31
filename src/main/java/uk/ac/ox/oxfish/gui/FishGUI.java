package uk.ac.ox.oxfish.gui;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.gui.SimpleColorMap;
import uk.ac.ox.oxfish.model.FishState;

import javax.swing.*;
import java.awt.*;

/**
 * The GUI of FishState
 * Created by carrknight on 3/29/15.
 */
public class FishGUI extends GUIState{

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private Display2D display2D;
    private JFrame displayFrame;

    private FastValueGridPortrayal2D myPortrayal = new FastValueGridPortrayal2D();
    private GeomVectorFieldPortrayal mpaPortrayal = new GeomVectorFieldPortrayal();

    /**
     * create a random fishstate with seed = milliseconds since epoch
     */
    public FishGUI()
    {
        super(new FishState(System.currentTimeMillis()));
    }

    /**
     * standard constructor, useful mostly for checkpointing
     * @param state checkpointing state
     */
    public FishGUI(SimState state)
    {
        super(state);
    }


    /**
     * create the right displays
     * @param controller the parent-given controller object
     */
    @Override
    public void init(Controller controller) {
        super.init(controller);
        
        
        //create the display2d
        display2D = new Display2D(WIDTH, HEIGHT,this);
        //attach it the portrayal
        display2D.attach(myPortrayal,"Bathymetry");
        display2D.attach(mpaPortrayal,"MPAs");
        displayFrame = display2D.createFrame();
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
    }

    /**
     * called when play is pressed
     */
    @Override
    public void start() {
        super.start();

        FishState state = (FishState) this.state;

        myPortrayal.setField(state.getRasterBathymetry().getGrid());
        myPortrayal.setMap(new TriColorMap(-6000, 0, 6000, Color.BLUE, Color.CYAN, Color.GREEN, Color.RED));

        mpaPortrayal.setField(state.getMpaVectorField());
        mpaPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK, true));


        display2D.reset();
        display2D.setBackdrop(Color.WHITE);
        display2D.repaint();
    }
}
