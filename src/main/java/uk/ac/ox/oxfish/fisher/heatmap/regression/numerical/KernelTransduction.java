package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RegressionDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.List;

/**
 * Iterative kernel regression, focusing on the seatiles you want to predict for should make the prediction
 * step a lot faster (even though the new observation step will be worse)
 * Created by carrknight on 7/8/16.
 */
public class KernelTransduction implements NumericalGeographicalRegression {


    private final HashMap<SeaTile,KernelTilePredictor> kernels;

    private final double forgettingFactor;

    public KernelTransduction(
            NauticalMap map,
            double forgettingFactor,
            RegressionDistance... distance) {

        this.forgettingFactor = forgettingFactor;
        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        kernels = new HashMap<>(tiles.size());
        for(SeaTile tile : tiles)
            kernels.put(tile,new KernelTilePredictor(forgettingFactor,tile,distance));

    }

    /**
     * returns the current kernel prediction
     * @return
     */
    @Override
    public double predict(SeaTile tile, double time, FishState state, Fisher fisher) {

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
     */
    @Override
    public void addObservation(GeographicalObservation newObservation, Fisher fisher) {

        //go through all the tiles
        for(KernelTilePredictor kernel : kernels.values())
        {
           kernel.addObservation(newObservation,fisher);
        }
    }

    public double getForgettingFactor() {
        return forgettingFactor;
    }

    public List<RegressionDistance> getDistances(){
        return         kernels.values().iterator().next().getDistances();
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
