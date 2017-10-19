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

package uk.ac.ox.oxfish.experiments.dedicated.habitat;

import com.esotericsoftware.minlog.Log;
import sim.display.Console;
import sim.engine.SimState;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.controls.GearSetterButton;
import uk.ac.ox.oxfish.gui.widget.ScenarioJComponent;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * demo to give to Ren and Mike to test that gears do what they are told. It extends fishGUI because Mason is annoying and stupid
 * Created by carrknight on 9/30/15.
 */
public class HabitatDeployment extends FishGUI{


    /**
     * standard constructor, useful mostly for checkpointing
     *
     * @param state checkpointing state
     */
    public HabitatDeployment(SimState state) {
        super(state);
    }

    //main
    public static void main(String[] args) throws IOException {



        JDialog scenarioSelection = new JDialog((JFrame)null,true);

        final ScenarioJComponent scenario = new ScenarioJComponent(new HabitatDeploymentScenario());

        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(new JScrollPane(scenario.getJComponent()), BorderLayout.CENTER);
        //create ok and exit button
        Box buttonBox = new Box( BoxLayout.LINE_AXIS);
        contentPane.add(buttonBox, BorderLayout.SOUTH);
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


        state.setScenario(scenario.getScenario());
        HabitatDeployment vid = new HabitatDeployment(state);
        vid.getPolicyButtons().add(new GearSetterButton());
        Console c = new Console(vid);
        c.setSize(1000, 600);
        c.setVisible(true);
    }

    public static Object getInfo()
    {
        return "This is a slice of the model with a few parameters to set to check how biomass, gear and habitats interact " +
                "with one another. It isn't particularly interesting except as a way to catch weird behaviours but I hope it solicits" +
                "good feedback on both the model and the gui usability.\n" +
                "Can read more about it at: http://carrknight.github.io/assets/oxfish/habitat/habitat.html";
    }
}
