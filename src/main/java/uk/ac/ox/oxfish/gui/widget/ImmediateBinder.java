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
            ((JSpinner)jComponent).addChangeListener(e -> writeToObject(metawidget));



        if(jComponent instanceof JCheckBox)
            ((JCheckBox)jComponent).addItemListener(e -> writeToObject(metawidget));

        if(jComponent instanceof JTextComponent)
            ((JTextComponent)jComponent).getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    writeToObject(metawidget);

                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    writeToObject(metawidget);

                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    writeToObject(metawidget);

                }
            });



        return jComponent;
    }

    private void writeToObject(SwingMetawidget metawidget) {
        metawidget.getWidgetProcessor( BeanUtilsBindingProcessor.class ).save( metawidget );
        metawidget.getWidgetProcessor( BeanUtilsBindingProcessor.class ).rebind( metawidget.getToInspect(),metawidget );
    }


}