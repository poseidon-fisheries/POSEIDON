package uk.ac.ox.oxfish.gui;

import uk.ac.ox.oxfish.gui.widget.ScenarioJComponent;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import javax.swing.*;

import static org.junit.Assert.*;


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