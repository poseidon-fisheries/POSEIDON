package uk.ac.ox.oxfish.gui.widget;

import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;
import sim.display.Display2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.ProfitFunctionRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.TriColorMap;
import uk.ac.ox.oxfish.gui.drawing.ColorEncoding;
import uk.ac.ox.oxfish.gui.drawing.ColorfulGrid;
import uk.ac.ox.oxfish.model.FishState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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



            if(actualClass == ProfitFunctionRegression.class)
            {

                JPanel panel = new JPanel(null);
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(
                 new GeographicalRegressionJButton(gui, regression,
                                                         metawidget.getToInspect(),
                                                         attributes.get("name")
                ));
                //now add all the species
                panel.add(new JSeparator());
                GeographicalRegression[] regressions = ((ProfitFunctionRegression) regression).catchesRegression();
                for(int i=0; i<regressions.length; i++)
                    panel.add(new GeographicalRegressionJButton(gui,regressions[i],metawidget.getToInspect(),
                                                                "Expected Catches Species " + i));
                return panel;
            }
            else {
                return new GeographicalRegressionJButton(gui, regression,
                                                         metawidget.getToInspect(),
                                                         attributes.get("name"));
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            //because of recursion this will happen
            //   e.printStackTrace();
            return null;
        }


    }


    private static class GeographicalRegressionJButton extends JButton implements ActionListener
    {

        private final FishGUI gui;

        private final GeographicalRegression regression;

        private final Fisher fisher;

        /**
         * Creates a button with no set text or icon.
         */
        GeographicalRegressionJButton(
                FishGUI gui, GeographicalRegression regression, Fisher fisher, String name) {
            this.gui = gui;
            this.regression = regression;
            this.setText("Show Heatmap");
            this.addActionListener(this);
            this.fisher=fisher;
            setText("Show " + name);
        }

        /**
         * Invoked when an action occurs.
         *
         */
        @Override
        public void actionPerformed(ActionEvent e)
        {

            ColorfulGrid heatmapPortrayal = new ColorfulGrid(gui.guirandom);
            heatmapPortrayal.addEnconding(
                "Heatmap",
                new ColorEncoding(
                        new TriColorMap(0, 10, 100, Color.RED, Color.WHITE, Color.BLUE),
                        new Function<SeaTile, Double>() {
                            @Override
                            public Double apply(SeaTile tile) {
                                return regression.predict(tile, ((FishState) gui.state).
                                        getHoursSinceStart(),(FishState) gui.state,
                                                          fisher);
                            }
                        },
                        false));
            Display2D heatmapDisplay = gui.setupPortrayal(heatmapPortrayal);
            JFrame heatmapFrame = gui.setupDisplay2D(heatmapPortrayal,heatmapDisplay,"Heatmap" + getText(),false);
            heatmapPortrayal.setSelectedEncoding("Heatmap");
            heatmapFrame.setVisible(true);
        }
    }


}
