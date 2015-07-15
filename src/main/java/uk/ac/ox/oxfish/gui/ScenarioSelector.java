package uk.ac.ox.oxfish.gui;

import javafx.scene.control.RadioButton;
import uk.ac.ox.oxfish.gui.widget.ScenarioJComponent;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.Scenarios;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Map;

/**
 * A panel to select scenarios and modify them before the simulation starts
 * Created by carrknight on 6/7/15.
 */
public class ScenarioSelector extends JPanel implements ActionListener
{

    private Scenario scenario;

    private final JPanel settings;


    public ScenarioSelector()
    {
        LinkedList<JRadioButton> buttons = new LinkedList<>();
        //border layout
        this.setLayout(new BorderLayout());

        //initially empty settings panel
        this.settings = new JPanel(new CardLayout());
        this.add(new JScrollPane(settings),BorderLayout.CENTER);


        //create radio buttons on the left
        JPanel scenarios = new JPanel(new GridLayout(0, 1));
        this.add(scenarios,BorderLayout.WEST);
        //populate radio button group
        ButtonGroup scenarioGroup = new ButtonGroup();
        for(Map.Entry<String,Scenario> scenarioItem : Scenarios.SCENARIOS.entrySet())
        {
            final JRadioButton scenarioButton = new JRadioButton(scenarioItem.getKey());
            scenarioButton.setActionCommand(scenarioItem.getKey());
            scenarioButton.setToolTipText(Scenarios.DESCRIPTIONS.get(scenarioItem.getKey()));
            scenarios.add(scenarioButton);
            buttons.add(scenarioButton);
            scenarioGroup.add(scenarioButton);
            scenarioButton.addActionListener(this);

            //create widget for scenario
            final ScenarioJComponent widget = new ScenarioJComponent(scenarioItem.getValue());
            settings.add(scenarioItem.getKey(),widget.getJComponent());

        }
        scenarioGroup.clearSelection();
        //now force the first one to be selected
        buttons.getLast().doClick();




    }

    /**
     * invoked by radio-button. Sets new scenario
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        //new scenario!
        scenario = Scenarios.SCENARIOS.get(e.getActionCommand());

        CardLayout cl = (CardLayout)(settings.getLayout());
        cl.show(settings, e.getActionCommand());

        settings.repaint();
        this.repaint();

    }

    public Scenario getScenario() {
        return scenario;
    }
}
