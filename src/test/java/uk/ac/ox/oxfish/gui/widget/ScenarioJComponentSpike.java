package uk.ac.ox.oxfish.gui.widget;

import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import javax.swing.*;

import static org.junit.Assert.*;

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
        frame.setSize( 400, 250 );
        frame.setVisible( true );

    }

}