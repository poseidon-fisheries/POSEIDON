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

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.DistanceFunction;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.AbsoluteRegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridXExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridYExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationTimeExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;


/**
 * KD-tree based regression.
 * Created by carrknight on 6/30/16.
 */
public class NearestNeighborRegression implements GeographicalRegression<Double> {


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
    private double[] bandwidths;

    /**
     * how do we judge the distance between two nodes
     */
    private DistanceFunction treeDistance;

    /**
     * object that weights the distance of observations by their bandwidth
     */
    private RegressionDistance transformer;

    /**
     * how many neighbors to use
     */
    private int neighbors;


    public NearestNeighborRegression(int neighbors, double[] bandwidths, RegressionDistance distance,
                                     ObservationExtractor... extractors)
    {
        Preconditions.checkArgument(bandwidths.length > 0);
        Preconditions.checkArgument(bandwidths.length  == extractors.length);
        this.transformer = distance;
        this.extractors = extractors;
        this.bandwidths = bandwidths;
        this.neighbors = neighbors;
        this.nearestNeighborTree = new KdTree<Double>(bandwidths.length);
        //distance is always absolute difference divided bandwidth
        rebuildDistanceFunction(bandwidths);


    }


    public NearestNeighborRegression(int neighbors, double[] bandwidths, ObservationExtractor... extractors)
    {
       this(neighbors,bandwidths,new AbsoluteRegressionDistance(0),extractors);


    }

    public void rebuildDistanceFunction(final double[] bandwidths) {
        this.treeDistance =  new DistanceFunction() {
            @Override
            public double distance(double[] obs1, double[] obs2) {

                double distance = 0;
                for(int i = 0; i < obs1.length; i++)
                {
                    transformer.setBandwidth(bandwidths[i]);
                    distance += transformer.distance(obs1[i],obs2[i]);
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
                    transformer.setBandwidth(bandwidths[i]);
                    double diff = 0;
                    if (observation[i] > max[i]) {
                        diff = transformer.distance(observation[i],max[i]);
                    }
                    else if (observation[i] < min[i]) {
                        diff = transformer.distance(observation[i],min[i]);
                    }
                    distance += diff;
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
             new GridXExtractor(),
             new GridYExtractor(),
             new ObservationTimeExtractor()
             );
    }

    @Override
    public double predict(SeaTile tile, double time, Fisher fisher, FishState model) {

        if (tile.isLand())
            return Double.NaN;
        else
            return predict(ObservationExtractor.convertToFeatures(tile, time, fisher, extractors, model));
    }


    public double predict(double... observation) {

        if(nearestNeighborTree.size()<1)
            return 0;

        MaxHeap<Double> neighbors = nearestNeighborTree.findNearestNeighbors(observation, this.neighbors,
                                                                             treeDistance);

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
    public void addObservation(GeographicalObservation<Double> observation, Fisher fisher, FishState model) {

        nearestNeighborTree.addPoint(ObservationExtractor.convertToFeatures(observation.getTile(),
                                                                            observation.getTime(),
                                                                            fisher, extractors,
                                                                            model),
                                     observation.getValue());


    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model,Fisher fisher) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff(Fisher fisher) {

    }

    /**
     * It's already a double so return it!
     */
    @Override
    public double extractNumericalYFromObservation(
            GeographicalObservation<Double> observation, Fisher fisher) {
        return observation.getValue();
    }


    /**
     * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
     * from the outside without knowing the inner workings of the regression
     *
     * @return an array containing all the parameters of the model
     */
    @Override
    public double[] getParametersAsArray() {
        double[] parameters = new double[bandwidths.length+1];
        System.arraycopy(bandwidths,0,parameters,0,bandwidths.length);
        parameters[parameters.length-1] = neighbors;
        return
            parameters;
    }

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     *
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(double[] parameterArray) {
        assert parameterArray.length == this.bandwidths.length+1;
        for(int i=0; i<bandwidths.length; i++)
            this.bandwidths[i] = parameterArray[i];
        neighbors = Math.max(1,(int) parameterArray[parameterArray.length-1]);
        rebuildDistanceFunction(bandwidths);
    }
}
