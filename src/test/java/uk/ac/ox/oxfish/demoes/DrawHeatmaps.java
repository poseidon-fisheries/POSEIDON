package uk.ac.ox.oxfish.demoes;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.PortDistanceExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.*;
import uk.ac.ox.oxfish.geography.ManhattanDistance;
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
public class DrawHeatmaps
{

    public static final Path MAIN_DIRECTORY = Paths.get("docs", "paper2","heatmap_examples");


    public static void main(String[] args) throws Exception {
        distancePlot(new NearestNeighborRegression(
                1, new double[]{1, 1},
                (tile, timeOfObservation, agent) -> tile.getGridX(),
                (tile, timeOfObservation, agent) -> tile.getGridY()
        ),"nn_simple");

        distancePlot(new NearestNeighborRegression(
                1, new double[]{1},
                new PortDistanceExtractor(new ManhattanDistance(),null)
        ),"nn_port");



        distancePlot(new NearestNeighborRegression(
                1, new double[]{1,1,1},
                (tile, timeOfObservation, agent) -> tile.getGridX(),
                (tile, timeOfObservation, agent) -> tile.getGridY(),
                new PortDistanceExtractor(new ManhattanDistance(),null)
        ),"nn_both");


        distancePlot(new KernelRegression(
                100,
                new Pair<>((tile, timeOfObservation, agent) -> tile.getGridX(), 10d),
                new Pair<>((tile, timeOfObservation, agent) -> tile.getGridY(), 10d)
        ), "epa_simple");


        distancePlot(new KernelRegression(
                100,
                new Pair<>((tile, timeOfObservation, agent) -> tile.getGridX(), 30d),
                new Pair<>((tile, timeOfObservation, agent) -> tile.getGridY(), 30d)
        ), "epa_tilded");


        distancePlot(new KernelRegression(
                100,
                new Pair<>(new PortDistanceExtractor(new ManhattanDistance(), null), 30d)), "epa_port");



    }



    public static  void distancePlot(final GeographicalRegression<Double> regression, final String fileName) throws Exception {



        FishState state = MovingTest.generateSimple10x10Map();

        Fisher mock = mock(Fisher.class);
        Port port = new Port("porto",state.getMap().getSeaTile(9,9),null,0.01);
        when(mock.getHomePort()).thenReturn(port);
        regression.addObservation(
                new GeographicalObservation<>(
                        state.getMap().getSeaTile(1, 1),
                        0,
                        100d
                ), mock
        );

        regression.addObservation(
                new GeographicalObservation<>(
                        state.getMap().getSeaTile(4, 5),
                        0,
                        30d
                ), mock
        );


        regression.addObservation(
                new GeographicalObservation<>(
                        state.getMap().getSeaTile(8, 6),
                        0,
                        5d
                ), mock
        );

        StringBuilder output = new StringBuilder("x,y,value").append("\n");
        for (int x = 0; x < 10; x++)
        {
            for (int y = 0; y < 10; y++) {
                output.append(x + "," + y + "," + regression.predict(
                        state.getMap().getSeaTile(x, y), 0, mock
                ));
                output.append("\n");
            }

        }

        Files.write(MAIN_DIRECTORY.resolve(fileName + ".csv"), output.toString().getBytes());

    }
}
