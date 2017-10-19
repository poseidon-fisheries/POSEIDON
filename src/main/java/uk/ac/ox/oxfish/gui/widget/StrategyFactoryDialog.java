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
import uk.ac.ox.oxfish.gui.MetaInspector;
import uk.ac.ox.oxfish.utility.AlgorithmFactories;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A panel containing a combo-box to select a strategy factory
 * Created by carrknight on 6/7/15.
 */
public class StrategyFactoryDialog<T> extends JPanel implements ActionListener
{

    private final JPanel settings;
    /**
     * what is selected
     */
    private AlgorithmFactory selected;


    /**
     * a map of the constructors instanced (unlike the original constructor map which has factories)
     */
    private final Map<String,AlgorithmFactory> instancedConstructorMap;

    /**
     * what strategy are we dealing with
     * @param strategyClass the strategy superclass
     */
    public StrategyFactoryDialog(Class strategyClass)
    {
        LinkedList<JRadioButton> buttons = new LinkedList<>();

        //first see if it's a super-class, that makes it easy
        Map<String, ? extends Supplier<? extends AlgorithmFactory<?>>> constructorMap
                = AlgorithmFactories.CONSTRUCTOR_MAP.get(strategyClass);
        instancedConstructorMap = new HashMap<>();
        this.setLayout(new BorderLayout());

        //initially empty settings panel
        settings = new JPanel(new CardLayout());
        this.add(new JScrollPane(settings),BorderLayout.CENTER);

        //create radio buttons on the left
        JPanel factories = new JPanel(new GridLayout(0,1));
        this.add(factories,BorderLayout.WEST);
        ButtonGroup factoryGroup  = new ButtonGroup();
        for(Map.Entry<String, ? extends Supplier<? extends AlgorithmFactory>> factoryItem : constructorMap.entrySet())
        {
            final JRadioButton factoryButton = new JRadioButton(factoryItem.getKey());
            factoryButton.setActionCommand(factoryItem.getKey());
            factories.add(factoryButton);
            buttons.add(factoryButton);
            factoryGroup.add(factoryButton);
            factoryButton.addActionListener(this);

            //create widget
            SwingMetawidget widget = new SwingMetawidget();
            MetaInspector.STANDARD_WIDGET_SETUP(widget, null);
            final AlgorithmFactory toInspect = factoryItem.getValue().get();
            instancedConstructorMap.put(factoryItem.getKey(),toInspect);
            widget.setToInspect(toInspect);
            settings.add(factoryItem.getKey(),widget);

        }

        factoryGroup.clearSelection();
        //foce first to be selection
        buttons.getFirst().doClick();


    }


    /**
     * Invoked when an action occurs.
     *
     * @param e click
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        //new scenario!
        selected = instancedConstructorMap.get(e.getActionCommand());

        CardLayout cl = (CardLayout)(settings.getLayout());
        cl.show(settings, e.getActionCommand());

        settings.repaint();
        this.repaint();
    }

    public AlgorithmFactory getSelected() {
        return selected;
    }
}
