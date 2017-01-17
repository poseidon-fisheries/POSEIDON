package uk.ac.ox.oxfish.model.regs.factory;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.ExternalOpenCloseSeason;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;

import java.util.function.Supplier;

/**
 * Special use controller that is basically a random shodan. Not listed.
 * Created by carrknight on 1/14/17.
 */
public class RandomOpenCloseController implements AlgorithmFactory<ExternalOpenCloseSeason>{

    private final Locker<FishState,ExternalOpenCloseSeason> locker = new Locker<>();

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public ExternalOpenCloseSeason apply(FishState fishState) {
        return locker.presentKey
                (fishState,
                 new Supplier<ExternalOpenCloseSeason>() {
                     @Override
                     public ExternalOpenCloseSeason get() {
                         ExternalOpenCloseSeason toReturn = new ExternalOpenCloseSeason();

                         fishState.scheduleEveryXDay(new Steppable() {
                             @Override
                             public void step(SimState simState) {
                                 toReturn.setOpen(fishState.getRandom().nextBoolean());
                             }
                         }, StepOrder.POLICY_UPDATE,30);

                         return toReturn;
                     }
                 });

    }
}
