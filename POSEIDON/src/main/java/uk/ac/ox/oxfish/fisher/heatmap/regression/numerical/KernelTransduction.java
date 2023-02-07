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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Iterative kernel regression, focusing on the seatiles you want to predict for should make the prediction
 * step a lot faster (even though the new observation step will be worse)
 * Created by carrknight on 7/8/16.
 */
public class KernelTransduction implements GeographicalRegression<Double> {


    private final HashMap<SeaTile,KernelTilePredictor> kernels;

    private final double forgettingFactor;

    public KernelTransduction(
            NauticalMap map,
            double forgettingFactor,
            Pair<ObservationExtractor,Double>... extractorsAndBandwidths) {

        this.forgettingFactor = forgettingFactor;
        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        kernels = new HashMap<>(tiles.size());
        for(SeaTile tile : tiles)
            kernels.put(tile,new KernelTilePredictor(forgettingFactor,tile, extractorsAndBandwidths));

    }

    /**
     * returns the current kernel prediction
     * @return
     */
    @Override
    public double predict(SeaTile tile, double time, Fisher fisher, FishState model) {

        KernelTilePredictor kernel = kernels.get(tile);
        if(kernel==null)
            return Double.NaN;
        else
            return kernel.getCurrentPrediction();

    }


    /**
     * returns the current geographical prediction
     * @param newObservation
     * @param fisher
     * @param model
     */
    @Override
    public void addObservation(GeographicalObservation newObservation, Fisher fisher, FishState model) {

        //go through all the tiles
        for(KernelTilePredictor kernel : kernels.values())
        {
           kernel.addObservation(newObservation,fisher, model);
        }
    }

    public double getForgettingFactor() {
        return forgettingFactor;
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
     *  The only hyper-parameter really is the forgetting value
     */
    @Override
    public double[] getParametersAsArray() {

        double[] bandwidths = kernels.values().iterator().next().getBandwidths();
        //check that they all have the same bandwidths!
        assert  kernels.values().stream().allMatch(
                kernelTile -> Arrays.equals(kernelTile.getBandwidths(),bandwidths));
        return bandwidths;

    }

    /**
     * receives and modifies the forgetting value
     */
    @Override
    public void setParameters(double[] parameterArray) {

        kernels.values().forEach(kernelTile -> kernelTile.setBandwidths(parameterArray));

    }

}
