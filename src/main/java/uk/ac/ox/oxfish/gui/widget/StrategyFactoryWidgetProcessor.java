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

import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.widgetprocessor.iface.WidgetProcessor;
import uk.ac.ox.oxfish.utility.AlgorithmFactories;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class looks for factory_strategy attributes and if it finds them it creates a combo-box so
 * users can change the scenario factory used
 * Created by carrknight on 5/29/15.
 */
public class StrategyFactoryWidgetProcessor implements WidgetProcessor<JComponent,SwingMetawidget>
{

    /**
     * Process the given widget. Called after a widget has been built by the
     * <code>WidgetBuilder</code>, and before it is added to the <code>Layout</code>.
     *
     * @param widget      the widget to process. Never null
     * @param elementName XML node name of the business field. Typically 'entity', 'property' or 'action'.
     *                    Never null
     * @param attributes  attributes of the widget to process. Never null. This Map is modifiable - changes
     *                    will be passed to subsequent WidgetProcessors and Layouts
     * @param metawidget  the parent Metawidget. Never null
     * @return generally the original widget (as passed in to the first argument). Can be a
     * different widget if the WidgetProcessor wishes to substitute the original widget for
     * another. Can be null if the WidgetProcessor wishes to cancel all further processing
     * of this widget (including laying out)
     */
    @Override
    public JComponent processWidget(
            JComponent widget, String elementName, Map<String, String> attributes, SwingMetawidget metawidget)
    {

        try {
            if (attributes.containsKey("factory_strategy"))
            {
                //find it what are you building
                Class strategyClass = Class.forName(attributes.get("factory_strategy"));
                //get list of constructors
                Map<String,? extends Supplier<? extends AlgorithmFactory>>
                        constructors =
                        AlgorithmFactories.CONSTRUCTOR_MAP.get(strategyClass);
                Map<? extends Class<? extends AlgorithmFactory>,String> names = AlgorithmFactories.NAMES_MAP.get(strategyClass);

                final Object beingInspected = metawidget.getToInspect();
                final String fieldName = attributes.get("name");

                //try to select

                //build JComponent
                final JComboBox<String> factoryBox = new JComboBox<>();
                //fill it with the strings from the constructor masterlist
                if(constructors == null)
                    return widget;
                constructors.keySet().forEach(factoryBox::addItem);
                factoryBox.setSelectedIndex(-1);
                //find out which strategy factory is currently selected and try to show it in the combo-box
                try {
                    String address = addressFromPath(attributes, metawidget);

                    //current class
                    Class actualClass = PropertyUtils.getProperty(metawidget.getToInspect(),
                                                                  address).getClass();
                    //go through the constructors looking for that class
                    String name = names.get(actualClass);

                    //if found, set selected
                    factoryBox.setSelectedItem(name);


                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }


                //gui layout and panelling:
                JPanel panel = new JPanel();
                BoxLayout layout = new BoxLayout(panel,BoxLayout.PAGE_AXIS);
                panel.setLayout(layout);
                panel.add(factoryBox);
                panel.add(new JSeparator());
                panel.add(widget);

                //now listen carefully to combobx
                factoryBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //we need to make changes!
                        try {
                            //nested address? no problem
                            String address = StrategyFactoryWidgetProcessor.addressFromPath(
                                    attributes,metawidget);

                            //use the beansutils to set the new value to the field
                            PropertyUtils.setProperty(
                                    //the object to modify
                                    beingInspected,
                                    //the name of the field
                                    address,
                                    //the new value (table lookup)
                                    constructors.get((String) factoryBox.getSelectedItem()).get());

                            //now update the gui
                            //for some reason rebind alone is not enough here (although it is strange because it works elsewhere for the same change)
                            //metawidget.getWidgetProcessor(BeanUtilsBindingProcessor.class).rebind(metawidget.getToInspect(),metawidget);

                            //so i bind it again by setter
                            metawidget.setToInspect(beingInspected);
                            if(metawidget.getParent()!=null) {
                                metawidget.getParent().revalidate();

                            }
                        } catch (IllegalAccessException | InvocationTargetException |
                                NoSuchMethodException e1) {
                            System.err.print("failed to find class! " + e1);
                            e1.printStackTrace();
                        }


                    }
                });

                return panel;
            }
        }
        catch (ClassNotFoundException c){
            System.err.print("failed to find class! " + c);
            c.printStackTrace();

        }



        return widget;
    }

    public static String addressFromPath(Map<String, String> attributes, SwingMetawidget metawidget) {
        String[] path = metawidget.getPath().split("/");
        if(path.length ==1 )
            return attributes.get("name");
        else
        {
            StringBuilder builder = new StringBuilder();
            for(int i=1; i<path.length; i++)
                builder.append(path[i]).append(".");
            builder.append(attributes.get("name"));
            return builder.toString();

        }


        //nested address? no problem
       // return osmoseWFSPath.length == 2? osmoseWFSPath[1] + "." + attributes.get("name") :
        //        attributes.get("name");
    }
}
