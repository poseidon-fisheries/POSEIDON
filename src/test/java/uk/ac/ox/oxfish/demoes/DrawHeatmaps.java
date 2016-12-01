package uk.ac.ox.oxfish.demoes;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicallyWeightedRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.GoodBadRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.*;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.*;
import uk.ac.ox.oxfish.geography.CartesianDistance;
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
    public static final FishState FISH_STATE = MovingTest.generateSimple10x10Map();


    public static void main(String[] args) throws Exception {
        distancePlot(new NearestNeighborRegression(
                1, new double[]{1, 1},
                (tile, timeOfObservation, agent, model) -> tile.getGridX(),
                (tile, timeOfObservation, agent, model) -> tile.getGridY()
        ),"nn_simple");

        distancePlot(new NearestNeighborRegression(
                1, new double[]{1},
                new PortDistanceExtractor(new ManhattanDistance(),null)
        ),"nn_port");


        distancePlot(new NearestNeighborRegression(
                2, new double[]{1, 1},
                (tile, timeOfObservation, agent, model) -> tile.getGridX(),
                (tile, timeOfObservation, agent, model) -> tile.getGridY()
        ),"nn_multiple");

        distancePlot(new NearestNeighborRegression(
                1, new double[]{1,1,1},
                (tile, timeOfObservation, agent,  model) -> tile.getGridX(),
                (tile, timeOfObservation, agent, model) -> tile.getGridY(),
                new PortDistanceExtractor(new ManhattanDistance(),null)
        ),"nn_both");


        distancePlot(new KernelRegression(
                100,
                new EpanechinikovKernel(0),
                new Pair<>((tile, timeOfObservation, agent,  model) -> tile.getGridX(), 30d),
                new Pair<>((tile, timeOfObservation, agent,  model) -> tile.getGridY(), 30d)
        ), "epa_simple");


        distancePlot(new KernelRegression(
                100,
                new EpanechinikovKernel(0),
                new Pair<>((tile, timeOfObservation, agent,  model) -> tile.getGridX(), 30d),
                new Pair<>((tile, timeOfObservation, agent,  model) -> tile.getGridY(), 30d)
        ), "epa_tilded");


        distancePlot(new KernelRegression(
                100,
                new EpanechinikovKernel(0),
                new Pair<>(new PortDistanceExtractor(new ManhattanDistance(), null), 30d)), "epa_port");



        distancePlot(new SimpleKalmanRegression(
                             10, 1, 40, 60, 50*50, .1, 0, 0, FISH_STATE.getMap(),
                             new MersenneTwisterFast(0)
                     ),
                     "kalman_simple");


        distancePlot(new KernelTransduction
                             (
                                     FISH_STATE.getMap(),
                                     1,
                                     new Pair<>(
                                             new GridXExtractor(),
                                             20d),
                                     new Pair<>(
                                             new GridYExtractor(),
                                             20d)
                             ),
                     "rbf_simple");

        distancePlot(new GeographicallyWeightedRegression(
                             FISH_STATE.getMap(),
                             1,
                             new ManhattanDistance(),
                             5,
                             new ObservationExtractor[0],
                             40,
                             60,
                             50*50,
                             new MersenneTwisterFast()),
                     "gwr_simple");



        distancePlot(new GoodBadRegression(
                             FISH_STATE.getMap(),
                             new CartesianDistance(1),
                             new MersenneTwisterFast(),
                             5,80,20,5,0),
                     "goodbad_simple");







    }



    public static  void distancePlot(final GeographicalRegression<Double> regression, final String fileName) throws Exception {


        Fisher mock = mock(Fisher.class);
        Port port = new Port("porto", FISH_STATE.getMap().getSeaTile(9, 9), null, 0.01);
        when(mock.getHomePort()).thenReturn(port);
        regression.addObservation(
                new GeographicalObservation<>(
                        FISH_STATE.getMap().getSeaTile(1, 1),
                        0,
                        100d
                ), mock,mock(FishState.class)
        );

        regression.addObservation(
                new GeographicalObservation<>(
                        FISH_STATE.getMap().getSeaTile(4, 5),
                        0,
                        30d
                ), mock,mock(FishState.class)
        );


        regression.addObservation(
                new GeographicalObservation<>(
                        FISH_STATE.getMap().getSeaTile(8, 6),
                        0,
                        5d
                ), mock,mock(FishState.class)
        );

        StringBuilder output = new StringBuilder("x,y,value").append("\n");
        for (int x = 0; x < 10; x++)
        {
            for (int y = 0; y < 10; y++) {
                output.append((x+1) + "," + (y+1) + "," + regression.predict(
                        FISH_STATE.getMap().getSeaTile(x, y), 0, mock,mock(FishState.class)
                ));
                output.append("\n");
            }

        }

        Files.write(MAIN_DIRECTORY.resolve(fileName + ".csv"), output.toString().getBytes());

    }
}
