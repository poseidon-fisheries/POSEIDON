import com.esotericsoftware.minlog.Log;
import com.google.common.io.Files;
import ec.util.MersenneTwisterFast;
import sim.display.Console;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.ConstantLocalBiology;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.experiments.MarketFirstDemo;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.ScenarioSelector;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.regs.factory.ITQSpecificFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

class Main{

    //main
    public static void main(String[] args) throws IOException {



        final JDialog scenarioSelection = new JDialog((JFrame)null,true);
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
                String extension = Files.getFileExtension(f.getAbsolutePath()).trim().toLowerCase();
                if(extension.equals("yaml") || extension.equals("yml"))
                    return true;
                return false;
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
                                Log.warn(file + " is not a valid YAML scenario!");
                            }
                        } else {
                            Log.info("open file cancelled");
                        }
                    }
                }


        );
        buttonBox.add(filer);

        FishState state = new FishState(System.currentTimeMillis(),1);
        Log.set(Log.LEVEL_TRACE);
        Log.setLogger(new FishStateLogger(state, Paths.get("log.txt")));
        scenarioSelection.setContentPane(contentPane);
        scenarioSelection.pack();
        scenarioSelection.setVisible(true);




        state.setScenario(scenarioSelector.getScenario());
        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);
    }





    //opportunity costs
    public static void opportunity(String[] args) throws IOException {
        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half
        /*
        ITQSpecificFactory regs2 = new ITQSpecificFactory(){
            public void computeOpportunityCosts(Species specie, Fisher seller, double biomass, double revenue,
                                                SpecificQuotaRegulation regulation, ITQOrderBook market)
            {
                //account for opportunity costs
                if(biomass > 0 && regulation.getProtectedSpecies().equals(specie))
                {
                    double lastClosingPrice = -10 + 20* state.getDayOfTheYear() /365d ;
                    if(Double.isFinite(lastClosingPrice))
                    {
                        seller.recordOpportunityCosts(lastClosingPrice * biomass); //you could have sold those quotas!
                    }
                }
            }
        };
        regs2.setIndividualQuota(new FixedDoubleParameter(500000000));
        state.getDailyDataSet().registerGatherer("fake", new Function<FishState, Double>() {
            @Override
            public Double apply(FishState state) {
                return -10 + 20 * state.getDayOfTheYear() / 365d;
            }
        }, Double.NaN);
*/

/*
        ITQSpecificFactory ignoreCosts = new ITQSpecificFactory() {

            @Override
            public void computeOpportunityCosts(
                    Species specie, Fisher seller, double biomass, double revenue, SpecificQuotaRegulation regulation,
                    ITQOrderBook market) {
            }
        };

*/
        ITQSpecificFactory germane = new ITQSpecificFactory();
        germane.setIndividualQuota(new FixedDoubleParameter(5000));

        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        //scenario.setBiologyInitializer(OpportunityCostsDemo.FIXED_AND_SPLIT_BIOLOGY);
        scenario.setBiologyInitializer(new SplitInitializerFactory());

        scenario.setRegulation(germane);
        scenario.setMapInitializer(new SimpleMapInitializerFactory(50,50,0,1000000,2));

        scenario.forcePortPosition(new int[]{40, 25});
        scenario.setUsePredictors(true);

        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);

    }


    public static void oilPriceMiniDemo(String[] args) throws IOException {



        PrototypeScenario scenario = new PrototypeScenario();

        scenario.setFishers(100);
        scenario.setHoldSize(new FixedDoubleParameter(500));
        scenario.setMapInitializer(new SimpleMapInitializerFactory(50,50,0,1000000,2));

        scenario.setBiologyInitializer(new FromLeftToRightFactory());

        RandomCatchabilityTrawlFactory gear = new RandomCatchabilityTrawlFactory();
        gear.setTrawlSpeed(new FixedDoubleParameter(0));
        scenario.setGear(gear);



        FishState state = new FishState(System.currentTimeMillis(),24);
        state.setScenario(scenario);


        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                state.scheduleEveryDay(new Steppable() {
                    Port port = state.getPorts().iterator().next();

                    @Override
                    public void step(SimState simState) {
                        if (state.getYear() > 0)
                            if (state.getYear() % 2 == 1) {
                                port.setGasPricePerLiter(port.getGasPricePerLiter() + 0.02);
                            } else
                                port.setGasPricePerLiter(port.getGasPricePerLiter() - 0.02);

                    }
                }, StepOrder.DAWN);
            }

            @Override
            public void turnOff() {

            }
        });





        Log.set(Log.LEVEL_NONE);
        Log.setLogger(new FishStateLogger(state, Paths.get("log.csv")));


        state.setScenario(scenario);
        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);

        c.setVisible(true);
    }

    //OneDudeFishingAloneDemo
    public static void OneDudeFishingAloneDemo(String[] args) throws IOException {



        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(1);
        EquidegreeBuilder networkBuilder = new EquidegreeBuilder();
        networkBuilder.setDegree(1);
        //make hill-climbing step closer
        PerTripImitativeDestinationFactory hill = new PerTripImitativeDestinationFactory();
        hill.setStepSize(new FixedDoubleParameter(1));
        scenario.setDestinationStrategy(hill);

        //scenario.setNetworkBuilder(networkBuilder);
        scenario.setNetworkBuilder(new EmptyNetworkBuilder());
        FromLeftToRightFactory biologyInitializer = new FromLeftToRightFactory();
        biologyInitializer.setBiologySmoothingIndex(new FixedDoubleParameter(100));
        scenario.setBiologyInitializer(biologyInitializer);
        scenario.setMapInitializer(new SimpleMapInitializerFactory(10,10,4,1000000,10));


        FishState state = new FishState(3,24);
        //i am going to ruin the spot 5-3 in order to break a tie with 6-3. This means there is a single "best" place to fish
        //and the video looks more interesting
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                model.getMap().getSeaTile(5,3).setBiology(new ConstantLocalBiology(800));
            }

            @Override
            public void turnOff() {

            }
        });

        Log.set(Log.LEVEL_NONE);
        Log.setLogger(new FishStateLogger(state, Paths.get("log.csv")));


        state.setScenario(scenario);
        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);

        c.setVisible(true);
    }




    public static void temp2(String[] args){

        FishState state = MarketFirstDemo.generateMarketedModel(MarketFirstDemo.MarketDemoPolicy.ITQ,
                                                                new FixedDoubleParameter(.05),
                                                                new FixedDoubleParameter(10),
                                                                System.currentTimeMillis());
        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);



    }



    public static void temp1(String[] args) throws IOException
    {


        FishYAML yaml = new FishYAML();
        Scenario scenario = yaml.loadAs(new FileReader(Paths.get("inputs", "temp.yaml").toFile()),
                                        PrototypeScenario.class);

        FishState state = new FishState(System.currentTimeMillis(),2);
        state.setScenario(scenario);
        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);

        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {

                for(Fisher fisher : model.getFishers())
                {
                    Adaptation<FixedProbabilityDepartingStrategy> departingChance
                            = new Adaptation<>(
                            fisher1 -> true,
                            new BeamHillClimbing<FixedProbabilityDepartingStrategy>() {
                                @Override
                                public FixedProbabilityDepartingStrategy randomStep(
                                        FishState state, MersenneTwisterFast random, Fisher fisher,
                                        FixedProbabilityDepartingStrategy current) {
                                    double probability = current.getProbabilityToLeavePort();
                                    probability = probability * (0.8 + 0.4 * random.nextDouble());
                                    probability = Math.min(Math.max(0, probability), 1);
                                    return new FixedProbabilityDepartingStrategy(probability);
                                }
                            },
                            (fisher1, change, model1) -> fisher1.setDepartingStrategy(change),
                            fisher1 -> ((FixedProbabilityDepartingStrategy) fisher1.getDepartingStrategy()),
                            new CashFlowObjective(60),
                            .2, .6
                    );
                    fisher.addBiMonthlyAdaptation(departingChance);


                }
                model.getDailyDataSet().registerGatherer("Probability to leave port", state1 -> {
                    double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (Fisher fisher1 : state1.getFishers())
                            total += ((FixedProbabilityDepartingStrategy) fisher1.getDepartingStrategy()).
                                    getProbabilityToLeavePort();
                        return total / size;
                    }
                }, Double.NaN);

            }

            @Override
            public void turnOff() {

            }
        });
    }

}
