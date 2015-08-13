import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.resources.JFreeChartResources;
import sim.display.Console;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.ScenarioSelector;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.maximization.Actuator;
import uk.ac.ox.oxfish.utility.maximization.Adaptation;
import uk.ac.ox.oxfish.utility.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.maximization.Sensor;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Function;

class Main{

    public static void temp(String[] args) throws IOException {




        JDialog scenarioSelection = new JDialog((JFrame)null,true);
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


        scenarioSelection.setContentPane(contentPane);
        scenarioSelection.pack();
        scenarioSelection.setVisible(true);


        FishState state = new FishState(System.currentTimeMillis(),1);
        Log.set(Log.LEVEL_NONE);
        Log.setLogger(new FishStateLogger(state, Paths.get("log.csv")));


        state.setScenario(scenarioSelector.getScenario());
        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);
    }


    public static void main(String[] args) throws IOException
    {


        FishYAML yaml = new FishYAML();
        Scenario scenario = (Scenario) yaml.loadAs(new FileReader(Paths.get("inputs", "temp.yaml").toFile()),
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
