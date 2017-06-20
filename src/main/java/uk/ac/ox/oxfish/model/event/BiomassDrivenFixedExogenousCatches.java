package uk.ac.ox.oxfish.model.event;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.OneSpecieGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Map;

/**
 * Created by carrknight on 5/25/17.
 */
public class BiomassDrivenFixedExogenousCatches extends AbstractExogenousCatches {
    public BiomassDrivenFixedExogenousCatches(
            Map<Species, Double> exogenousYearlyCatchesInKg) {
        super(exogenousYearlyCatchesInKg, "Exogenous catches of ");
    }

    /**
     * simulate exogenous catch
     *
     * @param model the model
     * @param target   species to kill
     * @param tile     where to kill it
     * @param step     how much at most to kill
     * @return
     */
    @Override
    protected Catch mortalityEvent(
            FishState model, Species target, SeaTile tile, double step) {
        //take it as a fixed proportion catchability (and never more than it is available anyway)
        assert tile.getBiomass(target) > FishStateUtilities.EPSILON;
        double proportionToCatch = Math.min(1,step/tile.getBiomass(target));
        //simulate the catches as a fixed proportion gear
        OneSpecieGear gear = new OneSpecieGear(target,proportionToCatch);
        //catch it
        Catch fish = gear.fish(null, tile, 1, model.getBiology());
        //round to be supersafe
        if(fish.totalCatchWeight()>tile.getBiomass(target)) {
            //should be by VERY little!
            assert tile.getBiomass(target) + FishStateUtilities.EPSILON > fish.getTotalWeight();
            assert proportionToCatch >=1.0;
            //bound it to what is available
            fish = new Catch(target,tile.getBiomass(target),model.getBiology());
            assert (fish.totalCatchWeight()<=tile.getBiomass(target));
        }
        assert (fish.totalCatchWeight()<=tile.getBiomass(target));
        tile.reactToThisAmountOfBiomassBeingFished(fish,fish,model.getBiology());
        return fish;
    }
}
