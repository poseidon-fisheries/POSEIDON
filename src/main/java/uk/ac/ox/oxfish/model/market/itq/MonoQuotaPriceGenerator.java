package uk.ac.ox.oxfish.model.market.itq;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;

/**
 * A simple method to compute quota values for agents. Works in isolation, that is considers the value of a quota
 * by the unit profit of the specie caught and not by the bottleneck effect it might have as a choke specie for other
 * quotas
 * Created by carrknight on 8/20/15.
 */
public class MonoQuotaPriceGenerator implements PriceGenerator
{



    private final int specieIndex;

    private Fisher fisher;

    private FishState state;

    private MonoQuotaRegulation quotas;


    /**
     * whether to include or not daily profits in the reservation price computation. This was something I attempted
     * on 20151006 but I decided to abandon as I don't think it makes as much economic sense as I thought it did
     */
    private final boolean includeDailyProfits;

    public MonoQuotaPriceGenerator(int specieIndex,
                                   boolean includeDailyProfits) {
        this.specieIndex = specieIndex;
        this.includeDailyProfits = includeDailyProfits;
    }

    @Override
    public void start(FishState model, Fisher fisher)
    {
        this.fisher = fisher;
        this.state = model;
        //only works with the right kind of regulation!
        quotas = ((MonoQuotaRegulation) fisher.getRegulation());



        fisher.getDailyData().registerGatherer("Reservation Quota Price of " + model.getSpecies().get(specieIndex),
                                               fisher1 -> computeLambda(),
                                               Double.NaN);



    }

    @Override
    public void turnOff() {
        //todo remove gatherer
    }


    public double computeLambda()
    {

        if(fisher == null)
            return Double.NaN;
        if (state.getDayOfTheYear() == 365)
            return Double.NaN;
        double probability = 1 - fisher.probabilitySumDailyCatchesBelow(specieIndex,quotas.getQuotaRemaining(specieIndex),
                                                                        365-state.getDayOfTheYear());

        if(!includeDailyProfits)
            return  (probability * fisher.predictUnitProfit(specieIndex));
        else
            return  (probability * (fisher.predictUnitProfit(specieIndex) +  (365 - state.getDayOfTheYear()) * fisher.predictDailyProfits() ));




    }
}
