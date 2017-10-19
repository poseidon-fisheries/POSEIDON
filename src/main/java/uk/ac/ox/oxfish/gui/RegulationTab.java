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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.gui.controls.PolicyButton;
import uk.ac.ox.oxfish.gui.drawing.MPADrawer;
import uk.ac.ox.oxfish.model.FishState;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A tab to change regulations on the fly
 * Created by carrknight on 7/20/15.
 */
public class RegulationTab extends JPanel
{

    private final RegulationProxy regulations;

    /**
     * Constructs a new TabPane.
     */
    public RegulationTab(FishGUI gui, MPADrawer drawer) {
        super();

        regulations = new RegulationProxy();
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.add(new MetaInspector(regulations,gui));

        JButton jButton = new JButton("Change regulations for all agents");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //synchronize with the model
                synchronized (gui.state.schedule) {
                    //go through all agents
                    final FishState state = (FishState) gui.state;
                    for (Fisher fisher : state.getFishers())
                        //set new regulations!
                        fisher.setRegulation(regulations.getGlobalRegulations().apply(state));
                }
                //done
            }
        });

        this.add(jButton);

        this.add(new JSeparator());
        //second button is the enabled kind
        JToggleButton drawerButton = new JToggleButton("Draw new MPAs");
        drawerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (drawerButton.isSelected())
                    drawer.attach();
                else
                    drawer.detach();
            }
        });
        this.add(drawerButton);
        for(PolicyButton policy : gui.getPolicyButtons()) {
            this.add(new JSeparator());
            this.add(policy.buildJComponent(gui));
        }

    }

}
