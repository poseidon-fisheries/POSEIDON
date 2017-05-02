package uk.ac.ox.oxfish.fisher.strategies.discarding;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * Discards everything that is not sellable (according to the regulation object)
 * Created by carrknight on 5/2/17.
 */
public class DiscardAllUnsellable implements DiscardingStrategy {
    /**
     * This strategy decides the new "catch" object, that is how much of the fish we are actually going to store
     * given how much we caught!
     *
     * @param where             where did we do the fishing
     * @param who               who did the fishing
     * @param fishCaught        the catch before any discard
     * @param hoursSpentFishing how many hours have we spent fishing
     * @param regulation        the regulation the fisher is subject to
     * @param model
     *@param random
     * @return a catch object holding how much we are actually going to load in the boat. The difference between
     * what is returned and the 'fishCaught' variable is the implicit discard
     */
    @Override
    public Catch chooseWhatToKeep(
            SeaTile where, Fisher who, Catch fishCaught, int hoursSpentFishing, Regulation regulation,
            FishState model, MersenneTwisterFast random) {


        //go through the regulation object and return everything that isn't kept
        double[] saved  = new double[model.getSpecies().size()];
        for(Species species : model.getSpecies())
        {
            saved[species.getIndex()] = Math.min(fishCaught.getWeightCaught(species),
                             regulation.maximumBiomassSellable(who,species,model));
        }

        return new Catch(saved);

    }
}
