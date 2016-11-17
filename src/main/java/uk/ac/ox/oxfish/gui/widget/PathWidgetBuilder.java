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
            filer.setText(directory.toString());

            final JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
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
                                    if(metawidget.getParent()!=null) {
                                        metawidget.getParent().revalidate();

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
            Log.error("failed to create osmoseWFSPath widget");
            throw new RuntimeException("failed to create osmoseWFSPath widget");
        }



    }

}
