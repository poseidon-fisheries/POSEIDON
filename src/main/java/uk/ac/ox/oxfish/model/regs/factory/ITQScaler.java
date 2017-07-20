package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;

/**
 * Created by carrknight on 7/20/17.
 */
class ITQScaler implements Startable {

    private final MultiQuotaRegulation toScale;

    public ITQScaler(MultiQuotaRegulation toScale) {
        this.toScale = toScale;
    }

    @Override
    public void start(FishState model) {
        for (int i = 0; i < model.getSpecies().size(); i++) {
            double availableQuota = toScale.getQuotaRemaining(i);
            if (Double.isFinite(availableQuota))
                toScale.setYearlyQuota(i,
                                       availableQuota /
                                               (double)model.getNumberOfFishers());
        }

    }

    @Override
    public void turnOff() {

    }

}
