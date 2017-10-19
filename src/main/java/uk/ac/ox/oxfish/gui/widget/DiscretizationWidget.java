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
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.gui.DiscreteColorMap;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.drawing.ColorEncoding;
import uk.ac.ox.oxfish.model.FishState;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Function;

/**
 * Similar to GeographicalRegressionWidget except it draws the discretization rather than
 * the heatmap (also it plots over the same grid rather than a new one)
 * Created by carrknight on 11/30/16.
 */
public class DiscretizationWidget implements WidgetBuilder<JComponent,SwingMetawidget> {


    private final FishGUI gui;

    public DiscretizationWidget(FishGUI gui) {
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
        if(actualClass == null || !MapDiscretization.class.isAssignableFrom(actualClass))
            return null;

        try {
            //nested address? no problem
            String address = StrategyFactoryWidgetProcessor.addressFromPath(
                    attributes,metawidget);
            MapDiscretization discretization = ((MapDiscretization)
                    PropertyUtils.getProperty(metawidget.getToInspect(),
                                              address));



                return new DiscretizationJButton(gui,discretization);

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            //because of recursion this will happen
            //   e.printStackTrace();
            return null;
        }


    }


    private static class DiscretizationJButton extends JButton implements ActionListener
    {

        private final FishGUI gui;

        private final MapDiscretization regression;

        /**
         * Creates a button with no set text or icon.
         */
        public DiscretizationJButton(FishGUI gui, MapDiscretization regression) {
            this.gui = gui;
            this.regression = regression;
            setText("Show discretization");
            addActionListener(this);
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            final FishState state = (FishState) gui.state;

            //remove a previous one if it exists
            gui.getMainPortrayal().getEncodings().remove("Discretization");
            gui.getMainPortrayal().addEnconding("Discretization",
                                                new ColorEncoding(
                                                        new DiscreteColorMap(state.getRandom()),
                                                        new Function<SeaTile, Double>() {
                                                            @Override
                                                            public Double apply(SeaTile tile) {
                                                                Integer group = regression.getGroup(tile);
                                                                return group == null ? -1 : (double) group;
                                                            }
                                                        },true
                                                ));
            gui.getMainPortrayal().setSelectedEncoding("Discretization");
            gui.forceRepaint();
        }
    }
}
