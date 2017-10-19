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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.gui.widget.ScenarioJComponent;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.Scenarios;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A panel to select scenarios and modify them before the simulation starts
 * Created by carrknight on 6/7/15.
 */
public class ScenarioSelector extends JPanel implements ActionListener
{

    private Scenario scenario;

    private final JPanel settings;
    private final JPanel scenariosPanel;
    private final HashMap<String,JRadioButton> radioButtons;
    private final HashMap<String,Scenario> scenarioMap = new HashMap<>();
    private final ButtonGroup radioButtonGroup;
    private final Map<String,ScenarioJComponent> widgets = new HashMap<>();


    public ScenarioSelector()
    {
        radioButtons = new HashMap<>();
        //border layout
        this.setLayout(new BorderLayout());

        //initially empty settings panel
        this.settings = new JPanel(new CardLayout());
        this.add(new JScrollPane(settings),BorderLayout.CENTER);


        //create radio buttons on the left
        scenariosPanel = new JPanel(new GridLayout(0, 1));
        this.add(scenariosPanel, BorderLayout.WEST);
        //populate radio button group
        radioButtonGroup = new ButtonGroup();
        for(Map.Entry<String,Supplier<Scenario>> scenarioItem : Scenarios.SCENARIOS.entrySet())
        {
            addScenarioOption(scenarioItem.getKey(),scenarioItem.getValue().get());


        }
        radioButtonGroup.clearSelection();
        //now force the prototype
        select("Abstract");


    }

    public void select(final String scenarioName) {
        radioButtons.get(scenarioName).doClick();
    }

    public void addScenarioOption(String name, Scenario scenario) {
        Scenario instance = scenario;
        scenarioMap.put(name, instance);
        final JRadioButton scenarioButton = new JRadioButton(name);
        scenarioButton.setActionCommand(name);
        scenarioButton.setToolTipText(Scenarios.DESCRIPTIONS.get(name));
        final ScenarioJComponent widget = new ScenarioJComponent(instance);
        widgets.put(name,widget);
        settings.add(name,widget.getJComponent());


        scenariosPanel.add(scenarioButton);
        radioButtons.put(name,scenarioButton);
        radioButtonGroup.add(scenarioButton);
        scenarioButton.addActionListener(this);

        scenariosPanel.revalidate();
        scenariosPanel.repaint();
    }


    public void removeScenarioOption(String name)
    {
        Preconditions.checkArgument(widgets.containsKey(name));
        Preconditions.checkArgument(radioButtons.containsKey(name));
        Preconditions.checkArgument(scenarioMap.containsKey(name));
        scenarioMap.put(name,scenario);


        ScenarioJComponent toRemove = widgets.get(name);
        settings.remove(toRemove.getJComponent());

        JRadioButton button = radioButtons.remove(name);
        scenariosPanel.remove(button);
        radioButtonGroup.remove(button);
        button.removeActionListener(this);

    }

    /**
     * invoked by radio-button. Sets new scenario
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        //new scenario!
        scenario = scenarioMap.get(e.getActionCommand());

        CardLayout cl = (CardLayout)(settings.getLayout());
        cl.show(settings, e.getActionCommand());

        settings.repaint();
        this.repaint();

    }

    public boolean hasScenario(String name)
    {
        return widgets.containsKey(name);
    }

    public Scenario getScenario() {
        return scenario;
    }
}
