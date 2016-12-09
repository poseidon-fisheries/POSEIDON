package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.geography.CentroidMapDiscretizer;
import uk.ac.ox.oxfish.geography.MapDiscretization;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.CsvColumnsToLists;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Locker;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Supplier;

/**
 * Steve saul's stuff, initialized here.
 * Created by carrknight on 12/6/16.
 */
public class FloridaLogitDestinationFactory implements AlgorithmFactory<LogitDestinationStrategy> {


    /**
     * file containing all the betas
     */
    private String coefficientsFile =
            FishStateUtilities.getAbsolutePath(
                    Paths.get("temp_wfs", "longline.csv").toString());


    private String coefficientsStandardDeviationFile =
            FishStateUtilities.getAbsolutePath(
                    Paths.get("temp_wfs", "longline_std.csv").toString());

    /**
     * file containing all the centroids
     */
    private String centroidFile =
            FishStateUtilities.getAbsolutePath(
                    Paths.get("temp_wfs", "areas.txt").toString());


    //



    /**
     * everybody shares the parent same destination logit strategy
     */
    private Locker<FishState,LogitDestinationStrategy> uberStrategy = new Locker<>();
    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public LogitDestinationStrategy apply(FishState state) {


        LogitDestinationStrategy parent = uberStrategy.presentKey(state, new Supplier<LogitDestinationStrategy>() {
            @Override
            public LogitDestinationStrategy get() {

                MersenneTwisterFast random = state.getRandom();

                MapDiscretization discretization = createDiscretization(state, centroidFile);
                CsvColumnsToLists reader = new CsvColumnsToLists(
                        coefficientsFile,
                        ',',
                        new String[]{"area", "intercept", "distance", "habit", "fuel_price", "wind_speed"}
                );
                LinkedList<Double>[] lists = reader.readColumns();
                reader = new CsvColumnsToLists(
                        coefficientsStandardDeviationFile,
                        ',',
                        new String[]{"area", "intercept", "distance", "habit", "fuel_price", "wind_speed"}
                );
                LinkedList<Double>[] std =  reader.readColumns();
                LinkedList<Integer> rowNames = new LinkedList<Integer>();
                double[][] betas = new double[lists[0].size()][];
                for (int i = 0; i < lists[0].size(); i++) {
                    //record which site this belongs to
                    rowNames.add(lists[0].get(i).intValue());
                    //record its coordinates
                    betas[i] = new double[lists.length - 1];
                    for (int j = 1; j < lists.length; j++)
                        betas[i][j - 1] = lists[j].get(i) + random.nextGaussian() * std[j].get(i) ;

                }
                ObservationExtractor[][] extractors = new ObservationExtractor[betas.length][];
                ObservationExtractor[] commonExtractor =
                        new ObservationExtractor[]{
                                //intercept
                                (tile, timeOfObservation, agent, model) -> 1d,
                                //distance
                                (tile, timeOfObservation, agent, model) -> {
                                    //it's in miles!
                                    return 0.621371 * model.getMap().distance(
                                            agent.getHomePort().getLocation(), tile);
                                },
                                //habit
                                (tile, timeOfObservation, agent, model) -> {
                                    //it it has been less than 90 days since you went there, you get the habit bonus!
                                    return  model.getDay() -
                                            ((int[])agent.remember(LogitDestinationStrategy.MEMORY_KEY))[discretization.getGroup(tile)] < 90 ?
                                            1 : 0;
                                },
                                //fuel_price TODO: gas per liter from the logbook
                                (tile, timeOfObservation, agent, model) ->
                                        agent.getHomePort().getGasPricePerLiter(),
                                //wind_speed
                                (tile, timeOfObservation, agent, model) -> tile.getWindSpeedInKph()
                        };
                for (int i = 0; i < extractors.length; i++)
                    extractors[i] = commonExtractor;

                return new LogitDestinationStrategy(betas, extractors, rowNames, discretization,
                                                    new FavoriteDestinationStrategy(state.getMap(), random),
                                                    random);

            }
        });

        return new LogitDestinationStrategy(parent,new FavoriteDestinationStrategy(state.getMap(), state.getRandom()));


    }

    private static MapDiscretization createDiscretization(FishState state, String centroidFile) {
        CsvColumnsToLists reader = new CsvColumnsToLists(
                centroidFile,
                ',',
                new String[]{"eastings", "northings"}
        );

        LinkedList<Double>[] lists = reader.readColumns();
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < lists[0].size(); i++)
            coordinates.add(new Coordinate(lists[0].get(i),
                                           lists[1].get(i),
                                           0));

        CentroidMapDiscretizer discretizer = new CentroidMapDiscretizer(
                coordinates);
        MapDiscretization discretization = new MapDiscretization(
                discretizer);
        discretization.discretize(state.getMap());
        return discretization;
    }

    public String getCoefficientsFile() {
        return coefficientsFile;
    }

    public void setCoefficientsFile(String coefficientsFile) {
        this.coefficientsFile = coefficientsFile;
    }

    public String getCoefficientsStandardDeviationFile() {
        return coefficientsStandardDeviationFile;
    }

    public void setCoefficientsStandardDeviationFile(String coefficientsStandardDeviationFile) {
        this.coefficientsStandardDeviationFile = coefficientsStandardDeviationFile;
    }

    public String getCentroidFile() {
        return centroidFile;
    }

    public void setCentroidFile(String centroidFile) {
        this.centroidFile = centroidFile;
    }
}
