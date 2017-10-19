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

import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import javax.swing.*;

/**
 * I might decide to make a full GUI test at some point, till then this is just a personal way
 * to check that things are correct
 * Created by carrknight on 5/29/15.
 */
public class ScenarioJComponentSpike {

    public static void main(String[] args){

        PrototypeScenario prototypeScenario = new PrototypeScenario();
        ScenarioJComponent component = new ScenarioJComponent(prototypeScenario);

        JFrame frame = new JFrame( "Metawidget Tutorial" );
        frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        frame.getContentPane().add(component.getJComponent());
        frame.pack();
        frame.setVisible( true );

    }

}