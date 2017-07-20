package uk.ac.ox.oxfish.gui.widget;

import com.esotericsoftware.minlog.Log;
import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Created by carrknight on 7/20/17.
 */
public class EnumWidgetBuilder implements WidgetBuilder<JComponent,SwingMetawidget>
{


    public JComponent buildWidget(
            String elementName, Map<String, String> attributes, SwingMetawidget metawidget)
    {
        final Class<?> actualClass =
                WidgetBuilderUtils.getActualClassOrType(attributes, String.class);
        if(actualClass == null || !Enum.class.isAssignableFrom(actualClass))
            return null;

        try{
            //nested address? no problem
            String address = StrategyFactoryWidgetProcessor.addressFromPath(
                    attributes,metawidget);
            Enum currentEnum = ((Enum) PropertyUtils.getProperty(metawidget.getToInspect(),
                                                                 address));

            JComboBox comboBox = new JComboBox(currentEnum.getDeclaringClass().getEnumConstants());
            comboBox.setSelectedItem(currentEnum);

            final Object parent = metawidget.getToInspect();


            comboBox.addItemListener(
                    new ItemListener() {

                        /**
                         * Invoked when an item has been selected or deselected by the user.
                         * The code written for this method performs the operations
                         * that need to occur when an item is selected (or deselected).
                         *
                         * @param e
                         */
                        @Override
                        public void itemStateChanged(ItemEvent e) {

                            Object newEnum = comboBox.getSelectedItem();
                            //use the beansutils to set the new value to the field
                            try {

                                Object oldEnum = PropertyUtils.getProperty(parent, address);
                                if(oldEnum == newEnum)
                                    return;

                                PropertyUtils.setProperty(
                                        //the object to modify
                                        parent,
                                        //the name of the field
                                        address,
                                        //the new value (table lookup)
                                        newEnum);

                                //so i bind it again by setter
                                metawidget.setToInspect(parent);
                                if(metawidget.getParent()!=null) {
                                    metawidget.getParent().revalidate();

                                }
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e1) {
                                e1.printStackTrace();
                            }
                        }


                    }
            );



            return comboBox;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)

        {
            Log.error("failed to create enum widget");
            throw new RuntimeException("failed to create enum widget");
        }



    }



}
