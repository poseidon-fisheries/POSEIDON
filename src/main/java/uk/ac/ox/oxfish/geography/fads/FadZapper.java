package uk.ac.ox.oxfish.geography.fads;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * checks each step all fads that pass a predicate; if they do, then lose them
 */
public class FadZapper implements Steppable, AdditionalStartable {


    private final Predicate<AbstractFad> validator;

    public FadZapper(Predicate<AbstractFad> validator) {
        this.validator = validator;
    }

    @Override
    public void step(SimState simState) {
        FadMap fadMap = ((FishState) simState).getFadMap();
        List<AbstractFad> listToRemove = new LinkedList<>();
        for (Object o : fadMap.allFadsAsList()) {
            if(o instanceof Fad && validator.test(((AbstractFad) o)))
            {
                listToRemove.add((AbstractFad) o);
            }
        }
        for (AbstractFad fad : listToRemove) {
            fadMap.destroyFad(fad);
            fad.releaseFish(((FishState) simState).getSpecies());

        }

    }


    @Override
    public void start(FishState model) {
        model.scheduleEveryDay(this, StepOrder.DAWN);
    }
}
