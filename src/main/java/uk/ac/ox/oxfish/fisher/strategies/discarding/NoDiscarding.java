package uk.ac.ox.oxfish.fisher.strategies.discarding;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * Always returns the amount of fish caught
 * Created by carrknight on 4/20/17.
 */
public class NoDiscarding implements DiscardingStrategy {
    /**
     * returns the original catch (that is, nothing is discarded)
     *
     * @param where             where did we do the fishing
     * @param who               who did the fishing
     * @param fishCaught        the catch before any discard
     * @param hoursSpentFishing how many hours have we spent fishing
     * @param regulation        the regulation the fisher is subject to
     * @param model
     *@param random @return a catch object holding how much we are actually going to load in the boat. The difference between
     * what is returned and the 'fishCaught' variable is the implicit discard
     */
    @Override
    public Catch chooseWhatToKeep(
            SeaTile where, Fisher who, Catch fishCaught, int hoursSpentFishing, Regulation regulation,
            FishState model, MersenneTwisterFast random) {
        return fishCaught;
    }


}
