/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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
import uk.ac.ox.oxfish.Main;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.gui.controls.PolicyButton;
import uk.ac.ox.oxfish.gui.drawing.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Map;

/**
 * The GUI of FishState
 * Created by carrknight on 3/29/15.
 */
public class  FishGUI extends GUIState{

    public static final int MIN_DIMENSION = 600;
    public static  Path IMAGES_PATH = Paths.get("inputs", "images");
    static {
        URI path = null;
        try {
            path = FishGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI();
/*
            JOptionPane.showMessageDialog(null,
                    path.toString());
            JOptionPane.showMessageDialog(null,
                    Paths.get(path));
            JOptionPane.showMessageDialog(null,
                    Paths.get(path).getParent());
                    */
            if(Paths.get(path).endsWith(".jar"))
                IMAGES_PATH = Paths.get(path).getParent().resolve("inputs").resolve("images");

//            JOptionPane.showMessageDialog(null,
//                    IMAGES_PATH.toAbsolutePath().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private Display2D display2D;
    private JFrame displayFrame;

    private final ColorfulGrid mainPortrayal;
    private CoordinateTransformer transformer;


    private final GeomVectorFieldPortrayal mpaPortrayal = new GeomVectorFieldPortrayal(false);

    private final SparseGridPortrayal2D ports = new SparseGridPortrayal2D();
    private final SparseGridPortrayal2D boats = new SparseGridPortrayal2D();
    private final SparseGridPortrayal2D trails = new SparseGridPortrayal2D();


    private final ImageIcon portIcon;// = new ImageIcon(FishGUI.class.getClassLoader().getResource("images/anchor.png"));
    private BoatPortrayalFactory boatPortrayalFactory;
    private final LinkedList<PolicyButton> policyButtons = new LinkedList<>();


    private TrawlingHeatMap heatMap ;
    private ListChangeListener<Fisher> fisherListListener;
    private ListChangeListener<Fisher> enableDisableFisherListListener;


    /**
     * create a random fishstate with seed = milliseconds since epoch
     */
    public FishGUI()
    {

        this(new FishState(System.currentTimeMillis()));

    }

    /**
     * standard constructor, useful mostly for checkpointing
     * @param state checkpointing state
     */
    public FishGUI(SimState state)
    {
        super(state);
        mainPortrayal = new ColorfulGrid(guirandom);

        portIcon = new ImageIcon(IMAGES_PATH.resolve("anchor.png").toString());

        try {
            boatPortrayalFactory = new BoatPortrayalFactory(this);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            JOptionPane.showMessageDialog(null,

                    IMAGES_PATH.toAbsolutePath().toString()+"\n"+
                            FishGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath().toString()+"\n"+
                            e.toString() +"\n"+exceptionAsString);
            throw new RuntimeException(e);
        }
    }


    /**
     * create the right displays
     * @param controller the parent-given controller object
     */
    @Override
    public void init(Controller controller) {


        super.init(controller);
        ((Console) controller).setSize(800,600);
        final Box timeBox = (Box) ((Console) controller).getContentPane().getComponents()[0];
        //turn stop button into a proper dispose and restart
        JButton stopButton = (JButton) timeBox.getComponent(2);

        //remove previous functionalities
        for(ActionListener act : stopButton.getActionListeners()) {
            stopButton.removeActionListener(act);
        }
        //the stop button will now close the gui and start a new scenario selector
        stopButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {




                        //in their infinite wisdom, MASON programmers have hard-coded a "system.exit" call
                        //and now we have to do acrobatics just to keep this thing from turning off
                        Console console = (Console) controller;

                        //we should probably pause, just to be safe
                        console.setShouldRepeat(false);


                        for (Object allFrame : console.getAllFrames()) {
                            ((JFrame) allFrame).dispose();
                            
                        }
                        console.dispose();
                        FishGUI.this.quit();
                        FishGUI.this.state.finish();
                        FishGUI.this.state=null;
                        System.gc();
                        try {
                            Main.main(null);
                        } catch (IOException e1) {
                            throw new RuntimeException(e1);
                        }
                    }
                }
        );


    }

    @Override
    public boolean readNewStateFromCheckpoint(
            File file) throws IOException, ClassNotFoundException, OptionalDataException, ClassCastException, Exception
    {
        FishState currentState = FishStateUtilities.readModelFromFile(file);
        if(currentState == null)
            return false;
        else
        {
            this.load(currentState);
            return true;
        }
    }

