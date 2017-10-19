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

    private final BufferedImage canoeIcon;

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
        canoeIcon= ImageIO.read(FishGUI.IMAGES_PATH.resolve("canoe.png").toFile());
        this.gui=gui;
    }




    public BoatPortrayal build(Fisher fisher)
    {

        BufferedImage correctImage;
        if(fisher.getTags().contains("ship"))
            correctImage = shipIcon;
        else if(fisher.getTags().contains("canoe"))
            correctImage = canoeIcon;
        else
            correctImage = shipIcon;

        for (Map.Entry<String, Color> color : colors.entrySet()) {
            if (fisher.getTags().contains(color.getKey()))
                return new BoatPortrayal(colorImage(correctImage, color.getValue()), gui);
        }
        //there is no color
        return new BoatPortrayal(correctImage,gui);




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
