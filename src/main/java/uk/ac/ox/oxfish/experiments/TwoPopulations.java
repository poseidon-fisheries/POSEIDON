package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import sim.display.Console;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.ScenarioSelector;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFisherTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.factory.ITQMonoFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.SelectDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedList;
import java.util.function.ToDoubleFunction;

/**
 * Created by carrknight on 2/12/16.
 */
public class TwoPopulations {

    public static FishState itqExample(double oilPrice)
    {

        FishState state = new FishState(System.currentTimeMillis());
        PrototypeScenario scenario = new PrototypeScenario();

        scenario.setGasPricePerLiter(new FixedDoubleParameter(oilPrice));

        scenario.setBiologyInitializer(new FromLeftToRightFactory());
        RandomCatchabilityTrawlFactory gear = new RandomCatchabilityTrawlFactory();
        gear.setGasPerHourFished(new FixedDoubleParameter(0d));
        scenario.setGear(gear);
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setCellSizeInKilometers(new FixedDoubleParameter(2));
        scenario.setMapInitializer(mapInitializer);

        scenario.setHoldSize(new SelectDoubleParameter(new double[]{10,500}));
        EquidegreeBuilder builder = new EquidegreeBuilder();
        builder.setDegree(2);
        //connect people that have the same hold to avoid stupid imitation noise.
        builder.addPredicate((from, to) -> Math.abs(from.getMaximumHold()-to.getMaximumHold()) < 1);
        scenario.setNetworkBuilder(builder);

        ITQMonoFactory regulation = new ITQMonoFactory();
        regulation.setIndividualQuota(new FixedDoubleParameter(2000));
        scenario.setRegulation(regulation);
        scenario.setUsePredictors(true);

        state.setScenario(scenario);

        state.start();
        while(state.getYear()<5)
            state.schedule.step(state);

        return state;
    }

    /*
            FishGUI gui = new FishGUI(state);
        Console c = new Console(gui);
        c.setVisible(true);
     */


    public static void main(String[] args) throws IOException {



        final JDialog scenarioSelection = new JDialog((JFrame)null, true);
        final ScenarioSelector scenarioSelector = new ScenarioSelector();
        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(scenarioSelector,BorderLayout.CENTER);
        //create ok and exit button
        Box buttonBox = new Box( BoxLayout.LINE_AXIS);
        contentPane.add(buttonBox,BorderLayout.SOUTH);
        final JButton ok = new JButton("OK");
        ok.addActionListener(e -> scenarioSelection.dispatchEvent(new WindowEvent(
                scenarioSelection,WindowEvent.WINDOW_CLOSING
        )));
        buttonBox.add(ok);
        final JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> System.exit(0));
        buttonBox.add(cancel);


