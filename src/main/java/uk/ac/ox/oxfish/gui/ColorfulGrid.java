package uk.ac.ox.oxfish.gui;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.portrayal.grid.FastObjectGridPortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.gui.SimpleColorMap;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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

    /**
     * the specie currently selected, no selection means depth
     */
    private Specie selectedSpecie = null;

    private int maxBiomass = 5000;

    private MersenneTwisterFast random;

    public ColorfulGrid(MersenneTwisterFast random)
    {
        colors = new HashMap<>();
        this.random = random;
        setSelectedSpecie(null);


    }

    /**
     * turn the seatile into a double that can be coded into color by the portrayal
     * @param tile the seatile
     * @return a double that should be drawn by the color map
     */
    public double encodeSeaTile(SeaTile tile)
    {
        if(selectedSpecie == null)
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

            colors.putIfAbsent(selectedSpecie, new Color(random.nextInt(256),
                                                                          random.nextInt(256),
                                                                          random.nextInt(256)));
            this.setMap(new SimpleColorMap(0, maxBiomass,Color.WHITE,
                                           colors.get(selectedSpecie)));
            this.setImmutableField(false);

        }
    }


}
