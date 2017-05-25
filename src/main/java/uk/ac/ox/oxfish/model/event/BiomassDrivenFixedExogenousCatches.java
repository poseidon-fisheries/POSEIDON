package uk.ac.ox.oxfish.model.event;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.HeterogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.OneSpecieGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Map;

/**
 * Created by carrknight on 5/25/17.
 */
public class BiomassDrivenFixedExogenousCatches extends AbstractExogenousCatches {
    public BiomassDrivenFixedExogenousCatches(
            Map<Species, Double> exogenousYearlyCatchesInKg) {
        super(exogenousYearlyCatchesInKg);
    }

    /**
     * simulate exogenous catch
     *
     * @param simState the model
     * @param target   species to kill
     * @param tile     where to kill it
     * @param step     how much at most to kill
     * @return
     */
    @Override
    protected Catch mortalityEvent(
            FishState simState, Species target, SeaTile tile, double step) {
        //take it as a fixed proportion catchability (and never more than it is available anyway)
        assert tile.getBiomass(target) > FishStateUtilities.EPSILON;
        double proportionToCatch = Math.min(1,step/tile.getBiomass(target));
        //simulate the catches as a fixed proportion gear
        OneSpecieGear gear = new OneSpecieGear(target,proportionToCatch);
        //catch it
        return gear.fish(null, tile, 1, simState.getBiology());
    }
}
