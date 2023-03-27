package uk.ac.ox.oxfish.model.plugins;

import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;

import static java.time.Month.JULY;
import static uk.ac.ox.oxfish.fisher.purseseiner.PurseSeineVesselReader.chooseClosurePeriod;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.dayOfYear;

public class IattcClosurePeriodRandomizer implements AdditionalStartable {

    private final double proportionOfBoatsInClosureA;

    public IattcClosurePeriodRandomizer(double proportionOfBoatsInClosureA) {
        this.proportionOfBoatsInClosureA = proportionOfBoatsInClosureA;
    }

    @Override
    public void start(FishState fishState) {

        // Every year, on July 15th, purse seine vessels must choose
        // which temporal closure period they will observe.
        final int daysFromNow = 1 + dayOfYear(JULY, 15);

        fishState.getFishers().forEach(fisher -> {

            final Steppable assignClosurePeriod = simState -> {
                final String closure = simState.random.nextDouble() < proportionOfBoatsInClosureA ? "A" : "B";
                if (fisher.getRegulation() instanceof MultipleRegulations) {
                    chooseClosurePeriod(fisher, closure);
                    ((MultipleRegulations) fisher.getRegulation()).reassignRegulations(fishState, fisher);
                }
            };

            fishState.scheduleOnceInXDays(assignClosurePeriod, StepOrder.DAWN, daysFromNow);
            fishState.scheduleOnceInXDays(
                simState -> fishState.scheduleEveryXDay(assignClosurePeriod, StepOrder.DAWN, 365),
                StepOrder.DAWN,
                daysFromNow
            );

        });
    }

}
