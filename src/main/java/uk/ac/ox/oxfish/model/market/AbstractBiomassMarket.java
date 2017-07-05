package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * Any market that does not care about size of the fish
 * but only its weight
 * Created by carrknight on 7/4/17.
 */
public abstract class AbstractBiomassMarket extends AbstractMarket {


    @Override
    protected TradeInfo sellFishImplementation(
            Hold hold, Fisher fisher, Regulation regulation, FishState state, Species species) {
        return  sellFishImplementation(
                hold.getWeightOfCatchInHold(species),
                fisher,
                regulation,
                state,
                species
        );
    }

    /**
     * the only method to implement for subclasses. Needs to actually do the trading and return the result
     * @param biomass the biomass caught by the seller
     * @param fisher the seller
     * @param regulation the rules the seller abides to
     * @param state the model
     * @return TradeInfo  results
     */
    protected abstract TradeInfo sellFishImplementation(double biomass, Fisher fisher,
                                                        Regulation regulation, FishState state,
                                                        Species species);


}
