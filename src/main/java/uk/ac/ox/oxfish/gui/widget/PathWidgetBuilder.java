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

import com.esotericsoftware.minlog.Log;
import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Created by carrknight on 4/18/16.
 */
public class PathWidgetBuilder implements WidgetBuilder<JComponent,SwingMetawidget> {


    public JComponent buildWidget(
            String elementName, Map<String, String> attributes, SwingMetawidget metawidget)
    {
        final Class<?> actualClass = WidgetBuilderUtils.getActualClassOrType(attributes, String.class);
        if(actualClass == null || !Path.class.isAssignableFrom(actualClass))
            return null;

        try{
            //nested address? no problem
            String address = StrategyFactoryWidgetProcessor.addressFromPath(
                    attributes,metawidget);
            Path directory = ((Path) PropertyUtils.getProperty(metawidget.getToInspect(),
                                                               address));

            JButton filer = new JButton();
            if(directory!=null)
                filer.setText(directory.toString());

            final JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if(directory!=null)
                chooser.setCurrentDirectory(directory.toFile());
            chooser.setAcceptAllFileFilterUsed(true);


            final Object beingInspected = metawidget.getToInspect();


            filer.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {

                            if(chooser.showOpenDialog(metawidget) == JFileChooser.APPROVE_OPTION)
                            {
                                Path newPath = chooser.getSelectedFile().toPath();
                                //use the beansutils to set the new value to the field
                                try {
                                    PropertyUtils.setProperty(
                                            //the object to modify
                                            beingInspected,
                                            //the name of the field
                                            address,
                                            //the new value (table lookup)
                                            newPath);

                                    //so i bind it again by setter
                                    metawidget.setToInspect(beingInspected);
                                    JRootPane root = metawidget.getRootPane();

                                    if(root!=null) {
                                       SwingUtilities.invokeLater(
                                               new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       root.setSize(root.getSize());
                                                       root.revalidate();

                                                   }
                                               }
                                       );

                                    }
                                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e1) {
                                    e1.printStackTrace();
                                }



                            }

                        }
                    }
            );



            return filer;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)

        {
            Log.error("failed to create path widget");
            throw new RuntimeException("failed to create path widget");
        }



    }

}
