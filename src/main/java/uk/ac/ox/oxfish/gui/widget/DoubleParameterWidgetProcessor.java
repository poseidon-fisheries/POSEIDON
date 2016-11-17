package uk.ac.ox.oxfish.gui.widget;

import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.widgetprocessor.iface.WidgetProcessor;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameters;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

/**
 * looks for the double_parameter attributes so that you can switch its distribution at gui
 * Created by carrknight on 6/7/15.
 */
public class DoubleParameterWidgetProcessor implements WidgetProcessor<JComponent,SwingMetawidget>
{


    public static final String KEY_TO_LOOK_FOR = "double_parameter";

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
            JComponent widget, String elementName, Map<String, String> attributes, SwingMetawidget metawidget) {

        try {
            if(attributes.containsKey(KEY_TO_LOOK_FOR))
            {
                assert widget instanceof SwingMetawidget;



                //build JComponent
                final JComboBox<String> parameterBox = new JComboBox<>();
                //fill it with the strings from the constructor masterlist
                DoubleParameters.DOUBLE_PARAMETERS.keySet().forEach(parameterBox::addItem);
                final Object toModify = getToInspectByTraversingMPath((SwingMetawidget) widget, metawidget);


                //get through reflection
                Class actualClass = PropertyUtils.getNestedProperty(toModify,
                                                attributes.get("name")).getClass();
                parameterBox.setSelectedItem(DoubleParameters.DOUBLE_PARAMETERS_NAME.get(actualClass));
                //add it
                Box box = new Box(BoxLayout.LINE_AXIS );
                box.add(new JLabel("Distribution:"));
                box.add(parameterBox);
                widget.add(box,0);
                parameterBox.addActionListener(new ActionListener() {
                    //todo ugly code replication from StrategyFactoryWidgetProcessor
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //we need to make changes!
                        try {
                            //use the beansutils to set the new value to the field
                            PropertyUtils.setSimpleProperty(
                                    //the object to modify
                                    toModify,
                                    //the name of the field
                                    attributes.get("name"),
                                    //the new value (table lookup)
                                    DoubleParameters.DOUBLE_PARAMETERS.get(
                                            parameterBox.getSelectedItem()).get());

                            //now update the gui
                            //for some reason rebind alone is not enough here (although it is strange because it works elsewhere for the same change)
                            //metawidget.getWidgetProcessor(BeanUtilsBindingProcessor.class).rebind(metawidget.getToInspect(),metawidget);

                            //so i bind it again by setter
                            metawidget.setToInspect(metawidget.getToInspect());
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e1) {
                            System.err.print("failed to find class! " + e1);
                            e1.printStackTrace();
                        }


                    }
                });


            }
        } catch (InvocationTargetException | NoSuchMethodException
                | IllegalAccessException e) {
            System.err.print("failed in the gui! " + e);
            e.printStackTrace();
        }
        return widget;

    }

    public static Object getToInspectByTraversingMPath(
            SwingMetawidget widget,
            SwingMetawidget metawidget) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        //if the property is nested we need to find it
        String[] path = widget.getPath().split("/");
        LinkedList<String> toTraverse = new LinkedList<>(Arrays.asList(path));
        //remove the first (root) and last (property name)
        toTraverse.removeFirst();
        toTraverse.removeLast();
        //if there are still some nodes in the osmoseWFSPath then it is a nested property and we need to traverse it
        Object toInspect = metawidget.getToInspect();
        for(String node : toTraverse)
        {
            toInspect = PropertyUtils.getProperty(toInspect, node);
            //toInspect.getClass().getDeclaredField(node).get(toInspect);
        }
        return toInspect;
    }
}
