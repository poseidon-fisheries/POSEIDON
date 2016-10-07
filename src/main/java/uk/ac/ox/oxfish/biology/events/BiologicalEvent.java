package uk.ac.ox.oxfish.biology.events;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An unspecified event that is checked for every day. if it happens then it applies to some seatiles and it performs
 * some sort of operation
 * Created by carrknight on 10/7/16.
 */
public class BiologicalEvent implements Startable,Steppable{


    /**
     * this gets checked every step to see whether the event occurs today
     */
    private final Predicate<FishState> trigger;

    /**
     * this predicate can be used to bound the effect of the event to certain tiles only
     */
    private final Predicate<SeaTile> appliesHere;


    /**
     * this method applies the event to the seatile
     */
    private final Consumer<SeaTile> event;

    private Stoppable stoppable;


    public BiologicalEvent(
            Predicate<FishState> trigger, Predicate<SeaTile> appliesHere,
            Consumer<SeaTile> event) {
        this.trigger = trigger;
        this.appliesHere = appliesHere;
        this.event = event;
    }

    @Override
    public void step(SimState state) {
        //if the trigger applies
        if(trigger.test(((FishState) state)))
            //for all the seatiles that qualify apply the event
            ((FishState) state).getMap().getAllSeaTilesExcludingLandAsList().stream().
                    filter(appliesHere).forEach(event);
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        stoppable = model.scheduleEveryDay(this, StepOrder.BIOLOGY_PHASE);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if(stoppable!=null)
            stoppable.stop();
    }
}
