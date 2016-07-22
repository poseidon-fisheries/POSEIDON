package uk.ac.ox.oxfish.gui.drawing;

import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.simple.ImagePortrayal2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.MetaInspector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Colors boat portrayals given tags
 * Created by carrknight on 7/22/16.
 */
public class BoatPortrayalFactory
{


    private final BufferedImage boatIcon;

    private final BufferedImage shipIcon;

    private final FishGUI gui;

    private final static HashMap<String,Color> colors = new HashMap<>();
    static
    {
        colors.put("red",Color.red);
        colors.put("blue",Color.blue);
        colors.put("yellow",Color.yellow);
        colors.put("green",Color.green);
        colors.put("grey",Color.gray);
        colors.put("gray",Color.gray);
        colors.put("pink",Color.pink);
        colors.put("orange",Color.orange);


    }


    public BoatPortrayalFactory(FishGUI gui) throws IOException {
        boatIcon = ImageIO.read(FishGUI.IMAGES_PATH.resolve("boat.png").toFile());
        shipIcon= ImageIO.read(FishGUI.IMAGES_PATH.resolve("ship.png").toFile());
        this.gui=gui;
    }




    public BoatPortrayal build(Fisher fisher)
    {

        //need a color?
        if(!fisher.getTags().stream().filter(s -> s.equalsIgnoreCase("ship")).findFirst().isPresent()) {
            for (Map.Entry<String, Color> color : colors.entrySet()) {
                if (fisher.getTags().stream().filter(s -> s.equalsIgnoreCase(color.getKey())).findFirst().isPresent())
                    return new BoatPortrayal(colorImage(boatIcon, color.getValue()),gui);
            }
            return new BoatPortrayal(boatIcon,gui);
        }
        else
        {
            for (Map.Entry<String, Color> color : colors.entrySet())
            {
                if (fisher.getTags().stream().filter(s -> s.equalsIgnoreCase(color.getKey())).findFirst().isPresent())
                    return new BoatPortrayal(colorImage(shipIcon, color.getValue()),gui);
            }
            return new BoatPortrayal(shipIcon,gui);

        }

    }


    public BufferedImage colorImage(BufferedImage old, Color newColor)
    {

        BufferedImage img = new BufferedImage(old.getColorModel(),old.copyData(null),old.isAlphaPremultiplied(),null);
        //grabbed from here: http://stackoverflow.com/questions/532586/change-a-specific-color-in-an-imageicon
        final int oldRGB = Color.black.getRGB();
        final int newRGB = newColor.getRGB();
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if (img.getRGB(x, y) == oldRGB)
                    img.setRGB(x, y, newRGB);
            }
        }

        return  img;


    }


    private static class BoatPortrayal extends ImagePortrayal2D{

        final FishGUI gui;
        public BoatPortrayal(Image image,FishGUI gui) {
            super(image);
            this.gui=gui;
        }


        @Override
        public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
            return wrapper == null ? null :
                    new MetaInspector(wrapper.getObject(), gui);
        }




    }



}
