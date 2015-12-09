package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.regs.KitchenSinkRegulation;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * A factory for the kitchen sink regulation, it is itself just a collection of factories
 * Created by carrknight on 12/9/15.
 */
public class KitchenSinkFactory implements AlgorithmFactory<KitchenSinkRegulation> {

    boolean individualTradeableQuotas = true;


    final private ProtectedAreasOnlyFactory mpa = new ProtectedAreasOnlyFactory();

    final private FishingSeasonFactory seasons = new FishingSeasonFactory();

    final private MultiITQStringFactory itqFactory = new MultiITQStringFactory();

    final private MultiTACStringFactory tacFactory = new MultiTACStringFactory();

    public KitchenSinkFactory() {
        //we are going to sync them
        tacFactory.setYearlyQuotaMaps(itqFactory.getYearlyQuotaMaps());
    }


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public KitchenSinkRegulation apply(FishState fishState) {
        ProtectedAreasOnly subcomponent1 = mpa.apply(fishState);
        FishingSeason subcomponent2 = seasons.apply(fishState);
        MultiQuotaRegulation subcomponent3;
        if(individualTradeableQuotas)
            subcomponent3 = itqFactory.apply(fishState);
        else
            subcomponent3 = tacFactory.apply(fishState);

        return new KitchenSinkRegulation(subcomponent1,
                                         subcomponent2,
                                         subcomponent3);

    }

    public FishingSeasonFactory getSeasons() {
        return seasons;
    }

    public ProtectedAreasOnlyFactory getMpa() {
        return mpa;
    }


    public boolean isIndividualTradeableQuotas() {
        return individualTradeableQuotas;
    }

    public void setIndividualTradeableQuotas(boolean individualTradeableQuotas) {
        this.individualTradeableQuotas = individualTradeableQuotas;
    }

    public DoubleParameter getSeasonLength() {
        return seasons.getSeasonLength();
    }

    public void setSeasonLength(DoubleParameter seasonLength) {
        seasons.setSeasonLength(seasonLength);
    }

    public String getYearlyQuotaMaps() {
        return itqFactory.getYearlyQuotaMaps();
    }

    public void setYearlyQuotaMaps(String yearlyQuotaMaps) {
        itqFactory.setYearlyQuotaMaps(yearlyQuotaMaps);
        tacFactory.setYearlyQuotaMaps(yearlyQuotaMaps);
    }
}
