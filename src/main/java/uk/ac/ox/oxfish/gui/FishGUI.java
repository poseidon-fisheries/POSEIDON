package uk.ac.ox.oxfish.gui;

import com.google.common.primitives.Ints;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.IntGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.ImagePortrayal2D;
import sim.util.gui.SimpleColorMap;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.gui.drawing.ColorfulGrid;
import uk.ac.ox.oxfish.gui.drawing.ColorfulGridSwitcher;
import uk.ac.ox.oxfish.gui.drawing.CoordinateTransformer;
import uk.ac.ox.oxfish.gui.drawing.MPADrawer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

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

    private final ColorfulGrid myPortrayal;
    private CoordinateTransformer transformer;


    private final GeomVectorFieldPortrayal mpaPortrayal = new GeomVectorFieldPortrayal(false);

    private final SparseGridPortrayal2D ports = new SparseGridPortrayal2D();
    private final SparseGridPortrayal2D boats = new SparseGridPortrayal2D();
    private final FastValueGridPortrayal2D fishingHotspots = new FastValueGridPortrayal2D("Fishing Hotspots");

    private final GeomVectorFieldPortrayal cities = new GeomVectorFieldPortrayal(true);

    private static ImageIcon portIcon = new ImageIcon(FishGUI.class.getClassLoader().getResource("images/anchor.png"));
    private static ImageIcon boatIcon = new ImageIcon(FishGUI.class.getClassLoader().getResource("images/boat.png"));



    /**
     * create a random fishstate with seed = milliseconds since epoch
     */
    public FishGUI()
    {
        super(new FishState(System.currentTimeMillis()));
        myPortrayal = new ColorfulGrid(guirandom);

    }

    /**
     * standard constructor, useful mostly for checkpointing
     * @param state checkpointing state
     */
    public FishGUI(SimState state)
    {
        super(state);
        myPortrayal = new ColorfulGrid(guirandom);

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
        display2D.attach(cities,"Cities");
        display2D.attach(fishingHotspots, "Fishing Hotspots");
        display2D.attach(boats, "Boats");
        display2D.attach(ports, "Ports");
        displayFrame = display2D.createFrame();
        controller.registerFrame(displayFrame);




    }

    /**
     * called when play is pressed
     */
    @Override
    public void start() {
        super.start();



        displayFrame.setVisible(true);

        final FishGUI self = this;
        FishState state = (FishState) this.state;

        //the console label is a pain in the ass so we need to really use a wrecking ball to modify the way
        //the label is used
        final Box timeBox = (Box) ((Console) controller).getContentPane().getComponents()[0];
        while(timeBox.getComponents().length>3)
            timeBox.remove(3);

        final JLabel timeLabel = new JLabel("Not Started Yet");
        (timeBox).add(timeLabel);
        scheduleRepeatingImmediatelyAfter(new Steppable() {
            @Override
            public void step(SimState simState) {
                SwingUtilities.invokeLater(() -> timeLabel.setText(state.timeString()));

            }
        });

        myPortrayal.setField(state.getRasterBathymetry().getGrid());
        myPortrayal.setMap(new TriColorMap(-6000, 0, 6000, Color.BLUE, Color.CYAN, Color.GREEN, Color.RED));
        //MPAs portrayal
        mpaPortrayal.setField(state.getMpaVectorField());
        mpaPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK, true));
        //cities portrayal
        cities.setField(state.getCities());
        cities.setPortrayalForAll(new GeomPortrayal(Color.BLACK, .05, true));
        //fishing hotspots
        state.getMap().guiStart(state);
        fishingHotspots.setField(state.getFishedMap());
        fishingHotspots.setMap(new SimpleColorMap(0, (state.getFishers().size()+1)*10, new Color(0, 0, 0, 0), Color.RED));
        //reset your color map every year
        fishingHotspots.setMap(new SimpleColorMap(0, (state.getFishers().size()+1)*10, new Color(0, 0, 0, 0), Color.RED));

        //boats
        boats.setField(state.getFisherGrid());
        boats.setPortrayalForAll(new ImagePortrayal2D(boatIcon)
        {
            @Override
            public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
                return wrapper == null?null:
                        new MetaInspector(wrapper.getObject(),self);
            }
        });

        //ports
        ports.setField(state.getPortGrid());
        ports.setPortrayalForAll(new ImagePortrayal2D(portIcon)        {
            @Override
            public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
                return wrapper == null?null:
                        new MetaInspector(((Port) wrapper.getObject()),self);
            }
        });

        ((JComponent) display2D.getComponent(0)).add(
                new ColorfulGridSwitcher(myPortrayal,state.getBiology(), display2D));
        display2D.reset();
        display2D.setBackdrop(Color.WHITE);
        display2D.repaint();


        //build aggregate data
        ScrollPane pane = new ScrollPane();
        pane.add(new MetaInspector(new FishStateProxy(state), self));

        ((Console) controller).getTabPane().add("Aggregate Data", pane);




        //mpa drawer
        transformer = new CoordinateTransformer(display2D, state.getMap());

        MPADrawer drawer = new MPADrawer(display2D, transformer, state.getMap(),
                                         myPortrayal, this);


        ((Console) controller).getTabPane().add("Regulations",new RegulationTab(this,drawer) );
        //drawer.attach();




    }


    //one of the most annoying parts of mason: overriding static methods
    public static Object getInfo()
    {
        return "On play the model will read the bathymetry of california and paste the known MPAs on it as little black polygons. \n" +
                "It takes a bit because the bathymetry data is about 30MB. Good news is that most of it is land or deep ocean" +
                "so there will be plenty to cut out";
    }

    public static String getName()
    {
        return  "Proto-Prototype of a Fishery Model";
    }
}
