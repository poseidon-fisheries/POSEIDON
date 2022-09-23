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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;

import org.apache.commons.collections15.map.UnmodifiableMap;
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
import java.util.*;
import java.util.List;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * Basically a transformer that changes color mapping according to species.
 * Created by carrknight on 4/22/15.
 */
public class ColorfulGrid extends FastObjectGridPortrayal2D {



    private Map<String,ColorEncoding> encodings;

    private ColorEncoding selected;

    private String selectedName;

    /**
     * the default encoder just returns altitude
     */

    private final ColorMap depthColor = new TriColorMap(-6000, 0, 6000,
                                                        Color.BLUE, Color.CYAN, Color.GREEN,
                                                        new Color(0,100,0));


    private final Queue<Color> defaultFishColors = new LinkedList<>();
    /**
     * the specie currently selected, no selection means depth
     */

    private List<ColorfulGridSwitcher> listeners = new LinkedList<>();

    /**
     * when drawing biomass use the transform of the current biomass rather than the biomass itself (to avoid large numbers dominating everything)
     */
    private final static Function<Double,Double> BIOMASS_TRANSFORM = aDouble -> Math.sqrt(aDouble);

    private final static double MAX_BIOMASS =  5000;

    @UiHidden
    private MersenneTwisterFast random;

    public ColorfulGrid(MersenneTwisterFast random)
    {
        encodings = new HashMap<>();
        this.random = random;
        //add the default color map showing depth
        encodings.put("Depth", new ColorEncoding(
                depthColor,
                seaTile -> seaTile.isProtected() ? Double.NaN : seaTile.getAltitude(), true));

        //add the default color map showing rocky areas
        encodings.put("Habitat",new ColorEncoding(
                new TriColorMap(-1,0,1,Color.black,new Color(237, 201, 175), new Color(69, 67, 67)),
                seaTile -> seaTile.isLand() ? Double.NaN : seaTile.getRockyPercentage(),
                true));

        setSelectedEncoding("Depth");

        defaultFishColors.add(Color.RED);
        defaultFishColors.add(Color.BLUE);
        defaultFishColors.add(Color.BLUE);
        defaultFishColors.add(Color.ORANGE);
        defaultFishColors.add(Color.YELLOW);
    }


    /**
     * create a colormap for each specie
     * @param biology
     * @param seaTiles
     */
    public void initializeGrid(GlobalBiology biology,
                               List<SeaTile> seaTiles)
    {

        double max = BIOMASS_TRANSFORM.apply(MAX_BIOMASS);
        for(Species species : biology.getSpecies())
        {
            max = Math.max(max,
                           BIOMASS_TRANSFORM.apply(
                                   seaTiles.stream().mapToDouble(value -> value.getBiomass(species)).
                                           filter(
                                                   Double::isFinite).max().orElse(MAX_BIOMASS)));
        }

        for(Species species : biology.getSpecies())
        {



            Color color =  defaultFishColors.size() == 0 ? Color.RED : defaultFishColors.poll();
            encodings.put(species.getName(), new SelfAdjustingColorEncoding(
                    new SimpleColorMap(0, max, Color.WHITE, color){

                        @Override
                        public boolean validLevel(double value) {
                            return true;
                        }

                        @Override
                        public int getRGB(double level) {
                            return getColor(level).getRGB();
                        }

                        @Override
                        public Color getColor(double level) {
                            if(Double.isFinite(level))
                                return super.getColor(level);
                            else
                                return Color.BLACK;

                        }
                    },
                    seaTile ->
                            seaTile.isLand() ? Double.NaN :
                            BIOMASS_TRANSFORM.apply(
                                    seaTile.getBiomass(species)),
                    false,
                    max,
                    0));
        };


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

        selectedName = encodingName;
        selected = encodings.get(encodingName);
        assert selected != null;
        this.setMap(selected.getMap());
        this.setImmutableField(selected.isImmutable());


    }


    public void addEnconding(String encodingName,ColorEncoding encoding)
    {

        Preconditions.checkArgument(!encodings.containsKey(encodingName), "Already present color encoding!");
        encodings.put(encodingName,encoding);

    }

    public void removeEncoding(String encodingName)
    {
        encodings.remove(encodingName);
    }



    @Override
    public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
        if(wrapper == null) {
            return null;
        } else {
            return new MetaInspector(wrapper.getObject(), ((FishGUI) state));
        }
    }


    /**
     * Getter for property 'encodings'.
     *
     * @return Value for property 'encodings'.
     */
    public ImmutableMap<String, ColorEncoding> getEncodings() {
        return ImmutableMap.copyOf(encodings);
    }


    public ColorEncoding put(String key, ColorEncoding value) {
        ColorEncoding put = encodings.put(key, value);
        for (ColorfulGridSwitcher listener : listeners) {
            listener.gridChanged();
        }

        return put;
    }

    /**
     * Removes the mapping for a key from this map if it is present
     * (optional operation).   More formally, if this map contains a mapping
     * from key {@code k} to value {@code v} such that
     * {@code Objects.equals(key, k)}, that mapping
     * is removed.  (The map can contain at most one such mapping.)
     *
     * <p>Returns the value to which this map previously associated the key,
     * or {@code null} if the map contained no mapping for the key.
     *
     * <p>If this map permits null values, then a return value of
     * {@code null} does not <i>necessarily</i> indicate that the map
     * contained no mapping for the key; it's also possible that the map
     * explicitly mapped the key to {@code null}.
     *
     * <p>The map will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     * @throws UnsupportedOperationException if the {@code remove} operation
     *         is not supported by this map
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this map
     * (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this
     *         map does not permit null keys
     * (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    public ColorEncoding remove(Object key) {
        ColorEncoding remove = encodings.remove(key);
        for (ColorfulGridSwitcher listener : listeners) {
            listener.gridChanged();
        }
        return remove;
    }

    /**
     * Removes all of the mappings from this map (optional operation).
     * The map will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the {@code clear} operation
     *         is not supported by this map
     */
    public void clear() {
        encodings.clear();
        for (ColorfulGridSwitcher listener : listeners) {
            listener.gridChanged();
        }
    }

    public String getSelectedName() {
        return selectedName;
    }

    public void addListener(ColorfulGridSwitcher switcher){
        listeners.add(switcher);
    }
}
