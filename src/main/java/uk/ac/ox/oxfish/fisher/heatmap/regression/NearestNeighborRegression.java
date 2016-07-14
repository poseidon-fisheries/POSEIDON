package uk.ac.ox.oxfish.fisher.heatmap.regression;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.DistanceFunction;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;


/**
 * Created by carrknight on 6/30/16.
 */
public class NearestNeighborRegression implements GeographicalRegression<Double> {


    /**
     * KdTree doing all the work
     */
    private final KdTree<Double> nearestNeighborTree = new KdTree<Double>(3);

    /**
     * how do we judge the distance between two nodes
     */
    private final DistanceFunction distanceFunction;

    private final int neighbors;


    public NearestNeighborRegression(int neighbors, double timeBandwidth, double spaceBandwidth)
    {
        this.neighbors=neighbors;
        this.distanceFunction = new DistanceFunction() {
            @Override
            public double distance(double[] obs1, double[] obs2) {

                double distance = 0;
                for(int i = 0; i < 2; i++) {
                    final double spaceDistance = obs1[i] - obs2[i];
                    if (spaceDistance != 0)
                        distance += (spaceDistance*spaceDistance)/spaceBandwidth;
                }
                final double timeDistance = Math.abs(obs1[2] - obs2[2]);
                distance += (timeDistance)/timeBandwidth;
                return distance;

            }

            @Override
            public double distanceToRect(double[] point, double[] min, double[] max) {
                double d = 0;

                for (int i = 0; i < 2; i++) {
                    double diff = 0;
                    if (point[i] > max[i]) {
                        diff = (point[i] - max[i]);
                    }
                    else if (point[i] < min[i]) {
                        diff = (point[i] - min[i]);
                    }
                    d += (diff * diff)/spaceBandwidth ;
                }
                double diff = 0;
                if (point[2] > max[2]) {
                    diff = (point[2] - max[2]);
                }
                else if (point[2] < min[2]) {
                    diff = (point[2] - min[2]);
                }
                d += Math.abs(diff) /timeBandwidth ;
                return d;
            }
        };
    }

    @Override
    public double predict(SeaTile tile, double time, FishState state, Fisher fisher) {

        if(tile.getAltitude()>=0)
            return Double.NaN;
        else
            return predict(tile.getGridX(),tile.getGridY(),time);
    }




    public double predict(int x, int y, double time) {

        if(nearestNeighborTree.size()<1)
            return 0;

        MaxHeap<Double> neighbors = nearestNeighborTree.findNearestNeighbors(new double[]{x, y, time}, this.neighbors,
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

        nearestNeighborTree.addPoint(new double[]{observation.getX(),observation.getY(),observation.getTime()},
                                     observation.getValue());

    }



}
