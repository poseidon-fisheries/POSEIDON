/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.List;

/**
 * Rather than building a k-d tree to search all the time why don't we just have a map of seatiles we are studying and keep
 * a map seatile---> nearest neighbor associated with it? It might not work for observations with additional information (like habitat and so on)
 * That's the idea here
 * Created by carrknight on 7/5/16.
 */
public class NearestNeighborTransduction implements GeographicalRegression<Double> {


    private final static GeographicalObservation<Double> PLACEHOLDER = new GeographicalObservation<Double>(null,
        -1d,
        Double.NaN);
    private final HashMap<SeaTile, GeographicalObservation<Double>> closestNeighborForNow;
    /**
     * functions used to turn an observation into a double[]
     */
    private final ObservationExtractor[] extractors;

    /**
     * divide each feature distance by this to reweight them
     */
    private double[] bandwidths;

    /**
     * object that weights the distance of observations by their bandwidth
     */
    private RegressionDistance transformer;


    public NearestNeighborTransduction(
        NauticalMap map,
        ObservationExtractor[] extractors, double[] bandwidths,
        RegressionDistance transformer
    ) {

        Preconditions.checkArgument(bandwidths.length > 0);
        Preconditions.checkArgument(bandwidths.length == extractors.length);

        this.extractors = extractors;
        this.bandwidths = bandwidths;
        this.transformer = transformer;
        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        closestNeighborForNow = new HashMap<>(tiles.size());
        for (SeaTile tile : tiles)
            closestNeighborForNow.put(tile, PLACEHOLDER);


    }

    /**
     * returns stored closest best
     *
     * @return
     */
    @Override
    public double predict(SeaTile tile, double time, Fisher fisher, FishState model) {

        return closestNeighborForNow.getOrDefault(tile, PLACEHOLDER).getValue();
    }


    @Override
    public void addObservation(GeographicalObservation<Double> newObservation, Fisher fisher, FishState model) {

        //go through all the tiles
        for (SeaTile tile : closestNeighborForNow.keySet()) {
            //if the new observation is closer than the old one this is your new closest observation
            GeographicalObservation<Double> oldObservation = closestNeighborForNow.get(tile);
            if (oldObservation == PLACEHOLDER || (
                distance(fisher, tile, newObservation.getTime(), model, newObservation) <
                    distance(fisher, tile, newObservation.getTime(), model, oldObservation)))
                closestNeighborForNow.put(tile, newObservation);
        }
    }


    private double distance(
        Fisher fisher, SeaTile tile, double time, FishState model, GeographicalObservation<Double> observation
    ) {
        double distance = 0;
        for (int i = 0; i < bandwidths.length; i++) {
            transformer.setBandwidth(bandwidths[i]);
            distance += transformer.distance(
                extractors[i].extract(tile, time, fisher, model),
                extractors[i].extract(observation.getTile(), observation.getTime(), fisher, model)
            );


        }

        return distance;


    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model, Fisher fisher) {

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
        GeographicalObservation<Double> observation, Fisher fisher
    ) {
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
        return bandwidths;
    }

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     *
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(double[] parameterArray) {
        bandwidths = parameterArray;
    }
}
