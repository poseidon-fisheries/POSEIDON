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

package uk.ac.ox.oxfish.gui.widget;


import org.metawidget.swing.SwingMetawidget;
import org.metawidget.swing.widgetprocessor.binding.beanutils.BeanUtilsBindingProcessor;
import org.metawidget.widgetprocessor.iface.WidgetProcessor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.util.Map;

/**
 * this is basically an hack: BeanUtils binding isn't immediate. It needs to have "save" and "rebind"
 * called to write and read respectively. This makes it so that the standard components call "save" when used
 * Created by carrknight on 5/28/15.
 */
public class ImmediateBinder implements WidgetProcessor<JComponent, SwingMetawidget> {


    /**
     * get standard components and make them call "save" if they are changed.
     * This way any change is immediately written to file
     */
    @Override
    public JComponent processWidget(
            JComponent jComponent, String s, Map<String, String> map, final SwingMetawidget metawidget) {
        if(jComponent instanceof JSpinner)
            ((JSpinner)jComponent).addChangeListener(e -> writeToObject(metawidget, true));



        if(jComponent instanceof JCheckBox)
            ((JCheckBox)jComponent).addItemListener(e -> writeToObject(metawidget, true));

        if(jComponent instanceof JTextComponent)
            ((JTextComponent)jComponent).getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    //this apparently is fonts and other stuff, not that useful!
                    writeToObject(metawidget, false);

                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    writeToObject(metawidget, false);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    writeToObject(metawidget, false);

                }
            });



        return jComponent;
    }

    public void writeToObject(SwingMetawidget metawidget, boolean rebind) {
        metawidget.getWidgetProcessor( BeanUtilsBindingProcessor.class ).save( metawidget );
        if(rebind)
            metawidget.getWidgetProcessor( BeanUtilsBindingProcessor.class ).rebind( metawidget.getToInspect(),metawidget );
    }


}