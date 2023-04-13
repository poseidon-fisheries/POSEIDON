package uk.ac.ox.oxfish.geography.fads;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * checks each step all fads that pass a predicate; if they do, then lose them
 */
public class FadZapper implements Steppable, AdditionalStartable {

    private final Predicate<? super Fad<?>> validator;

    public FadZapper(final Predicate<? super Fad<?>> validator) {
        this.validator = validator;
    }

    @Override
    public void step(final SimState simState) {
        Optional.ofNullable(((FishState) simState).getFadMap()).ifPresent(fadMap ->
            fadMap.allFads().collect(toList()).stream()
                .filter(validator)
                .forEach(fad -> {
                    fadMap.destroyFad(fad);
                    fad.releaseFish(((FishState) simState).getSpecies());
                })
        );
    }

    @Override
    public void start(final FishState model) {
        model.scheduleEveryDay(this, StepOrder.DAWN);
    }
}
