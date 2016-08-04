package uk.ac.ox.oxfish.gui;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.NearestNeighborRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.gui.drawing.ColorEncoding;
import uk.ac.ox.oxfish.gui.drawing.ColorfulGrid;
import uk.ac.ox.oxfish.gui.drawing.ColorfulGridSwitcher;
import uk.ac.ox.oxfish.gui.drawing.CoordinateTransformer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * Created by carrknight on 6/27/16.
 */
public class HeatmapTester extends GUIState
{



    public static final int MIN_DIMENSION = 600;
    public static final Path IMAGES_PATH = Paths.get("inputs", "images");
    private Display2D display2D;
    private JFrame displayFrame;

    private final ColorfulGrid myPortrayal;
    private final ColorfulGrid copy;
    private CoordinateTransformer transformer;

    private GeographicalRegression regression = new NearestNeighborRegression(1, 24*7, 5);
    private MouseListener heatmapClicker;

    /**
     * create a random fishstate with seed = milliseconds since epoch
     */
    public HeatmapTester()
    {

        this(new FishState(System.currentTimeMillis()));

    }

    /**
     * standard constructor, useful mostly for checkpointing
     * @param state checkpointing state
     */
    public HeatmapTester(SimState state)
    {
        super(state);
        myPortrayal = new ColorfulGrid(guirandom);
        copy = new ColorfulGrid(guirandom);
    }


    /**
     * create the right displays
     * @param controller the parent-given controller object
     */
    @Override
    public void init(Controller controller) {

        super.init(controller);

    }




    /**
     * called when play is pressed
     */
    @Override
    public void start() {
        super.start();


        initialize();






    }

    @Override
    public void load(SimState state) {
        super.load(state);
        initialize();
    }

    private void initialize() {
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

        display2D = setupPortrayal(state, myPortrayal);
        setupDisplay2D(state, myPortrayal, display2D, "Main");
        Display2D other = setupPortrayal(state, copy);
        setupDisplay2D(state, copy, other, "Mirror");




        //add heatmapper
        myPortrayal.addEnconding("Heatmap", new ColorEncoding(
                new TriColorMap(-6000, 0, 6000, Color.YELLOW, Color.WHITE, Color.RED),
                new Function<SeaTile, Double>() {
                    @Override
                    public Double apply(SeaTile tile) {
                        return regression.predict(tile, state.getHoursSinceStart(), null,null );
                    }
                },
                false
        ));



        //add heatmapper
        copy.addEnconding("Heatmap", new ColorEncoding(
                new TriColorMap(-6000, 0, 6000, Color.YELLOW, Color.WHITE, Color.RED),
                new Function<SeaTile, Double>() {
                    @Override
                    public Double apply(SeaTile tile) {
                        return regression.predict(tile, state.getHoursSinceStart(), null,null );
                    }
                },
                false
        ));


        transformer = new CoordinateTransformer(display2D, state.getMap());
        heatmapClicker = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Int2D gridPosition = transformer.guiToGridPosition(e.getX(), e.getY());
                System.out.println("converted: " + gridPosition);
                System.out.println(e.getX() + " --- " + e.getY());
                System.out.println("----------------------------------------------");
                Double observation = state.getMap().getSeaTile(gridPosition.getX(),
                                                               gridPosition.getY()).
                        getBiomass(state.getSpecies().get(0));
                regression.addObservation(new GeographicalObservation(state.getMap().getSeaTile(gridPosition.getX(),gridPosition.getY()),
                                                                      state.getHoursSinceStart(),
                                                                      observation),null );

                display2D.repaint();

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
        display2D.insideDisplay.addMouseListener(heatmapClicker);


    }

    private void setupDisplay2D(
            FishState state, final ColorfulGrid portrayal, final Display2D display, final String title) {
        ((JComponent) display.getComponent(0)).add(
                new ColorfulGridSwitcher(portrayal, state.getBiology(), display));
        display.reset();
        display.setBackdrop(Color.WHITE);
        display.repaint();
        //attach it the portrayal
        display.attach(portrayal, "Bathymetry");

        displayFrame = display.createFrame();
        controller.registerFrame(displayFrame);
        displayFrame.setTitle(title);
        displayFrame.setVisible(true);
    }

    private Display2D setupPortrayal(FishState state, final ColorfulGrid portrayal) {
        portrayal.initializeGrid(state.getBiology());

        portrayal.setField(state.getRasterBathymetry().getGrid());
        portrayal.setMap(new TriColorMap(-6000, 0, 6000, Color.BLUE, Color.CYAN, Color.GREEN, Color.RED));

        //now deal with display2d
        //change width and height to keep correct geographical ratio
        double width;
        double height;
        double heightToWidthRatio = ((double) state.getRasterBathymetry().getGridHeight())/state.getRasterBathymetry().getGridWidth();
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


    public static void main(String[] args)
    {

        FishState state = new FishState();
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new FromLeftToRightFactory());
        state.setScenario(scenario);
        scenario.setFishers(0);

        HeatmapTester tester = new HeatmapTester(state);
        Console c = new Console(tester);
        c.setVisible(true);

    }

}
