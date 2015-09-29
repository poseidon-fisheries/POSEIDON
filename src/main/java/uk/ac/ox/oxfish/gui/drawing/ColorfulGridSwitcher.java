package uk.ac.ox.oxfish.gui.drawing;

import sim.display.Display2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Custom combo-box to switch what is displayed
 * Created by carrknight on 4/22/15.
 */
public class ColorfulGridSwitcher extends JComboBox<String>{



    /**
     * Creates a <code>JComboBox</code> with a default data model.
     * The default data model is an empty list of objects.
     * Use <code>addItem</code> to add items.  By default the first item
     * in the data model becomes selected.
     *
     * @see DefaultComboBoxModel
     */
    public ColorfulGridSwitcher(ColorfulGrid toModify, GlobalBiology biology, Display2D toRefresh) {
        //add defaults
        String initialSelection = "Depth";
        addItem(initialSelection);
        addItem("Habitat");
        for(Specie specie : biology.getSpecies()) {
            addItem(specie.getName());
        }
        setSelectedItem(initialSelection);

        JComboBox<String> reference = this;

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {

                toModify.setSelectedEncoding(reference.getSelectedItem().toString());
                toRefresh.repaint();
            }
        });
    }
}
