package uk.ac.ox.oxfish.gui.drawing;

import sim.display.Display2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom combo-box to switch what is displayed
 * Created by carrknight on 4/22/15.
 */
public class ColorfulGridSwitcher extends JComboBox<String>{

    private final Map<String, Specie> speciesMap = new HashMap<>();


    /**
     * Creates a <code>JComboBox</code> with a default data model.
     * The default data model is an empty list of objects.
     * Use <code>addItem</code> to add items.  By default the first item
     * in the data model becomes selected.
     *
     * @see DefaultComboBoxModel
     */
    public ColorfulGridSwitcher(ColorfulGrid toModify, GlobalBiology biology, Display2D toRefresh) {
        String initialSelection = "Depth";
        addItem(initialSelection);
        for(Specie specie : biology.getSpecies()) {
            speciesMap.put(specie.getName(),specie);
            addItem(specie.getName());
        }
        setSelectedItem(initialSelection);

        JComboBox<String> reference = this;

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Specie selected = speciesMap.get(reference.getSelectedItem());
                toModify.setSelectedSpecie(selected);
                toRefresh.repaint();
            }
        });
    }
}
