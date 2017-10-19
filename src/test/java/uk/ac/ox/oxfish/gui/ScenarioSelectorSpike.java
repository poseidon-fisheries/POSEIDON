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

import javax.swing.*;


public class ScenarioSelectorSpike {


    public static void main(String[] args)
    {



        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ScenarioSelector selector = new ScenarioSelector();
                JFrame frame = new JFrame( "Work, dammit" );
                frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
                frame.getContentPane().add(selector);
                frame.pack();
                frame.setVisible( true );

            }
        });

    }

}