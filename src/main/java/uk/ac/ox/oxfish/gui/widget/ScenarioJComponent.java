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

package uk.ac.ox.oxfish.gui.widget;

import org.metawidget.swing.SwingMetawidget;
import uk.ac.ox.oxfish.gui.MetaInspector;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import javax.swing.*;

/**
 * The metawidget created by inspecting a scenario object. The name itself is a misnomer because
 * the JComponent (the widget) is actually a field rather than the class itself, but the use is the same:
 * point it to a scenario, it will generate all the gui you can access by getting the JComponent.
 *
 * <p>
 *     Notice that the scenario component is easier in general because the model isn't running while its fields
 *     are modified
 * </p>
 * Created by carrknight on 5/29/15.
 */
public class ScenarioJComponent {

    private final SwingMetawidget widget = new SwingMetawidget();

    private final Scenario scenario;

    /**
     * builds the gui: get the component with the getter
     * @param scenario the scenario to build the jcomponent for
     */
    public ScenarioJComponent(Scenario scenario) {
        this.scenario = scenario;


        MetaInspector.STANDARD_WIDGET_SETUP(widget, null);


        widget.setToInspect(scenario);

    }

     public JComponent getJComponent(){
         return widget;
     }

    public Scenario getScenario() {
        return scenario;
    }
}
