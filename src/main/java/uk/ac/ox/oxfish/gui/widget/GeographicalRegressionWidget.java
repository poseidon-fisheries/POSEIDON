package uk.ac.ox.oxfish.gui.widget;

import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;
import uk.ac.ox.oxfish.fisher.selfanalysis.heatmap.GeographicalRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.TriColorMap;
import uk.ac.ox.oxfish.gui.drawing.ColorEncoding;
import uk.ac.ox.oxfish.model.FishState;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by carrknight on 6/30/16.
 */
public class GeographicalRegressionWidget  implements WidgetBuilder<JComponent,SwingMetawidget>
{




    /**
     * needs the gui to plot
     */
    private final FishGUI gui;


    public GeographicalRegressionWidget(FishGUI gui) {
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
        if(actualClass == null || !GeographicalRegression.class.isAssignableFrom(actualClass))
            return null;

        //it's a MarketMap
        try {
            String[] path = metawidget.getPath().split("/");
            //nested address? no problem
            String address = path.length == 2? path[1] + "." + attributes.get("name") :
                    attributes.get("name");
            GeographicalRegression regression = ((GeographicalRegression)
                    PropertyUtils.getProperty(metawidget.getToInspect(),
                                              address));
            gui.getMainPortrayal().addEnconding(
                    metawidget.getPath(),
                    new ColorEncoding(
                    new TriColorMap(0, 10, 100, Color.RED, Color.WHITE, Color.BLUE),
                    new Function<SeaTile, Double>() {
                        @Override
                        public Double apply(SeaTile tile) {
                            return regression.predict(tile, ((FishState) gui.state).getHoursSinceStart());
                        }
                    },
                    false)
            );
            return null;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            //because of recursion this will happen
            //   e.printStackTrace();
            return null;
        }


    }



}
