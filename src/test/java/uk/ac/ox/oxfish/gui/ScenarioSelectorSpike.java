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