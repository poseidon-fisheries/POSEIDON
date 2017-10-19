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

package uk.ac.ox.oxfish.gui.drawing;

import javafx.collections.MapChangeListener;
import sim.display.Display2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Custom combo-box to switch what is displayed
 * Created by carrknight on 4/22/15.
 */
public class ColorfulGridSwitcher extends JComboBox<String> implements ActionListener,MapChangeListener<String, ColorEncoding>{


    private final  ColorfulGrid toModify;

    private final Display2D toRefresh;

    /**
     * Creates a <code>JComboBox</code> with a default data model.
     * The default data model is an empty list of objects.
     * Use <code>addItem</code> to add items.  By default the first item
     * in the data model becomes selected.
     *
     * @see DefaultComboBoxModel
     */
    public ColorfulGridSwitcher(ColorfulGrid toModify, GlobalBiology biology, Display2D toRefresh) {

        this.toModify = toModify;
        this.toRefresh =toRefresh;


        toModify.getEncodings().addListener(this);

        for(String key: toModify.getEncodings().keySet())
            addItem(key);
        setSelectedItem(toModify.getSelectedName());

        JComboBox<String> reference = this;

        addActionListener(this);


        toRefresh.display.getViewport().addChangeListener(e -> {
            boolean isImmutable = toModify.isImmutableField();
            toModify.setImmutableField(false);
            toRefresh.revalidate();
            toRefresh.repaint();
            toModify.setImmutableField(isImmutable);
        });


    }


    @Override
    public void actionPerformed(ActionEvent e)
    {

        toModify.setSelectedEncoding(getSelectedItem().toString());
        toRefresh.repaint();
    }

    @Override
    public void onChanged(
            Change<? extends String, ? extends ColorEncoding> change) {

        removeActionListener(this);

        removeAllItems();
        for(String key: toModify.getEncodings().keySet())
            addItem(key);
        setSelectedItem(toModify.getSelectedName());

        addActionListener(this);
    }
}