    /**
     * called when play is pressed
     */
    @Override
    public void start() {

        try {
            super.start();


            initialize();

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            JOptionPane.showMessageDialog(null,
                    e.toString() +"\n"+exceptionAsString);
            throw e;
        }






    }

    @Override
    public void load(SimState state) {
        super.load(state);
        initialize();
    }

    private void initialize() {
        final FishGUI self = this; //for anon classes
        FishState state = (FishState) this.state;


        //the console label is a pain in the ass so we need to really use a wrecking ball to modify the way
        //the label is used
        final Box timeBox = (Box) ((Console) controller).getContentPane().getComponents()[0];
        while(timeBox.getComponents().length>3)
            timeBox.remove(3);

        //add save button
        SaveButton saveButton = new SaveButton(this,timeBox.getComponent(0).getSize());
        timeBox.add(saveButton);



        final JLabel timeLabel = new JLabel("Not Started Yet");
        (timeBox).add(timeLabel);
        scheduleRepeatingImmediatelyAfter(new Steppable() {
            @Override
            public void step(SimState simState) {
                SwingUtilities.invokeLater(() -> timeLabel.setText(state.timeString()));

            }
        });

        display2D = setupPortrayal(mainPortrayal);


        //MPAs portrayal
        mpaPortrayal.setField(state.getMpaVectorField());
        mpaPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK, true));

        //boats
        trails.setField(state.getFisherGrid());
        trails.setPortrayalForAll(null);
        boats.setField(state.getFisherGrid());
        boats.setPortrayalForRemainder(null);


        for(Fisher o : state.getFishers()) {
            assignPortrayalToFisher(boatPortrayalFactory.build(o), o);
        }
        //start listening to the model for changes, but keep track of this because you need to stop listening
        //in the case of savings
        fisherListListener = new ListChangeListener<Fisher>() {
            @Override
            public void onChanged(Change<? extends Fisher> c) {
                while (c.next()) {
                    for (Fisher fisher : c.getRemoved()) {
                        boats.setPortrayalForObject(fisher, null);
                        trails.setPortrayalForObject(fisher, null);
                    }
                    if (c.wasAdded())
                        for (Fisher fisher : c.getAddedSubList())
                            assignPortrayalToFisher(boatPortrayalFactory.build(fisher), fisher);

                }
            }
        };
        state.getFishers().addListener(fisherListListener);


        //ports
        ports.setField(state.getPortGrid());
        ports.setPortrayalForAll(new ImagePortrayal2D(portIcon)        {
            @Override
            public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
                return wrapper == null?null:
                        new MetaInspector(((Port) wrapper.getObject()), self);
            }
        });




        //build aggregate data
        ScrollPane pane = new ScrollPane();
        pane.add(new MetaInspector(new FishStateProxy(state), self));

        ((Console) controller).getTabPane().add("Aggregate Data", pane);


        //if possible create buttons to add fishers
        if(state.canCreateMoreFishers()) {
            for (Map.Entry<String, FisherFactory> factory : state.getFisherFactories()) {
                policyButtons.add(gui -> {
                    JButton button = new JButton("Add Fisher - " + factory.getKey());
                    button.addActionListener(e -> {
                        scheduleImmediatelyBefore(
                                state1->
                                        state.createFisher(factory.getKey())
                        );

                    });
                    return button;
                });
            }

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
                enableDisableFisherListListener = new ListChangeListener<Fisher>() {
                    @Override
                    public void onChanged(Change<? extends Fisher> c) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (state.getFishers().size() == 0)
                                    button.setEnabled(false);
                                else
                                    button.setEnabled(true);
                            }
                        });

                    }
                };
                state.getFishers().addListener(enableDisableFisherListListener);
                return button;
            }
        });

        policyButtons.add(new PolicyButton() {
            @Override
            public JComponent buildJComponent(FishGUI gui) {

                JButton button = new JButton("Print additional outputs to file");
                //     button
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        JFileChooser chooser = new JFileChooser(Paths.get(".").toFile());
                        chooser.setDialogTitle("Choose directory where to output new files");
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        chooser.setAcceptAllFileFilterUsed(false);
                        try {
                            if (chooser.showOpenDialog(gui.displayFrame) == JFileChooser.APPROVE_OPTION) {

                                System.out.println("getCurrentDirectory(): "
                                        + chooser.getSelectedFile());
                                FishStateUtilities.writeAdditionalOutputsToFolder(
                                        chooser.getSelectedFile().toPath(),
                                        (FishState) gui.state
                                );
                            } else {
                                System.out.println("No Selection ");
                            }
                        }
                        catch (IOException io)
                        {
                            System.err.println("Failed to write additional outputs!");
                        }
                    }
                });

                return button;
            }
        });



        //mpa drawer
        transformer = new CoordinateTransformer(display2D, state.getMap());

        MPADrawer drawer = new MPADrawer(display2D, transformer, state.getMap(),
                mainPortrayal, this);


        ((Console) controller).getTabPane().add("Policies",new RegulationTab(this, drawer) );
        //drawer.attach();


        heatMap = new TrawlingHeatMap(state.getDailyTrawlsMap(),state, 30);
        scheduleRepeatingImmediatelyAfter(heatMap);

        displayFrame = setupDisplay2D(mainPortrayal, display2D,
                "Bathymetry", true);
        //attach it the portrayal
        display2D.attach(mainPortrayal, "Bathymetry");
        //    display2D.attach(mpaPortrayal,"MPAs");
        display2D.attach(heatMap.getHeatMapPortrayal(), "Fishing Hotspots");
        display2D.attach(trails, "Boat Trails");
        display2D.attach(boats, "Boats");
        display2D.attach(ports, "Ports");

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
        return "POSEIDON is a generic, simple agent-based model of fisheries. You can read more about it at https://link.springer.com/article/10.1007/s11625-018-0579-9";
    }

    public static String getName()
    {
        return  "Poseidon";
    }


    /**
     * stop listening to the model in order to avoid serialization issues
     */
    public void preCheckPoint(){
        if(fisherListListener != null)
            ((FishState) state).getFishers().removeListener(fisherListListener);
        if(enableDisableFisherListListener != null)
            ((FishState) state).getFishers().removeListener(enableDisableFisherListListener);

    }

    public void postCheckPoint(){
        if(fisherListListener != null)
            ((FishState) state).getFishers().addListener(fisherListListener);
        if(enableDisableFisherListListener != null)
            ((FishState) state).getFishers().addListener(enableDisableFisherListListener);

    }

    public LinkedList<PolicyButton> getPolicyButtons() {
        return policyButtons;
    }

    /**
     * Getter for property 'mainPortrayal'.
     *
     * @return Value for property 'mainPortrayal'.
     */
    public ColorfulGrid getMainPortrayal() {
        return mainPortrayal;
    }




    public Display2D setupPortrayal(final ColorfulGrid portrayal) {
        FishState model = (FishState) state;
        portrayal.initializeGrid(model.getBiology(), model.getMap().getAllSeaTilesExcludingLandAsList());

        portrayal.setField(model.getRasterBathymetry().getGrid());
        portrayal.setMap(new TriColorMap(-6000, 0, 6000, Color.BLUE, Color.CYAN, Color.GREEN, new Color(0,100,0)));

        //now deal with display2d
        //change width and height to keep correct geographical ratio
        double width;
        double height;
        double heightToWidthRatio = ((double) model.getRasterBathymetry().getGridHeight())/model.getRasterBathymetry().getGridWidth();
        if(heightToWidthRatio >= 1)
        {
            width = MIN_DIMENSION;
            height = MIN_DIMENSION * heightToWidthRatio;
        }
        else
        {
            width = MIN_DIMENSION / heightToWidthRatio;
            height = MIN_DIMENSION;
        }
        return new Display2D(width, height, this);
    }

    public JFrame setupDisplay2D(
            final ColorfulGrid portrayal,
            final Display2D display, final String title,
            boolean addColorSwitcher) {
        FishState model = (FishState) state;
        if(addColorSwitcher)
            ((JComponent) display.getComponent(0)).add(
                    new ColorfulGridSwitcher(portrayal, model.getBiology(), display));
        display.reset();
        display.setBackdrop(Color.WHITE);
        display.repaint();
        //attach it the portrayal
        display.attach(portrayal, "Bathymetry");

        displayFrame = display.createFrame();
        controller.registerFrame(displayFrame);
        displayFrame.setTitle(title);

        return displayFrame;
    }


    public void forceRepaint(){
        controller.refresh();
        displayFrame.revalidate();
        displayFrame.repaint();
    }

}
