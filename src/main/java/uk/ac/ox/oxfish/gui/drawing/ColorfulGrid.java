package uk.ac.ox.oxfish.gui.drawing;

import ec.util.MersenneTwisterFast;
import org.metawidget.inspector.annotation.UiHidden;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.grid.FastObjectGridPortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.gui.SimpleColorMap;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.MetaInspector;
import uk.ac.ox.oxfish.gui.TriColorMap;

import java.awt.*;
import java.util.*;

/**
 * Basically a transformer that changes color mapping according to species.
 * Created by carrknight on 4/22/15.
 */
public class ColorfulGrid extends FastObjectGridPortrayal2D {



    private Map<Specie,Color> colors;

    /**
     * the default encoder just returns altitude
     */

    private final ColorMap depthColor = new TriColorMap(-6000, 0, 6000, Color.BLUE, Color.CYAN, Color.GREEN, Color.RED);


    private final Queue<Color> defaultFishColors = new LinkedList<>();
    /**
     * the specie currently selected, no selection means depth
     */
    private Specie selectedSpecie = null;

    private int maxBiomass = 5000;

    @UiHidden
    private MersenneTwisterFast random;

    public ColorfulGrid(MersenneTwisterFast random)
    {
        colors = new HashMap<>();
        this.random = random;
        setSelectedSpecie(null);


        defaultFishColors.add(Color.RED);
        defaultFishColors.add(Color.BLUE);
    }

    /**
     * turn the seatile into a double that can be coded into color by the portrayal
     * @param tile the seatile
     * @return a double that should be drawn by the color map
     */
    public double encodeSeaTile(SeaTile tile)
    {
        if(selectedSpecie == null)
            if(tile.isProtected())
                return Double.NaN;
            else
                return tile.getAltitude();
        else
            return tile.getBiomass(selectedSpecie);
    }

    @Override
    public double doubleValue(Object obj) {
        return encodeSeaTile((SeaTile) obj);
    }

    /**
     * set the correct transform
     * @param selectedSpecie the species to show, null means bathymetry
     */
    public void setSelectedSpecie(Specie selectedSpecie) {
        this.selectedSpecie = selectedSpecie;
        if(selectedSpecie == null) {
            this.setMap(depthColor);
            this.setImmutableField(true);
        }
        else {

        /*    colors.putIfAbsent(selectedSpecie, new Color(random.nextInt(256),
                                                         random.nextInt(256),
                                                         random.nextInt(256)));
                                                         */
            colors.putIfAbsent(selectedSpecie, defaultFishColors.size() == 0 ? Color.RED : defaultFishColors.poll());
            this.setMap(new SimpleColorMap(0, maxBiomass,Color.WHITE,
                                           colors.get(selectedSpecie)));
            this.setImmutableField(false);

        }
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
