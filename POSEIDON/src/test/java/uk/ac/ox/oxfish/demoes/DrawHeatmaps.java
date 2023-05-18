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

package uk.ac.ox.oxfish.demoes;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicallyWeightedRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.GoodBadRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.EpanechinikovKernel;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridXExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridYExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PortDistanceExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.*;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.ManhattanDistance;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 9/1/16.
 */
public class DrawHeatmaps {

    public static final Path MAIN_DIRECTORY = Paths.get("docs", "paper2", "heatmap_examples");
    public static final FishState FISH_STATE = MovingTest.generateSimple10x10MapWithVaryingDepth();


    public static void main(String[] args) throws Exception {
        distancePlot(new NearestNeighborRegression(
            1, new double[]{1, 1},
            new GridXExtractor(),
            new GridYExtractor()
        ), "nn_simple");

        distancePlot(new NearestNeighborRegression(
            1, new double[]{1},
            new PortDistanceExtractor(new ManhattanDistance(), 1d)
        ), "nn_port");


        distancePlot(new NearestNeighborRegression(
            2, new double[]{1, 1},
            new GridXExtractor(),
            new GridYExtractor()
        ), "nn_multiple");

        distancePlot(new NearestNeighborRegression(
            1, new double[]{1, 1, 1},
            new GridXExtractor(),
            new GridYExtractor(),
            new PortDistanceExtractor(new ManhattanDistance(), 1d)
        ), "nn_both");


        distancePlot(new KernelRegression(
            100,
            new EpanechinikovKernel(0),
            new Pair<>(new GridXExtractor(), 30d),
            new Pair<>(new GridYExtractor(), 30d)
        ), "epa_simple");


        distancePlot(new KernelRegression(
            100,
            new EpanechinikovKernel(0),
            new Pair<>(new GridXExtractor(), 30d),
            new Pair<>(new GridYExtractor(), 30d)
        ), "epa_tilded");


        distancePlot(new KernelRegression(
            100,
            new EpanechinikovKernel(0),
            new Pair<>(new PortDistanceExtractor(new ManhattanDistance(), 1d), 30d)
        ), "epa_port");


        distancePlot(
            new SimpleKalmanRegression(
                10, 1, 40, 60, 50 * 50, .1, 0, 0, FISH_STATE.getMap(),
                new MersenneTwisterFast(0)
            ),
            "kalman_simple"
        );


        distancePlot(
            new KernelTransduction
                (
                    FISH_STATE.getMap(),
                    1,
                    new Pair<>(
                        new GridXExtractor(),
                        20d
                    ),
                    new Pair<>(
                        new GridYExtractor(),
                        20d
                    )
                ),
            "rbf_simple"
        );

        distancePlot(
            new GeographicallyWeightedRegression(
                FISH_STATE.getMap(),
                1,
                new ManhattanDistance(),
                5,
                new ObservationExtractor[0],
                40,
                60,
                50 * 50,
                new MersenneTwisterFast()
            ),
            "gwr_simple"
        );


        distancePlot(
            new GoodBadRegression(
                FISH_STATE.getMap(),
                new CartesianDistance(1),
                new MersenneTwisterFast(),
                5, 80, 20, 5, 0
            ),
            "goodbad_simple"
        );


    }


    public static void distancePlot(
        final GeographicalRegression<Double> regression,
        final String fileName
    ) throws Exception {


        Fisher mock = mock(Fisher.class);
        Port port = new Port("porto", FISH_STATE.getMap().getSeaTile(9, 9), null, 0.01);
        when(mock.getHomePort()).thenReturn(port);
        regression.addObservation(
            new GeographicalObservation<>(
                FISH_STATE.getMap().getSeaTile(1, 1),
                0,
                100d
            ), mock, mock(FishState.class)
        );

        regression.addObservation(
            new GeographicalObservation<>(
                FISH_STATE.getMap().getSeaTile(4, 5),
                0,
                30d
            ), mock, mock(FishState.class)
        );


        regression.addObservation(
            new GeographicalObservation<>(
                FISH_STATE.getMap().getSeaTile(8, 6),
                0,
                5d
            ), mock, mock(FishState.class)
        );

        StringBuilder output = new StringBuilder("x,y,value").append("\n");
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                output.append((x + 1) + "," + (y + 1) + "," + regression.predict(
                    FISH_STATE.getMap().getSeaTile(x, y), 0, mock, mock(FishState.class)
                ));
                output.append("\n");
            }

        }

        Files.write(MAIN_DIRECTORY.resolve(fileName + ".csv"), output.toString().getBytes());

    }
}
