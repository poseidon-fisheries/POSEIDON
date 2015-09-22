package uk.ac.ox.oxfish.model.market.itq;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;

/**
 * A simple method to compute quota values for agents. Works in isolation, that is considers the value of a quota
 * by the unit profit of the specie caught and not by the bottleneck effect it might have as a choke specie for other
 * quotas
 * Created by carrknight on 8/20/15.
 */
public class MonoQuotaPriceGenerator implements FisherStartable
{



    private final int specieIndex;

    private Fisher fisher;

    private FishState state;

    private MonoQuotaRegulation quotas;

    public MonoQuotaPriceGenerator(int specieIndex) {
        this.specieIndex = specieIndex;
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

    }


    double computeLambda()
    {

        if(fisher == null)
            return Double.NaN;
        if (state.getDayOfTheYear() == 365)
            return Double.NaN;
        double probability = 1 - fisher.probabilityDailyCatchesBelowLevel(
                specieIndex,
                 quotas.getQuotaRemaining(specieIndex) / (365 - state.getDayOfTheYear()));

        return  (probability * fisher.predictUnitProfit(specieIndex));



    }
}
