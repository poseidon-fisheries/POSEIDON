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

package uk.ac.ox.oxfish.gui.controls;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HabitatAwareGearFactory;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.MetaInspector;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class GearSetterButton implements PolicyButton {


    private AlgorithmFactory<? extends Gear> gearFactory = new HabitatAwareGearFactory();


    @Override
    public JComponent buildJComponent(FishGUI gui) {


        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(new JLabel("Gear Factory"));
        container.add(new MetaInspector(this,gui));
        JButton applyButton = new JButton("Apply new gear to all fishers");
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FishState model = (FishState) gui.state;

                for(Fisher fisher : model.getFishers())
                    fisher.setGear(gearFactory.apply(model));
            }
        });
        container.add(applyButton);
        return container;



    }


    public AlgorithmFactory<? extends Gear> getGearFactory() {
        return gearFactory;
    }

    public void setGearFactory(
            AlgorithmFactory<? extends Gear> gearFactory) {
        this.gearFactory = gearFactory;
    }
}
