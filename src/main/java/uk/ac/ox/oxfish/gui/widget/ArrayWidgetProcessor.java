package uk.ac.ox.oxfish.gui.widget;

import com.esotericsoftware.minlog.Log;
import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.widgetprocessor.iface.WidgetProcessor;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import javax.swing.*;
import java.util.Map;

/**
 * Creates a string to show the content of arrays (and arrays of arrays of arrays of ...)
 * Created by carrknight on 4/5/16.
 */
public class ArrayWidgetProcessor implements WidgetProcessor<JComponent,SwingMetawidget> {


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
        Class<?> clazz = WidgetBuilderUtils.getActualClassOrType(attributes, String.class );
        if(clazz!=null) {
            try {
                if (clazz.isArray()) {

                    String[] path = metawidget.getPath().split("/");
                    //nested address? no problem
                    String address = path.length == 2? path[1] + "." + attributes.get("name") :
                            attributes.get("name");
                    //current class

                    String toDisplay =
                            FishStateUtilities.deepToStringArray(
                                    PropertyUtils.getProperty(metawidget.getToInspect(), address), " , ","|");

                    JLabel label = new JLabel(toDisplay);

                    widget.add(label);
                }
            } catch (Exception e) {
                Log.trace("cannot display " + attributes.get("name"));

            }
        }
        return widget;
    }
}

/*

        return null;
 */
