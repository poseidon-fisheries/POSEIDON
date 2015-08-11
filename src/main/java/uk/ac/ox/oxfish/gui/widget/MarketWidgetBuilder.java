package uk.ac.ox.oxfish.gui.widget;

import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.MetaInspector;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketMap;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Grabs a MarketMap object and drill down into its components
 * Created by carrknight on 7/31/15.
 */
public class MarketWidgetBuilder implements WidgetBuilder<JComponent,SwingMetawidget>
{


    /**
     * needs the gui to plot
     */
    private final FishGUI gui;


    public MarketWidgetBuilder(FishGUI gui) {
        this.gui = gui;
    }



    /**
     *  tries to build a list of market subwidgets for each market available in this MarketMap object
     */
    @Override
    public JComponent buildWidget(
            String elementName, Map<String, String> attributes, SwingMetawidget metawidget)
    {

        final Class<?> actualClass = WidgetBuilderUtils.getActualClassOrType(attributes, String.class);
        //if it is a primitive or not a MarketMap we have no use for it
        if(actualClass == null || !MarketMap.class.isAssignableFrom(actualClass))
            return null;

        //it's a MarketMap
        try {


            String[] path = metawidget.getPath().split("/");
            //nested address? no problem
            String address = path.length == 2? path[1] + "." + attributes.get("name") :
                    attributes.get("name");
            MarketMap marketMap = ((MarketMap) PropertyUtils.getProperty(metawidget.getToInspect(),
                                                                   address));

            //get list of marketMap
            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));
            //for each market, please create a new sub-inspector to deal with it
            for(Market market : marketMap.getMarkets())
            {
                container.add(new MetaInspector(market,gui));
                container.add(new JSeparator(JSeparator.HORIZONTAL));
            }

            return container;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }


    }
}