        //create file opener (for YAML)
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        Log.info("current directory: " + Paths.get(".").toFile());
        chooser.setCurrentDirectory(Paths.get(".").toFile());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) //you can open directories
                    return true;
                String extension = com.google.common.io.Files.getFileExtension(f.getAbsolutePath()).trim().toLowerCase();
                return extension.equals("yaml") || extension.equals("yml");
            }

            @Override
            public String getDescription() {
                return "Any YAML scenario";
            }
        });


        final JButton filer = new JButton("Open from File");
        filer.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {


                        if (chooser.showOpenDialog(scenarioSelector) == JFileChooser.APPROVE_OPTION)
                        {
                            File file = chooser.getSelectedFile();
                            //This is where a real application would open the file.
                            Log.info("opened file " + file);
                            FishYAML yaml = new FishYAML();
                            try{
                                //read yaml
                                Scenario scenario = yaml.loadAs(
                                        String.join("\n", java.nio.file.Files.readAllLines(file.toPath())),
                                        Scenario.class);
                                //add it to the swing
                                SwingUtilities.invokeLater(() -> {
                                    if(scenarioSelector.hasScenario("yaml"))
                                        scenarioSelector.removeScenarioOption("yaml");
                                    scenarioSelector.addScenarioOption("yaml",scenario);
                                    scenarioSelector.select("yaml");
                                    scenarioSelector.repaint();
                                });

                            }
                            catch (Exception yamlError)
                            {
                                Log.warn(yamlError.getMessage());
                                Log.warn(yamlError.toString());
                                Log.warn(file + " is not a valid YAML scenario!");
                            }
                        } else {
                            Log.info("open file cancelled");
                        }
                    }
                }


        );
        buttonBox.add(filer);


        Log.set(Log.LEVEL_INFO);
        scenarioSelection.setContentPane(contentPane);
        scenarioSelection.pack();
        scenarioSelection.setVisible(true);


        FishState state = new FishState(System.currentTimeMillis(),1);
        Log.set(Log.LEVEL_NONE);
        Log.setLogger(new FishStateLogger(state, Paths.get("log.csv")));




        state.setScenario(scenarioSelector.getScenario());
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                LinkedList<Fisher> poorFishers = new LinkedList<Fisher>();
                for(Fisher fisher : model.getFishers())
                {
                    if(fisher.getID() <50)
                    {
                        //change their boat so they can't go very far
                        fisher.setBoat(new Boat(fisher.getBoat().getLength(),
                                                fisher.getBoat().getWidth(),
                                                fisher.getBoat().getEngine(),
                                                new FuelTank(500)));
                        fisher.setHold(new Hold(10, model.getSpecies().size()));
                        RandomCatchabilityTrawlFactory gearFactory = new RandomCatchabilityTrawlFactory();
                        gearFactory.setMeanCatchabilityFirstSpecies(new FixedDoubleParameter(.001));
                        fisher.setGear(gearFactory.apply(model));
                        fisher.setRegulation(new Anarchy());
                        poorFishers.add(fisher);
                    }
                }

                model.getYearlyDataSet().registerGatherer("Poor Fishers Total Income",
                                                          fishState -> poorFishers.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  YearlyFisherTimeSeries.CASH_COLUMN);
                                                                      }
                                                                  }).sum(), Double.NaN);
            }

            @Override
            public void turnOff() {

            }
        });

        PrototypeScenario scenario = (PrototypeScenario) state.getScenario();
        EquidegreeBuilder builder = (EquidegreeBuilder) scenario.getNetworkBuilder();
        //connect people that have the same hold to avoid stupid imitation noise.
        builder.addPredicate((from, to) -> {
            return (from.getID() <50 && to.getID() < 50) ||   (from.getID() >=50 && to.getID() >= 50);
            //return Math.abs(from.getMaximumHold() - to.getMaximumHold()) < 1;
        });
        scenario.setNetworkBuilder(builder);

        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);


    }



    public static void itq_sweep(String[] args) throws IOException {

        StringBuilder finalOutput = new StringBuilder();
        finalOutput.append("price,small,big\n");
        for(double price = 0; price<.5; price+=.01)
        {

            DoubleSummaryStatistics averageBigLandings =new DoubleSummaryStatistics();
            DoubleSummaryStatistics averageSmallLandings =new DoubleSummaryStatistics();
            for(int i=0; i<5; i++)
            {
                FishState state = itqExample(price);
                Species species = state.getSpecies().get(0);
                for(Fisher fisher : state.getFishers())
                {
                    if(Math.abs(fisher.getMaximumHold()-10)<.1)
                    {
                        averageSmallLandings.accept(
                                fisher.getLatestYearlyObservation(species + " " +AbstractMarket.LANDINGS_COLUMN_NAME)
                        );
                    }
                    else
                    {
                        assert Math.abs(fisher.getMaximumHold()-500)<.1;
                        averageBigLandings.accept(
                                fisher.getLatestYearlyObservation(species + " " +AbstractMarket.LANDINGS_COLUMN_NAME)
                        );
                    }
                }


            }
            finalOutput.
                    append(price).
                    append(",").
                    append(averageSmallLandings.getAverage()).
                    append(",").
                    append(averageBigLandings.getAverage()).append("\n");
            System.out.println(price);
        }


        System.out.println(finalOutput);
        Path outputFolder = Paths.get("docs", "20160215 heterogeneous");
        Files.write(outputFolder.resolve("heterogeneous.csv"), finalOutput.toString().getBytes());

    }
}
