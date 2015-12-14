package uk.ac.ox.oxfish.gui;

import javafx.collections.ListChangeListener;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.ImagePortrayal2D;
import sim.portrayal.simple.TrailedPortrayal2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.gui.controls.PolicyButton;
import uk.ac.ox.oxfish.gui.drawing.ColorfulGrid;
import uk.ac.ox.oxfish.gui.drawing.ColorfulGridSwitcher;
import uk.ac.ox.oxfish.gui.drawing.CoordinateTransformer;
import uk.ac.ox.oxfish.gui.drawing.MPADrawer;
import uk.ac.ox.oxfish.model.FishState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

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
    private final SparseGridPortrayal2D trails = new SparseGridPortrayal2D();

    private final GeomVectorFieldPortrayal cities = new GeomVectorFieldPortrayal(true);

    private static ImageIcon portIcon = new ImageIcon(FishGUI.class.getClassLoader().getResource("images/anchor.png"));
    private static ImageIcon boatIcon = new ImageIcon(FishGUI.class.getClassLoader().getResource("images/boat.png"));


    private final LinkedList<PolicyButton> policyButtons = new LinkedList<>();


    private TrawlingHeatMap heatMap ;


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





    }

    /**
     * called when play is pressed
     */
    @Override
    public void start() {
        super.start();




        final FishGUI self = this;
        FishState state = (FishState) this.state;
        myPortrayal.initializeGrid(state.getBiology());


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


        //boats
        trails.setField(state.getFisherGrid());
        trails.setPortrayalForAll(null);
        boats.setField(state.getFisherGrid());
        boats.setPortrayalForRemainder(null);
        SimplePortrayal2D boatPortrayal = new ImagePortrayal2D(boatIcon) {
            @Override
            public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
                return wrapper == null ? null :
                        new MetaInspector(wrapper.getObject(), self);
            }


        };
        for(Fisher o : state.getFishers()) {
            assignPortrayalToFisher(boatPortrayal, o);
        }
        state.getFishers().addListener(new ListChangeListener<Fisher>() {
            @Override
            public void onChanged(Change<? extends Fisher> c) {
                while (c.next()) {
                    for (Fisher fisher : c.getRemoved()) {
                        boats.setPortrayalForObject(fisher, null);
                        trails.setPortrayalForObject(fisher, null);
                    }
                    if (c.wasAdded())
                        for (Fisher fisher : c.getAddedSubList())
                            assignPortrayalToFisher(boatPortrayal, fisher);

                }
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



        //if possible create buttons to add fishers
        if(state.canCreateMoreFishers()) {
            policyButtons.add(gui -> {
                JButton button = new JButton("Add 1 Fisher");
                button.addActionListener(e -> {
                    scheduleImmediatelyBefore(
                            state1->
                    state.createFisher()
                    );

                });
                return button;
            });
        }
        //create a button to kill fishers, grey it out when there are no more fishers
        policyButtons.add(new PolicyButton() {
            @Override
            public JComponent buildJComponent(FishGUI gui) {
                JButton button = new JButton("Remove Random Fisher");
                if(state.getFishers().size()==0)
                    button.setEnabled(false);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //kill a fisher, but do so in the model thread
                        scheduleImmediatelyBefore(
                                state1->state.killRandomFisher());
                    }
                });
                state.getFishers().addListener(new ListChangeListener<Fisher>() {
                    @Override
                    public void onChanged(Change<? extends Fisher> c) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if(state.getFishers().size()==0)
                                    button.setEnabled(false);
                                else
                                    button.setEnabled(true);
                            }
                        });

                    }
                });
                return button;
            }
        });


        //mpa drawer
        transformer = new CoordinateTransformer(display2D, state.getMap());

        MPADrawer drawer = new MPADrawer(display2D, transformer, state.getMap(),
                                         myPortrayal, this);


        ((Console) controller).getTabPane().add("Policies",new RegulationTab(this,drawer) );
        //drawer.attach();


        heatMap = new TrawlingHeatMap(state.getDailyTrawlsMap(),state, 30);

        //attach it the portrayal
        display2D.attach(myPortrayal,"Bathymetry");
        //    display2D.attach(mpaPortrayal,"MPAs");
        display2D.attach(cities,"Cities");
        display2D.attach(heatMap.getHeatMapPortrayal(), "Fishing Hotspots");
        display2D.attach(trails, "Boat Trails");
        display2D.attach(boats, "Boats");
        display2D.attach(ports, "Ports");
        displayFrame = display2D.createFrame();
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);






    }

    private void assignPortrayalToFisher(SimplePortrayal2D boatPortrayal, Fisher o) {
        TrailedPortrayal2D trailed = new TrailedPortrayal2D
                (this,
                 boatPortrayal,
                 trails,
                 50, Color.BLUE,new Color(0,0,255,0));
        trailed.setOnlyGrowTrailWhenSelected(true);
        trailed.setOnlyShowTrailWhenSelected(false);
        CircledPortrayal2D circled = new CircledPortrayal2D(trailed);
        circled.setOnlyCircleWhenSelected(true);
        boats.setPortrayalForObject(o, circled);
        trails.setPortrayalForObject(o,trailed);
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


    public LinkedList<PolicyButton> getPolicyButtons() {
        return policyButtons;
    }
}
