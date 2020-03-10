/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.data.jsonexport.heatmap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.OutputPlugin;

import java.util.LinkedList;
import java.util.function.Function;

/**
 * object that is responsible to fill the JsonHeatmap over time
 */
public class JsonHeatmapManager implements OutputPlugin, Steppable, AdditionalStartable {


    final private JsonHeatmap jsonHeatmap;

    final private String fileName;

    final private Function<SeaTile,Double> numericExtractor;

//todo will have to add an averager here!


    public JsonHeatmapManager(
            String heatmapTitle,
            String fileName, Function<SeaTile, Double> numericExtractor) {
        this.fileName = fileName;
        this.jsonHeatmap = new JsonHeatmap();
        jsonHeatmap.setName(heatmapTitle);
        jsonHeatmap.setTimesteps(new LinkedList<>());
        this.numericExtractor = numericExtractor;
    }

    @Override
    public void step(SimState simState) {


        FishState model = (FishState) simState;
        int height = model.getMap().getHeight();
        int width = model.getMap().getWidth();

        double[][] grid = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                grid[x][y]=numericExtractor.apply(model.getMap().getSeaTile(x,y));

            }
        }
        jsonHeatmap.getTimesteps().add(
                new JsonTimestepGrid(
                        grid,
                        model.getDay()
                )
        );
    }


    /**
     * stores map info and schedules itself
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {




        NauticalMap map = model.getMap();
        int width = map.getWidth();
        int height = map.getHeight();
        jsonHeatmap.setCentroidsLatitude(new double[width][height]);
        jsonHeatmap.setCentroidsLongitude(new double[width][height]);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                Coordinate coordinates = map.getCoordinates(x, y);
                jsonHeatmap.getCentroidsLatitude()[x][y] = coordinates.y;
                jsonHeatmap.getCentroidsLongitude()[x][y] = coordinates.x;

            }
        }
        model.scheduleEveryDay(this,
                               //after daily collectors?
                               StepOrder.YEARLY_DATA_GATHERING);

        model.getOutputPlugins().add(this);


    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String composeFileContents() {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonHeatmap);
    }

    @Override
    public void reactToEndOfSimulation(FishState state) {
        //nada
    }
}
