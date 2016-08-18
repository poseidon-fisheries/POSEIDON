package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.DistanceFunction;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;


/**
 * Created by carrknight on 6/30/16.
 */
public class NearestNeighborRegression implements NumericalGeographicalRegression {


    /**
     * KdTree doing all the work
     */
    private final KdTree<Double> nearestNeighborTree;



    /**
     * functions used to turn an observation into a double[]
     */
    private final ObservationExtractor[] extractors;

    /**
     * divide each feature distance by this to reweight them
     */
    private final double[] bandwidths;

    /**
     * how do we judge the distance between two nodes
     */
    private final DistanceFunction distanceFunction;

    /**
     * how many neighbors to use
     */
    private final int neighbors;


    public NearestNeighborRegression(int neighbors, double[] bandwidths, ObservationExtractor... extractors)
    {
        Preconditions.checkArgument(bandwidths.length > 0);
        Preconditions.checkArgument(bandwidths.length  == extractors.length);
        this.extractors = extractors;
        this.bandwidths = bandwidths;
        this.neighbors = neighbors;
        this.nearestNeighborTree = new KdTree<Double>(bandwidths.length);
        //distance is always absolute difference divided bandwidth
        this.distanceFunction =  new DistanceFunction() {
            @Override
            public double distance(double[] obs1, double[] obs2) {

                double distance = 0;
                for(int i = 0; i < obs1.length; i++)
                {
                    final double featureDistance = Math.abs(obs1[i] - obs2[i]);
                    distance += featureDistance / bandwidths[i];
                }
                return distance;

            }

            @Override
            public double distanceToRect(double[] observation,
                                         double[] min,
                                         double[] max) {
                double distance = 0;
                for(int i = 0; i < observation.length; i++)
                {
                    double diff = 0;
                    if (observation[i] > max[i]) {
                        diff = (observation[i] - max[i]);
                    }
                    else if (observation[i] < min[i]) {
                        diff = (observation[i] - min[i]);
                    }
                    distance += Math.abs(diff)/bandwidths[i] ;
                }
                return distance;
            }
        };


    }


    /**
     * creates a nearest neighbor regression caring only about space and time
     */
    public NearestNeighborRegression(int neighbors, double timeBandwidth, double spaceBandwidth)
    {
        this(neighbors, new double[]{spaceBandwidth, spaceBandwidth, timeBandwidth},
             new ObservationExtractor() {
                 @Override
                 public double extract(SeaTile tile, double timeOfObservation, Fisher agent) {
                     return tile.getGridX();
                 }
             },
             new ObservationExtractor() {
                 @Override
                 public double extract(SeaTile tile, double timeOfObservation, Fisher agent) {
                     return tile.getGridY();
                 }
             },
             new ObservationExtractor() {
                 @Override
                 public double extract(SeaTile tile, double timeOfObservation, Fisher agent) {
                     return timeOfObservation;
                 }
             }
             );
    }

    @Override
    public double predict(SeaTile tile, double time, FishState state, Fisher fisher) {

        if(tile.getAltitude()>=0)
            return Double.NaN;
        else
            return predict(ObservationExtractor.convertToFeatures(tile, time, fisher, extractors));
    }


    public double predict(double... observation) {

        if(nearestNeighborTree.size()<1)
            return 0;

        MaxHeap<Double> neighbors = nearestNeighborTree.findNearestNeighbors(observation, this.neighbors,
                                                                             distanceFunction);

        double prediction = 0;
        double size = neighbors.size();
        while(neighbors.size()>0) {
            prediction += neighbors.getMax();
            neighbors.removeMax();
        }
        if(size>0)
            prediction= prediction/size;
        return  prediction;



    }

    @Override
    public void addObservation(GeographicalObservation<Double> observation, Fisher fisher) {

        nearestNeighborTree.addPoint(ObservationExtractor.convertToFeatures(observation.getTile(),
                                                                            observation.getTime(),
                                                                            fisher, extractors),
                                     observation.getValue());


    }

    //ignored

    @Override
    public void start(FishState model) {

    }

    //ignored

    @Override
    public void turnOff() {

    }



}
