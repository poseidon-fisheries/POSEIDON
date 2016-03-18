package uk.ac.ox.oxfish.gui.drawing;

import ec.util.MersenneTwisterFast;
import org.metawidget.inspector.annotation.UiHidden;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.grid.FastObjectGridPortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.gui.SimpleColorMap;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.MetaInspector;
import uk.ac.ox.oxfish.gui.TriColorMap;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Basically a transformer that changes color mapping according to species.
 * Created by carrknight on 4/22/15.
 */
public class ColorfulGrid extends FastObjectGridPortrayal2D {



    private Map<String,ColorEncoding> encodings;

    private ColorEncoding selected;

    /**
     * the default encoder just returns altitude
     */

    private final ColorMap depthColor = new TriColorMap(-6000, 0, 6000, Color.BLUE, Color.CYAN, Color.GREEN, Color.RED);


    private final Queue<Color> defaultFishColors = new LinkedList<>();
    /**
     * the specie currently selected, no selection means depth
     */

    private int maxBiomass = 500000;

    @UiHidden
    private MersenneTwisterFast random;

    public ColorfulGrid(MersenneTwisterFast random)
    {
        encodings = new HashMap<>();
        this.random = random;
        //add the default color map showing depth
        encodings.put("Depth", new ColorEncoding(
                new TriColorMap(-6000, 0, 6000, Color.BLUE, Color.CYAN, Color.GREEN, Color.RED),
                seaTile -> seaTile.isProtected() ? Double.NaN : seaTile.getAltitude(), true));

        //add the default color map showing rocky areas
        encodings.put("Habitat",new ColorEncoding(
                new TriColorMap(-1,0,1,Color.black,new Color(237, 201, 175), new Color(69, 67, 67)),
                seaTile -> seaTile.getAltitude() >=0 ? Double.NaN : seaTile.getRockyPercentage(),
                true));

        setSelectedEncoding("Depth");

        defaultFishColors.add(Color.RED);
        defaultFishColors.add(Color.BLUE);
        defaultFishColors.add(Color.ORANGE);
        defaultFishColors.add(Color.YELLOW);
    }


    /**
     * create a colormap for each specie
     * @param biology
     */
    public void initializeGrid(GlobalBiology biology)
    {

        for(Species species : biology.getSpecies())
        {
            Color color =  defaultFishColors.size() == 0 ? Color.RED : defaultFishColors.poll();
            encodings.put(species.getName(), new ColorEncoding(new SimpleColorMap(0, maxBiomass, Color.WHITE, color),
                                                           seaTile -> seaTile.getBiomass(species), false));
        }

    }


    /**
     * turn the seatile into a double that can be coded into color by the portrayal
     * @param tile the seatile
     * @return a double that should be drawn by the color map
     */
    public double encodeSeaTile(SeaTile tile)
    {

        return selected.getEncoding().apply(tile);
    }

    @Override
    public double doubleValue(Object obj) {
        return encodeSeaTile((SeaTile) obj);
    }

    /**
     * set the correct transform
     * @param encodingName the name of the correct encoding
     */
    public void setSelectedEncoding(String encodingName) {

        selected = encodings.get(encodingName);
        assert selected != null;
        this.setMap(selected.getMap());
        this.setImmutableField(selected.isImmutable());


    }




    @Override
    public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
        if(wrapper == null) {
            return null;
        } else {
            return new MetaInspector(wrapper.getObject(), ((FishGUI) state));
        }
    }

}
