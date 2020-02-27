/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.experiments.tuna;

import sim.field.geo.GeomGridField;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static java.util.stream.Collectors.joining;

public class FadPosForViz {
    private static final Path basePath = Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np");
    private static final Path scenarioPath = basePath.resolve(Paths.get("calibrations", "2019-12-13_2-all_targets", "tuna_calibrated.yaml"));
    private static final Path outputPath = basePath.resolve(Paths.get("runs", "fad_pos_for_viz"));

    public static void main(String[] args) throws IOException {

        FishState model = new FishState(System.currentTimeMillis());
        String scenarioString = String.join("\n", Files.readAllLines(scenarioPath));
        Scenario scenario = new FishYAML().loadAs(scenarioString, Scenario.class);

        FileWriter fileWriter = new FileWriter(outputPath.resolve("fad_pos_for_viz.csv").toFile());
        fileWriter.write("step,lon,lat\n");
        fileWriter.flush();

        model.setScenario(scenario);
        model.start();
        final FadMap fadMap = model.getFadMap();

        final GeomGridField field = model.getMap().getRasterBathymetry();
        final double minLon = field.MBR.getMinX();
        final double minLat = field.MBR.getMinY();
        final double maxLon = field.MBR.getMaxX();
        final double maxLat = field.MBR.getMaxY();
        final double maxX = field.getGridWidth();
        final double maxY = field.getGridHeight();

        @SuppressWarnings("unchecked") final Collection<Double2D> locations = fadMap.getField().doubleLocationHash.values();
        while (model.getYear() < 2) {
            fileWriter.write(locations.stream().map(loc -> {
                final double lon = minLon + (loc.x / maxX) * (maxLon - minLon);
                final double lat = maxLat - (loc.y / maxY) * (maxLat - minLat);
                return model.getStep() + "," + lon + "," + lat;
            }).collect(joining("\n")));
            fileWriter.flush();
            model.schedule.step(model);
        }
        fileWriter.close();
    }

}

